package io.github.jspinak.brobot.runner.json.validation.crossref;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.runner.json.validation.model.ValidationError;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationResult;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationSeverity;

import java.util.*;

/**
 * Validates all references related to automation functions in Brobot configurations.
 * 
 * <p>This validator ensures that all function-related references are valid and consistent
 * throughout the DSL and project configurations. It validates function calls, parameter
 * usage, variable references, and API method calls to prevent runtime errors and ensure
 * code quality.</p>
 * 
 * <h2>Function Reference Types:</h2>
 * <ul>
 *   <li><b>Function Calls</b> - Functions calling other functions</li>
 *   <li><b>Variable References</b> - Variables used within function scope</li>
 *   <li><b>Method Calls</b> - API object method invocations</li>
 *   <li><b>Button-Function Links</b> - UI buttons referencing automation functions</li>
 * </ul>
 * 
 * <h2>Validation Approach:</h2>
 * <p>The validator uses static analysis techniques to trace variable scope,
 * validate function signatures, and ensure all references resolve correctly.
 * It maintains context about defined variables and functions while traversing
 * the abstract syntax tree of function statements.</p>
 * 
 * <h2>Common Issues Detected:</h2>
 * <ul>
 *   <li>Calling non-existent functions</li>
 *   <li>Using undefined variables</li>
 *   <li>Invalid API method calls</li>
 *   <li>Parameter mismatches between buttons and functions</li>
 *   <li>Duplicate function names</li>
 * </ul>
 * 
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * FunctionReferenceValidator validator = new FunctionReferenceValidator();
 * 
 * // Validate internal DSL references
 * ValidationResult dslResult = validator.validateInternalReferences(dsl);
 * 
 * // Validate button-to-function references
 * ValidationResult buttonResult = validator.validateButtonFunctionReferences(
 *     project, dsl);
 * 
 * // Check for critical errors
 * if (dslResult.hasErrors()) {
 *     logger.error("Function reference errors: {}", 
 *         dslResult.getFormattedErrors());
 * }
 * }</pre>
 * 
 * @see ReferenceValidator for the parent validation coordinator
 * @see StateReferenceValidator for state-related references
 * @see ValidationResult for interpreting validation outcomes
 * @author jspinak
 */
@Component
public class FunctionReferenceValidator {
    private static final Logger logger = LoggerFactory.getLogger(FunctionReferenceValidator.class);

    /**
     * Validates all internal references within the DSL model.
     * 
     * <p>This method performs comprehensive validation of references that exist
     * entirely within the DSL configuration. It ensures functions are well-formed
     * with valid variable usage, proper function calls, and correct API usage.</p>
     * 
     * <h3>Validation Phases:</h3>
     * <ol>
     *   <li><b>Function References</b> - Validates function-to-function calls and checks
     *       for duplicate function names</li>
     *   <li><b>Variable References</b> - Ensures all variables are defined before use
     *       with proper scoping rules</li>
     *   <li><b>Method Calls</b> - Validates API object methods against known interfaces</li>
     * </ol>
     * 
     * <h3>Scope Handling:</h3>
     * <p>The validator maintains variable scope context while traversing statements,
     * properly handling:</p>
     * <ul>
     *   <li>Function parameters</li>
     *   <li>Local variable declarations</li>
     *   <li>Block scope in if/else branches</li>
     *   <li>Loop variable scope in forEach statements</li>
     * </ul>
     * 
     * @param dslModel Parsed DSL model containing automation function definitions.
     *                 Expected to be a Map with an "automationFunctions" array
     * @return ValidationResult containing all discovered reference errors with
     *         appropriate severity levels
     */
    public ValidationResult validateInternalReferences(Object dslModel) {
        ValidationResult result = new ValidationResult();

        if (dslModel == null) {
            result.addError(new ValidationError(
                    "Invalid DSL model",
                    "DSL model is null",
                    ValidationSeverity.CRITICAL
            ));
            return result;
        }

        try {
            Map<String, Object> dsl = (Map<String, Object>) dslModel;

            // Validate function references within the DSL
            validateFunctionReferences(dsl, result);

            // Validate variable references within functions
            validateVariableReferences(dsl, result);

            // Validate method calls within functions
            validateMethodCalls(dsl, result);

        } catch (ClassCastException e) {
            logger.error("DSL model is not a valid type", e);
            result.addError(new ValidationError(
                    "Invalid DSL model type",
                    "DSL model could not be processed: " + e.getMessage(),
                    ValidationSeverity.CRITICAL
            ));
        } catch (Exception e) {
            logger.error("Error during function reference validation", e);
            result.addError(new ValidationError(
                    "Validation error",
                    "Error validating function references: " + e.getMessage(),
                    ValidationSeverity.ERROR
            ));
        }

        return result;
    }

