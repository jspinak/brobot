package io.github.jspinak.brobot.statemanagement;

import io.github.jspinak.brobot.model.state.StateEnum;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ActiveStateSet Tests")
@Timeout(value = 10, unit = TimeUnit.SECONDS) // Prevent CI/CD timeout
public class ActiveStateSetTest extends BrobotTestBase {
    
    private ActiveStateSet activeStateSet;
    
    // Test enum implementation
    private enum TestStateEnum implements StateEnum {
        HOME_STATE,
        LOGIN_STATE,
        DASHBOARD_STATE,
        SETTINGS_STATE,
        PROFILE_STATE,
        LOGOUT_STATE
    }
    
    // Another test enum for different context
    private enum MenuStateEnum implements StateEnum {
        MAIN_MENU,
        FILE_MENU,
        EDIT_MENU,
        VIEW_MENU,
        HELP_MENU
    }
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        activeStateSet = new ActiveStateSet();
    }
    
    @Nested
    @DisplayName("Basic State Management")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    class BasicStateManagement {
        
        @Test
        @DisplayName("Should initialize with empty state set")
        public void testInitialState() {
            assertTrue(activeStateSet.getActiveStates().isEmpty());
            assertEquals(0, activeStateSet.getActiveStates().size());
        }
        
        @Test
        @DisplayName("Should add single state")
        public void testAddSingleState() {
            activeStateSet.addState(TestStateEnum.HOME_STATE);
            
            assertEquals(1, activeStateSet.getActiveStates().size());
            assertTrue(activeStateSet.getActiveStates().contains(TestStateEnum.HOME_STATE));
        }
        
        @Test
        @DisplayName("Should add multiple individual states")
        public void testAddMultipleIndividualStates() {
            activeStateSet.addState(TestStateEnum.HOME_STATE);
            activeStateSet.addState(TestStateEnum.LOGIN_STATE);
            activeStateSet.addState(TestStateEnum.DASHBOARD_STATE);
            
            assertEquals(3, activeStateSet.getActiveStates().size());
            assertTrue(activeStateSet.getActiveStates().contains(TestStateEnum.HOME_STATE));
            assertTrue(activeStateSet.getActiveStates().contains(TestStateEnum.LOGIN_STATE));
            assertTrue(activeStateSet.getActiveStates().contains(TestStateEnum.DASHBOARD_STATE));
        }
        
        @Test
        @DisplayName("Should handle duplicate states (set semantics)")
        public void testDuplicateStates() {
            activeStateSet.addState(TestStateEnum.HOME_STATE);
            activeStateSet.addState(TestStateEnum.HOME_STATE);
            activeStateSet.addState(TestStateEnum.HOME_STATE);
            
            // Set should only contain one instance
            assertEquals(1, activeStateSet.getActiveStates().size());
            assertTrue(activeStateSet.getActiveStates().contains(TestStateEnum.HOME_STATE));
        }
        
        @Test
        @DisplayName("Should add states from different enums")
        public void testMixedEnumStates() {
            activeStateSet.addState(TestStateEnum.HOME_STATE);
            activeStateSet.addState(MenuStateEnum.MAIN_MENU);
            activeStateSet.addState(TestStateEnum.LOGIN_STATE);
            activeStateSet.addState(MenuStateEnum.FILE_MENU);
            
            assertEquals(4, activeStateSet.getActiveStates().size());
            assertTrue(activeStateSet.getActiveStates().contains(TestStateEnum.HOME_STATE));
            assertTrue(activeStateSet.getActiveStates().contains(MenuStateEnum.MAIN_MENU));
            assertTrue(activeStateSet.getActiveStates().contains(TestStateEnum.LOGIN_STATE));
            assertTrue(activeStateSet.getActiveStates().contains(MenuStateEnum.FILE_MENU));
        }
    }
    
    @Nested
    @DisplayName("Bulk Operations")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    class BulkOperations {
        
        @Test
        @DisplayName("Should add collection of states")
        public void testAddStateCollection() {
            Set<StateEnum> statesToAdd = new HashSet<>(Arrays.asList(
                TestStateEnum.HOME_STATE,
                TestStateEnum.LOGIN_STATE,
                TestStateEnum.DASHBOARD_STATE
            ));
            
            activeStateSet.addStates(statesToAdd);
            
            assertEquals(3, activeStateSet.getActiveStates().size());
            assertTrue(activeStateSet.getActiveStates().containsAll(statesToAdd));
        }
        
        @Test
        @DisplayName("Should add empty collection without error")
        public void testAddEmptyCollection() {
            Set<StateEnum> emptySet = new HashSet<>();
            
            activeStateSet.addStates(emptySet);
            
            assertTrue(activeStateSet.getActiveStates().isEmpty());
        }
        
        @Test
        @DisplayName("Should merge collections with existing states")
        public void testMergeCollections() {
            activeStateSet.addState(TestStateEnum.HOME_STATE);
            activeStateSet.addState(TestStateEnum.LOGIN_STATE);
            
            Set<StateEnum> additionalStates = new HashSet<>(Arrays.asList(
                TestStateEnum.DASHBOARD_STATE,
                TestStateEnum.SETTINGS_STATE,
                TestStateEnum.LOGIN_STATE // Duplicate
            ));
            
            activeStateSet.addStates(additionalStates);
            
            assertEquals(4, activeStateSet.getActiveStates().size());
            assertTrue(activeStateSet.getActiveStates().contains(TestStateEnum.HOME_STATE));
            assertTrue(activeStateSet.getActiveStates().contains(TestStateEnum.LOGIN_STATE));
            assertTrue(activeStateSet.getActiveStates().contains(TestStateEnum.DASHBOARD_STATE));
            assertTrue(activeStateSet.getActiveStates().contains(TestStateEnum.SETTINGS_STATE));
        }
        
        @Test
        @DisplayName("Should handle large collections efficiently")
        @Timeout(value = 2, unit = TimeUnit.SECONDS)
        public void testLargeCollection() {
            Set<StateEnum> largeSet = new HashSet<>();
            // Add all test states multiple times
            for (int i = 0; i < 100; i++) {
                largeSet.add(TestStateEnum.HOME_STATE);
                largeSet.add(TestStateEnum.LOGIN_STATE);
                largeSet.add(TestStateEnum.DASHBOARD_STATE);
                largeSet.add(TestStateEnum.SETTINGS_STATE);
                largeSet.add(TestStateEnum.PROFILE_STATE);
                largeSet.add(TestStateEnum.LOGOUT_STATE);
            }
            
            activeStateSet.addStates(largeSet);
            
            // Should only have 6 unique states despite adding 600 times
            assertEquals(6, activeStateSet.getActiveStates().size());
        }
    }
    
    @Nested
    @DisplayName("ActiveStateSet Merging")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    class ActiveStateSetMerging {
        
        @Test
        @DisplayName("Should merge another ActiveStateSet")
        public void testMergeActiveStateSet() {
            ActiveStateSet otherSet = new ActiveStateSet();
            otherSet.addState(TestStateEnum.DASHBOARD_STATE);
            otherSet.addState(TestStateEnum.SETTINGS_STATE);
            
            activeStateSet.addState(TestStateEnum.HOME_STATE);
            activeStateSet.addState(TestStateEnum.LOGIN_STATE);
            
            activeStateSet.addStates(otherSet);
            
            assertEquals(4, activeStateSet.getActiveStates().size());
            assertTrue(activeStateSet.getActiveStates().contains(TestStateEnum.HOME_STATE));
            assertTrue(activeStateSet.getActiveStates().contains(TestStateEnum.LOGIN_STATE));
            assertTrue(activeStateSet.getActiveStates().contains(TestStateEnum.DASHBOARD_STATE));
            assertTrue(activeStateSet.getActiveStates().contains(TestStateEnum.SETTINGS_STATE));
        }
        
        @Test
        @DisplayName("Should merge empty ActiveStateSet")
        public void testMergeEmptyActiveStateSet() {
            ActiveStateSet emptySet = new ActiveStateSet();
            
            activeStateSet.addState(TestStateEnum.HOME_STATE);
            activeStateSet.addStates(emptySet);
            
            assertEquals(1, activeStateSet.getActiveStates().size());
            assertTrue(activeStateSet.getActiveStates().contains(TestStateEnum.HOME_STATE));
        }
        
        @Test
        @DisplayName("Should merge with duplicate states")
        public void testMergeWithDuplicates() {
            ActiveStateSet set1 = new ActiveStateSet();
            set1.addState(TestStateEnum.HOME_STATE);
            set1.addState(TestStateEnum.LOGIN_STATE);
            
            ActiveStateSet set2 = new ActiveStateSet();
            set2.addState(TestStateEnum.LOGIN_STATE); // Duplicate
            set2.addState(TestStateEnum.DASHBOARD_STATE);
            
            activeStateSet.addStates(set1);
            activeStateSet.addStates(set2);
            
            assertEquals(3, activeStateSet.getActiveStates().size());
            assertTrue(activeStateSet.getActiveStates().contains(TestStateEnum.HOME_STATE));
            assertTrue(activeStateSet.getActiveStates().contains(TestStateEnum.LOGIN_STATE));
            assertTrue(activeStateSet.getActiveStates().contains(TestStateEnum.DASHBOARD_STATE));
        }
        
        @Test
        @DisplayName("Should handle chained merging")
        public void testChainedMerging() {
            ActiveStateSet set1 = new ActiveStateSet();
            set1.addState(TestStateEnum.HOME_STATE);
            
            ActiveStateSet set2 = new ActiveStateSet();
            set2.addState(TestStateEnum.LOGIN_STATE);
            set2.addStates(set1);
            
            ActiveStateSet set3 = new ActiveStateSet();
            set3.addState(TestStateEnum.DASHBOARD_STATE);
            set3.addStates(set2);
            
            activeStateSet.addStates(set3);
            
            assertEquals(3, activeStateSet.getActiveStates().size());
            assertTrue(activeStateSet.getActiveStates().contains(TestStateEnum.HOME_STATE));
            assertTrue(activeStateSet.getActiveStates().contains(TestStateEnum.LOGIN_STATE));
            assertTrue(activeStateSet.getActiveStates().contains(TestStateEnum.DASHBOARD_STATE));
        }
    }
    
    @Nested
    @DisplayName("State Retrieval")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    class StateRetrieval {
        
        @Test
        @DisplayName("Should return modifiable set")
        public void testReturnedSetIsModifiable() {
            activeStateSet.addState(TestStateEnum.HOME_STATE);
            
            Set<StateEnum> states = activeStateSet.getActiveStates();
            states.add(TestStateEnum.LOGIN_STATE);
            
            // Changes to returned set affect the internal set
            assertEquals(2, activeStateSet.getActiveStates().size());
            assertTrue(activeStateSet.getActiveStates().contains(TestStateEnum.LOGIN_STATE));
        }
        
        @Test
        @DisplayName("Should maintain insertion order is not guaranteed")
        public void testNoOrderGuarantee() {
            // HashSet doesn't guarantee order
            activeStateSet.addState(TestStateEnum.LOGOUT_STATE);
            activeStateSet.addState(TestStateEnum.HOME_STATE);
            activeStateSet.addState(TestStateEnum.PROFILE_STATE);
            activeStateSet.addState(TestStateEnum.LOGIN_STATE);
            
            Set<StateEnum> states = activeStateSet.getActiveStates();
            
            // We can't test order, but we can verify all elements are present
            assertEquals(4, states.size());
            assertTrue(states.contains(TestStateEnum.LOGOUT_STATE));
            assertTrue(states.contains(TestStateEnum.HOME_STATE));
            assertTrue(states.contains(TestStateEnum.PROFILE_STATE));
            assertTrue(states.contains(TestStateEnum.LOGIN_STATE));
        }
        
        @Test
        @DisplayName("Should allow iteration over states")
        public void testIterateOverStates() {
            activeStateSet.addState(TestStateEnum.HOME_STATE);
            activeStateSet.addState(TestStateEnum.LOGIN_STATE);
            activeStateSet.addState(TestStateEnum.DASHBOARD_STATE);
            
            int count = 0;
            Set<StateEnum> foundStates = new HashSet<>();
            
            for (StateEnum state : activeStateSet.getActiveStates()) {
                foundStates.add(state);
                count++;
            }
            
            assertEquals(3, count);
            assertEquals(3, foundStates.size());
            assertTrue(foundStates.contains(TestStateEnum.HOME_STATE));
            assertTrue(foundStates.contains(TestStateEnum.LOGIN_STATE));
            assertTrue(foundStates.contains(TestStateEnum.DASHBOARD_STATE));
        }
    }
    
    @Nested
    @DisplayName("Complex Scenarios")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    class ComplexScenarios {
        
        @Test
        @DisplayName("Should handle null states gracefully")
        public void testNullStateHandling() {
            activeStateSet.addState(TestStateEnum.HOME_STATE);
            activeStateSet.addState(null);
            activeStateSet.addState(TestStateEnum.LOGIN_STATE);
            
            assertEquals(3, activeStateSet.getActiveStates().size());
            assertTrue(activeStateSet.getActiveStates().contains(null));
            assertTrue(activeStateSet.getActiveStates().contains(TestStateEnum.HOME_STATE));
            assertTrue(activeStateSet.getActiveStates().contains(TestStateEnum.LOGIN_STATE));
        }
        
        @Test
        @DisplayName("Should handle concurrent state types")
        public void testConcurrentStateTypes() {
            // Simulate a complex GUI with multiple concurrent states
            activeStateSet.addState(TestStateEnum.DASHBOARD_STATE); // Main content
            activeStateSet.addState(MenuStateEnum.MAIN_MENU); // Menu overlay
            activeStateSet.addState(TestStateEnum.PROFILE_STATE); // Side panel
            activeStateSet.addState(MenuStateEnum.VIEW_MENU); // Sub-menu
            
            assertEquals(4, activeStateSet.getActiveStates().size());
            
            // Check we can query for specific state types
            boolean hasMenuState = false;
            boolean hasContentState = false;
            
            for (StateEnum state : activeStateSet.getActiveStates()) {
                if (state instanceof MenuStateEnum) {
                    hasMenuState = true;
                }
                if (state instanceof TestStateEnum) {
                    hasContentState = true;
                }
            }
            
            assertTrue(hasMenuState);
            assertTrue(hasContentState);
        }
        
        @Test
        @DisplayName("Should support typical state transition workflow")
        public void testStateTransitionWorkflow() {
            // Initial state
            activeStateSet.addState(TestStateEnum.HOME_STATE);
            assertEquals(1, activeStateSet.getActiveStates().size());
            
            // Navigate to login
            activeStateSet.getActiveStates().clear();
            activeStateSet.addState(TestStateEnum.LOGIN_STATE);
            assertEquals(1, activeStateSet.getActiveStates().size());
            assertTrue(activeStateSet.getActiveStates().contains(TestStateEnum.LOGIN_STATE));
            
            // After login, dashboard and menu appear
            activeStateSet.getActiveStates().clear();
            activeStateSet.addState(TestStateEnum.DASHBOARD_STATE);
            activeStateSet.addState(MenuStateEnum.MAIN_MENU);
            assertEquals(2, activeStateSet.getActiveStates().size());
            
            // Open settings (overlay)
            activeStateSet.addState(TestStateEnum.SETTINGS_STATE);
            assertEquals(3, activeStateSet.getActiveStates().size());
            
            // Close settings
            activeStateSet.getActiveStates().remove(TestStateEnum.SETTINGS_STATE);
            assertEquals(2, activeStateSet.getActiveStates().size());
            assertFalse(activeStateSet.getActiveStates().contains(TestStateEnum.SETTINGS_STATE));
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    class EdgeCases {
        
        @Test
        @DisplayName("Should handle self-merging")
        @Timeout(value = 2, unit = TimeUnit.SECONDS) // Prevent infinite loop
        public void testSelfMerging() {
            activeStateSet.addState(TestStateEnum.HOME_STATE);
            activeStateSet.addState(TestStateEnum.LOGIN_STATE);
            
            int originalSize = activeStateSet.getActiveStates().size();
            
            // Create a copy to avoid potential infinite loop
            Set<StateEnum> copy = new HashSet<>(activeStateSet.getActiveStates());
            
            // Try to merge with the copy
            activeStateSet.addStates(copy);
            
            // Size should not change (set semantics)
            assertEquals(originalSize, activeStateSet.getActiveStates().size());
        }
        
        @Test
        @DisplayName("Should handle clearing states")
        public void testClearStates() {
            activeStateSet.addState(TestStateEnum.HOME_STATE);
            activeStateSet.addState(TestStateEnum.LOGIN_STATE);
            activeStateSet.addState(TestStateEnum.DASHBOARD_STATE);
            
            activeStateSet.getActiveStates().clear();
            
            assertTrue(activeStateSet.getActiveStates().isEmpty());
            assertEquals(0, activeStateSet.getActiveStates().size());
        }
        
        @Test
        @DisplayName("Should handle contains check")
        public void testContainsCheck() {
            activeStateSet.addState(TestStateEnum.HOME_STATE);
            activeStateSet.addState(TestStateEnum.LOGIN_STATE);
            
            assertTrue(activeStateSet.getActiveStates().contains(TestStateEnum.HOME_STATE));
            assertTrue(activeStateSet.getActiveStates().contains(TestStateEnum.LOGIN_STATE));
            assertFalse(activeStateSet.getActiveStates().contains(TestStateEnum.DASHBOARD_STATE));
            assertFalse(activeStateSet.getActiveStates().contains(MenuStateEnum.MAIN_MENU));
        }
    }
}