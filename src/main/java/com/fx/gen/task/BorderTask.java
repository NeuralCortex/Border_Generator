package com.fx.gen.task;

import com.fx.gen.dialog.ProgressDialog;
import com.fx.gen.painter.BorderPainter;
import com.fx.gen.pojo.BorderData;
import com.fx.gen.pojo.Position;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javafx.concurrent.Task;
import javafx.scene.control.TableView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;

/**
 *
 * @author pscha
 */
public class BorderTask extends Task<Integer> {

    private static final Logger _log = LogManager.getLogger(BorderTask.class);
    private final ProgressDialog progressDialog;
    private final List<File> borderList;
    private final JXMapViewer mapViewer;
    private final TableView<BorderData> tableView;
    private final List<Painter<JXMapViewer>> painters;
    private final HashMap<Integer, List<Position>> mapBorder=new HashMap<>();
    private boolean stop = false;

    public interface BorderTaskListener {

        public void importDone();
    }

    private BorderTaskListener borderTaskListener;

    public BorderTask(ProgressDialog progressDialog, List<File> borderList, JXMapViewer mapViewer, TableView<BorderData> tableView, List<Painter<JXMapViewer>> painters) {
        this.progressDialog = progressDialog;
        this.borderList = borderList;
        this.mapViewer = mapViewer;
        this.tableView = tableView;
        this.painters = painters;
        initProgressDlg();
    }

    private void initProgressDlg() {
        progressDialog.getLbLeft().setText("0");
        progressDialog.getProgressBar().progressProperty().bind(progressProperty());
        progressDialog.getLbRight().setText(borderList.size() + "");
        progressDialog.setProgressDialogInterface(() -> {
            stop = true;
        });
        progressDialog.show();
    }

    @Override
    protected Integer call() throws Exception {
        mapBorder.clear();
        for (int i = 0; i < borderList.size(); i++) {
            if (stop) {
                break;
            }
            try {
                //System.out.println(borderList.get(i).getAbsolutePath());
                List<Position> posList = new ArrayList<>();
                BufferedReader bufferedReader = new BufferedReader(new FileReader(borderList.get(i)));
                int idx=0;
                while (bufferedReader.ready()) {
                    String coordArray[] = bufferedReader.readLine().split(";");
                    Double lon = Double.valueOf(coordArray[0]);
                    Double lat = Double.valueOf(coordArray[1]);
                    posList.add(new Position(lon, lat));
                }
                bufferedReader.close();

                mapBorder.put(i, posList);

                BorderPainter borderPainter = new BorderPainter(posList, genRandomColor());
                painters.add(borderPainter);

            } catch (Exception ex) {
                _log.error(ex.getMessage());
            }

            updateProgress(i, borderList.size());
        }
        
        borderTaskListener.importDone();

        return 1;
    }

    private Color genRandomColor() {
        int r = (int) (Math.random() * 256);
        int g = (int) (Math.random() * 256);
        int b = (int) (Math.random() * 256);
        return new Color(r, g, b);
    }

    @Override
    protected void succeeded() {
        progressDialog.closeDialog();
    }

    public void setBorderTaskListener(BorderTaskListener borderTaskListener) {
        this.borderTaskListener = borderTaskListener;
    }
}
