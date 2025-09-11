package io.github.jspinak.brobot.runner.ui.log.services;

import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import org.springframework.stereotype.Service;

import io.github.jspinak.brobot.runner.ui.log.models.LogEntry;

import lombok.extern.slf4j.Slf4j;

/**
 * Factory service for creating and configuring log tables. Provides customizable table creation
 * with various column types and styles.
 */
@Slf4j
@Service
public class LogTableFactory {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter FULL_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /** Table configuration options. */
    public static class TableConfiguration {
        private boolean showTime = true;
        private boolean showLevel = true;
        private boolean showType = true;
        private boolean showSource = true;
        private boolean showMessage = true;
        private boolean enableRowStyling = true;
        private boolean enableTooltips = true;
        private boolean enableLevelIndicators = true;
        private SelectionMode selectionMode = SelectionMode.SINGLE;
        private Consumer<LogEntry> selectionHandler;
        private DateTimeFormatter timeFormatter = TIME_FORMATTER;

        public static TableConfigurationBuilder builder() {
            return new TableConfigurationBuilder();
        }

        public static class TableConfigurationBuilder {
            private TableConfiguration config = new TableConfiguration();

            public TableConfigurationBuilder showTime(boolean show) {
                config.showTime = show;
                return this;
            }

            public TableConfigurationBuilder showLevel(boolean show) {
                config.showLevel = show;
                return this;
            }

            public TableConfigurationBuilder showType(boolean show) {
                config.showType = show;
                return this;
            }

            public TableConfigurationBuilder showSource(boolean show) {
                config.showSource = show;
                return this;
            }

            public TableConfigurationBuilder showMessage(boolean show) {
                config.showMessage = show;
                return this;
            }

            public TableConfigurationBuilder enableRowStyling(boolean enable) {
                config.enableRowStyling = enable;
                return this;
            }

            public TableConfigurationBuilder enableTooltips(boolean enable) {
                config.enableTooltips = enable;
                return this;
            }

            public TableConfigurationBuilder enableLevelIndicators(boolean enable) {
                config.enableLevelIndicators = enable;
                return this;
            }

            public TableConfigurationBuilder selectionMode(SelectionMode mode) {
                config.selectionMode = mode;
                return this;
            }

            public TableConfigurationBuilder selectionHandler(Consumer<LogEntry> handler) {
                config.selectionHandler = handler;
                return this;
            }

            public TableConfigurationBuilder timeFormatter(DateTimeFormatter formatter) {
                config.timeFormatter = formatter;
                return this;
            }

            public TableConfiguration build() {
                return config;
            }
        }
    }

    /** Creates a log table with default configuration. */
    public TableView<LogEntry> createLogTable() {
        return createLogTable(TableConfiguration.builder().build());
    }

    /** Creates a log table with specified configuration. */
    public TableView<LogEntry> createLogTable(TableConfiguration config) {
        TableView<LogEntry> table = new TableView<>();
        table.setPlaceholder(new Label("No logs available"));
        table.getStyleClass().add("log-table");

        // Set selection mode
        table.getSelectionModel().setSelectionMode(config.selectionMode);

        // Create columns based on configuration
        if (config.showTime) {
            table.getColumns().add(createTimeColumn(config));
        }

        if (config.showLevel) {
            table.getColumns().add(createLevelColumn(config));
        }

        if (config.showType) {
            table.getColumns().add(createTypeColumn(config));
        }

        if (config.showSource) {
            table.getColumns().add(createSourceColumn(config));
        }

        if (config.showMessage) {
            table.getColumns().add(createMessageColumn(config));
        }

        // Setup row factory for styling
        if (config.enableRowStyling) {
            table.setRowFactory(tv -> createStyledRow());
        }

        // Setup selection handler
        if (config.selectionHandler != null) {
            table.getSelectionModel()
                    .selectedItemProperty()
                    .addListener(
                            (obs, oldVal, newVal) -> {
                                if (newVal != null) {
                                    config.selectionHandler.accept(newVal);
                                }
                            });
        }

        return table;
    }

    /** Creates the time column. */
    private TableColumn<LogEntry, String> createTimeColumn(TableConfiguration config) {
        TableColumn<LogEntry, String> column = new TableColumn<>("Time");
        column.setCellValueFactory(
                param ->
                        new SimpleStringProperty(
                                param.getValue().getTimestamp().format(config.timeFormatter)));
        column.setPrefWidth(100);
        column.setMinWidth(80);

        column.setCellFactory(
                col ->
                        new TableCell<>() {
                            @Override
                            protected void updateItem(String time, boolean empty) {
                                super.updateItem(time, empty);
                                if (empty || time == null) {
                                    setText(null);
                                } else {
                                    setText(time);
                                    getStyleClass().add("time-cell");

                                    if (config.enableTooltips) {
                                        LogEntry entry = getTableRow().getItem();
                                        if (entry != null) {
                                            setTooltip(
                                                    new Tooltip(
                                                            entry.getTimestamp()
                                                                    .format(FULL_TIME_FORMATTER)));
                                        }
                                    }
                                }
                            }
                        });

        return column;
    }

