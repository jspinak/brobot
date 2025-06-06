package io.github.jspinak.brobot.json.schemaValidation.crossref;

import io.github.jspinak.brobot.json.schemaValidation.model.ValidationError;
import io.github.jspinak.brobot.json.schemaValidation.model.ValidationResult;
import io.github.jspinak.brobot.json.schemaValidation.model.ValidationSeverity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Validates references related to automation functions, including function calls,
 * parameters, and references to functions from other components.
 */
@Component
public class FunctionReferenceValidator {
    private static final Logger logger = LoggerFactory.getLogger(FunctionReferenceValidator.class);

    /**
     * Validates internal references within the DSL model.
     * This includes function variable references, method calls, etc.
     *
     * @param dslModel Parsed DSL model
     * @return Validation result
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
     * Validates references to automation functions from UI buttons.
     *
     * @param projectModel Parsed project model
     * @param dslModel Parsed DSL model
     * @return Validation result
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
     * Validates function references within the DSL.
     *
     * @param dsl DSL model as a map
     * @param result Validation result to update
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
     * Validates variable references within functions.
     *
     * @param dsl DSL model as a map
     * @param result Validation result to update
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
     * Validates method calls within functions for correct object references and method names.
     *
     * @param dsl DSL model as a map
     * @param result Validation result to update
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
