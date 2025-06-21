package io.github.jspinak.brobot.actions.actionExecution;

import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;

/**
 * Core interface for all actions in the Brobot model-based GUI automation framework.
 * 
 * <p>ActionInterface defines the contract that all action implementations must follow, 
 * establishing a uniform execution pattern across the entire Action Model (α). This 
 * interface is the foundation of Brobot's action architecture, enabling polymorphic 
 * dispatch of diverse GUI operations through a single, consistent API.</p>
 * 
 * <p>Key design principles:
 * <ul>
 *   <li><b>Uniform Execution</b>: All actions, from simple clicks to complex workflows, 
 *       implement the same perform() method signature</li>
 *   <li><b>Result Accumulation</b>: Actions modify the provided Matches object to record 
 *       their results and maintain execution context</li>
 *   <li><b>Flexible Input</b>: Accepts variable ObjectCollections to support actions 
 *       requiring different numbers of targets</li>
 *   <li><b>Composability</b>: Enables actions to be combined into composite operations</li>
 * </ul>
 * </p>
 * 
 * <p>The perform method contract:
 * <ul>
 *   <li>Receives a Matches object containing ActionOptions and accumulating results</li>
 *   <li>Processes one or more ObjectCollections containing the action targets</li>
 *   <li>Updates the Matches object with results of the action</li>
 *   <li>May throw runtime exceptions for error conditions</li>
 * </ul>
 * </p>
 * 
 * <p>Implementation categories:
 * <ul>
 *   <li><b>Basic Actions</b>: Click, Type, Find, Drag, etc.</li>
 *   <li><b>Composite Actions</b>: Multi-step operations built from basic actions</li>
 *   <li><b>Custom Actions</b>: Application-specific operations</li>
 *   <li><b>Mock Actions</b>: Test implementations for development and testing</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, ActionInterface enables the framework to treat all 
 * GUI operations uniformly, regardless of their complexity. This abstraction is crucial 
 * for building maintainable automation scripts where actions can be easily substituted, 
 * extended, or composed without changing the calling code.</p>
 * 
 * @since 1.0
 * @see Action
 * @see Matches
 * @see ObjectCollection
 * @see BasicAction
 * @see CompositeAction
 */
public interface ActionInterface {

    void perform(Matches matches, ObjectCollection... objectCollections);
}