    /** Creates the level column with optional indicators. */
    private TableColumn<LogEntry, LogEntry.LogLevel> createLevelColumn(TableConfiguration config) {
        TableColumn<LogEntry, LogEntry.LogLevel> column = new TableColumn<>("Level");
        column.setCellValueFactory(
                param ->
                        new javafx.beans.property.SimpleObjectProperty<>(
                                param.getValue().getLevel()));
        column.setPrefWidth(80);
        column.setMinWidth(60);

        column.setCellFactory(
                col ->
                        new TableCell<>() {
                            @Override
                            protected void updateItem(LogEntry.LogLevel level, boolean empty) {
                                super.updateItem(level, empty);
                                if (empty || level == null) {
                                    setText(null);
                                    setGraphic(null);
                                } else {
                                    if (config.enableLevelIndicators) {
                                        HBox content = new HBox(8);
                                        content.setAlignment(Pos.CENTER_LEFT);

                                        // Colored indicator
                                        Circle indicator = new Circle(4);
                                        indicator.setFill(getLevelColor(level));

                                        Label label = new Label(level.name());
                                        label.getStyleClass()
                                                .add("level-" + level.name().toLowerCase());

                                        content.getChildren().addAll(indicator, label);
                                        setGraphic(content);
                                        setText(null);
                                    } else {
                                        setText(level.name());
                                        setGraphic(null);
                                        getStyleClass().add("level-" + level.name().toLowerCase());
                                    }
                                }
                            }
                        });

        return column;
    }

