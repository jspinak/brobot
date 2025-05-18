package io.github.jspinak.brobot.runner.ui.screens;

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
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * A showcase screen that demonstrates the custom UI components available in the application.
 * This serves as both documentation and a visual test for the components.
 */
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

        // Create header
        Label titleLabel = new Label("Component Showcase");
        titleLabel.getStyleClass().add("title");

        // Create toolbar with theme toggle
        ToggleButton themeToggle = new ToggleButton("Dark Mode");
        themeToggle.setSelected(themeManager.getCurrentTheme() == ThemeManager.Theme.DARK);
        themeToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            themeManager.setTheme(newVal ? ThemeManager.Theme.DARK : ThemeManager.Theme.LIGHT);
        });

        HBox toolBar = new HBox(10);
        toolBar.setAlignment(Pos.CENTER_RIGHT);
        toolBar.setPadding(new Insets(10));
        toolBar.getChildren().add(themeToggle);

        VBox header = new VBox(10, titleLabel, toolBar);
        header.setPadding(new Insets(20, 20, 10, 20));

        // Create breadcrumb navigation
        BreadcrumbBar breadcrumbBar = new BreadcrumbBar();
        breadcrumbBar.addItem("Home", item -> System.out.println("Home clicked"));
        breadcrumbBar.addItem("Components", item -> System.out.println("Components clicked"));
        breadcrumbBar.addItem("Showcase", null);

        // Create content
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        // Add demonstration sections
        for (DemoSection section : DemoSection.values()) {
            content.getChildren().add(createSection(section));
        }

        // Create status bar
        statusBar = new StatusBar();
        statusBar.setStatusMessage("Ready");
        statusBar.addStatusIndicator("Demo Mode", "warning");
        statusBar.createMemoryIndicator();

        // Add all to the layout
        setTop(new VBox(header, breadcrumbBar));
        setCenter(content);
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
        sectionTitle.getStyleClass().add("subtitle");

        VBox sectionContent = new VBox(10);
        sectionContent.setPadding(new Insets(10, 0, 20, 0));

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

        VBox sectionBox = new VBox(10);
        sectionBox.getChildren().addAll(sectionTitle, sectionContent);
        return sectionBox;
    }

    /**
     * Creates the buttons demonstration.
     *
     * @return The buttons demo node
     */
    private VBox createButtonsDemo() {
        VBox container = new VBox(15);

        // Standard buttons
        HBox standardButtons = new HBox(10);
        standardButtons.setAlignment(Pos.CENTER_LEFT);

        Button defaultButton = new Button("Default Button");
        defaultButton.setOnAction(e -> statusBar.setStatusMessage("Default button clicked"));

        Button primaryButton = new Button("Primary Button");
        primaryButton.getStyleClass().add("button-primary");
        primaryButton.setOnAction(e -> statusBar.setStatusMessage("Primary button clicked"));

        Button secondaryButton = new Button("Secondary Button");
        secondaryButton.getStyleClass().add("button-secondary");
        secondaryButton.setOnAction(e -> statusBar.setStatusMessage("Secondary button clicked"));

        Button dangerButton = new Button("Danger Button");
        dangerButton.getStyleClass().add("button-danger");
        dangerButton.setOnAction(e -> statusBar.setStatusMessage("Danger button clicked"));

        Button outlineButton = new Button("Outline Button");
        outlineButton.getStyleClass().add("button-outline");
        outlineButton.setOnAction(e -> statusBar.setStatusMessage("Outline button clicked"));

        standardButtons.getChildren().addAll(
                defaultButton, primaryButton, secondaryButton, dangerButton, outlineButton);

        // Toggle buttons
        HBox toggleButtons = new HBox(10);
        toggleButtons.setAlignment(Pos.CENTER_LEFT);

        ToggleButton toggleButton1 = new ToggleButton("Toggle 1");
        ToggleButton toggleButton2 = new ToggleButton("Toggle 2");
        toggleButton2.setSelected(true);

        toggleButtons.getChildren().addAll(toggleButton1, toggleButton2);

        // Add descriptions
        container.getChildren().addAll(
                new Label("Standard Buttons:"),
                standardButtons,
                new Label("Toggle Buttons:"),
                toggleButtons
        );

        return container;
    }

    /**
     * Creates the layout components demonstration.
     *
     * @return The layout demo node
     */
    private VBox createLayoutDemo() {
        VBox container = new VBox(15);

        // Card demo
        io.github.jspinak.brobot.runner.ui.components.Card card = new io.github.jspinak.brobot.runner.ui.components.Card("Sample Card");
        Label cardContent = new Label("This is a card component that can contain any content. Cards are useful for displaying grouped information.");
        cardContent.setWrapText(true);
        card.setContent(cardContent);

        Button cardAction = new Button("Card Action");
        cardAction.getStyleClass().add("button-primary");
        card.addAction(cardAction);

        // Panel demo
        io.github.jspinak.brobot.runner.ui.components.Panel panel = new io.github.jspinak.brobot.runner.ui.components.Panel("Sample Panel");
        panel.setCollapsible(true);

        Label panelContent = new Label("This is a panel component that can be collapsed. Panels are useful for sections that need to be toggled on and off.");
        panelContent.setWrapText(true);
        panel.setContent(panelContent);

        Button panelAction = new Button("Panel Action");
        panel.addAction(panelAction);

        HBox cardsLayout = new HBox(20);
        cardsLayout.getChildren().addAll(card, panel);
        HBox.setHgrow(card, Priority.ALWAYS);
        HBox.setHgrow(panel, Priority.ALWAYS);

        // Add descriptions
        container.getChildren().addAll(
                new Label("Cards and Panels:"),
                cardsLayout
        );

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
        alertBoxes.setAlignment(Pos.CENTER_LEFT);

        for (String type : new String[] {"info", "success", "warning", "error"}) {
            VBox alertBox = new VBox(5);
            alertBox.getStyleClass().addAll("alert-box", type);

            Label alertTitle = new Label(type.substring(0, 1).toUpperCase() + type.substring(1));
            alertTitle.setStyle("-fx-font-weight: bold;");

            Label alertText = new Label("This is a " + type + " message box");

            alertBox.getChildren().addAll(alertTitle, alertText);
            alertBoxes.getChildren().add(alertBox);
            HBox.setHgrow(alertBox, Priority.ALWAYS);
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