package io.github.jspinak.brobot.runner.ui;

import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.init.BrobotLibraryInitializer;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@FxmlView
public class BrobotRunnerView extends BorderPane {

    private final BrobotRunnerProperties properties;
    private final BrobotLibraryInitializer libraryInitializer;

    @Autowired
    public BrobotRunnerView(
            BrobotRunnerProperties properties,
            BrobotLibraryInitializer libraryInitializer) {
        this.properties = properties;
        this.libraryInitializer = libraryInitializer;

        initialize();
    }

    private void initialize() {
        // Create tab pane for different sections
        TabPane tabPane = new TabPane();

        // Configuration tab
        Tab configTab = new Tab("Configuration");
        configTab.setContent(new ConfigurationPanel(properties, libraryInitializer));
        configTab.setClosable(false);

        // Automation tab
        Tab automationTab = new Tab("Automation");
        automationTab.setContent(new AutomationPanel());
        automationTab.setClosable(false);

        // Add tabs to pane
        tabPane.getTabs().addAll(configTab, automationTab);

        // Set as the main content
        setCenter(tabPane);
    }
}