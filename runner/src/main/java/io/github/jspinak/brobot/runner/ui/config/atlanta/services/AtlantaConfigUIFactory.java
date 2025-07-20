package io.github.jspinak.brobot.runner.ui.config.atlanta.services;

import io.github.jspinak.brobot.runner.ui.components.BrobotButton;
import io.github.jspinak.brobot.runner.ui.components.base.AtlantaCard;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.springframework.stereotype.Service;

/**
 * Factory service for creating UI components for the Atlanta config panel.
 */
@Service
public class AtlantaConfigUIFactory {
    
    /**
     * Configuration for UI component creation.
     */
    public static class UIConfiguration {
        private int searchFieldWidth = 300;
        private int leftCardMinWidth = 600;
        private int rightCardMinWidth = 500;
        private int splitLayoutSpacing = 24;
        private int actionBarSpacing = 8;
        private int tableContentSpacing = 16;
        private Insets actionBarPadding = new Insets(0);
        
        // Builder pattern
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private final UIConfiguration config = new UIConfiguration();
            
            public Builder searchFieldWidth(int width) {
                config.searchFieldWidth = width;
                return this;
            }
            
            public Builder leftCardMinWidth(int width) {
                config.leftCardMinWidth = width;
                return this;
            }
            
            public Builder rightCardMinWidth(int width) {
                config.rightCardMinWidth = width;
                return this;
            }
            
            public Builder splitLayoutSpacing(int spacing) {
                config.splitLayoutSpacing = spacing;
                return this;
            }
            
            public UIConfiguration build() {
                return config;
            }
        }
        
