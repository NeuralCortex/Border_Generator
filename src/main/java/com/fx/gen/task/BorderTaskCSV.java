package com.fx.gen.task;

import com.fx.gen.dialog.ProgressDialog;
import com.fx.gen.painter.BorderRoutePainter;
import com.fx.gen.pojo.BorderPOJO;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.scene.control.TableView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;

/**
 *
 * @author pscha
 */
public class BorderTaskCSV extends Task<Integer> {

    private static final Logger _log = LogManager.getLogger(BorderTaskCSV.class);
    private final ProgressDialog progressDialog;
    private final List<File> borderList;
    private final JXMapViewer mapViewer;
    private final List<Painter<JXMapViewer>> painters;
    private final TableView<BorderPOJO> tableView;
    private boolean stop = false;

    public BorderTaskCSV(ProgressDialog progressDialog, List<File> borderList, JXMapViewer mapViewer, List<Painter<JXMapViewer>> painters, TableView<BorderPOJO> tableView) {
        this.progressDialog = progressDialog;
        this.borderList = borderList;
        this.mapViewer = mapViewer;
        this.painters = painters;
        this.tableView = tableView;
        initProgressDlg();
    }

    private void initProgressDlg() {
        progressDialog.getLbLeft().setText("0");
        progressDialog.getProgressBar().progressProperty().bind(progressProperty());
        progressDialog.getLbRight().setText(borderList.size() - 1 + "");
        progressDialog.setProgressDialogInterface(() -> {
            stop = true;
        });
        progressDialog.show();
    }

    @Override
    protected Integer call() throws Exception {
        BorderRoutePainter borderRoutePainter = null;

        for (int i = 0; i < borderList.size(); i++) {
            if (stop) {
                break;
            }

            List<GeoPosition> track = new ArrayList<>();

            try {
                String borderFile = borderList.get(i).getAbsolutePath();
                BufferedReader bufferedReader = new BufferedReader(new FileReader(borderFile));
                while (bufferedReader.ready()) {
                    String line[] = bufferedReader.readLine().trim().split("[\\s;]+");
                    double lonCsv = Double.valueOf(line[0]);
                    double latCsv = Double.valueOf(line[1]);
                    track.add(new GeoPosition(latCsv, lonCsv));
                }
                bufferedReader.close();
            } catch (Exception ex) {
                _log.error(ex.getMessage());
            }

            borderRoutePainter = new BorderRoutePainter(track);
            painters.add(borderRoutePainter);

            BorderPOJO borderPOJO = new BorderPOJO(borderList.get(i).getName(), true, borderRoutePainter, borderRoutePainter.getSize(), borderRoutePainter.getColor(), borderRoutePainter.getLength());

            borderPOJO.activeProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                if (newValue == false) {
                    painters.remove(borderPOJO.getBorderRoutePainter());
                } else {
                    painters.add(borderPOJO.getBorderRoutePainter());
                }
                CompoundPainter<JXMapViewer> painter = new CompoundPainter<>(painters);
                mapViewer.setOverlayPainter(painter);
                Platform.runLater(() -> {
                    mapViewer.repaint();
                });
            });

            Platform.runLater(() -> {
                tableView.getItems().add(borderPOJO);
            });

            updateProgress(i, borderList.size() - 1);
        }

        CompoundPainter<JXMapViewer> painter = new CompoundPainter<>(painters);
        mapViewer.setOverlayPainter(painter);

        return 1;
    }

    @Override
    protected void succeeded() {
        progressDialog.closeDialog();
    }
}
