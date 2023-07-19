package com.fx.gen.controller.tabs;

import com.fx.gen.Globals;
import com.fx.gen.cell.ColorCellBorder;
import com.fx.gen.controller.MainController;
import com.fx.gen.controller.PopulateInterface;
import com.fx.gen.dialog.ProgressDialog;
import com.fx.gen.painter.LinePainter;
import com.fx.gen.painter.PosPainter;
import com.fx.gen.pojo.BorderPOJO;
import com.fx.gen.task.BorderTaskCSV;
import com.fx.gen.task.BorderTaskHCM;
import com.fx.gen.tools.MousePopupListener;
import com.fx.gen.tools.MousePositionListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
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
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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

public class ResultController implements Initializable, PopulateInterface {

    @FXML
    private BorderPane borderPane;
    @FXML
    private HBox hBoxTop;
    @FXML
    private HBox hBoxInfo;
    @FXML
    private HBox hBoxScale;
    @FXML
    private SwingNode swingNode;
    @FXML
    private Label lbInfo;
    @FXML
    private Label lbScale;
    @FXML
    private TableView<BorderPOJO> tableCSV;
    @FXML
    private TableView<BorderPOJO> tableHCM;
    @FXML
    private VBox vBoxTables;
    @FXML
    private Button btnCSV;
    @FXML
    private Button btnHCM;
    @FXML
    private Button btnReset;

    private static final Logger _log = LogManager.getLogger(ResultController.class);

    private final MainController mainController;
    private final JXMapViewer mapViewer = new JXMapViewer();
    private final List<Painter<JXMapViewer>> painters = new ArrayList<>();

    private final double lon = 10.671745101119196;
    private final double lat = 50.661742127393836;

    public ResultController(MainController mainController) {
        this.mainController = mainController;
    }

    @Override
    public void initialize(URL url, ResourceBundle bundle) {
        vBoxTables.setPrefWidth(400.0f);

        hBoxTop.setId("hec-background-blue");
        hBoxInfo.setId("hec-background-blue");
        hBoxScale.setId("hec-background-blue");

        lbInfo.setId("hec-text-white");
        lbScale.setId("hec-text-white");

        lbInfo.setText(bundle.getString("lb.csv.border"));
        lbScale.setText(bundle.getString("lb.hcm.border"));
        btnCSV.setText(bundle.getString("btn.csv.import"));
        btnHCM.setText(bundle.getString("btn.hcm.import"));
        btnReset.setText(bundle.getString("btn.reset"));

        borderPane.widthProperty().addListener(e -> {
            mapViewer.repaint();
        });
        borderPane.heightProperty().addListener(e -> {
            mapViewer.repaint();
        });

        TableColumn<BorderPOJO, Boolean> colActiveCSV = new TableColumn<>(bundle.getString("map.table.active"));
        TableColumn<BorderPOJO, String> colFileNameCSV = new TableColumn<>(bundle.getString("map.table.filename"));
        TableColumn<BorderPOJO, Integer> colCountCoordCSV = new TableColumn<>(bundle.getString("map.table.countcoord"));
        TableColumn<BorderPOJO, String> colLengthCSV = new TableColumn<>(bundle.getString("map.table.length"));
        TableColumn<BorderPOJO, Color> colcolorCSV = new TableColumn<>(bundle.getString("map.table.color"));

        colActiveCSV.setCellValueFactory(new PropertyValueFactory<>("active"));
        colActiveCSV.setCellFactory(CheckBoxTableCell.forTableColumn(colActiveCSV));
        colFileNameCSV.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        colCountCoordCSV.setCellValueFactory(new PropertyValueFactory<>("countCoord"));
        colLengthCSV.setCellValueFactory(new PropertyValueFactory<>("length"));
        colcolorCSV.setCellValueFactory(new PropertyValueFactory<>("color"));
        colcolorCSV.setCellFactory((param) -> {
            return new ColorCellBorder();
        });

        tableCSV.setEditable(true);
        tableCSV.getColumns().addAll(colActiveCSV, colFileNameCSV, colLengthCSV, colCountCoordCSV, colcolorCSV);

        TableColumn<BorderPOJO, Boolean> colActiveHCM = new TableColumn<>(bundle.getString("map.table.active"));
        TableColumn<BorderPOJO, String> colFileNameHCM = new TableColumn<>(bundle.getString("map.table.filename"));
        TableColumn<BorderPOJO, Integer> colCountCoordHCM = new TableColumn<>(bundle.getString("map.table.countcoord"));
        TableColumn<BorderPOJO, String> colLengthHCM = new TableColumn<>(bundle.getString("map.table.length"));
        TableColumn<BorderPOJO, Color> colcolorHCM = new TableColumn<>(bundle.getString("map.table.color"));

        colActiveHCM.setCellValueFactory(new PropertyValueFactory<>("active"));
        colActiveHCM.setCellFactory(CheckBoxTableCell.forTableColumn(colActiveHCM));
        colFileNameHCM.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        colCountCoordHCM.setCellValueFactory(new PropertyValueFactory<>("countCoord"));
        colLengthHCM.setCellValueFactory(new PropertyValueFactory<>("length"));
        colcolorHCM.setCellValueFactory(new PropertyValueFactory<>("color"));
        colcolorHCM.setCellFactory((param) -> {
            return new ColorCellBorder();
        });

        tableHCM.setEditable(true);
        tableHCM.getColumns().addAll(colActiveHCM, colFileNameHCM, colLengthHCM, colCountCoordHCM, colcolorHCM);

        initOsmMap(bundle);

        btnCSV.setOnAction(e -> {
            openCSV(bundle);
        });

        btnHCM.setOnAction(e -> {
            openHCM(bundle);
        });

        btnReset.setOnAction(e -> {
            tableCSV.getItems().clear();
            tableHCM.getItems().clear();
            painters.clear();

            CompoundPainter<JXMapViewer> painter = new CompoundPainter<>(painters);
            mapViewer.setOverlayPainter(painter);
            mapViewer.repaint();
        });
    }

