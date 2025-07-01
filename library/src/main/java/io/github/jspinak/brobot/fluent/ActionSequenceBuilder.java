package io.github.jspinak.brobot.fluent;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.highlight.HighlightOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseMoveOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseDownOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseUpOptions;
import io.github.jspinak.brobot.action.basic.mouse.ScrollOptions;
import io.github.jspinak.brobot.action.basic.vanish.VanishOptions;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateString;
import io.github.jspinak.brobot.runner.dsl.BusinessTask;
import io.github.jspinak.brobot.runner.dsl.InstructionSet;
import io.github.jspinak.brobot.runner.dsl.model.ActionStep;
import io.github.jspinak.brobot.runner.dsl.model.TaskSequence;
import io.github.jspinak.brobot.runner.dsl.statements.Statement;
import io.github.jspinak.brobot.runner.dsl.statements.VariableDeclarationStatement;
import io.github.jspinak.brobot.runner.dsl.expressions.LiteralExpression;

import java.util.ArrayList;
import java.util.List;

/**
 * Fluent API builder for creating DSL-compatible action sequences.
 * 
 * This builder allows developers to programmatically create automation sequences
 * that are fully compatible with the Brobot DSL. Each method call creates
 * ActionStep objects that are added to an internal TaskSequence.
 * 
 * Example usage:
 * <pre>
 * // Assuming userField, passwordField, submitButton are StateImage objects
 * // and username, password are StateString objects
 * InstructionSet loginSequence = Brobot.buildSequence()
 *     .withName("login")
 *     .find(userField)
 *     .thenClick()
 *     .thenType(username)
 *     .find(passwordField)
 *     .thenClick()
 *     .thenType(password)
 *     .find(submitButton)
 *     .thenClick()
 *     .build();
 * </pre>
 */
public class ActionSequenceBuilder {
    private String name = "automation";
    private String description = "";
    private final List<ActionStep> steps = new ArrayList<>();
    private final List<Statement> statements = new ArrayList<>();
    private StateImage lastFindTarget;
    
    /**
     * Sets the name for the automation function.
     */
    public ActionSequenceBuilder withName(String name) {
        this.name = name;
        return this;
    }
    
    /**
     * Sets the description for the automation function.
     */
    public ActionSequenceBuilder withDescription(String description) {
        this.description = description;
        return this;
    }
    
    /**
     * Adds a find action to the sequence.
     * The found object becomes the target for subsequent actions.
     */
    public ActionSequenceBuilder find(StateImage target) {
        this.lastFindTarget = target;
        
        PatternFindOptions options = new PatternFindOptions.Builder()
            .build();
            
        ObjectCollection objects = new ObjectCollection.Builder()
            .withImages(target)
            .build();
            
        steps.add(new ActionStep(options, objects));
        return this;
    }
    
    /**
     * Adds a click action on the last found object.
     */
    public ActionSequenceBuilder thenClick() {
        if (lastFindTarget == null) {
            throw new IllegalStateException("No target found. Call find() before thenClick()");
        }
        
        ClickOptions options = new ClickOptions.Builder()
            .build();
            
        ObjectCollection objects = new ObjectCollection.Builder()
            .withImages(lastFindTarget)
            .build();
            
        steps.add(new ActionStep(options, objects));
        return this;
    }
    
    /**
     * Adds a type action with a StateString.
     */
    public ActionSequenceBuilder thenType(StateString stateString) {
        TypeOptions options = new TypeOptions.Builder()
            .build();
            
        ObjectCollection objects = new ObjectCollection.Builder()
            .withStrings(stateString)
            .build();
            
        steps.add(new ActionStep(options, objects));
        return this;
    }
    
