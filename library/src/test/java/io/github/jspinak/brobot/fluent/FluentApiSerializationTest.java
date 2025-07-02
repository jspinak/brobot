package io.github.jspinak.brobot.fluent;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateString;
import io.github.jspinak.brobot.runner.dsl.InstructionSet;
import io.github.jspinak.brobot.runner.dsl.BusinessTask;
import io.github.jspinak.brobot.runner.dsl.model.ActionStep;
import io.github.jspinak.brobot.runner.dsl.model.TaskSequence;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests to verify that the fluent API produces DSL objects that can be
 * serialized to JSON and are compatible with the existing DSL structure.
 */
public class FluentApiSerializationTest {
    
    private ObjectMapper mapper;
    private StateImage testImage;
    private StateString testString;
    
    @BeforeEach
    public void setup() {
        mapper = new ObjectMapper();
        
        // Create test state objects
        testImage = new StateImage();
        testImage.setName("testButton");
        
        testString = new StateString.InNullState().withString("test text");
    }
    
    @Test
    public void testSimpleSequenceSerializable() throws Exception {
        // Create a simple sequence using the fluent API
        InstructionSet instructionSet = Brobot.buildSequence()
            .withName("testSequence")
            .withDescription("Test sequence for serialization")
            .find(testImage)
            .thenClick()
            .build();
        
        // Serialize to JSON
        String json = mapper.writeValueAsString(instructionSet);
        assertNotNull(json);
        assertTrue(json.contains("testSequence"));
        
        // Deserialize back
        InstructionSet deserialized = mapper.readValue(json, InstructionSet.class);
        assertNotNull(deserialized);
        assertNotNull(deserialized.getAutomationFunctions());
        assertEquals(1, deserialized.getAutomationFunctions().size());
        
        BusinessTask task = deserialized.getAutomationFunctions().get(0);
        assertEquals("testSequence", task.getName());
        assertEquals("Test sequence for serialization", task.getDescription());
    }
    
    @Test
    public void testTaskSequenceStructure() {
        // Create a sequence and verify its structure
        TaskSequence taskSequence = Brobot.buildSequence()
            .find(testImage)
            .thenClick()
            .thenType(testString)
            .buildTaskSequence();
        
        assertNotNull(taskSequence);
        assertNotNull(taskSequence.getSteps());
        assertEquals(3, taskSequence.getSteps().size());
        
        // Verify first step (find)
        ActionStep findStep = taskSequence.getSteps().get(0);
        assertEquals("FIND", findStep.getActionOptions().getAction().name());
        assertNotNull(findStep.getObjectCollection());
        assertEquals(1, findStep.getObjectCollection().getStateImages().size());
        
        // Verify second step (click)
        ActionStep clickStep = taskSequence.getSteps().get(1);
        assertEquals("CLICK", clickStep.getActionOptions().getAction().name());
        
        // Verify third step (type)
        ActionStep typeStep = taskSequence.getSteps().get(2);
        assertEquals("TYPE", typeStep.getActionOptions().getAction().name());
        assertEquals(1, typeStep.getObjectCollection().getStateStrings().size());
    }
    
    @Test
    public void testComplexSequenceSerializable() throws Exception {
        // Create a more complex sequence
        StateImage menuButton = new StateImage();
        menuButton.setName("menuButton");
        
        StateImage submenu = new StateImage();
        submenu.setName("submenu");
        
        InstructionSet complexSet = Brobot.buildSequence()
            .withName("complexNavigation")
            .find(menuButton)
            .thenClick()
            .thenWait(1.0)
            .find(submenu)
            .thenClick()
            .thenWaitVanish(menuButton)
            .build();
        
        // Serialize and verify
        String json = mapper.writeValueAsString(complexSet);
        assertNotNull(json);
        
        // Deserialize and verify structure
        InstructionSet deserialized = mapper.readValue(json, InstructionSet.class);
        BusinessTask task = deserialized.getAutomationFunctions().get(0);
        
        assertEquals("complexNavigation", task.getName());
        // The task should have statements that reference the task sequence
        assertNotNull(task.getStatements());
    }
    
    @Test
    public void testDragSequenceStructure() {
        StateImage source = new StateImage();
        source.setName("source");
        
        StateImage target = new StateImage();
        target.setName("target");
        
        TaskSequence dragSequence = Brobot.buildSequence()
            .find(source)
            .thenDragTo(target)
            .buildTaskSequence();
        
        // Verify drag is implemented as 6 actions
        assertEquals(6, dragSequence.getSteps().size());
        
        // Verify the sequence: Find→Find→Move→MouseDown→Move→MouseUp
        assertEquals("FIND", dragSequence.getSteps().get(0).getActionOptions().getAction().name());
        assertEquals("FIND", dragSequence.getSteps().get(1).getActionOptions().getAction().name());
        assertEquals("MOVE", dragSequence.getSteps().get(2).getActionOptions().getAction().name());
        assertEquals("MOUSE_DOWN", dragSequence.getSteps().get(3).getActionOptions().getAction().name());
        assertEquals("MOVE", dragSequence.getSteps().get(4).getActionOptions().getAction().name());
        assertEquals("MOUSE_UP", dragSequence.getSteps().get(5).getActionOptions().getAction().name());
    }
}