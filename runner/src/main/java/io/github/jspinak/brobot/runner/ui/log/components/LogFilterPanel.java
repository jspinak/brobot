package io.github.jspinak.brobot.runner.ui.log.components;

import atlantafx.base.theme.Styles;
import io.github.jspinak.brobot.runner.ui.components.base.BrobotCard;
import io.github.jspinak.brobot.runner.ui.log.models.LogEntry;
import io.github.jspinak.brobot.runner.ui.log.services.LogFilterService;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.Node;
import lombok.Getter;

import java.time.LocalDate;
import java.util.function.Predicate;

/**
 * Panel for log filtering controls.
 * Provides search, type, level, and date range filtering.
 */
@Getter
public class LogFilterPanel extends VBox {
    
    private final LogFilterService filterService;
    
    // Filter controls
    private TextField searchField;
    private CheckBox regexCheckBox;
    private CheckBox caseSensitiveCheckBox;
    private ComboBox<String> typeComboBox;
    private ComboBox<LogEntry.LogLevel> levelComboBox;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private ComboBox<LogFilterService.QuickFilterType> quickFilterCombo;
    
    // Filter property that can be observed
    private final ObjectProperty<Predicate<LogEntry>> filterProperty = new SimpleObjectProperty<>(entry -> true);
    
    public LogFilterPanel(LogFilterService filterService) {
        this.filterService = filterService;
        setupUI();
        setupBindings();
        // Don't apply filters in constructor - let the parent component initialize when ready
    }
    
    /**
     * Sets up the UI components.
     */
    private void setupUI() {
        setSpacing(12);
        setPadding(new Insets(12));
        getStyleClass().add("log-filter-panel");
        
        // Search section
        BrobotCard searchCard = new BrobotCard("Search");
        searchCard.addContent(createSearchSection());
        
        // Filters section
        BrobotCard filtersCard = new BrobotCard("Filters");
        filtersCard.addContent(createFiltersSection());
        
        // Quick filters section
        BrobotCard quickFiltersCard = new BrobotCard("Quick Filters");
        quickFiltersCard.addContent(createQuickFiltersSection());
        
        getChildren().addAll(searchCard, filtersCard, quickFiltersCard);
    }
    
    /**
     * Creates the search section.
     */
    private Node createSearchSection() {
        VBox searchBox = new VBox(8);
        
        // Search field with icon
        HBox searchFieldBox = new HBox(8);
        searchFieldBox.setAlignment(Pos.CENTER_LEFT);
        
        searchField = new TextField();
        searchField.setPromptText("Search logs...");
        searchField.setPrefWidth(300);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        
        Button clearButton = new Button("✕");
        clearButton.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.BUTTON_CIRCLE, Styles.SMALL);
        clearButton.setOnAction(e -> searchField.clear());
        clearButton.disableProperty().bind(searchField.textProperty().isEmpty());
        
        searchFieldBox.getChildren().addAll(searchField, clearButton);
        
        // Search options
        HBox optionsBox = new HBox(12);
        regexCheckBox = new CheckBox("Use Regex");
        caseSensitiveCheckBox = new CheckBox("Case Sensitive");
        
        optionsBox.getChildren().addAll(regexCheckBox, caseSensitiveCheckBox);
        
