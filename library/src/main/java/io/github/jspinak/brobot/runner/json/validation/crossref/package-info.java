/**
 * Cross-reference validation for configuration integrity.
 * 
 * <p>This package contains validators that ensure all references within
 * configurations point to valid targets. It prevents broken references that
 * would cause runtime failures and maintains referential integrity across
 * the entire configuration.</p>
 * 
 * <h2>Validators</h2>
 * 
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.runner.json.validation.crossref.ReferenceValidator} - 
 *       Base reference validation interface</li>
 *   <li>{@link io.github.jspinak.brobot.runner.json.validation.crossref.StateReferenceValidator} - 
 *       Validates state name references</li>
 *   <li>{@link io.github.jspinak.brobot.runner.json.validation.crossref.FunctionReferenceValidator} - 
 *       Validates function call references</li>
 * </ul>
 * 
 * <h2>Reference Types</h2>
 * 
 * <h3>State References</h3>
 * <ul>
 *   <li>Transition source and target states</li>
 *   <li>Initial state declarations</li>
 *   <li>State image associations</li>
 *   <li>Hidden state references</li>
 * </ul>
 * 
 * <h3>Function References</h3>
 * <ul>
 *   <li>Function calls in DSL</li>
 *   <li>Button action functions</li>
 *   <li>Transition action functions</li>
 *   <li>Parameter type references</li>
 * </ul>
 * 
 * <h3>Image References</h3>
 * <ul>
 *   <li>StateImage pattern files</li>
 *   <li>ObjectCollection images</li>
 *   <li>Dynamic image references</li>
 * </ul>
 * 
 * <h2>Validation Examples</h2>
 * 
 * <h3>State Reference Validation</h3>
 * <pre>{@code
 * // Valid: state exists
 * {
 *   "transition": {
 *     "from": "loginPage",    // Must exist in states
 *     "to": "dashboardPage"   // Must exist in states
 *   }
 * }
 * 
 * // Invalid: state doesn't exist
 * {
 *   "transition": {
 *     "from": "loginPage",
 *     "to": "unknownPage"  // ERROR: State not found
 *   }
 * }
 * }</pre>
 * 
 * <h3>Function Reference Validation</h3>
 * <pre>{@code
 * // Valid: function defined
 * {
 *   "button": {
 *     "action": "loginUser",  // Function exists
 *     "parameters": {
 *       "username": "admin"
 *     }
 *   }
 * }
 * 
 * // Invalid: undefined function
 * {
 *   "button": {
 *     "action": "doSomething"  // ERROR: Function not found
 *   }
 * }
 * }</pre>
 * 
 * <h3>Circular Reference Detection</h3>
 * <pre>{@code
 * // Detects circular dependencies
 * validator.detectCircularReferences(configuration);
 * 
 * // Example circular reference
 * State A -> includes -> State B
 * State B -> includes -> State C  
 * State C -> includes -> State A  // ERROR: Circular
 * }</pre>
 * 
 * <h2>Reference Resolution</h2>
 * 
 * <p>The validation process includes:</p>
 * <ol>
 *   <li>Build reference index from definitions</li>
 *   <li>Scan configuration for references</li>
 *   <li>Resolve each reference to its target</li>
 *   <li>Report unresolved references</li>
 *   <li>Detect circular dependencies</li>
 * </ol>
 * 
 * <h2>Error Reporting</h2>
 * 
 * <pre>{@code
 * ValidationError error = new ValidationError(
 *     ValidationSeverity.ERROR,
 *     "State 'unknownState' referenced in transition " +
 *     "at $.transitions[2].to does not exist",
 *     "$.transitions[2].to"
 * );
 * 
 * // Suggestions included
 * error.setSuggestion("Did you mean 'knownState'?");
 * }</pre>
 * 
 * <h2>Performance Optimization</h2>
 * 
 * <ul>
 *   <li>Build reference index once</li>
 *   <li>Use maps for O(1) lookups</li>
 *   <li>Cache validation results</li>
 *   <li>Validate incrementally when possible</li>
 * </ul>
 * 
 * <h2>Best Practices</h2>
 * 
 * <ul>
 *   <li>Validate references before business rules</li>
 *   <li>Include path to broken reference</li>
 *   <li>Suggest similar valid references</li>
 *   <li>Group related reference errors</li>
 *   <li>Support case-insensitive matching option</li>
 * </ul>
 * 
 * @since 1.0
 * @see io.github.jspinak.brobot.runner.json.validation
 * @see io.github.jspinak.brobot.runner.project
 */
package io.github.jspinak.brobot.runner.json.validation.crossref;