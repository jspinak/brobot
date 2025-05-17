package io.github.jspinak.brobot.runner;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxWeaver;
import io.github.jspinak.brobot.runner.ui.BrobotRunnerView;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class JavaFxApplication extends Application {
    private ConfigurableApplicationContext applicationContext;

    @Override
    public void init() {
        String[] args = getParameters().getRaw().toArray(new String[0]);

        // Create and configure the Spring application context
        this.applicationContext = new SpringApplicationBuilder()
                .sources(BrobotRunnerApplication.class)
                .run(args);
    }

    @Override
    public void start(Stage stage) {
        // Use FxWeaver to load the main view
        FxWeaver fxWeaver = applicationContext.getBean(FxWeaver.class);
        Scene scene = new Scene(fxWeaver.loadView(BrobotRunnerView.class), 800, 600);

        stage.setScene(scene);
        stage.setTitle("Brobot Runner");
        stage.show();
    }

    @Override
    public void stop() {
        // Clean up Spring context on application close
        this.applicationContext.close();
        Platform.exit();
    }
}