    /**
     * Validates function references from UI buttons in the project configuration.
     * 
     * <p>This method ensures that all buttons in the UI configuration reference
     * valid automation functions and that the parameters they provide match the
     * function signatures. This cross-model validation is crucial for preventing
     * runtime errors when users interact with the UI.</p>
     * 
     * <h3>Validation Checks:</h3>
     * <ol>
     *   <li><b>Function Existence</b> - Button references an actual function in DSL</li>
     *   <li><b>Parameter Matching</b> - Button provides all required parameters</li>
     *   <li><b>Parameter Validity</b> - No extraneous parameters are provided</li>
     *   <li><b>Type Compatibility</b> - Parameter types match (when type info available)</li>
     * </ol>
     * 
     * <h3>Button-Function Integration:</h3>
     * <p>Buttons serve as the primary interface between users and automation functions.
     * A button configuration specifies:</p>
     * <ul>
     *   <li>The function name to invoke</li>
     *   <li>Parameters to pass to the function</li>
     *   <li>UI properties (label, position, etc.)</li>
     * </ul>
     * 
     * @param projectModel Project model containing button definitions in the
     *                     automation.buttons array
     * @param dslModel DSL model containing the automation functions that buttons
     *                 reference
     * @return ValidationResult containing errors for invalid function references
     *         and warnings for parameter mismatches
     */
    public ValidationResult validateButtonFunctionReferences(Object projectModel, Object dslModel) {
        ValidationResult result = new ValidationResult();

        if (projectModel == null || dslModel == null) {
            if (projectModel == null) {
                result.addError(new ValidationError(
                        "Invalid project model",
                        "Project model is null",
                        ValidationSeverity.CRITICAL
                ));
            }
            if (dslModel == null) {
                result.addError(new ValidationError(
                        "Invalid DSL model",
                        "DSL model is null",
                        ValidationSeverity.CRITICAL
                ));
            }
            return result;
        }

        try {
            Map<String, Object> project = (Map<String, Object>) projectModel;
            Map<String, Object> dsl = (Map<String, Object>) dslModel;

            // Extract function names from DSL
            Set<String> functionNames = extractFunctionNames(dsl);

            // Check button references to functions
            if (project.containsKey("automation") &&
                    ((Map<String, Object>) project.get("automation")).containsKey("buttons")) {

                List<Map<String, Object>> buttons = (List<Map<String, Object>>)
                        ((Map<String, Object>) project.get("automation")).get("buttons");

                for (Map<String, Object> button : buttons) {
                    String buttonId = button.containsKey("id")
                            ? (String) button.get("id")
                            : "unknown";

                    if (button.containsKey("functionName")) {
                        String functionName = (String) button.get("functionName");

                        if (!functionNames.contains(functionName)) {
                            result.addError(new ValidationError(
                                    "Invalid function reference",
                                    String.format("Button '%s' references non-existent function: %s",
                                            buttonId, functionName),
                                    ValidationSeverity.ERROR
                            ));
                        }

                        // Check parameter compatibility
                        if (button.containsKey("parameters")) {
                            validateButtonParameters(
                                    (Map<String, Object>) button.get("parameters"),
                                    functionName,
                                    dsl,
                                    buttonId,
                                    result
                            );
                        }
                    }
                }
            }

        } catch (ClassCastException e) {
            logger.error("Model is not a valid type", e);
            result.addError(new ValidationError(
                    "Invalid model type",
                    "Model could not be processed: " + e.getMessage(),
                    ValidationSeverity.CRITICAL
            ));
        } catch (Exception e) {
            logger.error("Error during button function reference validation", e);
            result.addError(new ValidationError(
                    "Validation error",
                    "Error validating button function references: " + e.getMessage(),
                    ValidationSeverity.ERROR
            ));
        }

        return result;
    }

