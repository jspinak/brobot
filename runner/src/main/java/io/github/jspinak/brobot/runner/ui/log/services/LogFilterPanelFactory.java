package io.github.jspinak.brobot.runner.ui.log.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import org.springframework.stereotype.Service;

import io.github.jspinak.brobot.tools.logging.model.LogEventType;

import lombok.extern.slf4j.Slf4j;

/**
 * Factory service for creating log filter UI components. Provides consistent filter panels with
 * various configurations.
 */
@Slf4j
@Service
public class LogFilterPanelFactory {

    /** Configuration for filter panel creation. */
    public static class FilterPanelConfiguration {
        private boolean showSearch = true;
        private boolean showTypeFilter = true;
        private boolean showLevelFilter = true;
        private boolean showDateFilters = true;
        private boolean showAutoScroll = true;
        private boolean showClearButton = true;
        private boolean showPresets = false;
        private String title = "Log Viewer";
        private List<String> customTypes = new ArrayList<>();
        private List<String> customLevels = new ArrayList<>();

        public static FilterPanelConfigurationBuilder builder() {
            return new FilterPanelConfigurationBuilder();
        }

        public static class FilterPanelConfigurationBuilder {
            private FilterPanelConfiguration config = new FilterPanelConfiguration();

            public FilterPanelConfigurationBuilder showSearch(boolean show) {
                config.showSearch = show;
                return this;
            }

            public FilterPanelConfigurationBuilder showTypeFilter(boolean show) {
                config.showTypeFilter = show;
                return this;
            }

            public FilterPanelConfigurationBuilder showLevelFilter(boolean show) {
                config.showLevelFilter = show;
                return this;
            }

            public FilterPanelConfigurationBuilder showDateFilters(boolean show) {
                config.showDateFilters = show;
                return this;
            }

            public FilterPanelConfigurationBuilder showAutoScroll(boolean show) {
                config.showAutoScroll = show;
                return this;
            }

            public FilterPanelConfigurationBuilder showClearButton(boolean show) {
                config.showClearButton = show;
                return this;
            }

            public FilterPanelConfigurationBuilder showPresets(boolean show) {
                config.showPresets = show;
                return this;
            }

            public FilterPanelConfigurationBuilder title(String title) {
                config.title = title;
                return this;
            }

            public FilterPanelConfigurationBuilder customTypes(List<String> types) {
                config.customTypes = types;
                return this;
            }

            public FilterPanelConfigurationBuilder customLevels(List<String> levels) {
                config.customLevels = levels;
                return this;
            }

            public FilterPanelConfiguration build() {
                return config;
            }
        }
    }

    /** Filter state holder. */
    public static class FilterState {
        private String searchText = "";
        private String selectedType = "All Types";
        private String selectedLevel = "All Levels";
        private LocalDate startDate;
        private LocalDate endDate;
        private boolean autoScroll = true;

        // Getters and setters
        public String getSearchText() {
            return searchText;
        }

        public void setSearchText(String searchText) {
            this.searchText = searchText;
        }

        public String getSelectedType() {
            return selectedType;
        }

        public void setSelectedType(String selectedType) {
            this.selectedType = selectedType;
        }

        public String getSelectedLevel() {
            return selectedLevel;
        }

        public void setSelectedLevel(String selectedLevel) {
            this.selectedLevel = selectedLevel;
        }

        public LocalDate getStartDate() {
            return startDate;
        }

        public void setStartDate(LocalDate startDate) {
            this.startDate = startDate;
        }

        public LocalDate getEndDate() {
            return endDate;
        }

        public void setEndDate(LocalDate endDate) {
            this.endDate = endDate;
        }

        public boolean isAutoScroll() {
            return autoScroll;
        }

        public void setAutoScroll(boolean autoScroll) {
            this.autoScroll = autoScroll;
        }
    }

    /** Filter controls holder. */
    public static class FilterControls {
        private TextField searchField;
        private ComboBox<String> typeFilter;
        private ComboBox<String> levelFilter;
        private DatePicker startDatePicker;
        private DatePicker endDatePicker;
        private CheckBox autoScrollCheckBox;
        private Button clearButton;
        private ComboBox<String> presetCombo;

        // Getters
        public TextField getSearchField() {
            return searchField;
        }

        public ComboBox<String> getTypeFilter() {
            return typeFilter;
        }

        public ComboBox<String> getLevelFilter() {
            return levelFilter;
        }

        public DatePicker getStartDatePicker() {
            return startDatePicker;
        }

        public DatePicker getEndDatePicker() {
            return endDatePicker;
        }

        public CheckBox getAutoScrollCheckBox() {
            return autoScrollCheckBox;
        }

        public Button getClearButton() {
            return clearButton;
        }

        public ComboBox<String> getPresetCombo() {
            return presetCombo;
        }
    }

    /** Filter presets. */
    public static class FilterPreset {
        private final String name;
        private final String type;
        private final String level;
        private final String searchText;

