package io.github.jspinak.brobot.action.basic.find;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Find Tests")
class FindTest extends BrobotTestBase {

    private Find find;
    
    @Mock
    private FindPipeline findPipeline;
    
    @Mock
    private ObjectCollection objectCollection;
    
    // Only keep the mocks we actually use
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        find = new Find(findPipeline);
    }
    
    @Test
    @DisplayName("Should perform find operation using pipeline")
    void testFind_WithBaseFindOptions() {
        // Arrange
        BaseFindOptions findOptions = mock(BaseFindOptions.class);
        ActionResult actionResult = new ActionResult(findOptions);
        
        // Act
        find.perform(actionResult, objectCollection);
        
        // Assert
        verify(findPipeline).execute(findOptions, actionResult, objectCollection);
    }
    
    @Test
    @DisplayName("Should handle multiple object collections")
    void testFind_MultipleCollections() {
        // Arrange
        ObjectCollection collection1 = mock(ObjectCollection.class);
        ObjectCollection collection2 = mock(ObjectCollection.class);
        BaseFindOptions findOptions = mock(BaseFindOptions.class);
        ActionResult actionResult = new ActionResult(findOptions);
        
        // Act
        find.perform(actionResult, collection1, collection2);
        
        // Assert
        verify(findPipeline).execute(findOptions, actionResult, collection1, collection2);
    }
    
    @Test
    @DisplayName("Should throw exception for invalid ActionConfig")
    void testFind_InvalidActionConfig() {
        // Arrange
        ActionConfig invalidConfig = mock(ActionConfig.class);
        ActionResult actionResult = new ActionResult(invalidConfig);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            find.perform(actionResult, objectCollection)
        );
    }
    
    @Test
    @DisplayName("Should get correct action type")
    void testGetActionType() {
        // Act
        ActionInterface.Type actionType = find.getActionType();
        
        // Assert
        assertEquals(ActionInterface.Type.FIND, actionType);
    }
    
}