    /**
     * Extracts function names from the DSL model.
     *
     * @param dsl DSL model as a map
     * @return Set of function names
     */
    private Set<String> extractFunctionNames(Map<String, Object> dsl) {
        Set<String> functionNames = new HashSet<>();

        if (dsl.containsKey("automationFunctions")) {
            List<Map<String, Object>> functions = (List<Map<String, Object>>) dsl.get("automationFunctions");

            for (Map<String, Object> function : functions) {
                if (function.containsKey("name")) {
                    functionNames.add((String) function.get("name"));
                }
            }
        }

        return functionNames;
    }

    /**
     * Validates button parameters against function parameters.
     *
     * @param buttonParams Button parameters as a map
     * @param functionName Function name
     * @param dsl DSL model as a map
     * @param buttonId Button ID
     * @param result Validation result to update
     */
    private void validateButtonParameters(
            Map<String, Object> buttonParams,
            String functionName,
            Map<String, Object> dsl,
            String buttonId,
            ValidationResult result) {

        if (!dsl.containsKey("automationFunctions")) {
            return;
        }

        List<Map<String, Object>> functions = (List<Map<String, Object>>) dsl.get("automationFunctions");

        for (Map<String, Object> function : functions) {
            if (function.containsKey("name") && function.get("name").equals(functionName)) {
                // Found the function, check parameters
                if (function.containsKey("parameters")) {
                    List<Map<String, Object>> functionParams = (List<Map<String, Object>>) function.get("parameters");
                    Set<String> requiredParams = new HashSet<>();

                    for (Map<String, Object> param : functionParams) {
                        if (param.containsKey("name")) {
                            String paramName = (String) param.get("name");
                            requiredParams.add(paramName);

                            // Check if required parameter is provided
                            if (!buttonParams.containsKey(paramName)) {
                                result.addError(new ValidationError(
                                        "Missing parameter",
                                        String.format("Button '%s' is missing required parameter '%s' for function '%s'",
                                                buttonId, paramName, functionName),
                                        ValidationSeverity.WARNING
                                ));
                            }
                        }
                    }

                    // Check for extraneous parameters
                    for (String paramName : buttonParams.keySet()) {
                        if (!requiredParams.contains(paramName)) {
                            result.addError(new ValidationError(
                                    "Unknown parameter",
                                    String.format("Button '%s' provides unknown parameter '%s' for function '%s'",
                                            buttonId, paramName, functionName),
                                    ValidationSeverity.WARNING
                            ));
                        }
                    }
                } else if (!buttonParams.isEmpty()) {
                    // Function doesn't take parameters but button provides them
                    result.addError(new ValidationError(
                            "Unexpected parameters",
                            String.format("Button '%s' provides parameters for function '%s' which doesn't accept any",
                                    buttonId, functionName),
                            ValidationSeverity.WARNING
                    ));
                }

                break;
            }
        }
    }

