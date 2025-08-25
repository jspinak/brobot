import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.mouse.*;
import io.github.jspinak.brobot.action.internal.mouse.*;
import io.github.jspinak.brobot.model.action.MouseButton;

public class StandaloneTestVerifier {
    
    private static int testsPassed = 0;
    private static int testsFailed = 0;
    
    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("Standalone Test Verification");
        System.out.println("===========================================\n");
        
        // Test MouseDown
        testMouseDown();
        
        // Test MouseUp
        testMouseUp();
        
        System.out.println("\n===========================================");
        System.out.println("Results:");
        System.out.println("Tests Passed: " + testsPassed);
        System.out.println("Tests Failed: " + testsFailed);
        System.out.println("===========================================");
    }
    
    private static void testMouseDown() {
        System.out.println("Testing MouseDown:");
        System.out.println("-----------------");
        
        // Test 1: Action Type
        try {
            MockMouseDownWrapper mockWrapper = new MockMouseDownWrapper();
            MouseDown mouseDown = new MouseDown(mockWrapper);
            
            if (mouseDown.getActionType() == ActionInterface.Type.MOUSE_DOWN) {
                System.out.println("✓ MouseDown returns correct action type");
                testsPassed++;
            } else {
                System.out.println("✗ MouseDown returns incorrect action type");
                testsFailed++;
            }
        } catch (Exception e) {
            System.out.println("✗ Error testing MouseDown action type: " + e.getMessage());
            testsFailed++;
        }
        
        // Test 2: Perform with default options
        try {
            MockMouseDownWrapper mockWrapper = new MockMouseDownWrapper();
            MouseDown mouseDown = new MouseDown(mockWrapper);
            
            MouseDownOptions options = new MouseDownOptions.Builder().build();
            ActionResult result = new MockActionResult(options);
            
            mouseDown.perform(result, new ObjectCollection());
            
            if (mockWrapper.wasPressed && mockWrapper.lastButton == ClickType.Type.LEFT) {
                System.out.println("✓ MouseDown performs left click by default");
                testsPassed++;
            } else {
                System.out.println("✗ MouseDown did not perform correct default click");
                testsFailed++;
            }
        } catch (Exception e) {
            System.out.println("✗ Error testing MouseDown perform: " + e.getMessage());
            testsFailed++;
        }
        
        // Test 3: Right button
        try {
            MockMouseDownWrapper mockWrapper = new MockMouseDownWrapper();
            MouseDown mouseDown = new MouseDown(mockWrapper);
            
            MouseDownOptions options = new MouseDownOptions.Builder()
                .setButton(MouseButton.RIGHT)
                .build();
            ActionResult result = new MockActionResult(options);
            
            mouseDown.perform(result, new ObjectCollection());
            
            if (mockWrapper.wasPressed && mockWrapper.lastButton == ClickType.Type.RIGHT) {
                System.out.println("✓ MouseDown handles right button correctly");
                testsPassed++;
            } else {
                System.out.println("✗ MouseDown did not handle right button correctly");
                testsFailed++;
            }
        } catch (Exception e) {
            System.out.println("✗ Error testing MouseDown right button: " + e.getMessage());
            testsFailed++;
        }
    }
    
    private static void testMouseUp() {
        System.out.println("\nTesting MouseUp:");
        System.out.println("---------------");
        
        // Test 1: Action Type
        try {
            MockMouseUpWrapper mockWrapper = new MockMouseUpWrapper();
            MouseUp mouseUp = new MouseUp(mockWrapper);
            
            if (mouseUp.getActionType() == ActionInterface.Type.MOUSE_UP) {
                System.out.println("✓ MouseUp returns correct action type");
                testsPassed++;
            } else {
                System.out.println("✗ MouseUp returns incorrect action type");
                testsFailed++;
            }
        } catch (Exception e) {
            System.out.println("✗ Error testing MouseUp action type: " + e.getMessage());
            testsFailed++;
        }
        
        // Test 2: Perform with default options
        try {
            MockMouseUpWrapper mockWrapper = new MockMouseUpWrapper();
            MouseUp mouseUp = new MouseUp(mockWrapper);
            
            MouseUpOptions options = new MouseUpOptions.Builder().build();
            ActionResult result = new MockActionResult(options);
            
            mouseUp.perform(result, new ObjectCollection());
            
            if (mockWrapper.wasReleased && mockWrapper.lastButton == ClickType.Type.LEFT) {
                System.out.println("✓ MouseUp performs left release by default");
                testsPassed++;
            } else {
                System.out.println("✗ MouseUp did not perform correct default release");
                testsFailed++;
            }
        } catch (Exception e) {
            System.out.println("✗ Error testing MouseUp perform: " + e.getMessage());
            testsFailed++;
        }
    }
    
    // Mock classes for testing
    static class MockMouseDownWrapper extends MouseDownWrapper {
        boolean wasPressed = false;
        ClickType.Type lastButton;
        double lastPauseBefore;
        double lastPauseAfter;
        
        @Override
        public void press(double pauseBeforeMouseDown, double pauseAfterMouseDown, ClickType.Type button) {
            wasPressed = true;
            lastButton = button;
            lastPauseBefore = pauseBeforeMouseDown;
            lastPauseAfter = pauseAfterMouseDown;
        }
    }
    
    static class MockMouseUpWrapper extends MouseUpWrapper {
        boolean wasReleased = false;
        ClickType.Type lastButton;
        double lastPauseBefore;
        double lastPauseAfter;
        
        // MouseUp actually calls press method (based on the source code)
        @Override
        public void press(double pauseBeforeMouseUp, double pauseAfterMouseUp, ClickType.Type button) {
            wasReleased = true;
            lastButton = button;
            lastPauseBefore = pauseBeforeMouseUp;
            lastPauseAfter = pauseAfterMouseUp;
        }
    }
    
    static class MockActionResult extends ActionResult {
        private Object config;
        
        MockActionResult(Object config) {
            this.config = config;
        }
        
        @Override
        public Object getActionConfig() {
            return config;
        }
    }
}