package io.github.jspinak.brobot.runner.ui.config.test;

import io.github.jspinak.brobot.runner.ui.components.EnhancedTable;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;

import static org.mockito.Mockito.*;

/**
 * Utility for mocking EnhancedTable components.
 */
public class EnhancedTableMocker {

    /**
     * Creates a mock EnhancedTable with basic functionality.
     */
    public static <T> EnhancedTable<T> createMock() {
        EnhancedTable<T> mockTable = mock(EnhancedTable.class);
        TableView<T> tableView = new TableView<>();

        when(mockTable.getTableView()).thenReturn(tableView);

        // Mock the addColumn method with appropriate type parameters
        doAnswer(invocation -> {
            String columnName = invocation.getArgument(0);
            Callback<TableColumn.CellDataFeatures<T, String>, ObservableValue<String>> callback =
                    invocation.getArgument(1);

            // Create a TableColumn and add it to the tableView
            TableColumn<T, String> column = new TableColumn<>(columnName);
            column.setCellValueFactory(callback);
            tableView.getColumns().add(column);

            return null;
        }).when(mockTable).addColumn(anyString(), (String) any());

        return mockTable;
    }
}