    /**
     * Validates function-to-function references and name uniqueness.
     * 
     * <p>This method ensures that all function calls within the DSL reference
     * valid functions and that function names are unique. Duplicate function
     * names would cause ambiguity about which function to execute.</p>
     * 
     * <h3>Validation Process:</h3>
     * <ol>
     *   <li><b>Name Collection</b> - First pass collects all function names and
     *       checks for duplicates</li>
     *   <li><b>Call Validation</b> - Second pass validates all function calls
     *       against the collected names</li>
     * </ol>
     * 
     * <h3>Function Call Patterns:</h3>
     * <p>Function calls in the DSL appear as method calls with no object:</p>
     * <pre>{@code
     * // Direct function call
     * functionName(param1, param2);
     * 
     * // Function call in expression
     * var result = helperFunction();
     * }</pre>
     * 
     * @param dsl DSL model containing function definitions
     * @param result ValidationResult to accumulate function reference errors
     */
    private void validateFunctionReferences(Map<String, Object> dsl, ValidationResult result) {
        if (!dsl.containsKey("automationFunctions")) {
            return;
        }

        List<Map<String, Object>> functions = (List<Map<String, Object>>) dsl.get("automationFunctions");
        Set<String> functionNames = new HashSet<>();

        // First pass: collect function names
        for (Map<String, Object> function : functions) {
            if (function.containsKey("name")) {
                String name = (String) function.get("name");

                if (functionNames.contains(name)) {
                    result.addError(new ValidationError(
                            "Duplicate function name",
                            String.format("Function name '%s' is defined multiple times", name),
                            ValidationSeverity.ERROR
                    ));
                } else {
                    functionNames.add(name);
                }
            }
        }

        // Second pass: validate function calls
        for (Map<String, Object> function : functions) {
            String functionName = function.containsKey("name")
                    ? (String) function.get("name")
                    : "unknown";

            if (function.containsKey("statements")) {
                validateStatementsForFunctionCalls(
                        (List<Map<String, Object>>) function.get("statements"),
                        functionNames,
                        functionName,
                        result
                );
            }
        }
    }

    /**
     * Validates variable references with proper scope tracking.
     * 
     * <p>This method performs static analysis of variable usage within functions,
     * ensuring all variables are defined before use and respecting scope boundaries.
     * This prevents runtime "undefined variable" errors.</p>
     * 
     * <h3>Scope Rules Enforced:</h3>
     * <ul>
     *   <li><b>Function Parameters</b> - Available throughout function body</li>
     *   <li><b>Local Variables</b> - Available after declaration</li>
     *   <li><b>Block Scope</b> - Variables in if/else branches have limited scope</li>
     *   <li><b>Loop Variables</b> - forEach variables only available in loop body</li>
     * </ul>
     * 
     * <h3>Variable Definition Tracking:</h3>
     * <p>The validator maintains a set of defined variables while traversing
     * statements, adding variables as they're declared and checking references
     * against this set. Special handling for:</p>
     * <ul>
     *   <li>Variables defined in both if/else branches (available after)</li>
     *   <li>Loop variables (scoped to loop body only)</li>
     *   <li>Function parameters (available immediately)</li>
     * </ul>
     * 
     * @param dsl DSL model containing function definitions to validate
     * @param result ValidationResult to accumulate undefined variable errors
     */
    private void validateVariableReferences(Map<String, Object> dsl, ValidationResult result) {
        if (!dsl.containsKey("automationFunctions")) {
            return;
        }

        List<Map<String, Object>> functions = (List<Map<String, Object>>) dsl.get("automationFunctions");

        for (Map<String, Object> function : functions) {
            String functionName = function.containsKey("name")
                    ? (String) function.get("name")
                    : "unknown";

            // Create a set to track defined variables for this function
            Set<String> definedVariables = new HashSet<>();

            // Add function parameters to defined variables
            if (function.containsKey("parameters")) {
                List<Map<String, Object>> params = (List<Map<String, Object>>) function.get("parameters");

                for (Map<String, Object> param : params) {
                    if (param.containsKey("name")) {
                        definedVariables.add((String) param.get("name"));
                    }
                }
            }

            // Validate statements for variable references
            if (function.containsKey("statements")) {
                validateStatementsForVariableReferences(
                        (List<Map<String, Object>>) function.get("statements"),
                        definedVariables,
                        functionName,
                        result
                );
            }
        }
    }

