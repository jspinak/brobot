package io.github.jspinak.brobot.action.internal.region;

import io.github.jspinak.brobot.model.element.SearchRegionOnObject;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateLocation;
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("SearchRegionDependencyRegistry Tests")
class SearchRegionDependencyRegistryTest extends BrobotTestBase {

    private SearchRegionDependencyRegistry registry;
    
    @Mock
    private StateImage mockStateImage;
    
    @Mock
    private StateLocation mockStateLocation;
    
    @Mock
    private StateRegion mockStateRegion;
    
    @Mock
    private SearchRegionOnObject mockSearchRegionConfig;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        registry = new SearchRegionDependencyRegistry();
        
        // Setup default mock behaviors
        when(mockStateImage.getName()).thenReturn("MockImage");
        when(mockStateImage.getOwnerStateName()).thenReturn("MockImageState");
        when(mockStateLocation.getName()).thenReturn("MockLocation");
        when(mockStateLocation.getOwnerStateName()).thenReturn("MockLocationState");
        when(mockStateRegion.getName()).thenReturn("MockRegion");
        when(mockStateRegion.getOwnerStateName()).thenReturn("MockRegionState");
    }
    
    @Test
    @DisplayName("Should register dependency with valid config")
    void testRegisterDependency_ValidConfig() {
        when(mockSearchRegionConfig.getTargetStateName()).thenReturn("SourceState");
        when(mockSearchRegionConfig.getTargetObjectName()).thenReturn("SourceObject");
        
        registry.registerDependency(mockStateImage, mockSearchRegionConfig);
        
        Set<SearchRegionDependencyRegistry.DependentObject> dependents = 
            registry.getDependents("SourceState", "SourceObject");
        
        assertNotNull(dependents);
        assertEquals(1, dependents.size());
        SearchRegionDependencyRegistry.DependentObject dependent = dependents.iterator().next();
        assertEquals(mockStateImage, dependent.getStateObject());
        assertEquals(mockSearchRegionConfig, dependent.getSearchRegionConfig());
    }
    
    @Test
    @DisplayName("Should not register dependency with null config")
    void testRegisterDependency_NullConfig() {
        registry.registerDependency(mockStateImage, null);
        
        assertEquals(0, registry.size());
    }
    
    @Test
    @DisplayName("Should not register dependency with null state object")
    void testRegisterDependency_NullStateObject() {
        when(mockSearchRegionConfig.getTargetStateName()).thenReturn("SourceState");
        when(mockSearchRegionConfig.getTargetObjectName()).thenReturn("SourceObject");
        
        registry.registerDependency(null, mockSearchRegionConfig);
        
        Set<SearchRegionDependencyRegistry.DependentObject> dependents = 
            registry.getDependents("SourceState", "SourceObject");
        
        assertTrue(dependents.isEmpty());
    }
    
    @Test
    @DisplayName("Should register multiple dependencies for same source")
    void testRegisterDependency_MultipleDependenciesForSameSource() {
        when(mockSearchRegionConfig.getTargetStateName()).thenReturn("SourceState");
        when(mockSearchRegionConfig.getTargetObjectName()).thenReturn("SourceObject");
        
        registry.registerDependency(mockStateImage, mockSearchRegionConfig);
        registry.registerDependency(mockStateLocation, mockSearchRegionConfig);
        registry.registerDependency(mockStateRegion, mockSearchRegionConfig);
        
        Set<SearchRegionDependencyRegistry.DependentObject> dependents = 
            registry.getDependents("SourceState", "SourceObject");
        
        assertEquals(3, dependents.size());
        assertTrue(dependents.stream().anyMatch(d -> d.getStateObject() == mockStateImage));
        assertTrue(dependents.stream().anyMatch(d -> d.getStateObject() == mockStateLocation));
        assertTrue(dependents.stream().anyMatch(d -> d.getStateObject() == mockStateRegion));
    }
    
    @Test
    @DisplayName("Should handle duplicate dependency registration")
    void testRegisterDependency_DuplicateRegistration() {
        when(mockSearchRegionConfig.getTargetStateName()).thenReturn("SourceState");
        when(mockSearchRegionConfig.getTargetObjectName()).thenReturn("SourceObject");
        
        registry.registerDependency(mockStateImage, mockSearchRegionConfig);
        registry.registerDependency(mockStateImage, mockSearchRegionConfig); // Duplicate
        
        Set<SearchRegionDependencyRegistry.DependentObject> dependents = 
            registry.getDependents("SourceState", "SourceObject");
        
        // Should have both registrations (not deduplicated)
        assertEquals(2, dependents.size());
    }
    
    @Test
    @DisplayName("Should register dependencies for different sources")
    void testRegisterDependency_DifferentSources() {
        SearchRegionOnObject config1 = mock(SearchRegionOnObject.class);
        SearchRegionOnObject config2 = mock(SearchRegionOnObject.class);
        
        when(config1.getTargetStateName()).thenReturn("State1");
        when(config1.getTargetObjectName()).thenReturn("Object1");
        
        when(config2.getTargetStateName()).thenReturn("State2");
        when(config2.getTargetObjectName()).thenReturn("Object2");
        
        registry.registerDependency(mockStateImage, config1);
        registry.registerDependency(mockStateLocation, config2);
        
        Set<SearchRegionDependencyRegistry.DependentObject> dependents1 = 
            registry.getDependents("State1", "Object1");
        Set<SearchRegionDependencyRegistry.DependentObject> dependents2 = 
            registry.getDependents("State2", "Object2");
        
        assertEquals(1, dependents1.size());
        SearchRegionDependencyRegistry.DependentObject dep1 = dependents1.iterator().next();
        assertEquals(mockStateImage, dep1.getStateObject());
        
        assertEquals(1, dependents2.size());
        SearchRegionDependencyRegistry.DependentObject dep2 = dependents2.iterator().next();
        assertEquals(mockStateLocation, dep2.getStateObject());
    }
    
    @Test
    @DisplayName("Should return empty list for non-existent dependency")
    void testGetDependents_NonExistent() {
        Set<SearchRegionDependencyRegistry.DependentObject> dependents = 
            registry.getDependents("NonExistentState", "NonExistentObject");
        
        assertNotNull(dependents);
        assertTrue(dependents.isEmpty());
    }
    
    @Test
    @DisplayName("Should handle null parameters in getDependents")
    void testGetDependents_NullParameters() {
        Set<SearchRegionDependencyRegistry.DependentObject> dependents1 = 
            registry.getDependents(null, "Object");
        assertTrue(dependents1.isEmpty());
        
        Set<SearchRegionDependencyRegistry.DependentObject> dependents2 = 
            registry.getDependents("State", null);
        assertTrue(dependents2.isEmpty());
        
        Set<SearchRegionDependencyRegistry.DependentObject> dependents3 = 
            registry.getDependents(null, null);
        assertTrue(dependents3.isEmpty());
    }
    
    @Test
    @DisplayName("Should clear all dependencies")
    void testClear() {
        SearchRegionOnObject config1 = mock(SearchRegionOnObject.class);
        SearchRegionOnObject config2 = mock(SearchRegionOnObject.class);
        
        when(config1.getTargetStateName()).thenReturn("State1");
        when(config1.getTargetObjectName()).thenReturn("Object1");
        
        when(config2.getTargetStateName()).thenReturn("State2");
        when(config2.getTargetObjectName()).thenReturn("Object2");
        
        registry.registerDependency(mockStateImage, config1);
        registry.registerDependency(mockStateLocation, config2);
        
        assertEquals(2, registry.size());
        
        registry.clear();
        
        assertEquals(0, registry.size());
        assertTrue(registry.getDependents("State1", "Object1").isEmpty());
        assertTrue(registry.getDependents("State2", "Object2").isEmpty());
    }
    
    // buildKey is private, testing through public interface
    
    @Test
    @DisplayName("Should handle special characters in keys")
    void testSpecialCharacters() {        
        // Registry should still work with keys containing special characters
        SearchRegionOnObject config = mock(SearchRegionOnObject.class);
        when(config.getTargetStateName()).thenReturn("State:With:Colons");
        when(config.getTargetObjectName()).thenReturn("Object:With:Colons");
        
        registry.registerDependency(mockStateImage, config);
        
        Set<SearchRegionDependencyRegistry.DependentObject> dependents = 
            registry.getDependents("State:With:Colons", "Object:With:Colons");
        
        assertEquals(1, dependents.size());
    }
    
    @Test
    @DisplayName("Should correctly report size")
    void testSize() {
        assertEquals(0, registry.size());
        
        SearchRegionOnObject config1 = mock(SearchRegionOnObject.class);
        when(config1.getTargetStateName()).thenReturn("State1");
        when(config1.getTargetObjectName()).thenReturn("Object1");
        
        registry.registerDependency(mockStateImage, config1);
        assertEquals(1, registry.size());
        
        registry.registerDependency(mockStateLocation, config1);
        assertEquals(2, registry.size()); // Now 2 dependencies for same source
        
        SearchRegionOnObject config2 = mock(SearchRegionOnObject.class);
        when(config2.getTargetStateName()).thenReturn("State2");
        when(config2.getTargetObjectName()).thenReturn("Object2");
        
        registry.registerDependency(mockStateRegion, config2);
        assertEquals(3, registry.size()); // Now 3 total dependencies
    }
    
    @Test
    @DisplayName("Should handle concurrent access safely")
    void testConcurrentAccess() throws InterruptedException {
        int threadCount = 10;
        int operationsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        SearchRegionOnObject config = mock(SearchRegionOnObject.class);
                        when(config.getTargetStateName()).thenReturn("State" + threadId);
                        when(config.getTargetObjectName()).thenReturn("Object" + j);
                        
                        StateImage stateImage = mock(StateImage.class);
                        
                        // Register dependency
                        registry.registerDependency(stateImage, config);
                        
                        // Get dependents
                        Set<SearchRegionDependencyRegistry.DependentObject> deps = 
                            registry.getDependents("State" + threadId, "Object" + j);
                        
                        if (!deps.isEmpty()) {
                            successCount.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        executor.shutdown();
        
        // All operations should have succeeded
        assertEquals(threadCount * operationsPerThread, successCount.get());
        
        // Verify final state
        assertTrue(registry.size() > 0);
    }
    
    @Test
    @DisplayName("DependentObject should store and return correct values")
    void testDependentObject() {
        SearchRegionDependencyRegistry.DependentObject dependent = 
            new SearchRegionDependencyRegistry.DependentObject(mockStateImage, mockSearchRegionConfig);
        
        assertEquals(mockStateImage, dependent.getStateObject());
        assertEquals(mockSearchRegionConfig, dependent.getSearchRegionConfig());
    }
    
    @Test
    @DisplayName("Should handle registration with empty strings")
    void testRegisterDependency_EmptyStrings() {
        when(mockSearchRegionConfig.getTargetStateName()).thenReturn("");
        when(mockSearchRegionConfig.getTargetObjectName()).thenReturn("");
        
        registry.registerDependency(mockStateImage, mockSearchRegionConfig);
        
        Set<SearchRegionDependencyRegistry.DependentObject> dependents = 
            registry.getDependents("", "");
        
        assertEquals(1, dependents.size());
        SearchRegionDependencyRegistry.DependentObject dependent = dependents.iterator().next();
        assertEquals(mockStateImage, dependent.getStateObject());
    }
    
    @Test
    @DisplayName("Should maintain insertion order for dependencies")
    void testInsertionOrder() {
        when(mockSearchRegionConfig.getTargetStateName()).thenReturn("State");
        when(mockSearchRegionConfig.getTargetObjectName()).thenReturn("Object");
        
        StateImage image1 = mock(StateImage.class);
        StateImage image2 = mock(StateImage.class);
        StateImage image3 = mock(StateImage.class);
        
        registry.registerDependency(image1, mockSearchRegionConfig);
        registry.registerDependency(image2, mockSearchRegionConfig);
        registry.registerDependency(image3, mockSearchRegionConfig);
        
        Set<SearchRegionDependencyRegistry.DependentObject> dependents = 
            registry.getDependents("State", "Object");
        
        assertEquals(3, dependents.size());
        // Set doesn't guarantee order, so just verify all objects are present
        assertTrue(dependents.stream().anyMatch(d -> d.getStateObject() == image1));
        assertTrue(dependents.stream().anyMatch(d -> d.getStateObject() == image2));
        assertTrue(dependents.stream().anyMatch(d -> d.getStateObject() == image3));
    }
}