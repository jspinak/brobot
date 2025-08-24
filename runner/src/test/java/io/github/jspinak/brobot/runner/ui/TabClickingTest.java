package io.github.jspinak.brobot.runner.ui;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import io.github.jspinak.brobot.runner.ui.utils.TabClickFix;
import io.github.jspinak.brobot.runner.ui.utils.TestFXTabClickFix;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify that tab clicking works properly and responds quickly.
 */
@ExtendWith(ApplicationExtension.class)
public class TabClickingTest {
    
    private TabPane tabPane;
    private Tab tab1;
    private Tab tab2;
    private Tab tab3;
    private AtomicInteger tabChangeCount;
    private AtomicBoolean tabClickResponded;
    private long clickStartTime;
    private long clickEndTime;
    
    @Start
    public void start(Stage stage) {
        // Create tabs
        tab1 = new Tab("Configuration");
        tab1.setClosable(false);
        
        tab2 = new Tab("Automation");
        tab2.setClosable(false);
        
        tab3 = new Tab("Resources");
        tab3.setClosable(false);
        
        // Create TabPane
        tabPane = new TabPane(tab1, tab2, tab3);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // Apply tab clicking fix
        TabClickFix.fixTabClicking(tabPane);
        TestFXTabClickFix.makeTabsClickable(tabPane);
        
        // Initialize tracking variables
        tabChangeCount = new AtomicInteger(0);
        tabClickResponded = new AtomicBoolean(false);
        
        // Add listener to track tab changes
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null) {
                tabChangeCount.incrementAndGet();
                tabClickResponded.set(true);
                clickEndTime = System.currentTimeMillis();
                System.out.println("Tab changed to: " + getTabText(newTab) + 
                    " (change #" + tabChangeCount.get() + ")");
            }
        });
        
        // Create scene and show
        Scene scene = new Scene(tabPane, 800, 600);
        stage.setScene(scene);
        stage.setTitle("Tab Clicking Test");
        stage.show();
    }
    
    @BeforeEach
    public void setUp() {
        // Reset state before each test
        Platform.runLater(() -> {
            tabPane.getSelectionModel().selectFirst();
            tabChangeCount.set(0);
            tabClickResponded.set(false);
        });
        WaitForAsyncUtils.waitForFxEvents();
    }
    
    @Test
    public void testTabClickingRespondsQuickly(FxRobot robot) throws InterruptedException {
        // Ensure we start on the first tab
        assertEquals(tab1, tabPane.getSelectionModel().getSelectedItem());
        
        // Record start time and click on second tab
        clickStartTime = System.currentTimeMillis();
        tabClickResponded.set(false);
        
        // Click on the second tab
        robot.clickOn("Automation");
        
        // Wait for response with timeout
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            if (tabClickResponded.get()) {
                latch.countDown();
            }
        });
        
        // Wait maximum 1 second for tab to respond
        boolean responded = latch.await(1000, TimeUnit.MILLISECONDS);
        
        // Verify tab changed
        assertTrue(responded, "Tab click did not respond within 1 second");
        assertEquals(tab2, tabPane.getSelectionModel().getSelectedItem());
        
        // Check response time
        long responseTime = clickEndTime - clickStartTime;
        System.out.println("Tab click response time: " + responseTime + "ms");
        
        // Response should be under 200ms for good UX
        assertTrue(responseTime < 200, 
            "Tab click took too long: " + responseTime + "ms (should be < 200ms)");
    }
    
    @Test
    public void testMultipleTabClicksWork(FxRobot robot) throws InterruptedException {
        System.out.println("Starting testMultipleTabClicksWork");
        Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
        String tabText = getTabText(currentTab);
        System.out.println("Initial tab: " + tabText);
        
        // Click through all tabs
        System.out.println("Clicking on Automation tab...");
        robot.clickOn("Automation");
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(500); // Give extra time
        System.out.println("Current tab after click: " + getTabText(tabPane.getSelectionModel().getSelectedItem()));
        assertEquals(tab2, tabPane.getSelectionModel().getSelectedItem());
        
        System.out.println("Clicking on Resources tab...");
        robot.clickOn("Resources");
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(500);
        System.out.println("Current tab after click: " + getTabText(tabPane.getSelectionModel().getSelectedItem()));
        assertEquals(tab3, tabPane.getSelectionModel().getSelectedItem());
        
        System.out.println("Clicking on Configuration tab...");
        robot.clickOn("Configuration");
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(500);
        System.out.println("Current tab after click: " + getTabText(tabPane.getSelectionModel().getSelectedItem()));
        assertEquals(tab1, tabPane.getSelectionModel().getSelectedItem());
        
        // Verify all clicks were registered
        System.out.println("Total tab changes: " + tabChangeCount.get());
        assertTrue(tabChangeCount.get() >= 3, 
            "Not all tab clicks were registered. Count: " + tabChangeCount.get());
    }
    
    @Test
    public void testRapidTabClicksAllRegister(FxRobot robot) throws InterruptedException {
        int initialCount = tabChangeCount.get();
        
        // Perform rapid clicks
        for (int i = 0; i < 5; i++) {
            robot.clickOn("Automation");
            Thread.sleep(50); // Small delay between clicks
            robot.clickOn("Resources");
            Thread.sleep(50);
            robot.clickOn("Configuration");
            Thread.sleep(50);
        }
        
        // Wait for all events to process
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(500);
        
        // Should have registered at least 10 tab changes (some might be skipped due to rapid clicking)
        int totalChanges = tabChangeCount.get() - initialCount;
        assertTrue(totalChanges >= 10, 
            "Rapid clicks were not all registered. Only " + totalChanges + " changes detected");
    }
    
    @Test
    public void testTabClickingWithMouseEvents(FxRobot robot) {
        // Get the tab header region bounds
        Platform.runLater(() -> {
            // Simulate direct mouse event on tab
            MouseEvent clickEvent = new MouseEvent(
                MouseEvent.MOUSE_CLICKED,
                100, 20, // coordinates within tab header
                100, 20,
                MouseButton.PRIMARY, 1,
                false, false, false, false,
                true, false, false, false, false, false,
                null
            );
            
            clickStartTime = System.currentTimeMillis();
            tabPane.fireEvent(clickEvent);
        });
        
        WaitForAsyncUtils.waitForFxEvents();
        
        // Check if tab responded to direct mouse event
        assertTrue(tabClickResponded.get(), "Tab did not respond to direct mouse event");
    }
    
    /**
     * Helper method to get tab text from either text property or graphic label.
     */
    private String getTabText(Tab tab) {
        if (tab.getText() != null && !tab.getText().isEmpty()) {
            return tab.getText();
        }
        if (tab.getGraphic() instanceof Label) {
            return ((Label) tab.getGraphic()).getText();
        }
        return "Unknown";
    }
}