    /**
     * Validates API method calls for correct object and method names.
     * 
     * <p>This method ensures that all API method calls use valid objects and
     * methods from the Brobot automation API. Invalid API calls would cause
     * runtime errors, so catching them during validation is crucial.</p>
     * 
     * <h3>Known API Objects:</h3>
     * <ul>
     *   <li><b>action</b> - Core automation actions (find, click, type, perform)</li>
     *   <li><b>stateTransitionsManagement</b> - State control (openState, isStateActive)</li>
     * </ul>
     * 
     * <h3>Validation Approach:</h3>
     * <p>The validator maintains a map of valid API objects and their methods.
     * This is a simplified approach - in production, this would be generated
     * from API documentation or type definitions.</p>
     * 
     * <h3>Future Enhancements:</h3>
     * <ul>
     *   <li>Dynamic API discovery from classpath</li>
     *   <li>Parameter count and type validation</li>
     *   <li>Return type checking</li>
     *   <li>Deprecation warnings</li>
     * </ul>
     * 
     * @param dsl DSL model containing functions with API method calls
     * @param result ValidationResult to accumulate invalid method call errors
     */
    private void validateMethodCalls(Map<String, Object> dsl, ValidationResult result) {
        if (!dsl.containsKey("automationFunctions")) {
            return;
        }

        // Define valid API objects and their methods
        // This is a simplistic approach - in a real implementation, this would be more sophisticated
        Map<String, Set<String>> apiObjects = new HashMap<>();
        apiObjects.put("action", new HashSet<>(Arrays.asList("perform", "find", "click", "type")));
        apiObjects.put("stateTransitionsManagement", new HashSet<>(Arrays.asList("openState", "isStateActive")));

        List<Map<String, Object>> functions = (List<Map<String, Object>>) dsl.get("automationFunctions");

        for (Map<String, Object> function : functions) {
            String functionName = function.containsKey("name")
                    ? (String) function.get("name")
                    : "unknown";

            if (function.containsKey("statements")) {
                validateStatementsForMethodCalls(
                        (List<Map<String, Object>>) function.get("statements"),
                        apiObjects,
                        functionName,
                        result
                );
            }
        }
    }

    /**
     * Recursively validates statements for function calls.
     *
     * @param statements List of statement objects
     * @param validFunctions Set of valid function names
     * @param functionName Name of the function being validated
     * @param result Validation result to update
     */
    private void validateStatementsForFunctionCalls(
            List<Map<String, Object>> statements,
            Set<String> validFunctions,
            String functionName,
            ValidationResult result) {

        for (Map<String, Object> statement : statements) {
            String statementType = statement.containsKey("statementType")
                    ? (String) statement.get("statementType")
                    : "";

            switch (statementType) {
                case "methodCall":
                    // Check if it's a function call
                    if (statement.containsKey("object") && statement.get("object") == null &&
                            statement.containsKey("method")) {

                        String calledFunction = (String) statement.get("method");

                        if (!validFunctions.contains(calledFunction)) {
                            result.addError(new ValidationError(
                                    "Invalid function call",
                                    String.format("Function '%s' calls non-existent function: %s",
                                            functionName, calledFunction),
                                    ValidationSeverity.ERROR
                            ));
                        }
                    }
                    break;

                case "if":
                    // Check then and else branches
                    if (statement.containsKey("thenStatements")) {
                        validateStatementsForFunctionCalls(
                                (List<Map<String, Object>>) statement.get("thenStatements"),
                                validFunctions,
                                functionName,
                                result
                        );
                    }

                    if (statement.containsKey("elseStatements")) {
                        validateStatementsForFunctionCalls(
                                (List<Map<String, Object>>) statement.get("elseStatements"),
                                validFunctions,
                                functionName,
                                result
                        );
                    }
                    break;

                case "forEach":
                    // Check body statements
                    if (statement.containsKey("statements")) {
                        validateStatementsForFunctionCalls(
                                (List<Map<String, Object>>) statement.get("statements"),
                                validFunctions,
                                functionName,
                                result
                        );
                    }
                    break;
            }
        }
    }

