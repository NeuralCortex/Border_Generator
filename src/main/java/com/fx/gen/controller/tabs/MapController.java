package com.fx.gen.controller.tabs;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fx.gen.Globals;
import com.fx.gen.cell.ColorCell;
import com.fx.gen.controller.MainController;
import com.fx.gen.controller.PopulateInterface;
import com.fx.gen.dialog.ProgressDialog;
import com.fx.gen.painter.BorderPainter;
import com.fx.gen.painter.PosPainter;
import com.fx.gen.pojo.Address;
import com.fx.gen.pojo.Position;
import com.fx.gen.pojo.BorderData;
import com.fx.gen.pojo.Combo;
import com.fx.gen.task.ScaleTask;
import com.fx.gen.tools.GeoSelectionAdapter;
import com.fx.gen.tools.HelperFunctions;
import com.fx.gen.tools.MousePositionListener;
import com.mapbox.geojson.MultiPolygon;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import java.awt.BorderLayout;
import java.awt.Color;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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

/**
 *
 * @author pscha
 */
public class MapController implements Initializable, PopulateInterface {

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
    private TableView<Address> tableInfo;
    @FXML
    private TableView<BorderData> tableScale;
    @FXML
    private VBox vBoxTables;
    @FXML
    private ComboBox<Combo> cbBorder;
    @FXML
    private Button btnCsv;
    @FXML
    private Button btnHcm;
    @FXML
    private Button btnReset;

    private static final Logger _log = LogManager.getLogger(MapController.class);

    private final MainController mainController;
    private final JXMapViewer mapViewer = new JXMapViewer();
    private final List<Painter<JXMapViewer>> painters = new ArrayList<>();
    private final HashMap<Integer, List<Position>> mapBorder = new HashMap<>();

    private final double lon = 10.671745101119196;
    private final double lat = 50.661742127393836;

    private HashMap<Double, List<Position>> mapLoad = new HashMap<>();

    public MapController(MainController mainController) {
        this.mainController = mainController;
    }

