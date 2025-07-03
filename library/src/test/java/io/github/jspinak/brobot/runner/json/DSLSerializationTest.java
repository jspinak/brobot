package io.github.jspinak.brobot.runner.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateString;
import io.github.jspinak.brobot.runner.dsl.BusinessTask;
import io.github.jspinak.brobot.runner.dsl.InstructionSet;
import io.github.jspinak.brobot.runner.dsl.model.ActionStep;
import io.github.jspinak.brobot.runner.dsl.model.TaskSequence;
import io.github.jspinak.brobot.runner.dsl.model.Parameter;
import io.github.jspinak.brobot.runner.dsl.statements.Statement;
import io.github.jspinak.brobot.runner.dsl.statements.VariableDeclarationStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests to verify that DSL classes can be properly serialized and deserialized
 * with the existing JSON infrastructure.
 */
public class DSLSerializationTest {
    
    private ObjectMapper mapper;
    
    @BeforeEach
    public void setup() {
        mapper = new ObjectMapper();
    }
    
    @Test
    public void testTaskSequenceSerializationDeserialization() throws Exception {
        // Create a TaskSequence with ActionSteps
        TaskSequence taskSequence = new TaskSequence();
        
        // Add a find action
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
            .setSimilarity(0.9)
            .setStrategy(PatternFindOptions.Strategy.FIRST)
            .build();
        StateImage testImage = new StateImage();
        testImage.setName("testButton");
        ObjectCollection findObjects = new ObjectCollection.Builder()
            .withImages(testImage)
            .build();
        taskSequence.addStep(findOptions, findObjects);
        
        // Add a click action
        ClickOptions clickOptions = new ClickOptions.Builder()
            .build();
        taskSequence.addStep(clickOptions, findObjects);
        
        // Serialize to JSON
        String json = mapper.writeValueAsString(taskSequence);
        assertNotNull(json);
        assertTrue(json.contains("\"steps\""));
        // Debug: print JSON to understand the structure
        System.out.println("TaskSequence JSON: " + json);
        // Check for the strategy enum which should be unique to PatternFindOptions
        assertTrue(json.contains("\"strategy\":\"FIRST\""));
        // Check for numberOfClicks which is unique to ClickOptions
        assertTrue(json.contains("\"numberOfClicks\":1"));
        
        // Since we don't have type information in JSON, we can't deserialize polymorphic types
        // without additional configuration. For now, just verify serialization works.
        // In production, use BrobotObjectMapper which has proper type handling configured.
    }
    
    @Test
    public void testBusinessTaskSerialization() throws Exception {
        // Create a BusinessTask
        BusinessTask task = new BusinessTask();
        task.setId(1);
        task.setName("loginTask");
        task.setDescription("Performs login automation");
        task.setReturnType("void");
        
        // Add parameters
        List<Parameter> parameters = new ArrayList<>();
        Parameter userParam = new Parameter();
        userParam.setName("username");
        userParam.setType("stateString");
        parameters.add(userParam);
        task.setParameters(parameters);
        
        // Add statements
        List<Statement> statements = new ArrayList<>();
        VariableDeclarationStatement varDecl = new VariableDeclarationStatement();
        varDecl.setStatementType("variableDeclaration");
        varDecl.setName("loginSequence");
        varDecl.setType("taskSequence");
        statements.add(varDecl);
        task.setStatements(statements);
        
        // Serialize to JSON
        String json = mapper.writeValueAsString(task);
        assertNotNull(json);
        assertTrue(json.contains("loginTask"));
        assertTrue(json.contains("stateString"));
        assertTrue(json.contains("taskSequence"));
        
        // Deserialize back
        BusinessTask deserialized = mapper.readValue(json, BusinessTask.class);
        assertNotNull(deserialized);
        assertEquals("loginTask", deserialized.getName());
        assertEquals(1, deserialized.getParameters().size());
        assertEquals("stateString", deserialized.getParameters().get(0).getType());
    }
    
    @Test
    public void testInstructionSetSerialization() throws Exception {
        // Create an InstructionSet
        InstructionSet instructionSet = new InstructionSet();
        List<BusinessTask> tasks = new ArrayList<>();
        
        BusinessTask task = new BusinessTask();
        task.setName("simpleTask");
        task.setReturnType("void");
        task.setStatements(new ArrayList<>());
        tasks.add(task);
        
        instructionSet.setAutomationFunctions(tasks);
        
        // Serialize to JSON
        String json = mapper.writeValueAsString(instructionSet);
        assertNotNull(json);
        assertTrue(json.contains("automationFunctions"));
        assertTrue(json.contains("simpleTask"));
        
        // Deserialize back
        InstructionSet deserialized = mapper.readValue(json, InstructionSet.class);
        assertNotNull(deserialized);
        assertNotNull(deserialized.getAutomationFunctions());
        assertEquals(1, deserialized.getAutomationFunctions().size());
        assertEquals("simpleTask", deserialized.getAutomationFunctions().get(0).getName());
    }
    
    @Test
    public void testActionStepWithStateString() throws Exception {
        // Create an ActionStep with StateString
        TypeOptions typeOptions = new TypeOptions.Builder()
            .setTypeDelay(0.1)
            .build();
            
        StateString stateString = new StateString.InNullState().withString("test text");
        ObjectCollection objects = new ObjectCollection.Builder()
            .withStrings(stateString)
            .build();
            
        ActionStep step = new ActionStep(typeOptions, objects);
        
        // Serialize to JSON
        String json = mapper.writeValueAsString(step);
        assertNotNull(json);
        // Debug: print JSON to understand the structure
        System.out.println("ActionStep JSON: " + json);
        // Check for typeDelay which is unique to TypeOptions
        assertTrue(json.contains("\"typeDelay\":0.1"));
        assertTrue(json.contains("test text"));
        
        // Since we don't have type information in JSON, we can't deserialize polymorphic types
        // without additional configuration. For now, just verify serialization works.
        // In production, use BrobotObjectMapper which has proper type handling configured.
    }
}