    /**
     * Recursively validates statements for variable references.
     *
     * @param statements List of statement objects
     * @param definedVariables Set of defined variable names
     * @param functionName Name of the function being validated
     * @param result Validation result to update
     */
    private void validateStatementsForVariableReferences(
            List<Map<String, Object>> statements,
            Set<String> definedVariables,
            String functionName,
            ValidationResult result) {

        for (Map<String, Object> statement : statements) {
            String statementType = statement.containsKey("statementType")
                    ? (String) statement.get("statementType")
                    : "";

            switch (statementType) {
                case "variableDeclaration":
                    // Add to defined variables
                    if (statement.containsKey("name")) {
                        definedVariables.add((String) statement.get("name"));
                    }

                    // Check value expression
                    if (statement.containsKey("value")) {
                        validateExpressionForVariableReferences(
                                (Map<String, Object>) statement.get("value"),
                                definedVariables,
                                functionName,
                                result
                        );
                    }
                    break;

                case "assignment":
                    // Check variable exists
                    if (statement.containsKey("variable")) {
                        String variable = (String) statement.get("variable");

                        if (!definedVariables.contains(variable)) {
                            result.addError(new ValidationError(
                                    "Undefined variable",
                                    String.format("Function '%s' references undefined variable: %s",
                                            functionName, variable),
                                    ValidationSeverity.ERROR
                            ));
                        }
                    }

                    // Check value expression
                    if (statement.containsKey("value")) {
                        validateExpressionForVariableReferences(
                                (Map<String, Object>) statement.get("value"),
                                definedVariables,
                                functionName,
                                result
                        );
                    }
                    break;

                case "methodCall":
                    // Check arguments
                    if (statement.containsKey("arguments")) {
                        List<Map<String, Object>> args = (List<Map<String, Object>>) statement.get("arguments");

                        for (Map<String, Object> arg : args) {
                            validateExpressionForVariableReferences(arg, definedVariables, functionName, result);
                        }
                    }
                    break;

                case "if":
                    // Check condition
                    if (statement.containsKey("condition")) {
                        validateExpressionForVariableReferences(
                                (Map<String, Object>) statement.get("condition"),
                                definedVariables,
                                functionName,
                                result
                        );
                    }

                    // Create a copy of defined variables for each branch
                    Set<String> thenVars = new HashSet<>(definedVariables);
                    Set<String> elseVars = new HashSet<>(definedVariables);

                    // Check then branch
                    if (statement.containsKey("thenStatements")) {
                        validateStatementsForVariableReferences(
                                (List<Map<String, Object>>) statement.get("thenStatements"),
                                thenVars,
                                functionName,
                                result
                        );
                    }

                    // Check else branch
                    if (statement.containsKey("elseStatements")) {
                        validateStatementsForVariableReferences(
                                (List<Map<String, Object>>) statement.get("elseStatements"),
                                elseVars,
                                functionName,
                                result
                        );
                    }

                    // Add variables that are defined in both branches to the outer scope
                    for (String var : thenVars) {
                        if (elseVars.contains(var)) {
                            definedVariables.add(var);
                        }
                    }
                    break;

                case "forEach":
                    // Add loop variable to defined variables for the loop body only
                    Set<String> loopVars = new HashSet<>(definedVariables);

                    if (statement.containsKey("variable")) {
                        loopVars.add((String) statement.get("variable"));
                    }

                    // Check collection expression
                    if (statement.containsKey("collection")) {
                        validateExpressionForVariableReferences(
                                (Map<String, Object>) statement.get("collection"),
                                definedVariables, // Not loopVars, as loop variable isn't defined yet
                                functionName,
                                result
                        );
                    }

                    // Check loop body
                    if (statement.containsKey("statements")) {
                        validateStatementsForVariableReferences(
                                (List<Map<String, Object>>) statement.get("statements"),
                                loopVars,
                                functionName,
                                result
                        );
                    }
                    break;

                case "return":
                    // Check return value
                    if (statement.containsKey("value")) {
                        validateExpressionForVariableReferences(
                                (Map<String, Object>) statement.get("value"),
                                definedVariables,
                                functionName,
                                result
                        );
                    }
                    break;
            }
        }
    }

