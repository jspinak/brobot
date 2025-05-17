package io.github.jspinak.brobot.runner.ui;

import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.init.BrobotLibraryInitializer;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@FxmlView
public class BrobotRunnerView extends BorderPane {

    private final BrobotRunnerProperties properties;
    private final BrobotLibraryInitializer libraryInitializer;
    private final ApplicationContext context;
    private final EventBus eventBus;

    @Autowired
    public BrobotRunnerView(
            BrobotRunnerProperties properties,
            BrobotLibraryInitializer libraryInitializer,
            ApplicationContext context,
            EventBus eventBus) {
        this.properties = properties;
        this.libraryInitializer = libraryInitializer;
        this.context = context;
        this.eventBus = eventBus;

        initialize();
    }

    @PostConstruct
    public void postConstruct() {
        // Publish startup event
        eventBus.publish(LogEvent.info(this, "Brobot Runner application started", "System"));
    }

    private void initialize() {
        // Create tab pane for different sections
        TabPane tabPane = new TabPane();

        // Configuration tab
        Tab configTab = new Tab("Configuration");
        ConfigurationPanel configPanel = new ConfigurationPanel(properties, libraryInitializer, eventBus);
        configTab.setContent(configPanel);
        configTab.setClosable(false);

        // Automation tab
        Tab automationTab = new Tab("Automation");
        AutomationPanel automationPanel = context.getBean(AutomationPanel.class);
        automationTab.setContent(automationPanel);
        automationTab.setClosable(false);

        // Add tabs to pane
        tabPane.getTabs().addAll(configTab, automationTab);

        // Set as the main content
        setCenter(tabPane);

        // Setup tab change listener to publish events
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab != null) {
                eventBus.publish(LogEvent.debug(this, "Tab changed to: " + newTab.getText(), "UI"));
            }
        });
    }
}