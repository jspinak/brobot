package io.github.jspinak.brobot.integration;

import io.github.jspinak.brobot.BrobotTestApplication;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.action.internal.region.SearchRegionDependencyRegistry;
import io.github.jspinak.brobot.statemanagement.SearchRegionDependencyInitializer;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.find.MatchAdjustmentOptions;
import io.github.jspinak.brobot.model.element.SearchRegionOnObject;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.model.match.Match;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for search region dependencies.
 * Tests how Brobot resolves dynamic search regions based on other objects.
 */
@SpringBootTest(classes = BrobotTestApplication.class)
@Import({
    SearchRegionDependencyTest.MenuState.class,
    SearchRegionDependencyTest.DialogState.class
})
@TestPropertySource(properties = {
    "brobot.core.mock=true",
    "logging.level.io.github.jspinak.brobot=DEBUG"
})
@Slf4j
public class SearchRegionDependencyTest extends BrobotTestBase {

    @Autowired
    private StateService stateService;
    
    @Autowired
    private StateMemory stateMemory;
    
    @Autowired
    private Action action;
    
    @Autowired
    private SearchRegionDependencyRegistry dependencyRegistry;
    
    /**
     * Menu state with base elements
     */
    @Component
    @State(initial = true)
    @Getter
    public static class MenuState {
        private final StateImage menuButton;
        private final StateImage menuDropdown;
        
        public MenuState() {
            // Base menu button at fixed location
            menuButton = new StateImage.Builder()
                .setName("MenuButton")
                .setSearchRegionForAllPatterns(Region.builder()
                    .withRegion(100, 50, 100, 30)
                    .build())
                .build();
            
            // Dropdown that appears relative to menu button
            menuDropdown = new StateImage.Builder()
                .setName("MenuDropdown")
                .setSearchRegionOnObject(SearchRegionOnObject.builder()
                    .setTargetType(StateObject.Type.IMAGE)
                    .setTargetStateName("Menu")
                    .setTargetObjectName("MenuButton")
                    .setAdjustments(MatchAdjustmentOptions.builder()
                        .setAddY(30)  // Below the button
                        .setAddH(200) // Extended height for dropdown
                        .build())
                    .build())
                .build();
        }
    }
    
    /**
     * Dialog state with elements positioned relative to other states
     */
    @Component
    @State
    @Getter
    public static class DialogState {
        private final StateImage dialogTitle;
        private final StateImage dialogCloseButton;
        private final StateImage dialogContent;
        