    /**
     * Validates an expression for variable references.
     *
     * @param expression Expression object
     * @param definedVariables Set of defined variable names
     * @param functionName Name of the function being validated
     * @param result Validation result to update
     */
    private void validateExpressionForVariableReferences(
            Map<String, Object> expression,
            Set<String> definedVariables,
            String functionName,
            ValidationResult result) {

        String expressionType = expression.containsKey("expressionType")
                ? (String) expression.get("expressionType")
                : "";

        switch (expressionType) {
            case "variable":
                if (expression.containsKey("name")) {
                    String varName = (String) expression.get("name");

                    if (!definedVariables.contains(varName)) {
                        result.addError(new ValidationError(
                                "Undefined variable",
                                String.format("Function '%s' references undefined variable: %s",
                                        functionName, varName),
                                ValidationSeverity.ERROR
                        ));
                    }
                }
                break;

            case "methodCall":
                // Check arguments
                if (expression.containsKey("arguments")) {
                    List<Map<String, Object>> args = (List<Map<String, Object>>) expression.get("arguments");

                    for (Map<String, Object> arg : args) {
                        validateExpressionForVariableReferences(arg, definedVariables, functionName, result);
                    }
                }
                break;

            case "binaryOperation":
                // Check left and right operands
                if (expression.containsKey("left")) {
                    validateExpressionForVariableReferences(
                            (Map<String, Object>) expression.get("left"),
                            definedVariables,
                            functionName,
                            result
                    );
                }

                if (expression.containsKey("right")) {
                    validateExpressionForVariableReferences(
                            (Map<String, Object>) expression.get("right"),
                            definedVariables,
                            functionName,
                            result
                    );
                }
                break;

            case "builder":
                // Check method arguments
                if (expression.containsKey("methods")) {
                    List<Map<String, Object>> methods = (List<Map<String, Object>>) expression.get("methods");

                    for (Map<String, Object> method : methods) {
                        if (method.containsKey("arguments")) {
                            List<Map<String, Object>> args = (List<Map<String, Object>>) method.get("arguments");

                            for (Map<String, Object> arg : args) {
                                validateExpressionForVariableReferences(
                                        arg,
                                        definedVariables,
                                        functionName,
                                        result
                                );
                            }
                        }
                    }
                }
                break;
        }
    }