    @Override
    public void initialize(URL url, ResourceBundle bundle) {
        vBoxTables.setPrefWidth(400.0f);

        cbBorder.setManaged(false);

        hBoxTop.setId("hec-background-blue");
        hBoxInfo.setId("hec-background-blue");
        hBoxScale.setId("hec-background-blue");

        lbInfo.setId("hec-text-white");
        lbScale.setId("hec-text-white");

        lbInfo.setText(bundle.getString("lb.info"));
        lbScale.setText(bundle.getString("lb.scale"));
        btnCsv.setText(bundle.getString("btn.csv"));
        btnHcm.setText(bundle.getString("btn.hcm"));
        btnReset.setText(bundle.getString("btn.reset"));

        borderPane.widthProperty().addListener(e -> {
            mapViewer.repaint();
        });
        borderPane.heightProperty().addListener(e -> {
            mapViewer.repaint();
        });

        TableColumn<Address, String> colParam = new TableColumn<>("Parameter");
        TableColumn<Address, String> colValue = new TableColumn<>("Wert");

        colParam.setCellValueFactory(new PropertyValueFactory<>("name"));
        colValue.setCellValueFactory(new PropertyValueFactory<>("wert"));

        tableInfo.getColumns().addAll(colParam, colValue);

        tableInfo.getSelectionModel().selectedItemProperty().addListener((ov, o, n) -> {
            loadPolygon(n);
        });

        tableScale.setEditable(true);

        TableColumn<BorderData, Boolean> colUse = new TableColumn<>("Aktiv");
        TableColumn<BorderData, Integer> colDist = new TableColumn<>("Abstand in km");
        TableColumn<BorderData, Color> colColor = new TableColumn<>("Farbe");

        colDist.setCellValueFactory(new PropertyValueFactory<>("dist"));
        colColor.setCellValueFactory(new PropertyValueFactory<>("color"));

        colUse.setCellFactory(c -> new CheckBoxTableCell<>());
        colUse.setCellValueFactory(col -> {
            BorderData borderData = col.getValue();
            BooleanProperty property = new SimpleBooleanProperty(borderData.isUse());
            property.addListener((ov, o, n) -> {
                borderData.setUse(n);
                if (n) {
                    BorderData border0 = null;
                    for (int i = 0; i < tableScale.getItems().size(); i++) {
                        BorderData data = tableScale.getItems().get(i);
                        if (data.getDist() == 0) {
                            border0 = data;
                        }
                    }

                    if (borderData.getDist() > 0) {

                        if (mapLoad.containsKey((double) borderData.getDist())) {
                            BorderPainter borderPainter = new BorderPainter(mapLoad.get((double) borderData.getDist()), borderData.getColor());
                            borderData.setBorderPainter(borderPainter);
                            painters.add(borderPainter);
                        } else {
                            ProgressDialog progressDialog = new ProgressDialog(mainController.getStage(), bundle);
                            ScaleTask scaleTask = new ScaleTask(progressDialog, border0, borderData);
                            scaleTask.setScaleTaskListener((List<Position> list) -> {
                                BorderPainter borderPainter = new BorderPainter(list, borderData.getColor());
                                borderData.setBorderPainter(borderPainter);
                                Platform.runLater(() -> {
                                    painters.add(borderPainter);
                                    mapLoad.put((double) borderData.getDist(), list);

                                    CompoundPainter<JXMapViewer> painter = new CompoundPainter<>(painters);
                                    mapViewer.setOverlayPainter(painter);
                                    mapViewer.repaint();
                                });
                            });
                            new Thread(scaleTask).start();
                        }
                    }
                    if (borderData.getDist() == 0) {
                        painters.add(borderData.getBorderPainter());
                        mapLoad.put(0.0, borderData.getBorderPainter().getBorder());
                    }
                } else {
                    painters.remove(borderData.getBorderPainter());
                }

                CompoundPainter<JXMapViewer> painter = new CompoundPainter<>(painters);
                mapViewer.setOverlayPainter(painter);
                mapViewer.repaint();
            });

            return property;
        });

        colColor.setCellFactory((param) -> {
            return new ColorCell();
        });

        tableScale.getColumns().addAll(colUse, colDist, colColor);

        cbBorder.getSelectionModel().selectedItemProperty().addListener((ov, o, n) -> {
            drawPolygon(mapBorder.get(n.getIdx()), genRandomColor());
        });

        btnCsv.setOnAction(e -> {
            Address address = tableInfo.getSelectionModel().getSelectedItem();

            if (address != null) {
                for (int i = 0; i < tableScale.getItems().size(); i++) {
                    BorderData borderData = tableScale.getItems().get(i);
                    //System.out.println(borderData.isUse());
                    if (borderData.isUse()) {
                        try {
                            String csvFileName = address.getWert() + "." + String.format("%03d", borderData.getDist()) + ".csv";

                            File dir = new File(Globals.CSV_PATH);
                            if (!dir.exists()) {
                                dir.mkdir();
                            }

                            BufferedWriter writer = new BufferedWriter(new FileWriter(Globals.CSV_PATH + csvFileName));
                            borderData.getBorderPainter().getBorder().forEach(c -> {
                                try {
                                    writer.write(c.getLon() + ";" + c.getLat() + "\n");
                                } catch (IOException ex) {
                                    _log.error(ex.getMessage());
                                }
                            });
                            writer.close();
                        } catch (Exception ex) {
                            _log.error(ex.getMessage());
                        }
                    }
                }
            }
        });

        btnHcm.setOnAction(e -> {
            Address address = tableInfo.getSelectionModel().getSelectedItem();

            if (address != null) {
                for (int i = 0; i < tableScale.getItems().size(); i++) {
                    BorderData borderData = tableScale.getItems().get(i);
                    //System.out.println(borderData.isUse());
                    if (borderData.isUse()) {
                        try {
                            String hcmFileName = address.getWert() + "." + String.format("%03d", borderData.getDist());

                            File dir = new File(Globals.HCM_PATH);
                            if (!dir.exists()) {
                                dir.mkdir();
                            }

                            DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(Globals.HCM_PATH + hcmFileName));
                            for (Position position : borderData.getBorderPainter().getBorder()) {
                                double corLon = position.getLon() / (180.0 / Math.PI);
                                double corLat = position.getLat() / (180.0 / Math.PI);

                                byte[] bytesLon = new byte[8];
                                byte[] bytesLat = new byte[8];

                                dataOutputStream.write(HelperFunctions.doubleToByte(corLon, Globals.BYTE_ORDER));
                                dataOutputStream.write(HelperFunctions.doubleToByte(corLat, Globals.BYTE_ORDER));
                            }
                            dataOutputStream.close();
                        } catch (Exception ex) {
                            _log.error(ex.getMessage());
                        }
                    }
                }
            }
        });

        btnReset.setOnAction(e -> {
            tableInfo.getItems().clear();
            tableScale.getItems().clear();
            painters.clear();
            mapLoad.clear();
            mapBorder.clear();

            CompoundPainter<JXMapViewer> painter = new CompoundPainter<>(painters);
            mapViewer.setOverlayPainter(painter);
            mapViewer.repaint();
        });

