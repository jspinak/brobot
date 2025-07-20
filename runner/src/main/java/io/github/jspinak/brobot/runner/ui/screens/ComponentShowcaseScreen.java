package io.github.jspinak.brobot.runner.ui.screens;

import lombok.Getter;
import lombok.EqualsAndHashCode;

import io.github.jspinak.brobot.runner.ui.components.BreadcrumbBar;
import io.github.jspinak.brobot.runner.ui.components.EnhancedTable;
import io.github.jspinak.brobot.runner.ui.components.StatusBar;
import io.github.jspinak.brobot.runner.ui.theme.ThemeManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * A showcase screen that demonstrates the custom UI components available in the application.
 * This serves as both documentation and a visual test for the components.
 */
@Getter
@EqualsAndHashCode(callSuper = false)
public class ComponentShowcaseScreen extends BorderPane {

    private final ThemeManager themeManager;
    private final StatusBar statusBar;

    private enum DemoSection {
        BUTTONS("Buttons"),
        LAYOUT("Layout Components"),
        NAVIGATION("Navigation Components"),
        DATA("Data Components"),
        STATUS("Status Components");

        private final String title;

        DemoSection(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }
    }

    public ComponentShowcaseScreen(ThemeManager themeManager) {
        this.themeManager = themeManager;
        
        // Add CSS class for specific styling
        getStyleClass().add("component-showcase-screen");

        // Create header
        Label titleLabel = new Label("Component Showcase");
        titleLabel.getStyleClass().add("title");

        VBox header = new VBox(10, titleLabel);
        header.setPadding(new Insets(20, 20, 10, 20));

        // Create status bar early so it can be used in callbacks
        statusBar = new StatusBar();
        statusBar.setStatusMessage("Ready");
        statusBar.addStatusIndicator("Demo Mode", "warning");
        statusBar.createMemoryIndicator();

        // Create breadcrumb navigation
        BreadcrumbBar breadcrumbBar = new BreadcrumbBar();
        breadcrumbBar.addItem("Home", item -> statusBar.setStatusMessage("Home clicked"));
        breadcrumbBar.addItem("Components", item -> statusBar.setStatusMessage("Components clicked"));
        breadcrumbBar.addItem("Showcase", null);

        // Create content with scroll pane for better layout
        VBox content = new VBox(30);
        content.getStyleClass().add("showcase-content");
        content.setPadding(new Insets(20));
        content.setFillWidth(true);
        content.setAlignment(Pos.TOP_CENTER);

        // Add demonstration sections
        for (DemoSection section : DemoSection.values()) {
            content.getChildren().add(createSection(section));
        }
        
        // Wrap content in scroll pane
        javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane(content);
        scrollPane.getStyleClass().add("scroll-pane");
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER);

        // Status bar already created above

