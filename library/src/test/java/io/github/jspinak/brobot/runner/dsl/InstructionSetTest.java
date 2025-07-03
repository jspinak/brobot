package io.github.jspinak.brobot.runner.dsl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InstructionSetTest {

    private InstructionSet instructionSet;
    
    @BeforeEach
    void setUp() {
        instructionSet = new InstructionSet();
    }
    
    @Test
    void testDefaultConstructor() {
        // Verify
        assertNotNull(instructionSet);
        assertNull(instructionSet.getAutomationFunctions());
    }
    
    @Test
    void testSetAndGetAutomationFunctions() {
        // Setup
        List<BusinessTask> functions = new ArrayList<>();
        BusinessTask task1 = mock(BusinessTask.class);
        BusinessTask task2 = mock(BusinessTask.class);
        functions.add(task1);
        functions.add(task2);
        
        // Execute
        instructionSet.setAutomationFunctions(functions);
        
        // Verify
        assertEquals(functions, instructionSet.getAutomationFunctions());
        assertEquals(2, instructionSet.getAutomationFunctions().size());
        assertTrue(instructionSet.getAutomationFunctions().contains(task1));
        assertTrue(instructionSet.getAutomationFunctions().contains(task2));
    }
    
    @Test
    void testSetAutomationFunctions_EmptyList() {
        // Setup
        List<BusinessTask> emptyList = new ArrayList<>();
        
        // Execute
        instructionSet.setAutomationFunctions(emptyList);
        
        // Verify
        assertNotNull(instructionSet.getAutomationFunctions());
        assertTrue(instructionSet.getAutomationFunctions().isEmpty());
    }
    
    @Test
    void testSetAutomationFunctions_Null() {
        // Execute
        instructionSet.setAutomationFunctions(null);
        
        // Verify
        assertNull(instructionSet.getAutomationFunctions());
    }
    
    @Test
    void testDataAnnotation_EqualsHashCode() {
        // Setup
        InstructionSet set1 = new InstructionSet();
        InstructionSet set2 = new InstructionSet();
        
        List<BusinessTask> functions = Arrays.asList(mock(BusinessTask.class));
        set1.setAutomationFunctions(functions);
        set2.setAutomationFunctions(functions);
        
        // Verify equals
        assertEquals(set1, set2);
        assertEquals(set1.hashCode(), set2.hashCode());
        
        // Verify not equals when different
        set2.setAutomationFunctions(new ArrayList<>());
        assertNotEquals(set1, set2);
    }
    
    @Test
    void testDataAnnotation_ToString() {
        // Setup
        List<BusinessTask> functions = new ArrayList<>();
        BusinessTask task = mock(BusinessTask.class);
        when(task.toString()).thenReturn("MockTask");
        functions.add(task);
        instructionSet.setAutomationFunctions(functions);
        
        // Execute
        String toString = instructionSet.toString();
        
        // Verify
        assertNotNull(toString);
        assertTrue(toString.contains("InstructionSet"));
        assertTrue(toString.contains("automationFunctions"));
    }
    
    @Test
    void testModifyingReturnedList() {
        // Setup
        List<BusinessTask> originalList = new ArrayList<>();
        BusinessTask task1 = mock(BusinessTask.class);
        originalList.add(task1);
        instructionSet.setAutomationFunctions(originalList);
        
        // Execute - modify returned list
        List<BusinessTask> returnedList = instructionSet.getAutomationFunctions();
        BusinessTask task2 = mock(BusinessTask.class);
        returnedList.add(task2);
        
        // Verify - changes are reflected
        assertEquals(2, instructionSet.getAutomationFunctions().size());
        assertTrue(instructionSet.getAutomationFunctions().contains(task2));
    }
    
    @Test
    void testJsonIgnoreProperties() {
        // This test verifies the class has the @JsonIgnoreProperties annotation
        // The actual behavior is tested via Jackson serialization in integration tests
        assertTrue(instructionSet.getClass().isAnnotationPresent(
            com.fasterxml.jackson.annotation.JsonIgnoreProperties.class));
        
        com.fasterxml.jackson.annotation.JsonIgnoreProperties annotation = 
            instructionSet.getClass().getAnnotation(
                com.fasterxml.jackson.annotation.JsonIgnoreProperties.class);
        assertTrue(annotation.ignoreUnknown());
    }
}