        public FilterPreset(String name, String type, String level, String searchText) {
            this.name = name;
            this.type = type;
            this.level = level;
            this.searchText = searchText;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public String getLevel() {
            return level;
        }

        public String getSearchText() {
            return searchText;
        }
    }

    private final List<FilterPreset> defaultPresets = createDefaultPresets();

    /** Creates a complete filter panel. */
    public VBox createFilterPanel(
            FilterPanelConfiguration config, Consumer<FilterState> onFilterChange) {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(0, 0, 10, 0));

        // Title
        Label titleLabel = new Label(config.title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        // Filter bar
        HBox filterBar = createFilterBar(config, onFilterChange);

        panel.getChildren().addAll(titleLabel, filterBar);

        return panel;
    }

    /** Creates just the filter bar. */
    public HBox createFilterBar(
            FilterPanelConfiguration config, Consumer<FilterState> onFilterChange) {
        HBox filterBar = new HBox(10);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        FilterControls controls = new FilterControls();
        FilterState state = new FilterState();

        // Search field
        if (config.showSearch) {
            controls.searchField = createSearchField(state, onFilterChange);
            filterBar.getChildren().addAll(new Label("Search:"), controls.searchField);
            HBox.setHgrow(controls.searchField, Priority.ALWAYS);
        }

        // Type filter
        if (config.showTypeFilter) {
            controls.typeFilter = createTypeFilter(config, state, onFilterChange);
            filterBar.getChildren().addAll(new Label("Type:"), controls.typeFilter);
        }

        // Level filter
        if (config.showLevelFilter) {
            controls.levelFilter = createLevelFilter(config, state, onFilterChange);
            filterBar.getChildren().addAll(new Label("Level:"), controls.levelFilter);
        }

        // Date filters
        if (config.showDateFilters) {
            controls.startDatePicker =
                    createDatePicker(
                            "Start Date",
                            date -> {
                                state.setStartDate(date);
                                onFilterChange.accept(state);
                            });
            controls.endDatePicker =
                    createDatePicker(
                            "End Date",
                            date -> {
                                state.setEndDate(date);
                                onFilterChange.accept(state);
                            });

            filterBar
                    .getChildren()
                    .addAll(
                            new Label("From:"), controls.startDatePicker,
                            new Label("To:"), controls.endDatePicker);
        }

        // Preset selector
        if (config.showPresets) {
            controls.presetCombo = createPresetSelector(controls, state, onFilterChange);
            filterBar
                    .getChildren()
                    .addAll(
                            new Separator(javafx.geometry.Orientation.VERTICAL),
                            new Label("Preset:"),
                            controls.presetCombo);
        }

        // Auto-scroll
        if (config.showAutoScroll) {
            controls.autoScrollCheckBox = new CheckBox("Auto-scroll");
            controls.autoScrollCheckBox.setSelected(state.isAutoScroll());
            controls.autoScrollCheckBox.setOnAction(
                    e -> {
                        state.setAutoScroll(controls.autoScrollCheckBox.isSelected());
                        onFilterChange.accept(state);
                    });
            filterBar.getChildren().add(controls.autoScrollCheckBox);
        }

        // Clear button
        if (config.showClearButton) {
            controls.clearButton = new Button("Clear Filters");
            controls.clearButton.setOnAction(
                    e -> {
                        clearFilters(controls, state);
                        onFilterChange.accept(state);
                    });
            filterBar.getChildren().add(controls.clearButton);
        }

        return filterBar;
    }

    /** Creates search field. */
    private TextField createSearchField(FilterState state, Consumer<FilterState> onChange) {
        TextField searchField = new TextField();
        searchField.setPromptText("Search logs...");
        searchField.setPrefWidth(300);

        searchField
                .textProperty()
                .addListener(
                        (obs, oldVal, newVal) -> {
                            state.setSearchText(newVal);
                            onChange.accept(state);
                        });

        return searchField;
    }

    /** Creates type filter combo box. */
    private ComboBox<String> createTypeFilter(
            FilterPanelConfiguration config, FilterState state, Consumer<FilterState> onChange) {
        ComboBox<String> typeFilter = new ComboBox<>();
        typeFilter.getItems().add("All Types");

        // Add standard types
        for (LogEventType type : LogEventType.values()) {
            typeFilter.getItems().add(type.name());
        }

        // Add custom types
        typeFilter.getItems().addAll(config.customTypes);

        typeFilter.setValue("All Types");
        typeFilter.setOnAction(
                e -> {
                    state.setSelectedType(typeFilter.getValue());
                    onChange.accept(state);
                });

        return typeFilter;
    }

    /** Creates level filter combo box. */
    private ComboBox<String> createLevelFilter(
            FilterPanelConfiguration config, FilterState state, Consumer<FilterState> onChange) {
        ComboBox<String> levelFilter = new ComboBox<>();
        levelFilter.getItems().addAll("All Levels", "INFO", "WARNING", "ERROR", "DEBUG");

        // Add custom levels
        levelFilter.getItems().addAll(config.customLevels);

        levelFilter.setValue("All Levels");
        levelFilter.setOnAction(
                e -> {
                    state.setSelectedLevel(levelFilter.getValue());
                    onChange.accept(state);
                });

        return levelFilter;
    }

