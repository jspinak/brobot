package io.github.jspinak.brobot.runner.ui.config.services;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jspinak.brobot.runner.ui.components.EnhancedTable;
import io.github.jspinak.brobot.runner.ui.config.ConfigEntry;

@ExtendWith(MockitoExtension.class)
class ConfigTableServiceTest {

    private ConfigTableService service;

    @BeforeEach
    void setUp() {
        service = new ConfigTableService();

        // Initialize JavaFX toolkit if needed
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
    }

    @Test
    void testSetupTable() {
        // Given
        EnhancedTable<ConfigEntry> table = new EnhancedTable<>();

        // When
        service.setupTable(table);

        // Then
        TableView<ConfigEntry> tableView = table.getTableView();
        assertEquals(5, tableView.getColumns().size()); // Name, Project, Date, Path, Actions

        // Verify column names
        assertEquals("Name", tableView.getColumns().get(0).getText());
        assertEquals("Project", tableView.getColumns().get(1).getText());
        assertEquals("Last Modified", tableView.getColumns().get(2).getText());
        assertEquals("Path", tableView.getColumns().get(3).getText());
        assertEquals("Actions", tableView.getColumns().get(4).getText());
    }

    @Test
    void testSetupTableWithoutPathAndActions() {
        // Given
        service.setConfiguration(
                ConfigTableService.TableConfiguration.builder()
                        .showPath(false)
                        .showActions(false)
                        .build());

        EnhancedTable<ConfigEntry> table = new EnhancedTable<>();

        // When
        service.setupTable(table);

        // Then
        TableView<ConfigEntry> tableView = table.getTableView();
        assertEquals(3, tableView.getColumns().size()); // Only Name, Project, Date
    }

    @Test
    void testUpdateTableData() throws InterruptedException {
        // Given
        EnhancedTable<ConfigEntry> table = new EnhancedTable<>();
        service.setupTable(table);

        List<ConfigEntry> configs =
                Arrays.asList(
                        createTestEntry("test1", "Project 1"),
                        createTestEntry("test2", "Project 2"));

        // When
        service.updateTableData(table, configs);

        // Wait for Platform.runLater
        Thread.sleep(100);

        // Then
        Platform.runLater(
                () -> {
                    assertEquals(2, table.getTableView().getItems().size());
                    assertEquals("test1", table.getTableView().getItems().get(0).getName());
                    assertEquals("test2", table.getTableView().getItems().get(1).getName());
                });
    }

    @Test
    void testSelectionHandler() throws InterruptedException {
        // Given
        EnhancedTable<ConfigEntry> table = new EnhancedTable<>();
        service.setupTable(table);

        AtomicReference<ConfigEntry> selectedEntry = new AtomicReference<>();
        service.setSelectionHandler(selectedEntry::set);

        ConfigEntry entry = createTestEntry("test1", "Project 1");
        service.updateTableData(table, Arrays.asList(entry));

        // Wait for Platform.runLater
        Thread.sleep(100);

        // When
        Platform.runLater(
                () -> {
                    table.getTableView().getSelectionModel().select(0);
                });

        // Wait for selection
        Thread.sleep(100);

        // Then
        assertNotNull(selectedEntry.get());
        assertEquals(entry, selectedEntry.get());
    }

    @Test
    void testGetSelectedConfiguration() throws InterruptedException {
        // Given
        EnhancedTable<ConfigEntry> table = new EnhancedTable<>();
        service.setupTable(table);

        ConfigEntry entry = createTestEntry("test1", "Project 1");
        service.updateTableData(table, Arrays.asList(entry));

        // Wait for Platform.runLater
        Thread.sleep(100);

        // When
        Platform.runLater(
                () -> {
                    table.getTableView().getSelectionModel().select(0);
                });

        // Wait for selection
        Thread.sleep(100);

        // Then
        ConfigEntry selected = service.getSelectedConfiguration(table);
        assertNotNull(selected);
        assertEquals(entry, selected);
    }

    @Test
    void testSelectConfiguration() throws InterruptedException {
        // Given
        EnhancedTable<ConfigEntry> table = new EnhancedTable<>();
        service.setupTable(table);

        ConfigEntry entry1 = createTestEntry("test1", "Project 1");
        ConfigEntry entry2 = createTestEntry("test2", "Project 2");
        service.updateTableData(table, Arrays.asList(entry1, entry2));

        // Wait for Platform.runLater
        Thread.sleep(100);

        // When
        service.selectConfiguration(table, entry2);

        // Wait for selection
        Thread.sleep(100);

        // Then
        Platform.runLater(
                () -> {
                    assertEquals(1, table.getTableView().getSelectionModel().getSelectedIndex());
                    assertEquals(
                            entry2, table.getTableView().getSelectionModel().getSelectedItem());
                });
    }

    @Test
    void testColumnConfiguration() {
        // Given
        ConfigTableService.TableConfiguration config =
                ConfigTableService.TableConfiguration.builder()
                        .nameColumnWidths(50, 100, 200)
                        .projectColumnWidths(60, 110, 210)
                        .dateColumnWidths(70, 120, 220)
                        .pathColumnWidths(80, 130)
                        .actionColumnWidths(90, 140, 230)
                        .build();

        service.setConfiguration(config);

        EnhancedTable<ConfigEntry> table = new EnhancedTable<>();

        // When
        service.setupTable(table);

        // Then
        TableView<ConfigEntry> tableView = table.getTableView();

        // Verify name column
        TableColumn<ConfigEntry, ?> nameColumn = tableView.getColumns().get(0);
        assertEquals(50, nameColumn.getMinWidth());
        assertEquals(100, nameColumn.getPrefWidth());
        assertEquals(200, nameColumn.getMaxWidth());

        // Verify project column
        TableColumn<ConfigEntry, ?> projectColumn = tableView.getColumns().get(1);
        assertEquals(60, projectColumn.getMinWidth());
        assertEquals(110, projectColumn.getPrefWidth());
        assertEquals(210, projectColumn.getMaxWidth());

        // Verify date column
        TableColumn<ConfigEntry, ?> dateColumn = tableView.getColumns().get(2);
        assertEquals(70, dateColumn.getMinWidth());
        assertEquals(120, dateColumn.getPrefWidth());
        assertEquals(220, dateColumn.getMaxWidth());

        // Verify path column
        TableColumn<ConfigEntry, ?> pathColumn = tableView.getColumns().get(3);
        assertEquals(80, pathColumn.getMinWidth());
        assertEquals(130, pathColumn.getPrefWidth());

        // Verify action column
        TableColumn<ConfigEntry, ?> actionColumn = tableView.getColumns().get(4);
        assertEquals(90, actionColumn.getMinWidth());
        assertEquals(140, actionColumn.getPrefWidth());
        assertEquals(230, actionColumn.getMaxWidth());
    }

    @Test
    void testAutoResizePolicy() {
        // Given
        service.setConfiguration(
                ConfigTableService.TableConfiguration.builder().autoResize(true).build());

        EnhancedTable<ConfigEntry> table = new EnhancedTable<>();

        // When
        service.setupTable(table);

        // Then
        assertEquals(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN,
                table.getTableView().getColumnResizePolicy());
    }

    private ConfigEntry createTestEntry(String name, String project) {
        Path projectConfigPath = Paths.get("config", name + ".json");
        Path dslConfigPath = Paths.get("config", name + "-dsl.json");
        Path imagePath = Paths.get("images");

        return new ConfigEntry(
                name, project, projectConfigPath, dslConfigPath, imagePath, LocalDateTime.now());
    }
}