    private void openCSV(ResourceBundle bundle) {
        FileChooser fileChooser = new FileChooser();
        String borderDir = Globals.CSV_PATH;

        File dir = new File(Globals.CSV_PATH);
        if (!dir.exists()) {
            dir.mkdir();
        }

        fileChooser.setInitialDirectory(new File(borderDir));
        fileChooser.setTitle(bundle.getString("dlg.open.border"));
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);
        List<File> borderList = fileChooser.showOpenMultipleDialog(mainController.getStage());

        if (borderList != null) {
            //String path = borderList.get(0).getAbsolutePath();
            //Globals.propman.setProperty(Globals.BORDER_DIR, path.substring(0, path.lastIndexOf("\\")));
            //Globals.propman.save();

            ProgressDialog progressDialog = new ProgressDialog(mainController.getStage(), bundle);
            BorderTaskCSV borderTaskImport = new BorderTaskCSV(progressDialog, borderList, mapViewer, painters, tableCSV);
            new Thread(borderTaskImport).start();
        }
    }

    private void openHCM(ResourceBundle bundle) {
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
        List<File> borderList = fileChooser.showOpenMultipleDialog(mainController.getStage());

        if (borderList != null) {
            //String path = borderList.get(0).getAbsolutePath();
            //Globals.propman.setProperty(Globals.BORDER_DIR, path.substring(0, path.lastIndexOf("\\")));
            //Globals.propman.save();

            ProgressDialog progressDialog = new ProgressDialog(mainController.getStage(), bundle);
            BorderTaskHCM borderTaskImport = new BorderTaskHCM(progressDialog, borderList, mapViewer, painters, tableHCM);
            new Thread(borderTaskImport).start();
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
        GeoPosition zellaMehlis = new GeoPosition(lat, lon);

        mapViewer.setZoom(7);
        mapViewer.setAddressLocation(zellaMehlis);

        // Add interactions
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

        //Popup
        MousePopupListener mousePopupListener = new MousePopupListener(mapViewer);
        mousePopupListener.setGeoClipboard(new MousePopupListener.GeoClipboardListener() {
            @Override
            public void setPoint(GeoPosition geoPosition) {
                Platform.runLater(() -> {

                    PosPainter posPainter = new PosPainter(geoPosition);
                    painters.add(posPainter);

                    CompoundPainter<JXMapViewer> painter = new CompoundPainter<>(painters);
                    mapViewer.setOverlayPainter(painter);
                    mapViewer.repaint();

                    drawLine();

                    deleteLine();
                });
            }
        });
        mapViewer.addMouseListener(mousePopupListener);

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

    private void drawLine() {
        List<GeoPosition> geoPositions = new ArrayList<>();
        for (Painter painter : painters) {
            if (painter instanceof PosPainter) {
                PosPainter posPainter = (PosPainter) painter;
                geoPositions.add(posPainter.getGeoPosition());
            }
        }
        if (geoPositions.size() == 2) {
            LinePainter linePainter = new LinePainter(geoPositions.get(0), geoPositions.get(1));
            painters.add(linePainter);

            CompoundPainter<JXMapViewer> painter = new CompoundPainter<>(painters);
            mapViewer.setOverlayPainter(painter);
            mapViewer.repaint();
        }
    }

    private void deleteLine() {
        boolean isPresent = false;
        for (int i = 0; i < painters.size(); i++) {
            if (painters.get(i) instanceof LinePainter) {
                isPresent = true;
                break;
            }
        }
        for (int i = painters.size() - 1; i >= 0; i--) {
            Painter painter = painters.get(i);
            if (painter instanceof PosPainter) {
                if (isPresent) {
                    painters.remove(i);
                }
            }
            if (painter instanceof LinePainter) {
                if (isPresent) {
                    painters.remove(i);
                }
            }
        }
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
