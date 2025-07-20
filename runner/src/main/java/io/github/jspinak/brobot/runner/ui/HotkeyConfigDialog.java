package io.github.jspinak.brobot.runner.ui;

import io.github.jspinak.brobot.runner.hotkeys.HotkeyManager;
import io.github.jspinak.brobot.runner.hotkeys.HotkeyManager.HotkeyAction;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dialog for configuring automation hotkeys.
 * Allows users to customize keyboard shortcuts for automation controls.
 */
public class HotkeyConfigDialog extends Stage {
    private final HotkeyManager hotkeyManager;
    private final Map<HotkeyAction, KeyCombination> tempHotkeys = new HashMap<>();
    private boolean saved = false;
    
    public HotkeyConfigDialog(HotkeyManager hotkeyManager) {
        this.hotkeyManager = hotkeyManager;
        
        setTitle("Configure Hotkeys");
        initModality(Modality.APPLICATION_MODAL);
        setResizable(false);
        
        // Copy current hotkeys
        tempHotkeys.putAll(hotkeyManager.getAllHotkeys());
        
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        
        // Title
        Label titleLabel = new Label("Configure Automation Hotkeys");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Instructions
        Label instructionLabel = new Label("Click on a hotkey field and press the desired key combination");
        instructionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");
        
        // Hotkey configuration grid
        GridPane grid = createHotkeyGrid();
        
        // Buttons
        HBox buttonBar = createButtonBar();
        
        root.getChildren().addAll(titleLabel, instructionLabel, grid, buttonBar);
        
        Scene scene = new Scene(root, 500, 400);
        setScene(scene);
    }
    
    private GridPane createHotkeyGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        
        int row = 0;
        
        // Headers
        Label actionHeader = new Label("Action");
        actionHeader.setStyle("-fx-font-weight: bold;");
        Label hotkeyHeader = new Label("Hotkey");
        hotkeyHeader.setStyle("-fx-font-weight: bold;");
        
        grid.add(actionHeader, 0, row);
        grid.add(hotkeyHeader, 1, row);
        grid.add(new Label(""), 2, row); // For clear button column
        
        row++;
        
        // Add separator
        grid.add(new Separator(), 0, row, 3, 1);
        row++;
        
        // Add hotkey rows
        for (HotkeyAction action : HotkeyAction.values()) {
            Label actionLabel = new Label(action.getDisplayName());
            
            TextField hotkeyField = new TextField();
            hotkeyField.setEditable(false);
            hotkeyField.setPrefWidth(200);
            hotkeyField.setText(getKeyCombinationText(tempHotkeys.get(action)));
            
            // Handle key press to set new hotkey
            hotkeyField.setOnKeyPressed(event -> {
                KeyCombination newCombo = createKeyCombination(event);
                if (newCombo != null && !isConflicting(newCombo, action)) {
                    tempHotkeys.put(action, newCombo);
                    hotkeyField.setText(getKeyCombinationText(newCombo));
                }
                event.consume();
            });
            
            // Focus visual feedback
            hotkeyField.setOnMouseClicked(e -> {
                hotkeyField.setStyle("-fx-background-color: #e8f4ff; -fx-border-color: #0066cc;");
                hotkeyField.setPromptText("Press key combination...");
            });
            
            hotkeyField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal) {
                    hotkeyField.setStyle("");
                    hotkeyField.setPromptText("");
                }
            });
            
            Button clearButton = new Button("Clear");
            clearButton.setOnAction(e -> {
                tempHotkeys.put(action, null);
                hotkeyField.setText("");
            });
            
            grid.add(actionLabel, 0, row);
            grid.add(hotkeyField, 1, row);
            grid.add(clearButton, 2, row);
            
            row++;
        }
        
        return grid;
    }
    
    private HBox createButtonBar() {
        HBox buttonBar = new HBox(10);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        buttonBar.setPadding(new Insets(10, 0, 0, 0));
        
        Button resetButton = new Button("Reset to Defaults");
        resetButton.setOnAction(e -> {
            hotkeyManager.resetToDefaults();
            tempHotkeys.putAll(hotkeyManager.getAllHotkeys());
            // Refresh the dialog
            close();
            new HotkeyConfigDialog(hotkeyManager).showAndWait();
        });
        
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> close());
        
        Button saveButton = new Button("Save");
        saveButton.setDefaultButton(true);
        saveButton.setOnAction(e -> {
            // Apply all changes
            for (Map.Entry<HotkeyAction, KeyCombination> entry : tempHotkeys.entrySet()) {
                if (entry.getValue() != null) {
                    hotkeyManager.updateHotkey(entry.getKey(), entry.getValue());
                }
            }
            saved = true;
            close();
        });
        
        buttonBar.getChildren().addAll(resetButton, cancelButton, saveButton);
        
        return buttonBar;
    }
    
    private KeyCombination createKeyCombination(KeyEvent event) {
        KeyCode code = event.getCode();
        
        // Ignore modifier keys alone
        if (code == KeyCode.SHIFT || code == KeyCode.CONTROL || 
            code == KeyCode.ALT || code == KeyCode.META || code == KeyCode.WINDOWS) {
            return null;
        }
        
        // Build key combination
        List<KeyCombination.Modifier> modifiers = new ArrayList<>();
        
        if (event.isControlDown()) {
            modifiers.add(KeyCombination.CONTROL_DOWN);
        }
        if (event.isAltDown()) {
            modifiers.add(KeyCombination.ALT_DOWN);
        }
        if (event.isShiftDown()) {
            modifiers.add(KeyCombination.SHIFT_DOWN);
        }
        if (event.isMetaDown()) {
            modifiers.add(KeyCombination.META_DOWN);
        }
        
        // Require at least one modifier for safety
        if (modifiers.isEmpty() && !isAllowedSingleKey(code)) {
            showAlert("Invalid Hotkey", "Please use at least one modifier key (Ctrl, Alt, Shift)");
            return null;
        }
        
        return new KeyCodeCombination(code, modifiers.toArray(new KeyCombination.Modifier[0]));
    }
    
    private boolean isAllowedSingleKey(KeyCode code) {
        // Function keys can be used without modifiers
        return code.name().startsWith("F") && code.isKeypadKey();
    }
    
    private boolean isConflicting(KeyCombination newCombo, HotkeyAction currentAction) {
        for (Map.Entry<HotkeyAction, KeyCombination> entry : tempHotkeys.entrySet()) {
            if (entry.getKey() != currentAction && 
                entry.getValue() != null && 
                entry.getValue().equals(newCombo)) {
                showAlert("Hotkey Conflict", 
                    "This key combination is already assigned to: " + entry.getKey().getDisplayName());
                return true;
            }
        }
        return false;
    }
    
    private String getKeyCombinationText(KeyCombination combo) {
        return combo != null ? combo.getDisplayText() : "";
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public boolean isSaved() {
        return saved;
    }
}