    /**
     * Adds a drag action from the last found object to a target.
     * This creates the 6 chained actions: Find→Find→MouseMove→MouseDown→MouseMove→MouseUp
     */
    public ActionSequenceBuilder thenDragTo(StateImage target) {
        if (lastFindTarget == null) {
            throw new IllegalStateException("No source found. Call find() before thenDragTo()");
        }
        
        // Step 1: Find source (already done)
        
        // Step 2: Find target
        PatternFindOptions findTargetOptions = new PatternFindOptions.Builder()
            .build();
        ObjectCollection findTargetObjects = new ObjectCollection.Builder()
            .withImages(target)
            .build();
        steps.add(new ActionStep(findTargetOptions, findTargetObjects));
        
        // Step 3: Move to source
        MouseMoveOptions moveToSourceOptions = new MouseMoveOptions.Builder()
            .build();
        ObjectCollection moveToSourceObjects = new ObjectCollection.Builder()
            .withImages(lastFindTarget)
            .build();
        steps.add(new ActionStep(moveToSourceOptions, moveToSourceObjects));
        
        // Step 4: Mouse down
        MouseDownOptions mouseDownOptions = new MouseDownOptions.Builder()
            .build();
        ObjectCollection mouseDownObjects = new ObjectCollection.Builder()
            .build();
        steps.add(new ActionStep(mouseDownOptions, mouseDownObjects));
        
        // Step 5: Move to target
        MouseMoveOptions moveToTargetOptions = new MouseMoveOptions.Builder()
            .build();
        ObjectCollection moveToTargetObjects = new ObjectCollection.Builder()
            .withImages(target)
            .build();
        steps.add(new ActionStep(moveToTargetOptions, moveToTargetObjects));
        
        // Step 6: Mouse up
        MouseUpOptions mouseUpOptions = new MouseUpOptions.Builder()
            .build();
        ObjectCollection mouseUpObjects = new ObjectCollection.Builder()
            .build();
        steps.add(new ActionStep(mouseUpOptions, mouseUpObjects));
        
        return this;
    }
    
    /**
     * Adds a highlight action to visually mark regions on screen.
     */
    public ActionSequenceBuilder thenHighlight(StateImage target, double seconds) {
        HighlightOptions options = new HighlightOptions.Builder()
            .setHighlightSeconds(seconds)
            .build();
            
        ObjectCollection objects = new ObjectCollection.Builder()
            .withImages(target)
            .build();
            
        steps.add(new ActionStep(options, objects));
        return this;
    }
    
    /**
     * Adds a vanish action to wait for an object to disappear.
     */
    public ActionSequenceBuilder thenWaitVanish(StateImage target) {
        VanishOptions options = new VanishOptions.Builder()
            .build();
            
        ObjectCollection objects = new ObjectCollection.Builder()
            .withImages(target)
            .build();
            
        steps.add(new ActionStep(options, objects));
        return this;
    }
    
    /**
     * Adds a scroll action.
     */
    public ActionSequenceBuilder thenScroll(ScrollOptions.Direction direction, int times) {
        // For now, we'll add the scroll action multiple times
        // In a future update, ScrollOptions could be enhanced to include repetition count
        ScrollOptions options = new ScrollOptions.Builder()
            .setDirection(direction)
            .build();
            
        ObjectCollection objects = new ObjectCollection.Builder()
            .build();
        
        // Add the scroll action the specified number of times
        for (int i = 0; i < times; i++) {
            steps.add(new ActionStep(options, objects));
        }
        
        return this;
    }
    
    /**
     * Adds a custom action with full control over options and objects.
     */
    public ActionSequenceBuilder addCustomAction(ActionConfig options, ObjectCollection objects) {
        steps.add(new ActionStep(options, objects));
        return this;
    }
    
    /**
     * Creates a variable declaration statement for the task sequence.
     */
    public ActionSequenceBuilder declareTaskSequence(String variableName) {
        TaskSequence taskSequence = new TaskSequence();
        steps.forEach(taskSequence::addStep);
        
        VariableDeclarationStatement declaration = new VariableDeclarationStatement();
        declaration.setStatementType("variableDeclaration");
        declaration.setName(variableName);
        declaration.setType("TaskSequence");
        // In a real implementation, we'd serialize the TaskSequence to a LiteralExpression
        // For now, we'll use a placeholder
        LiteralExpression value = new LiteralExpression();
        value.setExpressionType("literal");
        value.setValueType("object");
        value.setValue(taskSequence);
        declaration.setValue(value);
        
        statements.add(declaration);
        return this;
    }
    
    /**
     * Builds the final InstructionSet containing the automation function.
     */
    public InstructionSet build() {
        BusinessTask task = new BusinessTask();
        task.setName(name);
        task.setDescription(description);
        task.setReturnType("void");
        task.setParameters(new ArrayList<>());
        
        // If no explicit statements were added, create a default task sequence variable
        if (statements.isEmpty() && !steps.isEmpty()) {
            declareTaskSequence("taskSequence");
        }
        
        task.setStatements(statements);
        
        InstructionSet instructionSet = new InstructionSet();
        List<BusinessTask> tasks = new ArrayList<>();
        tasks.add(task);
        instructionSet.setAutomationFunctions(tasks);
        
        return instructionSet;
    }
    
    /**
     * Builds and returns just the TaskSequence without wrapping in InstructionSet.
     */
    public TaskSequence buildTaskSequence() {
        TaskSequence taskSequence = new TaskSequence();
        steps.forEach(taskSequence::addStep);
        return taskSequence;
    }
}