        initOsmMap(bundle);
    }

    private void initScaleTable(List<Position> border) {
        for (BorderData borderData : tableScale.getItems()) {
            painters.remove(borderData.getBorderPainter());
        }

        tableScale.getItems().clear();

        List<BorderData> list = new ArrayList<>();
        Color color = genRandomColor();
        BorderPainter borderPainter = new BorderPainter(border, color);
        painters.add(borderPainter);
        list.add(new BorderData(true, 0, color, borderPainter));
        color = genRandomColor();
        list.add(new BorderData(false, 6, color, null));
        color = genRandomColor();
        list.add(new BorderData(false, 15, color, null));
        color = genRandomColor();
        list.add(new BorderData(false, 30, color, null));
        color = genRandomColor();
        list.add(new BorderData(false, 40, color, null));
        color = genRandomColor();
        list.add(new BorderData(false, 50, color, null));
        color = genRandomColor();
        list.add(new BorderData(false, 60, color, null));
        color = genRandomColor();
        list.add(new BorderData(false, 80, color, null));
        color = genRandomColor();
        list.add(new BorderData(false, 100, color, null));

        tableScale.getItems().addAll(FXCollections.observableArrayList(list));

        CompoundPainter<JXMapViewer> painter = new CompoundPainter<>(painters);
        mapViewer.setOverlayPainter(painter);
        mapViewer.repaint();
    }

    public static Color genRandomColor() {
        int r = (int) (Math.random() * 256);
        int g = (int) (Math.random() * 256);
        int b = (int) (Math.random() * 256);
        return new Color(r, g, b);
    }

    private void drawPolygon(List<Position> list, Color color) {
        if (list != null) {
            BorderPainter borderPainter = new BorderPainter(list, color);
            painters.add(borderPainter);

            CompoundPainter<JXMapViewer> painter = new CompoundPainter<>(painters);
            mapViewer.setOverlayPainter(painter);
            mapViewer.repaint();
        }
    }

    private void loadPolygon(Address address) {
        if (address != null) {
            mapBorder.clear();

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            String state = address.getWert().replace(" ", "%20");
            String baseURL = "https://nominatim.openstreetmap.org/search?" + address.getName().toLowerCase() + "=" + state + "&polygon_geojson=1&format=geojson";

            JsonNode root;
            try {
                root = objectMapper.readTree(new URL(baseURL));
                //System.out.println(root.toPrettyString());

                JsonNode feat = root.get("features");
                JsonNode geo = feat.get(0).get("geometry");
                JsonNode type = geo.get("type");

                //System.out.println("type: " + type.toString());
                if (type.toString().replace("\"", "").startsWith("Multi")) {
                    MultiPolygon multiPolygon = MultiPolygon.fromJson(geo.toString());
                    int idx = 0;
                    for (List<List<Point>> points : multiPolygon.coordinates()) {
                        int size = points.size();
                        //System.out.println("size: "+size);
                        for (int i = 0; i < size; i++) {
                            List<Position> posList = new ArrayList<>();
                            //System.out.println("idx: "+idx+" size: "+points.get(i).size());
                            for (int j = 0; j < points.get(i).size(); j++) {
                                Point p = points.get(i).get(j);
                                posList.add(new Position(p.longitude(), p.latitude()));
                            }
                            mapBorder.put(idx, posList);
                            cbBorder.getItems().add(new Combo(idx, points.get(i).size()));
                            idx++;
                        }
                    }
                } else {
                    Polygon polygon = Polygon.fromJson(geo.toString());
                    int idx = 0;
                    for (List<Point> points : polygon.coordinates()) {
                        List<Position> posList = new ArrayList<>();
                        for (Point point : points) {
                            posList.add(new Position(point.longitude(), point.latitude()));
                        }
                        mapBorder.put(idx, posList);
                        cbBorder.getItems().add(new Combo(idx, points.size()));
                        idx++;
                    }
                }
                int max = -9999;
                int idx = 0;
                for (Integer key : mapBorder.keySet()) {
                    List<Position> list = mapBorder.get(key);
                    if (list.size() > max) {
                        max = list.size();
                        idx = key;
                    }
                }

                initScaleTable(mapBorder.get(idx));

            } catch (Exception ex) {
                ex.printStackTrace();
            }
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

        initPainter();

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
        GeoSelectionAdapter geoSelectionAdapter = new GeoSelectionAdapter(mapViewer, painters);
        geoSelectionAdapter.setGeoSelectionAdapterListener((GeoPosition geoPosition) -> {
            PosPainter posPainter = new PosPainter(geoPosition);
            painters.clear();
            painters.add(posPainter);

            Platform.runLater(() -> {
                tableInfo.getItems().clear();
                tableScale.getItems().clear();
                cbBorder.getItems().clear();
                mapBorder.clear();
                mapLoad.clear();
            });

            getGeoInfos(geoPosition);

            CompoundPainter<JXMapViewer> painter = new CompoundPainter<>(painters);
            mapViewer.setOverlayPainter(painter);
            mapViewer.repaint();
        });
        mapViewer.addMouseListener(geoSelectionAdapter);
    }

    private void getGeoInfos(GeoPosition geoPosition) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        String baseURL = "https://nominatim.openstreetmap.org/reverse?lat=" + geoPosition.getLatitude() + "&lon=" + geoPosition.getLongitude() + "&format=json&addressdetails=1&accept-language=en";
        JsonNode info;
        try {
            info = objectMapper.readTree(new URL(baseURL));
            JsonNode address = info.get("address");

            List<Address> list = new ArrayList<>();
            tableInfo.getItems().clear();

            for (Iterator<Map.Entry<String, JsonNode>> iter = address.fields(); iter.hasNext();) {
                Map.Entry entry = iter.next();
                String key = String.valueOf(entry.getKey());
                key = key.substring(0, 1).toUpperCase() + key.substring(1);
                String value = String.valueOf(entry.getValue());
                if (key.equalsIgnoreCase("country") || key.equalsIgnoreCase("state")) {
                    list.add(new Address(key, value.replace("\"", "")));
                }
            }

            tableInfo.setItems(FXCollections.observableArrayList(list));
        } catch (Exception ex) {
            ex.printStackTrace();
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
