package com.fx.gen;

import com.fx.gen.controller.MainController;
import java.io.File;
import java.io.FileInputStream;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Locale;
import java.util.ResourceBundle;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javax.swing.UIManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

/**
 * JavaFX App
 */
public class MainApp extends Application {

    private static final Logger _log = LogManager.getLogger(MainApp.class);
    private final ResourceBundle bundle = ResourceBundle.getBundle(Globals.BUNDLE_PATH, Globals.DEFAULT_LOCALE);

    @Override
    public void start(Stage stage) throws Exception {
        initLogger(Globals.LOG4J2_CONFIG_PATH);

        FXMLLoader loader = new FXMLLoader(getClass().getResource(Globals.FXML_MAIN_PATH), bundle);
        loader.setController(new MainController(stage));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(Globals.CSS_PATH);
        stage.getIcons().add(new Image(new FileInputStream(new File(Globals.APP_LOGO_PATH))));

        stage.setTitle(bundle.getString("app.name") + " " + bundle.getString("app.version"));
        stage.setScene(scene);
        stage.setMaximized(Globals.MAXIMIZED);
        stage.show();

        Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX((primScreenBounds.getWidth() - stage.getWidth()) / 2);
        stage.setY((primScreenBounds.getHeight() - stage.getHeight()) / 2);

        stage.setOnCloseRequest(e -> {
            //HibernateUtil.closeSessionFactory();
            Platform.exit();
        });

        _log.info("Start successfull ...");
        _log.debug("Start successfull ...");
        _log.error("Start successfull ...");
    }

    private void initLogger(String path) {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        context.setConfigLocation(new File(path).toURI());
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            _log.error(ex.getMessage());
        }
        launch(args);
    }
}