    /** Creates date picker. */
    private DatePicker createDatePicker(String prompt, Consumer<LocalDate> onChange) {
        DatePicker picker = new DatePicker();
        picker.setPromptText(prompt);
        picker.setPrefWidth(120);

        picker.valueProperty()
                .addListener(
                        (obs, oldVal, newVal) -> {
                            onChange.accept(newVal);
                        });

        return picker;
    }

    /** Creates preset selector. */
    private ComboBox<String> createPresetSelector(
            FilterControls controls, FilterState state, Consumer<FilterState> onChange) {
        ComboBox<String> presetCombo = new ComboBox<>();
        presetCombo.setPromptText("Select preset...");

        for (FilterPreset preset : defaultPresets) {
            presetCombo.getItems().add(preset.getName());
        }

        presetCombo.setOnAction(
                e -> {
                    String selected = presetCombo.getValue();
                    if (selected != null) {
                        applyPreset(selected, controls, state);
                        onChange.accept(state);
                    }
                });

        return presetCombo;
    }

    /** Clears all filters. */
    private void clearFilters(FilterControls controls, FilterState state) {
        if (controls.searchField != null) {
            controls.searchField.clear();
        }
        if (controls.typeFilter != null) {
            controls.typeFilter.setValue("All Types");
        }
        if (controls.levelFilter != null) {
            controls.levelFilter.setValue("All Levels");
        }
        if (controls.startDatePicker != null) {
            controls.startDatePicker.setValue(null);
        }
        if (controls.endDatePicker != null) {
            controls.endDatePicker.setValue(null);
        }

        state.setSearchText("");
        state.setSelectedType("All Types");
        state.setSelectedLevel("All Levels");
        state.setStartDate(null);
        state.setEndDate(null);
    }

    /** Applies a preset. */
    private void applyPreset(String presetName, FilterControls controls, FilterState state) {
        FilterPreset preset =
                defaultPresets.stream()
                        .filter(p -> p.getName().equals(presetName))
                        .findFirst()
                        .orElse(null);

        if (preset == null) return;

        if (controls.searchField != null && preset.getSearchText() != null) {
            controls.searchField.setText(preset.getSearchText());
        }
        if (controls.typeFilter != null && preset.getType() != null) {
            controls.typeFilter.setValue(preset.getType());
        }
        if (controls.levelFilter != null && preset.getLevel() != null) {
            controls.levelFilter.setValue(preset.getLevel());
        }

        state.setSearchText(preset.getSearchText() != null ? preset.getSearchText() : "");
        state.setSelectedType(preset.getType() != null ? preset.getType() : "All Types");
        state.setSelectedLevel(preset.getLevel() != null ? preset.getLevel() : "All Levels");

        log.debug("Applied filter preset: {}", presetName);
    }

    /** Creates default filter presets. */
    private List<FilterPreset> createDefaultPresets() {
        List<FilterPreset> presets = new ArrayList<>();

        presets.add(new FilterPreset("Errors Only", null, "ERROR", null));
        presets.add(new FilterPreset("Warnings & Errors", null, "WARNING", null));
        presets.add(new FilterPreset("State Transitions", "TRANSITION", null, null));
        presets.add(new FilterPreset("Actions Only", "ACTION", null, null));
        presets.add(new FilterPreset("System Events", "SYSTEM", null, null));
        presets.add(new FilterPreset("Exceptions", null, "ERROR", "exception"));

        return presets;
    }

    /** Creates a minimal filter bar with just search. */
    public HBox createSearchOnlyBar(Consumer<String> onSearchChange) {
        HBox bar = new HBox(10);
        bar.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Search...");
        searchField.setPrefWidth(300);
        searchField
                .textProperty()
                .addListener((obs, oldVal, newVal) -> onSearchChange.accept(newVal));

        bar.getChildren().addAll(new Label("Search:"), searchField);

        HBox.setHgrow(searchField, Priority.ALWAYS);

        return bar;
    }

    /** Creates action buttons bar. */
    public HBox createActionBar(
            boolean showExport, boolean showClear, Runnable onExport, Runnable onClear) {
        HBox actionBar = new HBox(10);
        actionBar.setPadding(new Insets(5, 0, 5, 0));
        actionBar.setAlignment(Pos.CENTER_LEFT);

        if (showExport) {
            Button exportButton = new Button("Export Logs");
            exportButton.setOnAction(
                    e -> {
                        if (onExport != null) onExport.run();
                    });
            actionBar.getChildren().add(exportButton);
        }

        if (showClear) {
            Button clearButton = new Button("Clear Logs");
            clearButton.setOnAction(
                    e -> {
                        if (onClear != null) onClear.run();
                    });
            actionBar.getChildren().add(clearButton);
        }

        return actionBar;
    }
}
