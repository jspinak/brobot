package io.github.jspinak.brobot.runner.ui.execution;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.Setter;
import lombok.AccessLevel;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Panel component for displaying state transition history in a table format.
 * 
 * <p>This panel shows a table of state transitions that occurred during
 * automation execution, including source state, target state, trigger,
 * and timestamp information.</p>
 */
@Getter
@Setter(AccessLevel.PRIVATE)
public class StateTransitionTablePanel extends TitledPane {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Getter
    private TableView<StateTransitionRecord> stateTransitionTable;
    
    @Getter
    private final ObservableList<StateTransitionRecord> stateTransitions = FXCollections.observableArrayList();

    /**
     * Creates a new StateTransitionTablePanel.
     */
    public StateTransitionTablePanel() {
        super("State Transitions", null);
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
        Label descriptionLabel = new Label("History of state transitions during execution:");

        stateTransitionTable = new TableView<>();
        stateTransitionTable.setItems(stateTransitions);
        stateTransitionTable.setPrefHeight(200);

        // Time column
        TableColumn<StateTransitionRecord, String> timeColumn = new TableColumn<>("Time");
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        timeColumn.setPrefWidth(80);

        // From State column
        TableColumn<StateTransitionRecord, String> fromColumn = new TableColumn<>("From State");
        fromColumn.setCellValueFactory(new PropertyValueFactory<>("fromState"));
        fromColumn.setPrefWidth(150);

        // To State column
        TableColumn<StateTransitionRecord, String> toColumn = new TableColumn<>("To State");
        toColumn.setCellValueFactory(new PropertyValueFactory<>("toState"));
        toColumn.setPrefWidth(150);

        // Trigger column
        TableColumn<StateTransitionRecord, String> triggerColumn = new TableColumn<>("Trigger");
        triggerColumn.setCellValueFactory(new PropertyValueFactory<>("trigger"));
        triggerColumn.setPrefWidth(100);

        // Duration column
        TableColumn<StateTransitionRecord, String> durationColumn = new TableColumn<>("Duration");
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));
        durationColumn.setPrefWidth(80);

        stateTransitionTable.getColumns().addAll(timeColumn, fromColumn, toColumn, triggerColumn, durationColumn);

        content.getChildren().addAll(descriptionLabel, stateTransitionTable);
    }

    /**
     * Adds a state transition record to the table.
     *
     * @param fromState The source state name
     * @param toState The target state name
     * @param trigger The trigger that caused the transition
     * @param duration The duration of the transition in milliseconds
     */
    public void addStateTransition(String fromState, String toState, String trigger, long duration) {
        StateTransitionRecord record = new StateTransitionRecord(
            LocalDateTime.now(),
            fromState,
            toState,
            trigger,
            duration
        );
        stateTransitions.add(0, record); // Add to beginning for most recent first
    }

    /**
     * Clears all state transition records.
     */
    public void clear() {
        stateTransitions.clear();
    }

    /**
     * Record class for state transitions.
     */
    @Getter
    public static class StateTransitionRecord {
        private final SimpleStringProperty time;
        private final SimpleStringProperty fromState;
        private final SimpleStringProperty toState;
        private final SimpleStringProperty trigger;
        private final SimpleStringProperty duration;

        public StateTransitionRecord(LocalDateTime timestamp, String fromState, String toState, 
                                   String trigger, long durationMs) {
            this.time = new SimpleStringProperty(timestamp.format(TIME_FORMATTER));
            this.fromState = new SimpleStringProperty(fromState);
            this.toState = new SimpleStringProperty(toState);
            this.trigger = new SimpleStringProperty(trigger);
            this.duration = new SimpleStringProperty(durationMs + " ms");
        }

        // Property getters for TableView
        public String getTime() { return time.get(); }
        public String getFromState() { return fromState.get(); }
        public String getToState() { return toState.get(); }
        public String getTrigger() { return trigger.get(); }
        public String getDuration() { return duration.get(); }
    }
}