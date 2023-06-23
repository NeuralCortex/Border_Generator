package com.fx.gen.task;

import com.fx.gen.Globals;
import com.fx.gen.dialog.ProgressDialog;
import com.fx.gen.pojo.BorderData;
import com.fx.gen.pojo.Position;
import java.util.ArrayList;
import java.util.List;
import javafx.concurrent.Task;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.operation.buffer.BufferOp;
import org.locationtech.jts.operation.buffer.BufferParameters;

/**
 *
 * @author pscha
 */
public class ScaleTask extends Task<Integer> {

    private final ProgressDialog progressDialog;
    private final BorderData borderData0;
    private final BorderData borderData;
    private boolean stop = false;

    public interface ScaleTaskListener {

        public void getScaledBorder(List<Position> list);
    }
    private ScaleTaskListener scaleTaskListener;

    public ScaleTask(ProgressDialog progressDialog, BorderData borderData0, BorderData borderData) {
        this.progressDialog = progressDialog;
        this.borderData0 = borderData0;
        this.borderData = borderData;
        initProgressDlg();
    }

    private void initProgressDlg() {
        progressDialog.getLbLeft().setText("0");
        progressDialog.getProgressBar().progressProperty().bind(progressProperty());
        progressDialog.getLbRight().setText(borderData0.getBorderPainter().getBorder().size() + "");
        progressDialog.getBtnClose().setManaged(false);
        progressDialog.setProgressDialogInterface(() -> {
            stop = true;
        });
        progressDialog.show();
    }

    @Override
    protected Integer call() throws Exception {
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.maximumPreciseValue), Globals.WGS84_SRID);

        List<Coordinate> posList = new ArrayList<>();
        for (int i = 0; i < borderData0.getBorderPainter().getBorder().size(); i++) {
            Position position = borderData0.getBorderPainter().getBorder().get(i);
            posList.add(new Coordinate(position.getLon(), position.getLat()));
        }
        Position position = borderData0.getBorderPainter().getBorder().get(0);
        posList.add(new Coordinate(position.getLon(),position.getLat()));

        Polygon polygon = geometryFactory.createPolygon(posList.stream().toArray(Coordinate[]::new));
        double dist = borderData.getDist() / 100.0;
        BufferParameters bufferParameters=new BufferParameters();
        bufferParameters.setSingleSided(true);
        bufferParameters.setQuadrantSegments(180);
        //Geometry geometry = polygon.buffer(dist, bufferParameters);
        Geometry geometry = BufferOp.bufferOp(polygon, dist, bufferParameters);

        List<Position> scaleList = new ArrayList<>();
        Coordinate coordinates[] = geometry.getCoordinates();
        for (int i = 0; i < coordinates.length; i++) {
            scaleList.add(new Position(coordinates[i].x, coordinates[i].y));
            updateProgress(i, coordinates.length);
        }

        scaleTaskListener.getScaledBorder(scaleList);

        return 1;
    }
    
    @Override
    protected void succeeded() {
        progressDialog.closeDialog();
    }

    public void setScaleTaskListener(ScaleTaskListener scaleTaskListener) {
        this.scaleTaskListener = scaleTaskListener;
    }
}
