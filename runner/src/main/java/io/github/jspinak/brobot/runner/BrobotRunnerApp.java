package io.github.jspinak.brobot.runner;

import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.init.BrobotLibraryInitializer;
import io.github.jspinak.brobot.runner.ui.AutomationPanel;
import io.github.jspinak.brobot.runner.ui.ConfigurationPanel;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import lombok.Setter;
import org.springframework.context.ApplicationContext;

public class BrobotRunnerApp extends Application {
    @Setter
    private static ApplicationContext applicationContext;

    @Override
    public void start(Stage primaryStage) {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");

        // Get required beans from Spring context
        BrobotRunnerProperties properties = applicationContext.getBean(BrobotRunnerProperties.class);
        BrobotLibraryInitializer libraryInitializer = applicationContext.getBean(BrobotLibraryInitializer.class);
        EventBus eventBus = applicationContext.getBean(EventBus.class);

        // Create tab pane for different sections
        TabPane tabPane = new TabPane();

        // Configuration tab
        Tab configTab = new Tab("Configuration");
        configTab.setContent(new ConfigurationPanel(properties, libraryInitializer, eventBus));
        configTab.setClosable(false);

        // Automation tab
        Tab automationTab = new Tab("Automation");
        automationTab.setContent(new AutomationPanel()); // This would be your UI for running automation
        automationTab.setClosable(false);

        // Add tabs to pane
        tabPane.getTabs().addAll(configTab, automationTab);

        // Create scene
        Scene scene = new Scene(tabPane, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Brobot Runner - Java " + javaVersion + ", JavaFX " + javafxVersion);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}