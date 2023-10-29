package com.fx.gen.controller.tabs;

import com.fx.gen.Globals;
import com.fx.gen.controller.MainController;
import com.fx.gen.controller.PopulateInterface;
import com.fx.gen.painter.BorderLinePainter;
import com.fx.gen.painter.CirclePainter;
import com.fx.gen.painter.IntersectionPainter;
import com.fx.gen.painter.LinePainter;
import com.fx.gen.painter.SelectionLinePainter;
import com.fx.gen.pojo.Line;
import com.fx.gen.pojo.Position;
import com.fx.gen.tools.CircleSelectionAdapter;
import com.fx.gen.tools.HelperFunctions;
import com.fx.gen.tools.MousePositionListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanKeyListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.locationtech.jts.algorithm.Intersection;
import org.locationtech.jts.geom.Coordinate;

/**
 *
 * @author pscha
 */
public class ConstController implements Initializable, PopulateInterface {

    @FXML
    private BorderPane borderPane;
    @FXML
    private HBox hBoxTop;
    @FXML
    private SwingNode swingNode;
    @FXML
    private Button btnFrom;
    @FXML
    private Button btnTo;
    @FXML
    private Button btnReset;
    @FXML
    private Button btnCsv;
    @FXML
    private Button btnHcm;
    @FXML
    private CheckBox cbInvert;
    @FXML
    private Label lbFrom;
    @FXML
    private Label lbTo;

    private static final Logger _log = LogManager.getLogger(MapController.class);

    private final MainController mainController;
    private final JXMapViewer mapViewer = new JXMapViewer();
    private final List<Painter<JXMapViewer>> painters = new ArrayList<>();
    private CircleSelectionAdapter cirlceSelectionAdapter;
    private List<Position> posListBackup;
    private List<Position> posListReducedBackup;
    private Line2D lineFrom;
    private Line2D lineTo;

    private double lon;
    private double lat;

    public ConstController(MainController mainController) {
        this.mainController = mainController;
    }

    @Override
    public void initialize(URL url, ResourceBundle bundle) {
        hBoxTop.setId("hec-background-blue");
        lbFrom.setId("hec-text-white");
        lbTo.setId("hec-text-white");
        cbInvert.setId("hec-text-white");

        lbFrom.setText("");
        lbTo.setText("");

        btnFrom.setText(bundle.getString("btn.from"));
        btnTo.setText(bundle.getString("btn.to"));
        btnReset.setText(bundle.getString("btn.reset"));
        btnCsv.setText(bundle.getString("btn.csv"));
        btnHcm.setText(bundle.getString("btn.hcm"));
        cbInvert.setText(bundle.getString("cb.invert"));

        btnCsv.setDisable(true);
        btnHcm.setDisable(true);

        borderPane.widthProperty().addListener(e -> {
            mapViewer.repaint();
        });
        borderPane.heightProperty().addListener(e -> {
            mapViewer.repaint();
        });

        btnFrom.setOnAction(e -> {
            openBorderFile(bundle, true);
        });

        btnTo.setOnAction(e -> {
            openBorderFile(bundle, false);
        });

        btnReset.setOnAction(e -> {

            lbFrom.setText("");
            lbTo.setText("");

            painters.clear();

            cirlceSelectionAdapter.setBorder_type(BorderLinePainter.BORDER_TYPE.FROM);

            CompoundPainter<JXMapViewer> p = new CompoundPainter<>(painters);
            mapViewer.setOverlayPainter(p);
            mapViewer.repaint();

            btnCsv.setDisable(true);
            btnHcm.setDisable(true);
        });

        cbInvert.selectedProperty().addListener((ov, o, n) -> {
            for (int i = painters.size() - 1; i >= 0; i--) {
                Painter painter = painters.get(i);
                if (painter instanceof SelectionLinePainter) {
                    SelectionLinePainter selectionLinePainter = (SelectionLinePainter) painter;
                    selectionLinePainter.setInvert(n);
                }
            }
            mapViewer.repaint();
        });

        btnCsv.setOnAction(e -> {
            openSaveCsvDlg(bundle);
        });

        btnHcm.setOnAction(e -> {
            openSaveHcmDlg(bundle);
        });

        initOsmMap(bundle);
    }

