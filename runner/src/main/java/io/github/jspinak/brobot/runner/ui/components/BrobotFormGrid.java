package io.github.jspinak.brobot.runner.ui.components;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/**
 * Custom GridPane for forms that ensures proper spacing and alignment. This prevents overlap
 * detection issues and ensures minimum spacing.
 */
public class BrobotFormGrid extends GridPane {

    private static final double DEFAULT_HGAP = 16.0;
    private static final double DEFAULT_VGAP = 12.0;
    private static final double DEFAULT_PADDING = 16.0;
    private static final double LABEL_MIN_WIDTH = 140.0;

    private int currentRow = 0;

    public BrobotFormGrid() {
        super();
        initialize();
    }

    private void initialize() {
        // Set default gaps
        setHgap(DEFAULT_HGAP);
        setVgap(DEFAULT_VGAP);
        setPadding(new Insets(DEFAULT_PADDING));

        // Add style class
        getStyleClass().add("brobot-form-grid");

        // Set constraints for proper alignment
        setAlignment(javafx.geometry.Pos.TOP_LEFT);
    }

    /**
     * Adds a form field with a label and control.
     *
     * @param labelText The label text
     * @param control The form control (TextField, ComboBox, etc.)
     */
    public void addField(String labelText, Node control) {
        Label label = createFormLabel(labelText);
        addField(label, control);
    }

    /**
     * Adds a form field with a pre-created label and control.
     *
     * @param label The label
     * @param control The form control
     */
    public void addField(Label label, Node control) {
        // Configure label
        label.setMinWidth(LABEL_MIN_WIDTH);
        label.setPrefWidth(LABEL_MIN_WIDTH);
        label.getStyleClass().add("form-label");

        // Add to grid with proper constraints
        add(label, 0, currentRow);
        add(control, 1, currentRow);

        // Set constraints
        GridPane.setHalignment(label, HPos.LEFT);
        GridPane.setValignment(label, VPos.CENTER);
        GridPane.setHgrow(control, Priority.ALWAYS);
        GridPane.setFillWidth(control, true);

        // Ensure minimum spacing by setting margins
        GridPane.setMargin(label, new Insets(0, 8, 0, 0));
        GridPane.setMargin(control, new Insets(0, 0, 0, 0));

        currentRow++;
    }

    /**
     * Adds a full-width component that spans both columns.
     *
     * @param node The node to add
     */
    public void addFullWidth(Node node) {
        add(node, 0, currentRow, 2, 1);
        GridPane.setHgrow(node, Priority.ALWAYS);
        GridPane.setFillWidth(node, true);
        GridPane.setMargin(node, new Insets(8, 0, 8, 0));
        currentRow++;
    }

    /** Adds a separator row. */
    public void addSeparator() {
        javafx.scene.control.Separator separator = new javafx.scene.control.Separator();
        addFullWidth(separator);
    }

    /**
     * Adds a section header.
     *
     * @param text The header text
     */
    public void addSectionHeader(String text) {
        Label header = new Label(text);
        header.getStyleClass().add("section-header");
        header.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        addFullWidth(header);
    }

    /**
     * Creates a properly styled form label.
     *
     * @param text The label text
     * @return The created label
     */
    private Label createFormLabel(String text) {
        Label label = new Label(text);
        if (!text.endsWith(":")) {
            label.setText(text + ":");
        }
        return label;
    }

    /** Skips a row in the grid. */
    public void skipRow() {
        currentRow++;
    }

    /**
     * Gets the current row index.
     *
     * @return The current row
     */
    public int getCurrentRow() {
        return currentRow;
    }

    /** Resets the row counter. */
    public void resetRows() {
        currentRow = 0;
    }

    /**
     * Sets custom gaps for this form.
     *
     * @param hgap Horizontal gap
     * @param vgap Vertical gap
     */
    public void setGaps(double hgap, double vgap) {
        setHgap(Math.max(8, hgap)); // Ensure minimum 8px
        setVgap(Math.max(8, vgap)); // Ensure minimum 8px
    }

    /**
     * Sets custom padding for this form.
     *
     * @param padding The padding value for all sides
     */
    public void setPadding(double padding) {
        setPadding(new Insets(Math.max(8, padding)));
    }

    /**
     * Sets custom padding for this form.
     *
     * @param top Top padding
     * @param right Right padding
     * @param bottom Bottom padding
     * @param left Left padding
     */
    public void setPadding(double top, double right, double bottom, double left) {
        setPadding(
                new Insets(
                        Math.max(8, top),
                        Math.max(8, right),
                        Math.max(8, bottom),
                        Math.max(8, left)));
    }
}