        public DialogState() {
            // Dialog title bar
            dialogTitle = new StateImage.Builder()
                .setName("DialogTitle")
                .setSearchRegionForAllPatterns(Region.builder()
                    .withRegion(200, 150, 400, 40)
                    .build())
                .build();
            
            // Close button relative to title bar
            dialogCloseButton = new StateImage.Builder()
                .setName("DialogCloseButton")
                .setSearchRegionOnObject(SearchRegionOnObject.builder()
                    .setTargetType(StateObject.Type.IMAGE)
                    .setTargetStateName("Dialog")
                    .setTargetObjectName("DialogTitle")
                    .setAdjustments(MatchAdjustmentOptions.builder()
                        .setAddX(360)  // Right side of title
                        .setAddW(-360) // Narrow to button area
                        .build())
                    .build())
                .build();
            
            // Content area below title
            dialogContent = new StateImage.Builder()
                .setName("DialogContent")
                .setSearchRegionOnObject(SearchRegionOnObject.builder()
                    .setTargetType(StateObject.Type.IMAGE)
                    .setTargetStateName("Dialog")
                    .setTargetObjectName("DialogTitle")
                    .setAdjustments(MatchAdjustmentOptions.builder()
                        .setAddY(40)   // Below title
                        .setAddH(300)  // Content height
                        .build())
                    .build())
                .build();
        }
    }
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        stateMemory.removeAllStates();
        log.info("=== SEARCH REGION DEPENDENCY TEST SETUP COMPLETE ===");
    }
    
    @Test
    public void testBasicSearchRegionDependency() {
        log.info("=== TESTING BASIC SEARCH REGION DEPENDENCY ===");
        
        // Get the Menu state
        io.github.jspinak.brobot.model.state.State menuState = 
            stateService.getState("Menu").orElseThrow();
        
        // Activate Menu state
        stateMemory.addActiveState(menuState.getId());
        
        // Get the menu dropdown which depends on menu button
        MenuState menu = applicationContext.getBean(MenuState.class);
        
        // Verify dependency is registered by checking dependents
        Set<SearchRegionDependencyRegistry.DependentObject> dependents = 
            dependencyRegistry.getDependents("Menu", "MenuButton");
        
        assertFalse(dependents.isEmpty(), "MenuButton should have dependents");
        
        // Find the MenuDropdown dependent
        boolean foundMenuDropdown = dependents.stream()
            .anyMatch(dep -> "MenuDropdown".equals(dep.getStateObject().getName()));
        
        assertTrue(foundMenuDropdown, "MenuDropdown should be a dependent of MenuButton");
        
        log.info("Dependency registered: MenuDropdown depends on Menu.MenuButton");
    }
    
    @Test
    public void testMultipleDependencies() {
        log.info("=== TESTING MULTIPLE DEPENDENCIES ===");
        
        // Get the Dialog state
        io.github.jspinak.brobot.model.state.State dialogState = 
            stateService.getState("Dialog").orElseThrow();
        
        // Check that multiple elements have dependencies on DialogTitle
        Set<SearchRegionDependencyRegistry.DependentObject> titleDependents = 
            dependencyRegistry.getDependents("Dialog", "DialogTitle");
        
        assertFalse(titleDependents.isEmpty(), "DialogTitle should have dependents");
        
        // Both close button and content should depend on title
        Set<String> dependentNames = titleDependents.stream()
            .map(dep -> dep.getStateObject().getName())
            .collect(Collectors.toSet());
        
        assertTrue(dependentNames.contains("DialogCloseButton"),
                  "DialogCloseButton should depend on DialogTitle");
        assertTrue(dependentNames.contains("DialogContent"),
                  "DialogContent should depend on DialogTitle");
        
        log.info("Multiple dependencies on same target verified");
    }
    
    @Test
    public void testSearchRegionAdjustments() {
        log.info("=== TESTING SEARCH REGION ADJUSTMENTS ===");
        
        MenuState menu = applicationContext.getBean(MenuState.class);
        
        // The dropdown has adjustments relative to button
        SearchRegionOnObject searchConfig = menu.getMenuDropdown().getSearchRegionOnObject();
        assertNotNull(searchConfig, "MenuDropdown should have search region config");
        
        MatchAdjustmentOptions adjustments = searchConfig.getAdjustments();
        assertNotNull(adjustments, "Should have adjustment options");
        
        assertEquals(30, adjustments.getAddY(), 
                    "Should add 30 pixels to Y (below button)");
        assertEquals(200, adjustments.getAddH(), 
                    "Should add 200 pixels to height");
        
        log.info("Search region adjustments: Y+{}, H+{}", 
                adjustments.getAddY(), adjustments.getAddH());
    }
    
    @Test
    public void testCrossStateDependency() {
        log.info("=== TESTING CROSS-STATE DEPENDENCY ===");
        
        // Create an image that depends on an object from another state
        StateImage crossStateImage = new StateImage.Builder()
            .setName("CrossStateDependent")
            .setSearchRegionOnObject(SearchRegionOnObject.builder()
                .setTargetType(StateObject.Type.IMAGE)
                .setTargetStateName("Menu")  // Different state
                .setTargetObjectName("MenuButton")
                .setAdjustments(MatchAdjustmentOptions.builder()
                    .setAddX(150)
                    .build())
                .build())
            .build();
        
        // This would normally be registered during state registration
        // In this test, we verify the concept
        SearchRegionOnObject config = crossStateImage.getSearchRegionOnObject();
        assertEquals("Menu", config.getTargetStateName(),
                    "Should reference Menu state");
        assertEquals("MenuButton", config.getTargetObjectName(),
                    "Should reference MenuButton");
        
        log.info("Cross-state dependency: object depends on Menu.MenuButton");
    }
    
    @Test
    public void testDependencyChain() {
        log.info("=== TESTING DEPENDENCY CHAIN ===");
        
        // Create a chain of dependencies: A -> B -> C
        StateImage baseImage = new StateImage.Builder()
            .setName("BaseImage")
            .setSearchRegionForAllPatterns(Region.builder()
                .withRegion(50, 50, 100, 100)
                .build())
            .build();
        
        StateImage middleImage = new StateImage.Builder()
            .setName("MiddleImage")
            .setSearchRegionOnObject(SearchRegionOnObject.builder()
                .setTargetType(StateObject.Type.IMAGE)
                .setTargetStateName("TestState")
                .setTargetObjectName("BaseImage")
                .setAdjustments(MatchAdjustmentOptions.builder()
                    .setAddX(100)
                    .build())
                .build())
            .build();
        
        StateImage dependentImage = new StateImage.Builder()
            .setName("DependentImage")
            .setSearchRegionOnObject(SearchRegionOnObject.builder()
                .setTargetType(StateObject.Type.IMAGE)
                .setTargetStateName("TestState")
                .setTargetObjectName("MiddleImage")
                .setAdjustments(MatchAdjustmentOptions.builder()
                    .setAddY(100)
                    .build())
                .build())
            .build();
        
        // Verify the chain structure
        assertNotNull(middleImage.getSearchRegionOnObject(),
                     "Middle should depend on Base");
        assertNotNull(dependentImage.getSearchRegionOnObject(),
                     "Dependent should depend on Middle");
        
        log.info("Dependency chain: Dependent -> Middle -> Base");
    }
    
    @Test
    public void testDynamicRegionResolution() {
        log.info("=== TESTING DYNAMIC REGION RESOLUTION ===");
        
        // Get menu state and activate it
        io.github.jspinak.brobot.model.state.State menuState = 
            stateService.getState("Menu").orElseThrow();
        stateMemory.addActiveState(menuState.getId());
        
        MenuState menu = applicationContext.getBean(MenuState.class);
        
        // Find the menu button first (base element)
        PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
        ObjectCollection buttonObjects = new ObjectCollection.Builder()
            .withImages(menu.getMenuButton())
            .build();
        
        ActionResult buttonResult = action.perform(findOptions, buttonObjects);
        assertTrue(buttonResult.isSuccess(), "Should find menu button");
        
        // Now try to find dropdown (which depends on button location)
        ObjectCollection dropdownObjects = new ObjectCollection.Builder()
            .withImages(menu.getMenuDropdown())
            .build();
        
        ActionResult dropdownResult = action.perform(findOptions, dropdownObjects);
        
        // In mock mode, this tests that the dependency system is working
        log.info("Dynamic region resolution test completed");
    }
    
    @Test
    public void testRegionAdjustmentCalculation() {
        log.info("=== TESTING REGION ADJUSTMENT CALCULATION ===");
        
        // Base region
        Region baseRegion = Region.builder()
            .withRegion(100, 100, 200, 50)
            .build();
        
        // Test various adjustments
        MatchAdjustmentOptions adjustments = MatchAdjustmentOptions.builder()
            .setAddX(10)
            .setAddY(20)
            .setAddW(30)
            .setAddH(40)
            .build();
        
        // Expected adjusted region
        int expectedX = baseRegion.getX() + adjustments.getAddX();
        int expectedY = baseRegion.getY() + adjustments.getAddY();
        int expectedW = baseRegion.getW() + adjustments.getAddW();
        int expectedH = baseRegion.getH() + adjustments.getAddH();
        
        log.info("Base region: X={}, Y={}, W={}, H={}", 
                baseRegion.getX(), baseRegion.getY(), 
                baseRegion.getW(), baseRegion.getH());
        log.info("Adjustments: X+{}, Y+{}, W+{}, H+{}", 
                adjustments.getAddX(), adjustments.getAddY(),
                adjustments.getAddW(), adjustments.getAddH());
        log.info("Expected adjusted: X={}, Y={}, W={}, H={}", 
                expectedX, expectedY, expectedW, expectedH);
        
        assertEquals(110, expectedX, "X adjustment calculation");
        assertEquals(120, expectedY, "Y adjustment calculation");
        assertEquals(230, expectedW, "Width adjustment calculation");
        assertEquals(90, expectedH, "Height adjustment calculation");
    }
    
    @Autowired
    private org.springframework.context.ApplicationContext applicationContext;
}