    /** Creates the type column. */
    private TableColumn<LogEntry, String> createTypeColumn(TableConfiguration config) {
        TableColumn<LogEntry, String> column = new TableColumn<>("Type");
        column.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getType()));
        column.setPrefWidth(120);
        column.setMinWidth(80);

        column.setCellFactory(
                col ->
                        new TableCell<>() {
                            @Override
                            protected void updateItem(String type, boolean empty) {
                                super.updateItem(type, empty);
                                if (empty || type == null) {
                                    setText(null);
                                } else {
                                    setText(type);
                                    getStyleClass().add("type-cell");
                                    getStyleClass()
                                            .add("type-" + type.toLowerCase().replace("_", "-"));
                                }
                            }
                        });

        return column;
    }

    /** Creates the source column. */
    private TableColumn<LogEntry, String> createSourceColumn(TableConfiguration config) {
        TableColumn<LogEntry, String> column = new TableColumn<>("Source");
        column.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getSource()));
        column.setPrefWidth(200);
        column.setMinWidth(100);

        column.setCellFactory(
                col ->
                        new TableCell<>() {
                            @Override
                            protected void updateItem(String source, boolean empty) {
                                super.updateItem(source, empty);
                                if (empty || source == null) {
                                    setText(null);
                                    setTooltip(null);
                                } else {
                                    setText(source);
                                    getStyleClass().add("source-cell");

                                    if (config.enableTooltips && source.length() > 30) {
                                        setTooltip(new Tooltip(source));
                                    }
                                }
                            }
                        });

        return column;
    }

    /** Creates the message column. */
    private TableColumn<LogEntry, String> createMessageColumn(TableConfiguration config) {
        TableColumn<LogEntry, String> column = new TableColumn<>("Message");
        column.setCellValueFactory(
                param -> new SimpleStringProperty(param.getValue().getMessage()));
        column.setPrefWidth(500);
        column.setMinWidth(200);

        column.setCellFactory(
                col ->
                        new TableCell<>() {
                            @Override
                            protected void updateItem(String message, boolean empty) {
                                super.updateItem(message, empty);
                                if (empty || message == null) {
                                    setText(null);
                                    setTooltip(null);
                                } else {
                                    // Truncate long messages
                                    String display =
                                            message.length() > 200
                                                    ? message.substring(0, 197) + "..."
                                                    : message;
                                    setText(display);
                                    getStyleClass().add("message-cell");

                                    if (config.enableTooltips) {
                                        setTooltip(new Tooltip(message));
                                    }
                                }
                            }
                        });

        return column;
    }

    /** Creates a custom column with specified configuration. */
    public <T> TableColumn<LogEntry, T> createCustomColumn(
            String title,
            java.util.function.Function<LogEntry, javafx.beans.value.ObservableValue<T>>
                    valueFactory,
            int prefWidth) {

        TableColumn<LogEntry, T> column = new TableColumn<>(title);
        column.setCellValueFactory(param -> valueFactory.apply(param.getValue()));
        column.setPrefWidth(prefWidth);

        return column;
    }

    /** Creates a styled table row. */
    private TableRow<LogEntry> createStyledRow() {
        return new TableRow<>() {
            @Override
            protected void updateItem(LogEntry item, boolean empty) {
                super.updateItem(item, empty);

                // Remove all style classes
                getStyleClass()
                        .removeAll(
                                "log-row-error",
                                "log-row-warning",
                                "log-row-info",
                                "log-row-debug",
                                "log-row-trace",
                                "log-row-fatal",
                                "log-row-with-exception");

                if (!empty && item != null) {
                    // Add level-based styling
                    String levelClass = "log-row-" + item.getLevel().name().toLowerCase();
                    getStyleClass().add(levelClass);

                    // Add exception indicator
                    if (item.hasException()) {
                        getStyleClass().add("log-row-with-exception");
                    }
                }
            }
        };
    }

    /** Gets the color for a log level. */
    private Color getLevelColor(LogEntry.LogLevel level) {
        switch (level) {
            case FATAL:
            case ERROR:
                return Color.web("#dc3545"); // Red
            case WARNING:
                return Color.web("#ffc107"); // Yellow
            case INFO:
                return Color.web("#17a2b8"); // Blue
            case DEBUG:
                return Color.web("#6c757d"); // Gray
            case TRACE:
                return Color.web("#868e96"); // Light gray
            default:
                return Color.GRAY;
        }
    }

    /** Configures table columns for resizing. */
    public void configureColumnResizing(TableView<LogEntry> table) {
        // Set column resize policy
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        // Calculate proportions
        double totalWidth =
                table.getColumns().stream().mapToDouble(TableColumn::getPrefWidth).sum();

        // Set percentage widths
        for (TableColumn<LogEntry, ?> column : table.getColumns()) {
            double percentage = column.getPrefWidth() / totalWidth;
            column.prefWidthProperty().bind(table.widthProperty().multiply(percentage));
        }
    }

    /** Applies a theme to the table. */
    public void applyTheme(TableView<LogEntry> table, TableTheme theme) {
        table.getStyleClass()
                .removeAll("log-table-light", "log-table-dark", "log-table-high-contrast");

        switch (theme) {
            case LIGHT:
                table.getStyleClass().add("log-table-light");
                break;
            case DARK:
                table.getStyleClass().add("log-table-dark");
                break;
            case HIGH_CONTRAST:
                table.getStyleClass().add("log-table-high-contrast");
                break;
        }
    }

    /** Available table themes. */
    public enum TableTheme {
        LIGHT,
        DARK,
        HIGH_CONTRAST
    }

    /** Creates a context menu for the table. */
    public ContextMenu createTableContextMenu(TableView<LogEntry> table) {
        ContextMenu menu = new ContextMenu();

        MenuItem copyMessage = new MenuItem("Copy Message");
        copyMessage.setOnAction(
                e -> {
                    LogEntry selected = table.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        Clipboard clipboard = Clipboard.getSystemClipboard();
                        ClipboardContent content = new ClipboardContent();
                        content.putString(selected.getMessage());
                        clipboard.setContent(content);
                    }
                });

        MenuItem copyRow = new MenuItem("Copy Row");
        copyRow.setOnAction(
                e -> {
                    LogEntry selected = table.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        Clipboard clipboard = Clipboard.getSystemClipboard();
                        ClipboardContent content = new ClipboardContent();
                        content.putString(formatLogEntry(selected));
                        clipboard.setContent(content);
                    }
                });

        MenuItem viewDetails = new MenuItem("View Details");
        viewDetails.setOnAction(
                e -> {
                    LogEntry selected = table.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        showDetailsDialog(selected);
                    }
                });

        menu.getItems().addAll(copyMessage, copyRow, new SeparatorMenuItem(), viewDetails);

        return menu;
    }

    /** Formats a log entry for copying. */
    private String formatLogEntry(LogEntry entry) {
        return String.format(
                "[%s] [%s] [%s] %s",
                entry.getTimestamp().format(FULL_TIME_FORMATTER),
                entry.getLevel(),
                entry.getType(),
                entry.getMessage());
    }

    /** Shows a details dialog for a log entry. */
    private void showDetailsDialog(LogEntry entry) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Log Entry Details");
        alert.setHeaderText(null);

        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setText(formatDetailedLogEntry(entry));
        textArea.setPrefRowCount(20);
        textArea.setPrefColumnCount(80);

        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    /** Formats a detailed log entry. */
    private String formatDetailedLogEntry(LogEntry entry) {
        StringBuilder sb = new StringBuilder();
        sb.append("ID: ").append(entry.getId()).append("\n");
        sb.append("Time: ").append(entry.getTimestamp().format(FULL_TIME_FORMATTER)).append("\n");
        sb.append("Level: ").append(entry.getLevel()).append("\n");
        sb.append("Type: ").append(entry.getType()).append("\n");
        sb.append("Source: ").append(entry.getSource()).append("\n");
        sb.append("\nMessage:\n").append(entry.getMessage()).append("\n");

        if (entry.getDetails() != null) {
            sb.append("\nDetails:\n").append(entry.getDetails()).append("\n");
        }

        if (entry.hasException()) {
            sb.append("\nException:\n").append(entry.getExceptionStackTrace()).append("\n");
        }

        if (entry.hasMetadata()) {
            sb.append("\nMetadata:\n");
            entry.getMetadata()
                    .forEach(
                            (key, value) ->
                                    sb.append("  ")
                                            .append(key)
                                            .append(": ")
                                            .append(value)
                                            .append("\n"));
        }

        return sb.toString();
    }
}
