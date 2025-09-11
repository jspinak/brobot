package io.github.jspinak.brobot.runner.ui.components;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.runner.ui.theme.AtlantaFXThemeManager;
import io.github.jspinak.brobot.runner.ui.theme.AtlantaFXThemeManager.AtlantaTheme;

/** A component that provides UI controls for selecting and switching themes. */
@Component
public class ThemeSelector extends VBox {

    private final AtlantaFXThemeManager themeManager;
    private final ComboBox<AtlantaTheme> themeComboBox;
    private final ToggleButton darkModeToggle;

    @Autowired
    public ThemeSelector(AtlantaFXThemeManager themeManager) {
        this.themeManager = themeManager;

        setSpacing(10);
        setPadding(new Insets(10));

        // Theme selection label
        Label themeLabel = new Label("Theme:");
        themeLabel.getStyleClass().add("text-bold");

        // Theme combo box
        themeComboBox = new ComboBox<>();
        themeComboBox.getItems().addAll(themeManager.getAvailableThemes());
        themeComboBox.setValue(themeManager.getCurrentTheme());
        themeComboBox.setConverter(
                new StringConverter<AtlantaTheme>() {
                    @Override
                    public String toString(AtlantaTheme theme) {
                        return theme != null ? theme.getDisplayName() : "";
                    }

                    @Override
                    public AtlantaTheme fromString(String string) {
                        return themeManager.getAvailableThemes().stream()
                                .filter(theme -> theme.getDisplayName().equals(string))
                                .findFirst()
                                .orElse(null);
                    }
                });

        // Dark mode toggle
        darkModeToggle = new ToggleButton("🌙 Dark Mode");
        darkModeToggle.setSelected(themeManager.isDarkTheme());
        darkModeToggle.getStyleClass().add("toggle-button");

        // Layout
        HBox themeBox = new HBox(10);
        themeBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        themeBox.getChildren().addAll(themeLabel, themeComboBox);

        getChildren().addAll(themeBox, darkModeToggle);

        // Event handlers
        setupEventHandlers();

        // Listen for theme changes
        themeManager.addThemeChangeListener(this::onThemeChanged);
    }

    private void setupEventHandlers() {
        // Theme selection
        themeComboBox.setOnAction(
                e -> {
                    AtlantaTheme selectedTheme = themeComboBox.getValue();
                    if (selectedTheme != null) {
                        themeManager.setTheme(selectedTheme);
                    }
                });

        // Dark mode toggle
        darkModeToggle.setOnAction(
                e -> {
                    themeManager.toggleLightDark();
                });
    }

    private void onThemeChanged(AtlantaTheme oldTheme, AtlantaTheme newTheme) {
        // Update UI to reflect theme change
        themeComboBox.setValue(newTheme);
        darkModeToggle.setSelected(themeManager.isDarkTheme());

        // Update toggle button text
        darkModeToggle.setText(themeManager.isDarkTheme() ? "☀️ Light Mode" : "🌙 Dark Mode");
    }

    /** Creates a compact theme selector suitable for toolbars. */
    public static ToolBar createToolbarThemeSelector(AtlantaFXThemeManager themeManager) {
        ToolBar toolBar = new ToolBar();

        // Quick theme buttons
        Button primerBtn = new Button("Primer");
        primerBtn.setOnAction(
                e ->
                        themeManager.setTheme(
                                themeManager.isDarkTheme()
                                        ? AtlantaTheme.PRIMER_DARK
                                        : AtlantaTheme.PRIMER_LIGHT));

        Button nordBtn = new Button("Nord");
        nordBtn.setOnAction(
                e ->
                        themeManager.setTheme(
                                themeManager.isDarkTheme()
                                        ? AtlantaTheme.NORD_DARK
                                        : AtlantaTheme.NORD_LIGHT));

        Button cupertinoBtn = new Button("Cupertino");
        cupertinoBtn.setOnAction(
                e ->
                        themeManager.setTheme(
                                themeManager.isDarkTheme()
                                        ? AtlantaTheme.CUPERTINO_DARK
                                        : AtlantaTheme.CUPERTINO_LIGHT));

        Button draculaBtn = new Button("Dracula");
        draculaBtn.setOnAction(e -> themeManager.setTheme(AtlantaTheme.DRACULA));

        Separator separator = new Separator();

        ToggleButton darkToggle = new ToggleButton("🌙");
        darkToggle.setSelected(themeManager.isDarkTheme());
        darkToggle.setOnAction(e -> themeManager.toggleLightDark());

        toolBar.getItems()
                .addAll(
                        new Label("Theme:"),
                        primerBtn,
                        nordBtn,
                        cupertinoBtn,
                        draculaBtn,
                        separator,
                        darkToggle);

        // Update toggle state on theme change
        themeManager.addThemeChangeListener(
                (oldTheme, newTheme) -> {
                    darkToggle.setSelected(themeManager.isDarkTheme());
                });

        return toolBar;
    }
}
