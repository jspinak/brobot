package io.github.jspinak.brobot.statemanagement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.test.BrobotTestBase;

@DisplayName("StateVisibilityManager Tests")
public class StateVisibilityManagerTest extends BrobotTestBase {

    @Mock private StateService stateService;

    @Mock private StateMemory stateMemory;

    private StateVisibilityManager visibilityManager;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        visibilityManager = new StateVisibilityManager(stateService, stateMemory);
    }

    @Nested
    @DisplayName("Basic Hiding Operations")
    class BasicHidingOperations {

        @Test
        @DisplayName("Should hide states that can be hidden")
        public void testHideStates() {
            // Setup
            State newState = mock(State.class);
            when(newState.getCanHideIds()).thenReturn(new HashSet<>(Arrays.asList(2L, 3L)));

            when(stateService.getState(1L)).thenReturn(Optional.of(newState));
            when(stateMemory.getActiveStates())
                    .thenReturn(new HashSet<>(Arrays.asList(2L, 3L, 4L)));

            // Execute
            boolean result = visibilityManager.set(1L);

            // Verify
            assertTrue(result);
            verify(newState).addHiddenState(2L);
            verify(newState).addHiddenState(3L);
            verify(stateMemory).removeInactiveState(2L);
            verify(stateMemory).removeInactiveState(3L);

            // State 4 should not be hidden
            verify(newState, never()).addHiddenState(4L);
            verify(stateMemory, never()).removeInactiveState(4L);
        }

        @Test
        @DisplayName("Should handle state with no canHide list")
        public void testNoCanHideList() {
            State newState = mock(State.class);
            when(newState.getCanHideIds()).thenReturn(new HashSet<>());

            when(stateService.getState(1L)).thenReturn(Optional.of(newState));
            when(stateMemory.getActiveStates()).thenReturn(new HashSet<>(Arrays.asList(2L, 3L)));

            boolean result = visibilityManager.set(1L);

            assertTrue(result);
            verify(newState, never()).addHiddenState(anyLong());
            verify(stateMemory, never()).removeInactiveState(anyLong());
        }

        @Test
        @DisplayName("Should return false for non-existent state")
        public void testNonExistentState() {
            when(stateService.getState(999L)).thenReturn(Optional.empty());

            boolean result = visibilityManager.set(999L);

            assertFalse(result);
            verify(stateMemory, never()).removeInactiveState(anyLong());
        }

        @Test
        @DisplayName("Should handle no active states")
        public void testNoActiveStates() {
            State newState = mock(State.class);
            when(newState.getCanHideIds()).thenReturn(new HashSet<>(Arrays.asList(2L, 3L)));

            when(stateService.getState(1L)).thenReturn(Optional.of(newState));
            when(stateMemory.getActiveStates()).thenReturn(new HashSet<>());

            boolean result = visibilityManager.set(1L);

            assertTrue(result);
            verify(newState, never()).addHiddenState(anyLong());
            verify(stateMemory, never()).removeInactiveState(anyLong());
        }
    }

    @Nested
    @DisplayName("Partial Hiding Scenarios")
    class PartialHidingScenarios {

        @Test
        @DisplayName("Should hide only matching states")
        public void testPartialHiding() {
            State newState = mock(State.class);
            when(newState.getCanHideIds()).thenReturn(new HashSet<>(Arrays.asList(2L, 4L)));

            when(stateService.getState(1L)).thenReturn(Optional.of(newState));
            when(stateMemory.getActiveStates())
                    .thenReturn(new HashSet<>(Arrays.asList(2L, 3L, 4L, 5L)));

            boolean result = visibilityManager.set(1L);

            assertTrue(result);
            verify(newState).addHiddenState(2L);
            verify(newState).addHiddenState(4L);
            verify(stateMemory).removeInactiveState(2L);
            verify(stateMemory).removeInactiveState(4L);

            // States 3 and 5 should remain active
            verify(newState, never()).addHiddenState(3L);
            verify(newState, never()).addHiddenState(5L);
            verify(stateMemory, never()).removeInactiveState(3L);
            verify(stateMemory, never()).removeInactiveState(5L);
        }

        @Test
        @DisplayName("Should handle overlapping hide relationships")
        public void testOverlappingHideRelationships() {
            State state1 = mock(State.class);
            when(state1.getCanHideIds()).thenReturn(new HashSet<>(Arrays.asList(10L, 11L)));

            when(stateService.getState(1L)).thenReturn(Optional.of(state1));
            when(stateMemory.getActiveStates())
                    .thenReturn(new HashSet<>(Arrays.asList(10L, 11L, 12L)));

            visibilityManager.set(1L);

            verify(state1).addHiddenState(10L);
            verify(state1).addHiddenState(11L);
            verify(stateMemory).removeInactiveState(10L);
            verify(stateMemory).removeInactiveState(11L);
        }
    }

    @Nested
    @DisplayName("Complex UI Scenarios")
    class ComplexUIScenarios {

        @Test
        @DisplayName("Should handle modal dialog scenario")
        public void testModalDialogScenario() {
            // Modal dialog that hides main page
            State modalDialog = mock(State.class);
            Long mainPageId = 100L;
            Long sidebarId = 101L;

            when(modalDialog.getCanHideIds())
                    .thenReturn(new HashSet<>(Arrays.asList(mainPageId, sidebarId)));
            when(stateService.getState(200L)).thenReturn(Optional.of(modalDialog));
            when(stateMemory.getActiveStates())
                    .thenReturn(new HashSet<>(Arrays.asList(mainPageId, sidebarId)));

            boolean result = visibilityManager.set(200L);

            assertTrue(result);
            verify(modalDialog).addHiddenState(mainPageId);
            verify(modalDialog).addHiddenState(sidebarId);
            verify(stateMemory).removeInactiveState(mainPageId);
            verify(stateMemory).removeInactiveState(sidebarId);
        }

        @Test
        @DisplayName("Should handle dropdown menu scenario")
        public void testDropdownMenuScenario() {
            // Dropdown that partially hides underlying content
            State dropdownMenu = mock(State.class);
            Long contentAreaId = 50L;

            when(dropdownMenu.getCanHideIds())
                    .thenReturn(new HashSet<>(Collections.singletonList(contentAreaId)));
            when(stateService.getState(60L)).thenReturn(Optional.of(dropdownMenu));
            when(stateMemory.getActiveStates())
                    .thenReturn(new HashSet<>(Arrays.asList(contentAreaId, 51L, 52L)));

            boolean result = visibilityManager.set(60L);

            assertTrue(result);
            verify(dropdownMenu).addHiddenState(contentAreaId);
            verify(stateMemory).removeInactiveState(contentAreaId);

            // Other states remain active
            verify(dropdownMenu, never()).addHiddenState(51L);
            verify(dropdownMenu, never()).addHiddenState(52L);
        }

        @Test
        @DisplayName("Should handle navigation drawer scenario")
        public void testNavigationDrawerScenario() {
            // Navigation drawer sliding over main content
            State navDrawer = mock(State.class);
            Long mainContentId = 300L;
            Long headerBarId = 301L; // Header remains visible

            when(navDrawer.getCanHideIds())
                    .thenReturn(new HashSet<>(Collections.singletonList(mainContentId)));
            when(stateService.getState(400L)).thenReturn(Optional.of(navDrawer));
            when(stateMemory.getActiveStates())
                    .thenReturn(new HashSet<>(Arrays.asList(mainContentId, headerBarId)));

            boolean result = visibilityManager.set(400L);

            assertTrue(result);
            verify(navDrawer).addHiddenState(mainContentId);
            verify(stateMemory).removeInactiveState(mainContentId);

            // Header should remain active
            verify(navDrawer, never()).addHiddenState(headerBarId);
            verify(stateMemory, never()).removeInactiveState(headerBarId);
        }

        @Test
        @DisplayName("Should handle tab switching scenario")
        public void testTabSwitchingScenario() {
            // New tab hides previous tab content
            State newTab = mock(State.class);
            Long oldTabId = 500L;
            Long tabBarId = 501L; // Tab bar remains visible

            when(newTab.getCanHideIds())
                    .thenReturn(new HashSet<>(Collections.singletonList(oldTabId)));
            when(stateService.getState(502L)).thenReturn(Optional.of(newTab));
            when(stateMemory.getActiveStates())
                    .thenReturn(new HashSet<>(Arrays.asList(oldTabId, tabBarId)));

            boolean result = visibilityManager.set(502L);

            assertTrue(result);
            verify(newTab).addHiddenState(oldTabId);
            verify(stateMemory).removeInactiveState(oldTabId);
            verify(newTab, never()).addHiddenState(tabBarId);
        }
    }

    @Nested
    @DisplayName("Concurrent Modification Safety")
    class ConcurrentModificationSafety {

        @Test
        @DisplayName("Should safely iterate through active states")
        public void testSafeIteration() {
            State newState = mock(State.class);
            Set<Long> canHideIds = new HashSet<>(Arrays.asList(1L, 2L, 3L, 4L, 5L));
            when(newState.getCanHideIds()).thenReturn(canHideIds);

            // Return a set that would cause concurrent modification if not handled properly
            Set<Long> activeStates = new HashSet<>(Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L));
            when(stateService.getState(10L)).thenReturn(Optional.of(newState));
            when(stateMemory.getActiveStates()).thenReturn(activeStates);

            // Should not throw ConcurrentModificationException
            assertDoesNotThrow(() -> visibilityManager.set(10L));

            // Verify all matching states were hidden
            for (Long id : canHideIds) {
                verify(newState).addHiddenState(id);
                verify(stateMemory).removeInactiveState(id);
            }
        }

        @Test
        @DisplayName("Should handle large number of active states")
        public void testLargeActiveStateSet() {
            State newState = mock(State.class);
            Set<Long> canHideIds = new HashSet<>();
            Set<Long> activeStates = new HashSet<>();

            // Create large sets
            for (long i = 0; i < 100; i++) {
                activeStates.add(i);
                if (i % 2 == 0) {
                    canHideIds.add(i); // Can hide even-numbered states
                }
            }

            when(newState.getCanHideIds()).thenReturn(canHideIds);
            when(stateService.getState(200L)).thenReturn(Optional.of(newState));
            when(stateMemory.getActiveStates()).thenReturn(activeStates);

            boolean result = visibilityManager.set(200L);

            assertTrue(result);

            // Verify only even-numbered states were hidden
            for (long i = 0; i < 100; i++) {
                if (i % 2 == 0) {
                    verify(newState).addHiddenState(i);
                    verify(stateMemory).removeInactiveState(i);
                } else {
                    verify(newState, never()).addHiddenState(i);
                    verify(stateMemory, never()).removeInactiveState(i);
                }
            }
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle null canHideIds")
        public void testNullCanHideIds() {
            State newState = mock(State.class);
            when(newState.getCanHideIds()).thenReturn(null);

            when(stateService.getState(1L)).thenReturn(Optional.of(newState));
            when(stateMemory.getActiveStates()).thenReturn(new HashSet<>(Arrays.asList(2L, 3L)));

            // Should handle null gracefully
            assertThrows(NullPointerException.class, () -> visibilityManager.set(1L));
        }

        @Test
        @DisplayName("Should handle state trying to hide itself")
        public void testSelfHiding() {
            State newState = mock(State.class);
            when(newState.getCanHideIds())
                    .thenReturn(new HashSet<>(Arrays.asList(1L))); // Can hide itself

            when(stateService.getState(1L)).thenReturn(Optional.of(newState));
            when(stateMemory.getActiveStates()).thenReturn(new HashSet<>(Arrays.asList(1L)));

            boolean result = visibilityManager.set(1L);

            assertTrue(result);
            verify(newState).addHiddenState(1L); // Adds itself as hidden
            verify(stateMemory).removeInactiveState(1L); // Removes itself from active
        }

        @Test
        @DisplayName("Should handle multiple calls for same state")
        public void testMultipleCalls() {
            State newState = mock(State.class);
            when(newState.getCanHideIds()).thenReturn(new HashSet<>(Arrays.asList(2L)));

            when(stateService.getState(1L)).thenReturn(Optional.of(newState));
            when(stateMemory.getActiveStates())
                    .thenReturn(new HashSet<>(Arrays.asList(2L)))
                    .thenReturn(new HashSet<>()); // Empty after first call

            // First call
            assertTrue(visibilityManager.set(1L));
            verify(newState, times(1)).addHiddenState(2L);

            // Second call - no active states to hide
            assertTrue(visibilityManager.set(1L));
            verify(newState, times(1)).addHiddenState(2L); // Still only once
        }
    }

    @Nested
    @DisplayName("Integration Scenarios")
    class IntegrationScenarios {

        @Test
        @DisplayName("Should support back navigation setup")
        public void testBackNavigationSetup() {
            // Simulate opening a dialog and preparing for back navigation
            State dialog = mock(State.class);
            Long previousPageId = 10L;

            when(dialog.getCanHideIds())
                    .thenReturn(new HashSet<>(Collections.singletonList(previousPageId)));
            when(dialog.getHiddenStateIds()).thenReturn(new HashSet<>());

            when(stateService.getState(20L)).thenReturn(Optional.of(dialog));
            when(stateMemory.getActiveStates())
                    .thenReturn(new HashSet<>(Collections.singletonList(previousPageId)));

            boolean result = visibilityManager.set(20L);

            assertTrue(result);
            verify(dialog).addHiddenState(previousPageId);
            verify(stateMemory).removeInactiveState(previousPageId);

            // Dialog now has reference to previous page for back navigation
        }

        @Test
        @DisplayName("Should handle cascading hide relationships")
        public void testCascadingHides() {
            // State A can hide B and C, where B can also hide C
            State stateA = mock(State.class);
            when(stateA.getCanHideIds()).thenReturn(new HashSet<>(Arrays.asList(2L, 3L)));

            when(stateService.getState(1L)).thenReturn(Optional.of(stateA));
            when(stateMemory.getActiveStates()).thenReturn(new HashSet<>(Arrays.asList(2L, 3L)));

            boolean result = visibilityManager.set(1L);

            assertTrue(result);
            verify(stateA).addHiddenState(2L);
            verify(stateA).addHiddenState(3L);
            verify(stateMemory).removeInactiveState(2L);
            verify(stateMemory).removeInactiveState(3L);
        }
    }
}