        searchBox.getChildren().addAll(searchFieldBox, optionsBox);
        return searchBox;
    }
    
    /**
     * Creates the filters section.
     */
    private Node createFiltersSection() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(8);
        
        int row = 0;
        
        // Type filter
        Label typeLabel = new Label("Type:");
        typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll("ALL", "LOG", "ERROR", "EXECUTION", "STATE", "ACTION", "SYSTEM");
        typeComboBox.setValue("ALL");
        typeComboBox.setPrefWidth(150);
        
        grid.add(typeLabel, 0, row);
        grid.add(typeComboBox, 1, row);
        
        // Level filter
        Label levelLabel = new Label("Min Level:");
        levelComboBox = new ComboBox<>();
        levelComboBox.getItems().addAll(LogEntry.LogLevel.values());
        levelComboBox.setValue(LogEntry.LogLevel.TRACE);
        levelComboBox.setPrefWidth(150);
        levelComboBox.setCellFactory(lv -> new ListCell<LogEntry.LogLevel>() {
            @Override
            protected void updateItem(LogEntry.LogLevel level, boolean empty) {
                super.updateItem(level, empty);
                if (empty || level == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(level.getIcon() + " " + level.getDisplayName());
                }
            }
        });
        levelComboBox.setButtonCell(levelComboBox.getCellFactory().call(null));
        
        grid.add(levelLabel, 2, row);
        grid.add(levelComboBox, 3, row);
        
        row++;
        
        // Date range filter
        Label dateLabel = new Label("Date Range:");
        startDatePicker = new DatePicker();
        startDatePicker.setPromptText("Start Date");
        startDatePicker.setPrefWidth(130);
        
        Label toLabel = new Label("to");
        
        endDatePicker = new DatePicker();
        endDatePicker.setPromptText("End Date");
        endDatePicker.setPrefWidth(130);
        
        HBox dateBox = new HBox(8, startDatePicker, toLabel, endDatePicker);
        dateBox.setAlignment(Pos.CENTER_LEFT);
        
        grid.add(dateLabel, 0, row);
        grid.add(dateBox, 1, row, 3, 1);
        
        return grid;
    }
    
    /**
     * Creates the quick filters section.
     */
    private Node createQuickFiltersSection() {
        VBox quickFilterBox = new VBox(8);
        
        quickFilterCombo = new ComboBox<>();
        quickFilterCombo.getItems().addAll(LogFilterService.QuickFilterType.values());
        quickFilterCombo.setValue(LogFilterService.QuickFilterType.ALL);
        quickFilterCombo.setPrefWidth(200);
        
        // Format the display
        quickFilterCombo.setCellFactory(lv -> new ListCell<LogFilterService.QuickFilterType>() {
            @Override
            protected void updateItem(LogFilterService.QuickFilterType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatQuickFilterType(item));
                }
            }
        });
        quickFilterCombo.setButtonCell(quickFilterCombo.getCellFactory().call(null));
        
        // Action buttons
        HBox buttonBox = new HBox(8);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        
        Button applyButton = new Button("Apply Filters");
        applyButton.getStyleClass().addAll(Styles.ACCENT);
        applyButton.setOnAction(e -> applyFilters());
        
        Button clearButton = new Button("Clear All");
        clearButton.getStyleClass().add(Styles.BUTTON_OUTLINED);
        clearButton.setOnAction(e -> clearFilters());
        
        buttonBox.getChildren().addAll(applyButton, clearButton);
        
        quickFilterBox.getChildren().addAll(quickFilterCombo, buttonBox);
        return quickFilterBox;
    }
    
    /**
     * Sets up bindings and listeners.
     */
    private void setupBindings() {
        // Apply filters on Enter in search field
        searchField.setOnAction(e -> applyFilters());
        
        // Quick filter changes
        quickFilterCombo.valueProperty().addListener((obs, old, value) -> {
            if (value != LogFilterService.QuickFilterType.ALL) {
                applyQuickFilter(value);
            }
        });
        
        // Date picker validation
        startDatePicker.valueProperty().addListener((obs, old, value) -> {
            if (value != null && endDatePicker.getValue() != null && value.isAfter(endDatePicker.getValue())) {
                endDatePicker.setValue(value);
            }
        });
        
        endDatePicker.valueProperty().addListener((obs, old, value) -> {
            if (value != null && startDatePicker.getValue() != null && value.isBefore(startDatePicker.getValue())) {
                startDatePicker.setValue(value);
            }
        });
    }
    
    /**
     * Applies the current filters.
     */
    public void applyFilters() {
        LogFilterService.FilterCriteria criteria = LogFilterService.FilterCriteria.builder()
                .searchText(searchField.getText())
                .useRegex(regexCheckBox.isSelected())
                .caseSensitive(caseSensitiveCheckBox.isSelected())
                .logType(typeComboBox.getValue())
                .minLevel(levelComboBox.getValue())
                .dateRange(startDatePicker.getValue(), endDatePicker.getValue())
                .build();
        
        Predicate<LogEntry> filter = filterService.createFilter(criteria);
        filterProperty.set(filter);
        
        // Reset quick filter combo
        quickFilterCombo.setValue(LogFilterService.QuickFilterType.ALL);
    }
    
    /**
     * Applies a quick filter.
     */
    private void applyQuickFilter(LogFilterService.QuickFilterType type) {
        Predicate<LogEntry> filter = filterService.createQuickFilter(type);
        filterProperty.set(filter);
        
        // Clear other filters to show quick filter is active
        clearFilterControls();
    }
    
    /**
     * Clears all filters.
     */
    private void clearFilters() {
        clearFilterControls();
        quickFilterCombo.setValue(LogFilterService.QuickFilterType.ALL);
        filterProperty.set(entry -> true);
    }
    
    /**
     * Clears filter controls without applying.
     */
    private void clearFilterControls() {
        searchField.clear();
        regexCheckBox.setSelected(false);
        caseSensitiveCheckBox.setSelected(false);
        typeComboBox.setValue("ALL");
        levelComboBox.setValue(LogEntry.LogLevel.TRACE);
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
    }
    
    /**
     * Formats quick filter type for display.
     */
    private String formatQuickFilterType(LogFilterService.QuickFilterType type) {
        switch (type) {
            case ALL:
                return "All Logs";
            case ERRORS_ONLY:
                return "Errors Only";
            case WARNINGS_AND_ABOVE:
                return "Warnings & Above";
            case TODAY_ONLY:
                return "Today Only";
            case LAST_HOUR:
                return "Last Hour";
            case WITH_EXCEPTIONS:
                return "With Exceptions";
            case STATE_TRANSITIONS:
                return "State Transitions";
            case ACTIONS_ONLY:
                return "Actions Only";
            default:
                return type.toString();
        }
    }
    
    /**
     * Gets the current filter predicate.
     */
    public Predicate<LogEntry> getFilter() {
        return filterProperty.get();
    }
    
    /**
     * Sets the search text programmatically.
     */
    public void setSearchText(String text) {
        searchField.setText(text);
        applyFilters();
    }
    
    /**
     * Sets the type filter programmatically.
     */
    public void setTypeFilter(String type) {
        if (typeComboBox.getItems().contains(type)) {
            typeComboBox.setValue(type);
            applyFilters();
        }
    }
    
    /**
     * Sets the level filter programmatically.
     */
    public void setLevelFilter(LogEntry.LogLevel level) {
        levelComboBox.setValue(level);
        applyFilters();
    }
}