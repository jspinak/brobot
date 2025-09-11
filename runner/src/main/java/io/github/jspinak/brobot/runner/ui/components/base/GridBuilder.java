package io.github.jspinak.brobot.runner.ui.components.base;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;

import atlantafx.base.theme.Styles;

/**
 * Builder class for creating properly constrained GridPanes that prevent label overlapping and
 * ensure consistent layout.
 */
public class GridBuilder {

    private final GridPane gridPane;
    private int currentRow = 0;

    public GridBuilder() {
        this.gridPane = new GridPane();
        this.gridPane.getStyleClass().add("brobot-grid");
        setDefaultSpacing();
    }

    /** Set default spacing for the grid. */
    private void setDefaultSpacing() {
        gridPane.setHgap(16);
        gridPane.setVgap(8);
        gridPane.setPadding(new Insets(0));
    }

    /**
     * Add column constraints to prevent label overlapping. This is crucial for fixing the label
     * overlap issues.
     */
    public GridBuilder withLabelValueColumns() {
        ColumnConstraints labelColumn = new ColumnConstraints();
        labelColumn.setMinWidth(150);
        labelColumn.setPrefWidth(200);
        labelColumn.setMaxWidth(250);
        labelColumn.setHalignment(HPos.LEFT);

        ColumnConstraints valueColumn = new ColumnConstraints();
        valueColumn.setMinWidth(80);
        valueColumn.setPrefWidth(150);
        valueColumn.setHgrow(Priority.ALWAYS);
        valueColumn.setHalignment(HPos.LEFT);

        gridPane.getColumnConstraints().addAll(labelColumn, valueColumn);
        return this;
    }

    /** Add custom column constraints. */
    public GridBuilder withColumns(ColumnConstraints... constraints) {
        gridPane.getColumnConstraints().addAll(constraints);
        return this;
    }

    /** Add a label-value pair row. */
    public GridBuilder addRow(String labelText, Node valueNode) {
        Label label = new Label(labelText);
        label.getStyleClass().add(Styles.TEXT_MUTED);
        label.setMinWidth(Label.USE_PREF_SIZE);

        GridPane.setConstraints(label, 0, currentRow);
        GridPane.setConstraints(valueNode, 1, currentRow);
        GridPane.setHalignment(label, HPos.LEFT);
        GridPane.setValignment(label, VPos.CENTER);

        gridPane.getChildren().addAll(label, valueNode);
        currentRow++;

        return this;
    }

    /** Add a label-value pair row with custom styling. */
    public GridBuilder addRow(String labelText, Node valueNode, String... labelStyles) {
        Label label = new Label(labelText);
        label.getStyleClass().add(Styles.TEXT_MUTED);
        label.getStyleClass().addAll(labelStyles);
        label.setMinWidth(Label.USE_PREF_SIZE);

        GridPane.setConstraints(label, 0, currentRow);
        GridPane.setConstraints(valueNode, 1, currentRow);
        GridPane.setHalignment(label, HPos.LEFT);
        GridPane.setValignment(label, VPos.CENTER);

        gridPane.getChildren().addAll(label, valueNode);
        currentRow++;

        return this;
    }

    /** Add a full-width row spanning all columns. */
    public GridBuilder addFullWidthRow(Node node) {
        GridPane.setConstraints(node, 0, currentRow, 2, 1);
        gridPane.getChildren().add(node);
        currentRow++;
        return this;
    }

    /** Add custom spacing between rows. */
    public GridBuilder addSpacing(double height) {
        RowConstraints spacer = new RowConstraints();
        spacer.setPrefHeight(height);
        spacer.setMinHeight(height);
        spacer.setMaxHeight(height);
        gridPane.getRowConstraints().add(spacer);
        currentRow++;
        return this;
    }

    /** Set custom gap between cells. */
    public GridBuilder withGap(double hgap, double vgap) {
        gridPane.setHgap(hgap);
        gridPane.setVgap(vgap);
        return this;
    }

    /** Set padding for the grid. */
    public GridBuilder withPadding(Insets padding) {
        gridPane.setPadding(padding);
        return this;
    }

    /** Apply custom style classes to the grid. */
    public GridBuilder withStyle(String... styleClasses) {
        gridPane.getStyleClass().addAll(styleClasses);
        return this;
    }

    /** Build and return the configured GridPane. */
    public GridPane build() {
        return gridPane;
    }

    /** Create a standard label-value grid with proper constraints. */
    public static GridPane createLabelValueGrid() {
        return new GridBuilder().withLabelValueColumns().build();
    }
}
