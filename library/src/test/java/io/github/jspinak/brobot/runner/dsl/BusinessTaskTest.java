package io.github.jspinak.brobot.runner.dsl;

import io.github.jspinak.brobot.runner.dsl.model.Parameter;
import io.github.jspinak.brobot.runner.dsl.statements.Statement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BusinessTaskTest {

    private BusinessTask businessTask;
    
    @BeforeEach
    void setUp() {
        businessTask = new BusinessTask();
    }
    
    @Test
    void testDefaultConstructor() {
        // Verify
        assertNotNull(businessTask);
        assertNull(businessTask.getId());
        assertNull(businessTask.getName());
        assertNull(businessTask.getDescription());
        assertNull(businessTask.getReturnType());
        assertNull(businessTask.getParameters());
        assertNull(businessTask.getStatements());
    }
    
    @Test
    void testSetAndGetId() {
        // Execute
        businessTask.setId(42);
        
        // Verify
        assertEquals(42, businessTask.getId());
    }
    
    @Test
    void testSetAndGetName() {
        // Execute
        businessTask.setName("processOrder");
        
        // Verify
        assertEquals("processOrder", businessTask.getName());
    }
    
    @Test
    void testSetAndGetDescription() {
        // Execute
        businessTask.setDescription("Processes customer orders");
        
        // Verify
        assertEquals("Processes customer orders", businessTask.getDescription());
    }
    
    @Test
    void testSetAndGetReturnType() {
        // Execute
        businessTask.setReturnType("boolean");
        
        // Verify
        assertEquals("boolean", businessTask.getReturnType());
    }
    
    @Test
    void testSetAndGetParameters() {
        // Setup
        List<Parameter> params = new ArrayList<>();
        Parameter param1 = mock(Parameter.class);
        Parameter param2 = mock(Parameter.class);
        params.add(param1);
        params.add(param2);
        
        // Execute
        businessTask.setParameters(params);
        
        // Verify
        assertEquals(params, businessTask.getParameters());
        assertEquals(2, businessTask.getParameters().size());
    }
    
    @Test
    void testSetAndGetStatements() {
        // Setup
        List<Statement> statements = new ArrayList<>();
        Statement stmt1 = mock(Statement.class);
        Statement stmt2 = mock(Statement.class);
        statements.add(stmt1);
        statements.add(stmt2);
        
        // Execute
        businessTask.setStatements(statements);
        
        // Verify
        assertEquals(statements, businessTask.getStatements());
        assertEquals(2, businessTask.getStatements().size());
    }
    
    @Test
    void testFullyPopulatedTask() {
        // Setup
        businessTask.setId(1);
        businessTask.setName("validateInput");
        businessTask.setDescription("Validates user input");
        businessTask.setReturnType("string");
        businessTask.setParameters(Arrays.asList(mock(Parameter.class)));
        businessTask.setStatements(Arrays.asList(mock(Statement.class)));
        
        // Verify
        assertEquals(1, businessTask.getId());
        assertEquals("validateInput", businessTask.getName());
        assertEquals("Validates user input", businessTask.getDescription());
        assertEquals("string", businessTask.getReturnType());
        assertEquals(1, businessTask.getParameters().size());
        assertEquals(1, businessTask.getStatements().size());
    }
    
    @Test
    void testDataAnnotation_EqualsHashCode() {
        // Setup
        BusinessTask task1 = new BusinessTask();
        BusinessTask task2 = new BusinessTask();
        
        task1.setId(1);
        task1.setName("test");
        task2.setId(1);
        task2.setName("test");
        
        // Verify equals
        assertEquals(task1, task2);
        assertEquals(task1.hashCode(), task2.hashCode());
        
        // Verify not equals when different
        task2.setName("different");
        assertNotEquals(task1, task2);
    }
    
    @Test
    void testDataAnnotation_ToString() {
        // Setup
        businessTask.setId(100);
        businessTask.setName("myFunction");
        businessTask.setDescription("Test function");
        businessTask.setReturnType("void");
        
        // Execute
        String toString = businessTask.toString();
        
        // Verify
        assertNotNull(toString);
        assertTrue(toString.contains("BusinessTask"));
        assertTrue(toString.contains("id=100"));
        assertTrue(toString.contains("name=myFunction"));
        assertTrue(toString.contains("description=Test function"));
        assertTrue(toString.contains("returnType=void"));
    }
    
    @Test
    void testNullValues() {
        // Setup - all fields are null by default
        
        // Execute
        businessTask.setId(null);
        businessTask.setName(null);
        businessTask.setDescription(null);
        businessTask.setReturnType(null);
        businessTask.setParameters(null);
        businessTask.setStatements(null);
        
        // Verify - should handle nulls gracefully
        assertNull(businessTask.getId());
        assertNull(businessTask.getName());
        assertNull(businessTask.getDescription());
        assertNull(businessTask.getReturnType());
        assertNull(businessTask.getParameters());
        assertNull(businessTask.getStatements());
    }
    
    @Test
    void testEmptyCollections() {
        // Setup
        businessTask.setParameters(new ArrayList<>());
        businessTask.setStatements(new ArrayList<>());
        
        // Verify
        assertNotNull(businessTask.getParameters());
        assertNotNull(businessTask.getStatements());
        assertTrue(businessTask.getParameters().isEmpty());
        assertTrue(businessTask.getStatements().isEmpty());
    }
    
    @Test
    void testJsonIgnoreProperties() {
        // This test verifies the class has the @JsonIgnoreProperties annotation
        assertTrue(businessTask.getClass().isAnnotationPresent(
            com.fasterxml.jackson.annotation.JsonIgnoreProperties.class));
        
        com.fasterxml.jackson.annotation.JsonIgnoreProperties annotation = 
            businessTask.getClass().getAnnotation(
                com.fasterxml.jackson.annotation.JsonIgnoreProperties.class);
        assertTrue(annotation.ignoreUnknown());
    }
}