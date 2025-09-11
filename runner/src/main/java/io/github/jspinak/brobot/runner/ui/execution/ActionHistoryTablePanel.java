package io.github.jspinak.brobot.runner.ui.execution;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Panel component for displaying action history in a table format.
 *
 * <p>This panel shows a table of actions that were executed during automation, including action
 * type, target, result, and timing information.
 */
@Getter
@Setter(AccessLevel.PRIVATE)
public class ActionHistoryTablePanel extends TitledPane {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final int MAX_HISTORY_SIZE = 1000;

    @Getter private TableView<ActionRecord> actionHistoryTable;

    @Getter
    private final ObservableList<ActionRecord> actionHistory = FXCollections.observableArrayList();

    /** Creates a new ActionHistoryTablePanel. */
    public ActionHistoryTablePanel() {
        super("Action History", null);
        setCollapsible(true);
        setExpanded(false);
        initializeContent();
    }

    private void initializeContent() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        setupUI(content);
        super.setContent(content);
    }

    private void setupUI(VBox content) {
        Label descriptionLabel = new Label("History of actions performed during execution:");

        actionHistoryTable = new TableView<>();
        actionHistoryTable.setItems(actionHistory);
        actionHistoryTable.setPrefHeight(200);

        // Time column
        TableColumn<ActionRecord, String> timeColumn = new TableColumn<>("Time");
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        timeColumn.setPrefWidth(80);

        // Action column
        TableColumn<ActionRecord, String> actionColumn = new TableColumn<>("Action");
        actionColumn.setCellValueFactory(new PropertyValueFactory<>("action"));
        actionColumn.setPrefWidth(100);

        // Target column
        TableColumn<ActionRecord, String> targetColumn = new TableColumn<>("Target");
        targetColumn.setCellValueFactory(new PropertyValueFactory<>("target"));
        targetColumn.setPrefWidth(150);

        // Result column
        TableColumn<ActionRecord, String> resultColumn = new TableColumn<>("Result");
        resultColumn.setCellValueFactory(new PropertyValueFactory<>("result"));
        resultColumn.setPrefWidth(80);

        // Duration column
        TableColumn<ActionRecord, String> durationColumn = new TableColumn<>("Duration");
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));
        durationColumn.setPrefWidth(80);

        // Details column
        TableColumn<ActionRecord, String> detailsColumn = new TableColumn<>("Details");
        detailsColumn.setCellValueFactory(new PropertyValueFactory<>("details"));
        detailsColumn.setPrefWidth(200);

        actionHistoryTable
                .getColumns()
                .addAll(
                        timeColumn,
                        actionColumn,
                        targetColumn,
                        resultColumn,
                        durationColumn,
                        detailsColumn);

        content.getChildren().addAll(descriptionLabel, actionHistoryTable);
    }

    /**
     * Adds an action record to the history table.
     *
     * @param action The action type
     * @param target The target of the action
     * @param result The result (SUCCESS/FAILURE)
     * @param duration The duration in milliseconds
     * @param details Additional details about the action
     */
    public void addActionRecord(
            String action, String target, String result, long duration, String details) {
        ActionRecord record =
                new ActionRecord(LocalDateTime.now(), action, target, result, duration, details);

        actionHistory.add(0, record); // Add to beginning for most recent first

        // Limit history size
        if (actionHistory.size() > MAX_HISTORY_SIZE) {
            actionHistory.remove(actionHistory.size() - 1);
        }
    }

    /** Clears all action history records. */
    public void clear() {
        actionHistory.clear();
    }

    /**
     * Gets the total number of actions in history.
     *
     * @return The number of actions
     */
    public int getActionCount() {
        return actionHistory.size();
    }

    /** Record class for action history. */
    @Getter
    public static class ActionRecord {
        private final SimpleStringProperty time;
        private final SimpleStringProperty action;
        private final SimpleStringProperty target;
        private final SimpleStringProperty result;
        private final SimpleStringProperty duration;
        private final SimpleStringProperty details;

        public ActionRecord(
                LocalDateTime timestamp,
                String action,
                String target,
                String result,
                long durationMs,
                String details) {
            this.time = new SimpleStringProperty(timestamp.format(TIME_FORMATTER));
            this.action = new SimpleStringProperty(action);
            this.target = new SimpleStringProperty(target != null ? target : "");
            this.result = new SimpleStringProperty(result);
            this.duration = new SimpleStringProperty(durationMs + " ms");
            this.details = new SimpleStringProperty(details != null ? details : "");
        }

        // Property getters for TableView
        public String getTime() {
            return time.get();
        }

        public String getAction() {
            return action.get();
        }

        public String getTarget() {
            return target.get();
        }

        public String getResult() {
            return result.get();
        }

        public String getDuration() {
            return duration.get();
        }

        public String getDetails() {
            return details.get();
        }
    }
}