        // Add all to the layout
        setTop(new VBox(header, breadcrumbBar));
        setCenter(scrollPane);
        setBottom(statusBar);
    }

    /**
     * Creates a section of component demonstrations.
     *
     * @param section The section to create
     * @return The section node
     */
    private VBox createSection(DemoSection section) {
        Label sectionTitle = new Label(section.getTitle());
        sectionTitle.getStyleClass().add("showcase-section-title");

        VBox sectionContent = new VBox(15);
        sectionContent.getStyleClass().add("showcase-section");
        sectionContent.setFillWidth(true);

        // Create section content based on type
        switch (section) {
            case BUTTONS:
                sectionContent.getChildren().addAll(createButtonsDemo());
                break;
            case LAYOUT:
                sectionContent.getChildren().addAll(createLayoutDemo());
                break;
            case NAVIGATION:
                sectionContent.getChildren().addAll(createNavigationDemo());
                break;
            case DATA:
                sectionContent.getChildren().addAll(createDataDemo());
                break;
            case STATUS:
                sectionContent.getChildren().addAll(createStatusDemo());
                break;
        }

        VBox sectionBox = new VBox(16);
        sectionBox.getStyleClass().add("showcase-section-container");
        sectionBox.getChildren().addAll(sectionTitle, sectionContent);
        sectionBox.setFillWidth(true);
        sectionBox.setMaxWidth(1000);
        return sectionBox;
    }

    /**
     * Creates the buttons demonstration.
     *
     * @return The buttons demo node
     */
    private VBox createButtonsDemo() {
        VBox container = new VBox(20);
        container.getStyleClass().add("button-showcase");

        // Standard buttons section
        VBox standardSection = new VBox(12);
        standardSection.getStyleClass().add("demo-section");
        
        Label standardLabel = new Label("Standard Buttons");
        standardLabel.getStyleClass().add("demo-title");
        
        javafx.scene.layout.FlowPane standardButtons = new javafx.scene.layout.FlowPane(12, 12);
        standardButtons.setAlignment(Pos.CENTER_LEFT);

        Button defaultButton = new Button("Default Button");
        defaultButton.setOnAction(e -> statusBar.setStatusMessage("Default button clicked"));

        Button primaryButton = new Button("Primary Button");
        primaryButton.getStyleClass().add("button-primary");
        primaryButton.setOnAction(e -> statusBar.setStatusMessage("Primary button clicked"));

        Button secondaryButton = new Button("Secondary Button");
        secondaryButton.getStyleClass().add("button-success");
        secondaryButton.setOnAction(e -> statusBar.setStatusMessage("Secondary button clicked"));

        Button dangerButton = new Button("Danger Button");
        dangerButton.getStyleClass().add("button-danger");
        dangerButton.setOnAction(e -> statusBar.setStatusMessage("Danger button clicked"));

        standardButtons.getChildren().addAll(
                defaultButton, primaryButton, secondaryButton, dangerButton);
        
        standardSection.getChildren().addAll(standardLabel, standardButtons);

        // Toggle buttons section
        VBox toggleSection = new VBox(12);
        toggleSection.getStyleClass().add("demo-section");
        
        Label toggleLabel = new Label("Toggle Buttons");
        toggleLabel.getStyleClass().add("demo-title");
        
        HBox toggleButtons = new HBox(12);
        toggleButtons.setAlignment(Pos.CENTER_LEFT);

        ToggleButton toggleButton1 = new ToggleButton("Toggle 1");
        ToggleButton toggleButton2 = new ToggleButton("Toggle 2");
        toggleButton2.setSelected(true);

        toggleButtons.getChildren().addAll(toggleButton1, toggleButton2);
        
        toggleSection.getChildren().addAll(toggleLabel, toggleButtons);

        container.getChildren().addAll(standardSection, toggleSection);
        return container;
    }

    /**
     * Creates the layout components demonstration.
     *
     * @return The layout demo node
     */
    private VBox createLayoutDemo() {
        VBox container = new VBox(20);

        // Cards and Panels section
        VBox cardsSection = new VBox(12);
        cardsSection.getStyleClass().add("demo-section");
        
        Label cardsLabel = new Label("Cards and Panels");
        cardsLabel.getStyleClass().add("demo-title");

        // Card demo
        io.github.jspinak.brobot.runner.ui.components.Card card = new io.github.jspinak.brobot.runner.ui.components.Card("Sample Card");
        card.getStyleClass().add("showcase-card");
        Label cardContent = new Label("This is a card component that can contain any content. Cards are useful for displaying grouped information.");
        cardContent.setWrapText(true);
        card.setContent(cardContent);

        Button cardAction = new Button("Card Action");
        cardAction.getStyleClass().add("button-primary");
        card.addAction(cardAction);

        // Panel demo
        io.github.jspinak.brobot.runner.ui.components.Panel panel = new io.github.jspinak.brobot.runner.ui.components.Panel("Sample Panel");
        panel.getStyleClass().add("showcase-panel");
        panel.setCollapsible(true);

        Label panelContent = new Label("This is a panel component that can be collapsed. Panels are useful for sections that need to be toggled on and off.");
        panelContent.setWrapText(true);
        panel.setContent(panelContent);

        Button panelAction = new Button("Panel Action");
        panel.addAction(panelAction);

        javafx.scene.layout.FlowPane cardsLayout = new javafx.scene.layout.FlowPane(20, 20);
        cardsLayout.setAlignment(Pos.TOP_LEFT);
        cardsLayout.getChildren().addAll(card, panel);
        
        cardsSection.getChildren().addAll(cardsLabel, cardsLayout);
        container.getChildren().add(cardsSection);

        return container;
    }

    /**
     * Creates the navigation components demonstration.
     *
     * @return The navigation demo node
     */
    private VBox createNavigationDemo() {
        VBox container = new VBox(15);

        // Breadcrumb demo
        BreadcrumbBar breadcrumb = new BreadcrumbBar();
        breadcrumb.addItem("Home", item -> statusBar.setStatusMessage("Home clicked"));
        breadcrumb.addItem("Products", item -> statusBar.setStatusMessage("Products clicked"));
        breadcrumb.addItem("Electronics", item -> statusBar.setStatusMessage("Electronics clicked"));
        breadcrumb.addItem("Smartphones", null);

        // Add descriptions
        container.getChildren().addAll(
                new Label("Breadcrumb Navigation:"),
                breadcrumb
        );

        return container;
    }

    /**
     * Creates the data components demonstration.
     *
     * @return The data demo node
     */
    private VBox createDataDemo() {
        VBox container = new VBox(15);

        // Table demo
        EnhancedTable<DemoItem> table = new EnhancedTable<>();
        table.addColumn("ID", "id");
        table.addColumn("Name", "name");
        table.addColumn("Description", "description");

        // Set search provider
        table.setSearchProvider(item -> item.getId() + " " + item.getName() + " " + item.getDescription());

        // Add sample data
        javafx.collections.ObservableList<DemoItem> items = javafx.collections.FXCollections.observableArrayList();
        for (int i = 1; i <= 100; i++) {
            items.add(new DemoItem(
                    String.format("ITEM-%03d", i),
                    "Item " + i,
                    "Description for item " + i
            ));
        }
        table.setItems(items);

        // Set preferred height
        table.setPrefHeight(300);

        // Add descriptions
        container.getChildren().addAll(
                new Label("Enhanced Table with Filtering and Pagination:"),
                table
        );

        return container;
    }

    /**
     * Creates the status components demonstration.
     *
     * @return The status demo node
     */
    private VBox createStatusDemo() {
        VBox container = new VBox(15);

        // Status bar demo
        StatusBar demoStatusBar = new StatusBar();
        demoStatusBar.setStatusMessage("Status message example");

        Button updateStatusButton = new Button("Update Status");
        updateStatusButton.setOnAction(e -> {
            demoStatusBar.setStatusMessage("Status updated at " + java.time.LocalTime.now().toString());
        });

        Button showProgressButton = new Button("Show Progress");
        showProgressButton.setOnAction(e -> {
            demoStatusBar.setProgress(0.0);

            // Simulate progress
            javafx.animation.Timeline timeline = new javafx.animation.Timeline();
            for (int i = 0; i <= 10; i++) {
                final int step = i;
                javafx.animation.KeyFrame keyFrame = new javafx.animation.KeyFrame(
                        javafx.util.Duration.seconds(i * 0.5),
                        event -> demoStatusBar.setProgress(step / 10.0)
                );
                timeline.getKeyFrames().add(keyFrame);
            }
            timeline.play();
        });

        Button addIndicatorButton = new Button("Add Indicator");
        addIndicatorButton.setOnAction(e -> {
            demoStatusBar.addStatusIndicator("Sample", "warning");
        });

        HBox statusControls = new HBox(10);
        statusControls.getChildren().addAll(updateStatusButton, showProgressButton, addIndicatorButton);

        // Alert boxes
        HBox alertBoxes = new HBox(20);
        alertBoxes.setAlignment(Pos.TOP_LEFT);
        alertBoxes.setFillHeight(false);

        for (String type : new String[] {"info", "success", "warning", "error"}) {
            VBox alertBox = new VBox(5);
            alertBox.getStyleClass().addAll("alert-box", type);

            Label alertTitle = new Label(type.substring(0, 1).toUpperCase() + type.substring(1));
            alertTitle.setStyle("-fx-font-weight: bold;");

            Label alertText = new Label("This is a " + type + " message box");

            alertBox.getChildren().addAll(alertTitle, alertText);
            alertBox.setMaxWidth(200);
            alertBoxes.getChildren().add(alertBox);
        }

        // Add descriptions
        container.getChildren().addAll(
                new Label("Status Bar:"),
                demoStatusBar,
                statusControls,
                new Label("Alert Boxes:"),
                alertBoxes
        );

        return container;
    }

    /**
     * Sample data class for the table demo.
     */
    public static class DemoItem {
        private final String id;
        private final String name;
        private final String description;

        public DemoItem(String id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }
    }
}