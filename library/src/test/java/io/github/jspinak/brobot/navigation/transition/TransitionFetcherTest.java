package io.github.jspinak.brobot.navigation.transition;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.special.SpecialStateType;
import io.github.jspinak.brobot.model.transition.StateTransition;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.navigation.service.StateTransitionService;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for TransitionFetcher.
 * Tests retrieval and packaging of transition components.
 */
@DisplayName("TransitionFetcher Tests")
@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Test incompatible with CI environment")
class TransitionFetcherTest extends BrobotTestBase {
    
    @Mock
    private StateMemory stateMemory;
    
    @Mock
    private StateService stateService;
    
    @Mock
    private StateTransitionService stateTransitionService;
    
    @Mock
    private TransitionConditionPackager conditionPackager;
    
    @Mock
    private StateTransitions mockFromTransitions;
    
    @Mock
    private StateTransitions mockToTransitions;
    
    @Mock
    private StateTransition mockFromTransition;
    
    @Mock
    private StateTransition mockToTransition;
    
    @Mock
    private State mockFromState;
    
    @Mock
    private State mockToState;
    
    @Mock
    private BooleanSupplier mockBooleanSupplier;
    
    private TransitionFetcher transitionFetcher;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        
        transitionFetcher = new TransitionFetcher(
            stateMemory, 
            stateService, 
            stateTransitionService, 
            conditionPackager
        );
    }
    
    @Nested
    @DisplayName("Complete Transition Fetching")
    class CompleteTransitionFetching {
        
        @Test
        @DisplayName("Should fetch all transition components successfully")
        void testFetchAllComponentsSuccess() {
            // Arrange
            Long fromId = 1L;
            Long toId = 2L;
            
            setupSuccessfulFetch(fromId, toId, toId);
            
            // Act
            Optional<TransitionFetcher> result = transitionFetcher.getTransitions(fromId, toId);
            
            // Assert
            assertTrue(result.isPresent());
            TransitionFetcher fetcher = result.get();
            
            assertNotNull(fetcher.getFromTransitions());
            assertNotNull(fetcher.getFromTransition());
            assertNotNull(fetcher.getFromState());
            assertNotNull(fetcher.getFromTransitionFunction());
            assertNotNull(fetcher.getToTransitions());
            assertNotNull(fetcher.getToTransition());
            assertNotNull(fetcher.getToState());
            assertEquals(toId, fetcher.getTransitionToEnum());
        }
        
        @Test
        @DisplayName("Should handle PREVIOUS state transitions")
        void testHandlePreviousStateTransition() {
            // Arrange
            Long fromId = 3L;
            Long previousId = SpecialStateType.PREVIOUS.getId();
            Long resolvedId = 2L; // PREVIOUS resolves to this
            
            setupSuccessfulFetch(fromId, previousId, resolvedId);
            
            // Act
            Optional<TransitionFetcher> result = transitionFetcher.getTransitions(fromId, previousId);
            
            // Assert
            assertTrue(result.isPresent());
            assertEquals(resolvedId, result.get().getTransitionToEnum());
        }
        
        @Test
        @DisplayName("Should reset state between fetches")
        void testResetBetweenFetches() {
            // Arrange - First fetch
            Long fromId1 = 1L;
            Long toId1 = 2L;
            setupSuccessfulFetch(fromId1, toId1, toId1);
            
            // Act - First fetch
            Optional<TransitionFetcher> result1 = transitionFetcher.getTransitions(fromId1, toId1);
            assertTrue(result1.isPresent());
            
            // Arrange - Second fetch with different IDs
            Long fromId2 = 3L;
            Long toId2 = 4L;
            setupSuccessfulFetch(fromId2, toId2, toId2);
            
            // Act - Second fetch
            Optional<TransitionFetcher> result2 = transitionFetcher.getTransitions(fromId2, toId2);
            
            // Assert - Should have new values, not old ones
            assertTrue(result2.isPresent());
            assertEquals(toId2, result2.get().getTransitionToEnum());
        }
    }
    
    @Nested
    @DisplayName("Incomplete Transition Fetching")
    class IncompleteTransitionFetching {
        
        @Test
        @DisplayName("Should return empty when from transitions not found")
        void testMissingFromTransitions() {
            // Arrange
            Long fromId = 1L;
            Long toId = 2L;
            
            when(stateTransitionService.getTransitions(fromId)).thenReturn(Optional.empty());
            when(stateTransitionService.getTransitions(toId)).thenReturn(Optional.of(mockToTransitions));
            when(stateService.getState(toId)).thenReturn(Optional.of(mockToState));
            when(mockToTransitions.getTransitionFinish()).thenReturn(mockToTransition);
            
            // Act
            Optional<TransitionFetcher> result = transitionFetcher.getTransitions(fromId, toId);
            
            // Assert
            assertFalse(result.isPresent());
        }
        
        @Test
        @DisplayName("Should return empty when to transitions not found")
        void testMissingToTransitions() {
            // Arrange
            Long fromId = 1L;
            Long toId = 2L;
            
            when(stateTransitionService.getTransitions(fromId)).thenReturn(Optional.of(mockFromTransitions));
            when(stateService.getState(fromId)).thenReturn(Optional.of(mockFromState));
            when(stateTransitionService.getTransitionToEnum(fromId, toId)).thenReturn(toId);
            when(mockFromTransitions.getTransitionFunctionByActivatedStateId(toId))
                .thenReturn(Optional.of(mockFromTransition));
            when(conditionPackager.toBooleanSupplier(mockFromTransition)).thenReturn(mockBooleanSupplier);
            
            when(stateTransitionService.getTransitions(toId)).thenReturn(Optional.empty());
            
            // Act
            Optional<TransitionFetcher> result = transitionFetcher.getTransitions(fromId, toId);
            
            // Assert
            assertFalse(result.isPresent());
        }
        
        @Test
        @DisplayName("Should return empty when from state not found")
        void testMissingFromState() {
            // Arrange
            Long fromId = 1L;
            Long toId = 2L;
            
            when(stateTransitionService.getTransitions(fromId)).thenReturn(Optional.of(mockFromTransitions));
            when(stateService.getState(fromId)).thenReturn(Optional.empty()); // Missing from state
            when(stateTransitionService.getTransitionToEnum(fromId, toId)).thenReturn(toId);
            when(mockFromTransitions.getTransitionFunctionByActivatedStateId(toId))
                .thenReturn(Optional.of(mockFromTransition));
            when(conditionPackager.toBooleanSupplier(mockFromTransition)).thenReturn(mockBooleanSupplier);
            
            when(stateTransitionService.getTransitions(toId)).thenReturn(Optional.of(mockToTransitions));
            when(stateService.getState(toId)).thenReturn(Optional.of(mockToState));
            when(mockToTransitions.getTransitionFinish()).thenReturn(mockToTransition);
            
            // Act
            Optional<TransitionFetcher> result = transitionFetcher.getTransitions(fromId, toId);
            
            // Assert
            assertFalse(result.isPresent());
        }
        
        @Test
        @DisplayName("Should return empty when to state not found")
        void testMissingToState() {
            // Arrange
            Long fromId = 1L;
            Long toId = 2L;
            
            setupSuccessfulFromFetch(fromId, toId);
            
            when(stateTransitionService.getTransitions(toId)).thenReturn(Optional.of(mockToTransitions));
            when(stateService.getState(toId)).thenReturn(Optional.empty()); // Missing to state
            when(mockToTransitions.getTransitionFinish()).thenReturn(mockToTransition);
            
            // Act
            Optional<TransitionFetcher> result = transitionFetcher.getTransitions(fromId, toId);
            
            // Assert
            assertFalse(result.isPresent());
        }
        
        @Test
        @DisplayName("Should return empty when from transition not found")
        void testMissingFromTransition() {
            // Arrange
            Long fromId = 1L;
            Long toId = 2L;
            
            when(stateTransitionService.getTransitions(fromId)).thenReturn(Optional.of(mockFromTransitions));
            when(stateService.getState(fromId)).thenReturn(Optional.of(mockFromState));
            when(stateTransitionService.getTransitionToEnum(fromId, toId)).thenReturn(toId);
            when(mockFromTransitions.getTransitionFunctionByActivatedStateId(toId))
                .thenReturn(Optional.empty()); // Missing from transition
            
            when(stateTransitionService.getTransitions(toId)).thenReturn(Optional.of(mockToTransitions));
            when(stateService.getState(toId)).thenReturn(Optional.of(mockToState));
            when(mockToTransitions.getTransitionFinish()).thenReturn(mockToTransition);
            
            // Act
            Optional<TransitionFetcher> result = transitionFetcher.getTransitions(fromId, toId);
            
            // Assert
            assertFalse(result.isPresent());
        }
        
        @Test
        @DisplayName("Should return empty when transition finish is null")
        void testNullTransitionFinish() {
            // Arrange
            Long fromId = 1L;
            Long toId = 2L;
            
            setupSuccessfulFromFetch(fromId, toId);
            
            when(stateTransitionService.getTransitions(toId)).thenReturn(Optional.of(mockToTransitions));
            when(stateService.getState(toId)).thenReturn(Optional.of(mockToState));
            when(mockToTransitions.getTransitionFinish()).thenReturn(null); // Null transition finish
            
            // Act
            Optional<TransitionFetcher> result = transitionFetcher.getTransitions(fromId, toId);
            
            // Assert
            assertFalse(result.isPresent());
        }
        
        @Test
        @DisplayName("Should return empty when transition resolves to null")
        void testTransitionResolvesToNull() {
            // Arrange
            Long fromId = 1L;
            Long toId = 2L;
            
            when(stateTransitionService.getTransitions(fromId)).thenReturn(Optional.of(mockFromTransitions));
            when(stateService.getState(fromId)).thenReturn(Optional.of(mockFromState));
            when(stateTransitionService.getTransitionToEnum(fromId, toId)).thenReturn(null);
            
            // When transitionToEnum is null, getTransitionFunctionByActivatedStateId(null) returns empty
            when(mockFromTransitions.getTransitionFunctionByActivatedStateId(null))
                .thenReturn(Optional.empty());
            
            when(stateTransitionService.getTransitions(toId)).thenReturn(Optional.of(mockToTransitions));
            when(stateService.getState(toId)).thenReturn(Optional.of(mockToState));
            when(mockToTransitions.getTransitionFinish()).thenReturn(mockToTransition);
            
            // Act
            Optional<TransitionFetcher> result = transitionFetcher.getTransitions(fromId, toId);
            
            // Assert - Should be empty because transitionToEnum is null
            assertFalse(result.isPresent());
        }
    }
    
    @Nested
    @DisplayName("Getter Methods")
    class GetterMethods {
        
        @Test
        @DisplayName("Should provide access to all fetched components")
        void testGetterMethods() {
            // Arrange
            Long fromId = 5L;
            Long toId = 6L;
            
            setupSuccessfulFetch(fromId, toId, toId);
            
            // Act
            Optional<TransitionFetcher> result = transitionFetcher.getTransitions(fromId, toId);
            
            // Assert
            assertTrue(result.isPresent());
            TransitionFetcher fetcher = result.get();
            
            assertEquals(mockFromTransitions, fetcher.getFromTransitions());
            assertEquals(mockFromTransition, fetcher.getFromTransition());
            assertEquals(mockFromState, fetcher.getFromState());
            assertEquals(mockBooleanSupplier, fetcher.getFromTransitionFunction());
            assertEquals(mockToTransitions, fetcher.getToTransitions());
            assertEquals(mockToTransition, fetcher.getToTransition());
            assertEquals(mockToState, fetcher.getToState());
            assertEquals(toId, fetcher.getTransitionToEnum());
        }
    }
    
    // Helper methods
    
    private void setupSuccessfulFetch(Long fromId, Long toId, Long resolvedToId) {
        setupSuccessfulFromFetch(fromId, resolvedToId);
        setupSuccessfulToFetch(toId);
    }
    
    private void setupSuccessfulFromFetch(Long fromId, Long toId) {
        when(stateTransitionService.getTransitions(fromId)).thenReturn(Optional.of(mockFromTransitions));
        when(stateService.getState(fromId)).thenReturn(Optional.of(mockFromState));
        when(stateTransitionService.getTransitionToEnum(anyLong(), anyLong())).thenReturn(toId);
        when(mockFromTransitions.getTransitionFunctionByActivatedStateId(toId))
            .thenReturn(Optional.of(mockFromTransition));
        when(conditionPackager.toBooleanSupplier(mockFromTransition)).thenReturn(mockBooleanSupplier);
    }
    
    private void setupSuccessfulToFetch(Long toId) {
        when(stateTransitionService.getTransitions(toId)).thenReturn(Optional.of(mockToTransitions));
        when(stateService.getState(toId)).thenReturn(Optional.of(mockToState));
        when(mockToTransitions.getTransitionFinish()).thenReturn(mockToTransition);
    }
}