        // Getters
        public int getSearchFieldWidth() { return searchFieldWidth; }
        public int getLeftCardMinWidth() { return leftCardMinWidth; }
        public int getRightCardMinWidth() { return rightCardMinWidth; }
        public int getSplitLayoutSpacing() { return splitLayoutSpacing; }
        public int getActionBarSpacing() { return actionBarSpacing; }
        public int getTableContentSpacing() { return tableContentSpacing; }
        public Insets getActionBarPadding() { return actionBarPadding; }
    }
    
    private UIConfiguration configuration = new UIConfiguration();
    
    /**
     * Sets the UI configuration.
     *
     * @param configuration The configuration to use
     */
    public void setConfiguration(UIConfiguration configuration) {
        this.configuration = configuration;
    }
    
    /**
     * Creates the action bar with buttons.
     *
     * @return The action bar HBox
     */
    public HBox createActionBar() {
        HBox actionBar = new HBox(configuration.getActionBarSpacing());
        actionBar.getStyleClass().add("action-bar");
        actionBar.setAlignment(Pos.CENTER_LEFT);
        actionBar.setPadding(configuration.getActionBarPadding());
        return actionBar;
    }
    
    /**
     * Creates a primary button.
     *
     * @param text The button text
     * @return The styled button
     */
    public BrobotButton createPrimaryButton(String text) {
        return BrobotButton.primary(text);
    }
    
    /**
     * Creates a secondary button.
     *
     * @param text The button text
     * @return The styled button
     */
    public BrobotButton createSecondaryButton(String text) {
        return BrobotButton.secondary(text);
    }
    
    /**
     * Creates a standard button with custom style classes.
     *
     * @param text The button text
     * @param styleClasses The style classes to apply
     * @return The styled button
     */
    public Button createButton(String text, String... styleClasses) {
        Button button = new Button(text);
        button.getStyleClass().addAll(styleClasses);
        return button;
    }
    
    /**
     * Creates the split layout container.
     *
     * @return The split layout HBox
     */
    public HBox createSplitLayout() {
        HBox splitLayout = new HBox(configuration.getSplitLayoutSpacing());
        splitLayout.getStyleClass().add("split-layout");
        return splitLayout;
    }
    
    /**
     * Creates an Atlanta card.
     *
     * @param title The card title
     * @param minWidth The minimum width
     * @param styleClass Additional style class
     * @return The configured card
     */
    public AtlantaCard createCard(String title, double minWidth, String styleClass) {
        AtlantaCard card = new AtlantaCard(title);
        card.getStyleClass().add(styleClass);
        card.setMinWidth(minWidth);
        card.setExpand(true);
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }
    
    /**
     * Creates the search bar.
     *
     * @param searchField The search field to include
     * @param itemsPerPage The items per page combo box
     * @return The search bar HBox
     */
    public HBox createSearchBar(TextField searchField, ComboBox<Integer> itemsPerPage) {
        HBox searchBar = new HBox(12);
        searchBar.getStyleClass().add("search-bar");
        searchBar.setAlignment(Pos.CENTER_LEFT);
        
        searchField.getStyleClass().add("search-input");
        searchField.setPromptText("Search configurations...");
        searchField.setPrefWidth(configuration.getSearchFieldWidth());
        HBox.setHgrow(searchField, Priority.ALWAYS);
        
        Label itemsLabel = new Label("Items per page:");
        itemsLabel.getStyleClass().add("form-label");
        
        itemsPerPage.getStyleClass().add("select");
        
        searchBar.getChildren().addAll(searchField, itemsLabel, itemsPerPage);
        
        return searchBar;
    }
    
    /**
     * Creates a table content container.
     *
     * @return The table content VBox
     */
    public VBox createTableContent() {
        VBox tableContent = new VBox(configuration.getTableContentSpacing());
        return tableContent;
    }
    
    /**
     * Creates a spacer region.
     *
     * @return The spacer region
     */
    public Region createSpacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }
    
    /**
     * Creates a label with specific style.
     *
     * @param text The label text
     * @param styleClasses The style classes
     * @return The styled label
     */
    public Label createLabel(String text, String... styleClasses) {
        Label label = new Label(text);
        label.getStyleClass().addAll(styleClasses);
        return label;
    }
    
    /**
     * Creates the items per page combo box.
     *
     * @return The configured combo box
     */
    public ComboBox<Integer> createItemsPerPageCombo() {
        ComboBox<Integer> itemsPerPage = new ComboBox<>();
        itemsPerPage.getItems().addAll(25, 50, 100);
        itemsPerPage.setValue(25);
        itemsPerPage.getStyleClass().add("select");
        return itemsPerPage;
    }
    
    /**
     * Creates a search field.
     *
     * @return The search field
     */
    public TextField createSearchField() {
        TextField searchField = new TextField();
        searchField.getStyleClass().add("search-input");
        searchField.setPromptText("Search configurations...");
        searchField.setPrefWidth(configuration.getSearchFieldWidth());
        return searchField;
    }
    
    /**
     * Holder class for action bar components.
     */
    public static class ActionBarComponents {
        private final BrobotButton newConfigBtn;
        private final BrobotButton importBtn;
        private final BrobotButton refreshBtn;
        private final Label configPathLabel;
        private final BrobotButton changePathBtn;
        private final BrobotButton openFolderBtn;
        private final Button importConfigBtn;
        
        public ActionBarComponents(BrobotButton newConfigBtn, BrobotButton importBtn, 
                                 BrobotButton refreshBtn, Label configPathLabel,
                                 BrobotButton changePathBtn, BrobotButton openFolderBtn,
                                 Button importConfigBtn) {
            this.newConfigBtn = newConfigBtn;
            this.importBtn = importBtn;
            this.refreshBtn = refreshBtn;
            this.configPathLabel = configPathLabel;
            this.changePathBtn = changePathBtn;
            this.openFolderBtn = openFolderBtn;
            this.importConfigBtn = importConfigBtn;
        }
        
        // Getters
        public BrobotButton getNewConfigBtn() { return newConfigBtn; }
        public BrobotButton getImportBtn() { return importBtn; }
        public BrobotButton getRefreshBtn() { return refreshBtn; }
        public Label getConfigPathLabel() { return configPathLabel; }
        public BrobotButton getChangePathBtn() { return changePathBtn; }
        public BrobotButton getOpenFolderBtn() { return openFolderBtn; }
        public Button getImportConfigBtn() { return importConfigBtn; }
    }
    
    /**
     * Creates all action bar components.
     *
     * @param configPath The current config path
     * @return The action bar components
     */
    public ActionBarComponents createActionBarComponents(String configPath) {
        BrobotButton newConfigBtn = createPrimaryButton("+ New Configuration");
        BrobotButton importBtn = createSecondaryButton("📁 Import");
        BrobotButton refreshBtn = createSecondaryButton("🔄 Refresh");
        
        Label configPathLabel = new Label("Config Path: " + configPath);
        configPathLabel.getStyleClass().addAll("button", "secondary");
        
        BrobotButton changePathBtn = createSecondaryButton("🔧 Change...");
        BrobotButton openFolderBtn = createSecondaryButton("📂 Open Folder");
        
        Button importConfigBtn = createButton("Import Config", "button", "primary");
        
        return new ActionBarComponents(newConfigBtn, importBtn, refreshBtn, 
            configPathLabel, changePathBtn, openFolderBtn, importConfigBtn);
    }
}