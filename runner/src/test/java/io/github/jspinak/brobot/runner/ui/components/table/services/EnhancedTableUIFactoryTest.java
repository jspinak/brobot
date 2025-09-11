package io.github.jspinak.brobot.runner.ui.components.table.services;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import atlantafx.base.theme.Styles;

class EnhancedTableUIFactoryTest {

    private EnhancedTableUIFactory uiFactory;

    @BeforeAll
    static void initJFX() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.startup(
                () -> {
                    new JFXPanel(); // initializes JavaFX environment
                    latch.countDown();
                });

        if (!latch.await(5, TimeUnit.SECONDS)) {
            throw new RuntimeException("Could not initialize JavaFX");
        }
    }

    @BeforeEach
    void setUp() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    uiFactory = new EnhancedTableUIFactory();
                    latch.countDown();
                });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testCreateMainContainer() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    VBox container = uiFactory.createMainContainer();

                    assertNotNull(container);

                    latch.countDown();
                });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testCreateTableView() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    TableView<String> tableView = uiFactory.createTableView();

                    assertNotNull(tableView);
                    assertTrue(tableView.getStyleClass().contains(Styles.STRIPED));
                    assertTrue(tableView.getStyleClass().contains(Styles.BORDERED));
                    assertNotNull(tableView.getPlaceholder());
                    assertEquals(
                            "No data available", ((Label) tableView.getPlaceholder()).getText());

                    latch.countDown();
                });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testCreateSearchField() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    TextField searchField = uiFactory.createSearchField();

                    assertNotNull(searchField);
                    assertEquals("Search...", searchField.getPromptText());

                    latch.countDown();
                });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testCreatePageSizeSelector() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    ComboBox<Integer> pageSizeSelector = uiFactory.createPageSizeSelector();

                    assertNotNull(pageSizeSelector);
                    assertTrue(pageSizeSelector.getItems().contains(10));
                    assertTrue(pageSizeSelector.getItems().contains(25));
                    assertTrue(pageSizeSelector.getItems().contains(50));
                    assertTrue(pageSizeSelector.getItems().contains(100));
                    assertEquals(25, pageSizeSelector.getValue());

                    latch.countDown();
                });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testCreatePagination() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    Pagination pagination = uiFactory.createPagination();

                    assertNotNull(pagination);
                    assertEquals(1, pagination.getPageCount());

                    latch.countDown();
                });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testCreateToolbar() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    TextField searchField = new TextField();
                    ComboBox<Integer> pageSizeSelector = new ComboBox<>();

                    HBox toolbar = uiFactory.createToolbar(searchField, pageSizeSelector);

                    assertNotNull(toolbar);
                    assertEquals(10, toolbar.getSpacing());
                    assertEquals(new Insets(5, 10, 5, 10), toolbar.getPadding());

                    // Should contain search label, search field, spacer, page size label, and
                    // selector
                    assertEquals(5, toolbar.getChildren().size());

                    // Check first label
                    assertTrue(toolbar.getChildren().get(0) instanceof Label);
                    assertEquals("Search:", ((Label) toolbar.getChildren().get(0)).getText());

                    // Check search field
                    assertEquals(searchField, toolbar.getChildren().get(1));

                    // Check spacer
                    assertTrue(toolbar.getChildren().get(2) instanceof javafx.scene.layout.Region);

                    // Check page size label
                    assertTrue(toolbar.getChildren().get(3) instanceof Label);
                    assertEquals(
                            "Items per page:", ((Label) toolbar.getChildren().get(3)).getText());

                    // Check page size selector
                    assertEquals(pageSizeSelector, toolbar.getChildren().get(4));

                    latch.countDown();
                });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testCreatePaginationBox() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    Pagination pagination = new Pagination();
                    HBox paginationBox = uiFactory.createPaginationBox(pagination);

                    assertNotNull(paginationBox);
                    assertEquals(javafx.geometry.Pos.CENTER, paginationBox.getAlignment());
                    assertEquals(1, paginationBox.getChildren().size());
                    assertEquals(pagination, paginationBox.getChildren().get(0));

                    latch.countDown();
                });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testCreateSpacer() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    javafx.scene.layout.Region spacer = uiFactory.createSpacer();

                    assertNotNull(spacer);
                    assertEquals(Priority.ALWAYS, HBox.getHgrow(spacer));

                    latch.countDown();
                });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testCreateLabel() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    // Test without style classes
                    Label label1 = uiFactory.createLabel("Test Label");
                    assertNotNull(label1);
                    assertEquals("Test Label", label1.getText());

                    // Test with style classes
                    Label label2 = uiFactory.createLabel("Styled Label", "style1", "style2");
                    assertNotNull(label2);
                    assertEquals("Styled Label", label2.getText());
                    assertTrue(label2.getStyleClass().contains("style1"));
                    assertTrue(label2.getStyleClass().contains("style2"));

                    latch.countDown();
                });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testCreateToolbarComponents() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    EnhancedTableUIFactory.ToolbarComponents components =
                            uiFactory.createToolbarComponents();

                    assertNotNull(components);
                    assertNotNull(components.getSearchField());
                    assertNotNull(components.getPageSizeSelector());
                    assertNotNull(components.getToolbar());

                    // Verify search field properties
                    assertEquals("Search...", components.getSearchField().getPromptText());

                    // Verify page size selector
                    assertEquals(25, components.getPageSizeSelector().getValue());

                    // Verify toolbar contains the components
                    assertTrue(
                            components
                                    .getToolbar()
                                    .getChildren()
                                    .contains(components.getSearchField()));
                    assertTrue(
                            components
                                    .getToolbar()
                                    .getChildren()
                                    .contains(components.getPageSizeSelector()));

                    latch.countDown();
                });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testAssembleTableUI() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    VBox container = new VBox();
                    HBox toolbar = new HBox();
                    TableView<String> tableView = new TableView<>();
                    HBox paginationBox = new HBox();

                    VBox result =
                            uiFactory.assembleTableUI(container, toolbar, tableView, paginationBox);

                    assertEquals(container, result);
                    assertEquals(3, container.getChildren().size());
                    assertEquals(toolbar, container.getChildren().get(0));
                    assertEquals(tableView, container.getChildren().get(1));
                    assertEquals(paginationBox, container.getChildren().get(2));

                    latch.countDown();
                });
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testCustomConfiguration() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(
                () -> {
                    // Create custom configuration
                    EnhancedTableUIFactory.UIConfiguration config =
                            EnhancedTableUIFactory.UIConfiguration.builder()
                                    .searchPrompt("Find...")
                                    .toolbarSpacing(20)
                                    .toolbarPadding(new Insets(10, 20, 10, 20))
                                    .tablePlaceholder("Empty table")
                                    .defaultPageSize(50)
                                    .build();

                    uiFactory.setConfiguration(config);

                    // Test that configuration is applied
                    TextField searchField = uiFactory.createSearchField();
                    assertEquals("Find...", searchField.getPromptText());

                    ComboBox<Integer> pageSizeSelector = uiFactory.createPageSizeSelector();
                    assertEquals(50, pageSizeSelector.getValue());

                    TableView<String> tableView = uiFactory.createTableView();
                    assertEquals("Empty table", ((Label) tableView.getPlaceholder()).getText());

                    HBox toolbar = uiFactory.createToolbar(searchField, pageSizeSelector);
                    assertEquals(20, toolbar.getSpacing());
                    assertEquals(new Insets(10, 20, 10, 20), toolbar.getPadding());

                    latch.countDown();
                });
        latch.await(5, TimeUnit.SECONDS);
    }
}