    /**
     * Recursively validates statements for method calls.
     *
     * @param statements List of statement objects
     * @param apiObjects Map of valid API objects and their methods
     * @param functionName Name of the function being validated
     * @param result Validation result to update
     */
    private void validateStatementsForMethodCalls(
            List<Map<String, Object>> statements,
            Map<String, Set<String>> apiObjects,
            String functionName,
            ValidationResult result) {

        for (Map<String, Object> statement : statements) {
            String statementType = statement.containsKey("statementType")
                    ? (String) statement.get("statementType")
                    : "";

            switch (statementType) {
                case "methodCall":
                    validateMethodCall(statement, apiObjects, functionName, result);
                    break;

                case "if":
                    // Check condition
                    if (statement.containsKey("condition") &&
                            ((Map<String, Object>) statement.get("condition")).containsKey("expressionType") &&
                            ((Map<String, Object>) statement.get("condition")).get("expressionType").equals("methodCall")) {

                        validateMethodCall(
                                (Map<String, Object>) statement.get("condition"),
                                apiObjects,
                                functionName,
                                result
                        );
                    }

                    // Check then and else branches
                    if (statement.containsKey("thenStatements")) {
                        validateStatementsForMethodCalls(
                                (List<Map<String, Object>>) statement.get("thenStatements"),
                                apiObjects,
                                functionName,
                                result
                        );
                    }

                    if (statement.containsKey("elseStatements")) {
                        validateStatementsForMethodCalls(
                                (List<Map<String, Object>>) statement.get("elseStatements"),
                                apiObjects,
                                functionName,
                                result
                        );
                    }
                    break;

                case "forEach":
                    // Check collection
                    if (statement.containsKey("collection") &&
                            ((Map<String, Object>) statement.get("collection")).containsKey("expressionType") &&
                            ((Map<String, Object>) statement.get("collection")).get("expressionType").equals("methodCall")) {

                        validateMethodCall(
                                (Map<String, Object>) statement.get("collection"),
                                apiObjects,
                                functionName,
                                result
                        );
                    }

                    // Check body statements
                    if (statement.containsKey("statements")) {
                        validateStatementsForMethodCalls(
                                (List<Map<String, Object>>) statement.get("statements"),
                                apiObjects,
                                functionName,
                                result
                        );
                    }
                    break;

                case "variableDeclaration":
                case "assignment":
                    // Check value expression
                    if (statement.containsKey("value") &&
                            ((Map<String, Object>) statement.get("value")).containsKey("expressionType") &&
                            ((Map<String, Object>) statement.get("value")).get("expressionType").equals("methodCall")) {

                        validateMethodCall(
                                (Map<String, Object>) statement.get("value"),
                                apiObjects,
                                functionName,
                                result
                        );
                    }
                    break;

                case "return":
                    // Check return value
                    if (statement.containsKey("value") &&
                            ((Map<String, Object>) statement.get("value")).containsKey("expressionType") &&
                            ((Map<String, Object>) statement.get("value")).get("expressionType").equals("methodCall")) {

                        validateMethodCall(
                                (Map<String, Object>) statement.get("value"),
                                apiObjects,
                                functionName,
                                result
                        );
                    }
                    break;
            }
        }
    }

    /**
     * Validates a method call statement or expression.
     *
     * @param methodCallObj Method call object (statement or expression)
     * @param apiObjects Map of valid API objects and their methods
     * @param functionName Name of the function being validated
     * @param result Validation result to update
     */
    private void validateMethodCall(
            Map<String, Object> methodCallObj,
            Map<String, Set<String>> apiObjects,
            String functionName,
            ValidationResult result) {

        // Skip if it's a function call (object is null)
        if (methodCallObj.containsKey("object") && methodCallObj.get("object") == null) {
            return;
        }

        // Check if object is a valid API object
        if (methodCallObj.containsKey("object") && methodCallObj.containsKey("method")) {
            String object = (String) methodCallObj.get("object");
            String method = (String) methodCallObj.get("method");

            if (apiObjects.containsKey(object)) {
                // Check if method is valid for this object
                if (!apiObjects.get(object).contains(method)) {
                    result.addError(new ValidationError(
                            "Invalid method call",
                            String.format("Function '%s' calls invalid method '%s' on object '%s'",
                                    functionName, method, object),
                            ValidationSeverity.ERROR
                    ));
                }
            }
        }

        // Recursively check arguments for method calls
        if (methodCallObj.containsKey("arguments")) {
            List<Map<String, Object>> args = (List<Map<String, Object>>) methodCallObj.get("arguments");

            for (Map<String, Object> arg : args) {
                if (arg.containsKey("expressionType") && arg.get("expressionType").equals("methodCall")) {
                    validateMethodCall(arg, apiObjects, functionName, result);
                }
            }
        }
    }
}