    private void openSaveCsvDlg(ResourceBundle bundle) {
        FileChooser fileChooser = new FileChooser();
        String borderDir = Globals.CSV_PATH;
        fileChooser.setInitialDirectory(new File(borderDir));
        fileChooser.setTitle(bundle.getString("dlg.open.border"));
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
        fileChooser.getExtensionFilters().add(extFilter);
        File borderFile = fileChooser.showSaveDialog(mainController.getStage());

        if (borderFile != null) {
            try {
                for (int i = 0; i < painters.size(); i++) {
                    Painter painter = painters.get(i);
                    if (painter instanceof SelectionLinePainter) {

                        SelectionLinePainter selectionLinePainter = (SelectionLinePainter) painter;
                        List<Position> list = selectionLinePainter.getFullBorder();

                        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(borderFile));
                        for (Position position : list) {
                            bufferedWriter.write(position.getLon() + ";" + position.getLat() + "\n");
                        }
                        bufferedWriter.close();
                    }
                }
            } catch (Exception ex) {
                _log.error(ex.getMessage());
            }
        }
    }

    private void openSaveHcmDlg(ResourceBundle bundle) {
        FileChooser fileChooser = new FileChooser();
        String borderDir = Globals.HCM_PATH;

        File dir = new File(Globals.HCM_PATH);
        if (!dir.exists()) {
            dir.mkdir();
        }

        fileChooser.setInitialDirectory(new File(borderDir));
        fileChooser.setTitle(bundle.getString("dlg.open.border"));
        //FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
        //fileChooser.getExtensionFilters().add(extFilter);
        File borderFile = fileChooser.showSaveDialog(mainController.getStage());

        if (borderFile != null) {
            try {
                for (int i = 0; i < painters.size(); i++) {
                    Painter painter = painters.get(i);
                    if (painter instanceof SelectionLinePainter) {

                        SelectionLinePainter selectionLinePainter = (SelectionLinePainter) painter;
                        List<Position> list = selectionLinePainter.getFullBorder();

                        DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(borderFile));
                        for (Position position : list) {
                            double corLon = position.getLon() / (180.0 / Math.PI);
                            double corLat = position.getLat() / (180.0 / Math.PI);

                            byte[] bytesLon = new byte[8];
                            byte[] bytesLat = new byte[8];

                            /*
                            dataOutputStream.write(HelperFunctions.doubleToBytes(corLon, bytesLon, 0, false));
                            dataOutputStream.write(HelperFunctions.doubleToBytes(corLat, bytesLat, 0, false));
                             */
                            dataOutputStream.write(HelperFunctions.doubleToByte(corLon, Globals.BYTE_ORDER));
                            dataOutputStream.write(HelperFunctions.doubleToByte(corLat, Globals.BYTE_ORDER));
                        }
                        dataOutputStream.close();
                    }
                }
            } catch (Exception ex) {
                _log.error(ex.getMessage());
            }
        }
    }

    private void openBorderFile(ResourceBundle bundle, boolean from) {
        FileChooser fileChooser = new FileChooser();
        String borderDir = Globals.CSV_PATH;

        File dir = new File(Globals.CSV_PATH);
        if (!dir.exists()) {
            dir.mkdir();
        }

        fileChooser.setInitialDirectory(new File(borderDir));
        fileChooser.setTitle(bundle.getString("dlg.open.border"));
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
        fileChooser.getExtensionFilters().add(extFilter);
        File borderFile = fileChooser.showOpenDialog(mainController.getStage());

        if (borderFile != null) {

            List<Position> posList = new ArrayList<>();
            List<Position> posListReduced = new ArrayList<>();

            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(borderFile));
                int idx = 0;
                while (bufferedReader.ready()) {
                    String line[] = bufferedReader.readLine().split(";");
                    double lonCsv = Double.valueOf(line[0]);
                    double latCsv = Double.valueOf(line[1]);
                    posList.add(new Position(lonCsv, latCsv, idx++));
                }
                bufferedReader.close();
            } catch (Exception ex) {
                _log.error(ex.getMessage());
            }

            if (from) {
                posListBackup = posList;

                painters.clear();
                lbFrom.setText(bundle.getString("btn.from") + ": " + borderFile.getName());

                for (int i = 0; i < posList.size(); i++) {
                    if (i % 21 == 0 || i % 22 == 0) {
                        posListReduced.add(posList.get(i));
                    }
                }

                posListReducedBackup = posListReduced;

                BorderLinePainter borderLinePainter = new BorderLinePainter(posList, /*MapController.genRandomColor()*/ Color.BLACK, BorderLinePainter.BORDER_TYPE.FROM);
                painters.add(borderLinePainter);
            } else {
                lbTo.setText(bundle.getString("btn.to") + ": " + borderFile.getName());

                BorderLinePainter borderLinePainter = new BorderLinePainter(posList, Color.BLACK, BorderLinePainter.BORDER_TYPE.TO);
                painters.add(borderLinePainter);
            }

            CompoundPainter<JXMapViewer> painter = new CompoundPainter<>(painters);
            mapViewer.setOverlayPainter(painter);
            mapViewer.repaint();
        }
    }

    private void initOsmMap(ResourceBundle bundle) {

        TileFactoryInfo tileFactoryInfo = new OSMTileFactoryInfo();
        DefaultTileFactory defaultTileFactory = new DefaultTileFactory(tileFactoryInfo);
        defaultTileFactory.setThreadPoolSize(Runtime.getRuntime().availableProcessors());
        mapViewer.setTileFactory(defaultTileFactory);

        final JLabel labelAttr = new JLabel();
        mapViewer.setLayout(new BorderLayout());
        mapViewer.add(labelAttr, BorderLayout.SOUTH);
        labelAttr.setText(defaultTileFactory.getInfo().getAttribution() + " - " + defaultTileFactory.getInfo().getLicense());

        // Set the focus
        double lonZM = 10.671745101119196;
        double latZM = 50.661742127393836;

        GeoPosition zellaMehlis = new GeoPosition(latZM, lonZM);

        mapViewer.setZoom(7);
        mapViewer.setAddressLocation(zellaMehlis);

        // Add interactions
        initPainter();

        MouseInputListener mil = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(mil);
        mapViewer.addMouseMotionListener(mil);
        mapViewer.addMouseListener(new CenterMapListener(mapViewer));
        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));
        mapViewer.addKeyListener(new PanKeyListener(mapViewer));

        MousePositionListener mousePositionListener = new MousePositionListener(mapViewer);
        mousePositionListener.setGeoPosListener((GeoPosition geoPosition) -> {
            Platform.runLater(() -> {
                mainController.getLbStatus().setText(bundle.getString("col.lon") + ": " + geoPosition.getLongitude() + " " + bundle.getString("col.lat") + ": " + geoPosition.getLatitude());
            });
        });
        mapViewer.addMouseMotionListener(mousePositionListener);

        try {
            SwingUtilities.invokeAndWait(() -> {
                swingNode.setContent(mapViewer);
                swingNode.requestFocus();
            });
        } catch (InterruptedException | InvocationTargetException ex) {
            _log.error(ex.getMessage());
        }

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                swingNode.getContent().repaint();
            }
        }, 1000);
    }

    private void initPainter() {
        cirlceSelectionAdapter = new CircleSelectionAdapter(mapViewer, painters);
        cirlceSelectionAdapter.setCirlceSelectionAdapterListener(new CircleSelectionAdapter.CirlceSelectionAdapterListener() {
            @Override
            public void drawCircle(GeoPosition geoPosition, BorderLinePainter.BORDER_TYPE border_type) {
                for (int i = 0; i < painters.size(); i++) {
                    Painter painter = painters.get(i);
                    if (painter instanceof CirclePainter) {
                        painters.remove(painter);
                    }
                }

                CirclePainter circlePainter = new CirclePainter(geoPosition);

                circlePainter.setCirclePainterListener((Ellipse2D ellipse2D) -> {
                    for (int i = 0; i < painters.size(); i++) {
                        Painter painter = painters.get(i);
                        if (painter instanceof BorderLinePainter) {
                            BorderLinePainter borderLinePainter = (BorderLinePainter) painter;
                            if (borderLinePainter.getLineList() != null) {

                                //Neu
                                if (borderLinePainter.getBorder_type() == BorderLinePainter.BORDER_TYPE.FROM) {
                                    for (Line line : borderLinePainter.getLineList()) {

                                        if (ellipse2D.contains(line.getMiddle())) {
                                            borderLinePainter.setSelLine(line.getLine2D());
                                            lineFrom = line.getLine2D();
                                            borderLinePainter.setSelColor(Color.RED);
                                            cirlceSelectionAdapter.setIdx(line.getIdx());
                                        }
                                    }
                                }

                                if (borderLinePainter.getBorder_type() == BorderLinePainter.BORDER_TYPE.TO) {
                                    if (lineFrom != null) {
                                        boolean isInter = true;
                                        for (Line line : borderLinePainter.getLineList()) {
                                            if (lineFrom.intersectsLine(line.getLine2D())) {
                                                borderLinePainter.setSelLine(line.getLine2D());
                                                borderLinePainter.setSelColor(Color.BLUE);
                                                isInter = true;
                                                lineTo = line.getLine2D();
                                                break;
                                            } else {
                                                isInter = false;
                                            }
                                        }
                                        if (!isInter) {
                                            borderLinePainter.setSelLine(null);
                                        }
                                    }
                                }

                                /*
                                switch (border_type) {
                                    case FROM:
                                        if (borderLinePainter.getBorder_type() == BorderLinePainter.BORDER_TYPE.FROM) {
                                            for (Line line : borderLinePainter.getLineList()) {
                                                if (ellipse2D.contains(line.getLine2D().getP1())) {
                                                    borderLinePainter.setSelLine(line.getLine2D());
                                                    borderLinePainter.setSelColor(Color.RED);
                                                    cirlceSelectionAdapter.setIdx(line.getIdx());
                                                }
                                            }
                                        }
                                        break;
                                    case TO:
                                        if (borderLinePainter.getBorder_type() == BorderLinePainter.BORDER_TYPE.TO) {
                                            for (Line line : borderLinePainter.getLineList()) {
                                                if (ellipse2D.contains(line.getLine2D().getP1())) {
                                                    borderLinePainter.setSelLine(line.getLine2D());
                                                    borderLinePainter.setSelColor(Color.BLUE);
                                                }
                                            }
                                        }
                                        break;
                                    default:
                                        throw new AssertionError();
                                }
                                 */
                            }
                        }
                    }
                });
                painters.add(circlePainter);

                CompoundPainter<JXMapViewer> painter = new CompoundPainter<>(painters);
                mapViewer.setOverlayPainter(painter);
                mapViewer.repaint();
            }

            @Override
            public void drawStartLine1(int start) {
                drawStart(start, true);
            }

            @Override
            public void drawStartLine2() {
                drawEnd(true);

                for (int i = painters.size() - 1; i >= 0; i--) {
                    Painter painter = painters.get(i);
                    if (painter instanceof LinePainter) {
                        LinePainter linePainter = (LinePainter) painter;
                        if (linePainter.getColor() == Color.BLUE) {
                            painters.remove(painter);
                        }
                    }
                }
            }

            @Override
            public void drawEndLine1(int start) {
                drawStart(start, false);
            }

            @Override
            public void drawEndLine2() {
                drawEnd(false);

                for (int i = painters.size() - 1; i >= 0; i--) {
                    Painter painter = painters.get(i);
                    if (painter instanceof LinePainter) {
                        LinePainter linePainter = (LinePainter) painter;
                        if (linePainter.getColor() == Color.BLUE) {
                            painters.remove(painter);
                        }
                    }
                }
            }

            @Override
            public void drawFullResBorder() {
                int start = 0;
                int end = 0;

                GeoPosition geoPositionStart = null;
                GeoPosition geoPositionEnd = null;

                List<Position> posList = null;

                for (int i = 0; i < painters.size(); i++) {
                    Painter painter = painters.get(i);

                    if (painter instanceof LinePainter) {
                        LinePainter linePainter = (LinePainter) painter;
                        switch (linePainter.getLine_pos()) {
                            case START:
                                if (linePainter.getBorder_type() == BorderLinePainter.BORDER_TYPE.FROM) {
                                    start = linePainter.getIdx();
                                }
                                break;
                            case END:
                                if (linePainter.getBorder_type() == BorderLinePainter.BORDER_TYPE.FROM) {
                                    end = linePainter.getIdx();
                                }
                                break;
                            default:
                                throw new AssertionError();
                        }
                    }

                    if (painter instanceof IntersectionPainter) {
                        IntersectionPainter intersectionPainter = (IntersectionPainter) painter;
                        switch (intersectionPainter.getLine_pos()) {
                            case START:
                                geoPositionStart = intersectionPainter.getGeoPosition();
                                break;
                            case END:
                                geoPositionEnd = intersectionPainter.getGeoPosition();
                                break;
                            default:
                                throw new AssertionError();
                        }
                    }

                    if (painter instanceof BorderLinePainter) {
                        BorderLinePainter borderLinePainter = (BorderLinePainter) painter;
                        switch (borderLinePainter.getBorder_type()) {
                            case FROM:
                                posList = borderLinePainter.getBorder();
                                break;
                            default:
                            //throw new AssertionError();
                        }
                    }
                }

                for (int i = painters.size() - 1; i >= 0; i--) {
                    Painter painter = painters.get(i);
                    if (painter instanceof LinePainter) {
                        painters.remove(i);
                    }
                    if (painter instanceof IntersectionPainter) {
                        painters.remove(i);
                    }
                    if (painter instanceof BorderLinePainter) {
                        painters.remove(i);
                    }
                }

                SelectionLinePainter selectionLinePainter = new SelectionLinePainter(posList, start, end, posList, geoPositionStart, geoPositionEnd);
                painters.add(selectionLinePainter);

                CompoundPainter<JXMapViewer> painter = new CompoundPainter<>(painters);
                mapViewer.setOverlayPainter(painter);
                mapViewer.repaint();

                btnCsv.setDisable(false);
                btnHcm.setDisable(false);
            }

            @Override
            public void showErrorDlg() {
                //TODO Error-Dlg
            }

        });

        cbInvert.setSelected(false);
        btnCsv.setDisable(true);
        btnHcm.setDisable(true);

        mapViewer.setFocusable(true);
        mapViewer.addMouseListener(cirlceSelectionAdapter);
        mapViewer.addMouseMotionListener(cirlceSelectionAdapter);
        mapViewer.addKeyListener(cirlceSelectionAdapter);
    }

    private void drawStart(int start, boolean isStart) {
        List<Position> posList;

        for (int i = painters.size() - 1; i >= 0; i--) {
            Painter painter = painters.get(i);
            if (painter instanceof BorderLinePainter) {
                BorderLinePainter borderLinePainter = (BorderLinePainter) painter;
                switch (borderLinePainter.getBorder_type()) {
                    case FROM:
                        posList = borderLinePainter.getBorder();

                        LinePainter linePainter;
                        if (isStart) {
                            linePainter = new LinePainter(posList, start, LinePainter.LINE_POS.START, Color.RED, BorderLinePainter.BORDER_TYPE.FROM);
                        } else {
                            linePainter = new LinePainter(posList, start, LinePainter.LINE_POS.END, Color.RED, BorderLinePainter.BORDER_TYPE.FROM);
                        }

                        painters.add(linePainter);
                        break;
                    default:
                    //throw new AssertionError();
                }
            }
        }
    }

    private void drawEnd(boolean isStart) {
        List<Position> posList;

        GeoPosition geoPosition1 = null;
        GeoPosition geoPosition2 = null;
        GeoPosition geoPosition3 = null;
        GeoPosition geoPosition4 = null;

        for (int i = painters.size() - 1; i >= 0; i--) {
            Painter painter = painters.get(i);
            if (painter instanceof BorderLinePainter) {
                BorderLinePainter borderLinePainter = (BorderLinePainter) painter;
                switch (borderLinePainter.getBorder_type()) {
                    case FROM:
                        geoPosition1 = borderLinePainter.getGeoPositionLine1();
                        geoPosition2 = borderLinePainter.getGeoPositionLine2();
                        break;
                    case TO:
                        geoPosition3 = borderLinePainter.getGeoPositionLine1();
                        geoPosition4 = borderLinePainter.getGeoPositionLine2();
                        posList = borderLinePainter.getBorder();

                        LinePainter linePainter;
                        if (isStart) {
                            linePainter = new LinePainter(posList, LinePainter.LINE_POS.START, Color.BLUE, BorderLinePainter.BORDER_TYPE.TO);
                            //System.out.println(linePainter);
                        } else {
                            linePainter = new LinePainter(posList, LinePainter.LINE_POS.END, Color.BLUE, BorderLinePainter.BORDER_TYPE.TO);
                            //System.out.println(linePainter);
                        }
                        painters.add(linePainter);

                        break;
                    default:
                    //throw new AssertionError();
                }
            }
        }

        Coordinate c1 = new Coordinate(geoPosition1.getLongitude(), geoPosition1.getLatitude());
        Coordinate c2 = new Coordinate(geoPosition2.getLongitude(), geoPosition2.getLatitude());
        Coordinate c3 = new Coordinate(geoPosition3.getLongitude(), geoPosition3.getLatitude());
        Coordinate c4 = new Coordinate(geoPosition4.getLongitude(), geoPosition4.getLatitude());

        Coordinate inter1 = Intersection.intersection(c1, c2, c3, c4);

        IntersectionPainter intersectionPainter;
        if (isStart) {
            intersectionPainter = new IntersectionPainter(new GeoPosition(inter1.getY(), inter1.getX()), LinePainter.LINE_POS.START);
        } else {
            intersectionPainter = new IntersectionPainter(new GeoPosition(inter1.getY(), inter1.getX()), LinePainter.LINE_POS.END);
        }

        painters.add(intersectionPainter);
    }

    @Override
    public void populate() {

    }

    @Override
    public void reset() {

    }

    @Override
    public void clear() {

    }
}
