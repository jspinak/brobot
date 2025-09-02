package io.github.jspinak.brobot.runner.json.validation.business;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.runner.json.validation.model.ValidationError;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationResult;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationSeverity;

import java.util.*;

/**
 * Validates business rules for automation functions in Brobot DSL configurations.
 * 
 * <p>This validator enforces best practices and quality constraints on automation
 * functions to ensure they are maintainable, performant, and reliable. It goes
 * beyond syntax validation to analyze function implementation patterns, complexity
 * metrics, and potential runtime issues.</p>
 * 
 * <h2>Validation Categories:</h2>
 * <ul>
 *   <li><b>Complexity Analysis</b> - Enforces limits on function size and nesting depth</li>
 *   <li><b>Error Handling</b> - Ensures proper error handling for action calls</li>
 *   <li><b>State Management</b> - Validates safe state manipulation patterns</li>
 *   <li><b>Performance Optimization</b> - Detects inefficient patterns and anti-patterns</li>
 * </ul>
 * 
 * <h2>Key Metrics and Thresholds:</h2>
 * <ul>
 *   <li>Maximum statements per function: 50</li>
 *   <li>Maximum nesting depth: 5 levels</li>
 *   <li>Maximum action calls per function: 20</li>
 * </ul>
 * 
 * <h2>Why These Rules Matter:</h2>
 * <p>Complex functions are harder to test, debug, and maintain. Functions with
 * excessive nesting or too many statements often indicate design issues that
 * should be refactored. Performance rules help prevent common automation
 * bottlenecks like repeated image searches or unnecessary waits.</p>
 * 
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * FunctionRuleValidator validator = new FunctionRuleValidator();
 * ValidationResult result = validator.validateFunctionRules(dslModel);
 * 
 * // Check for complexity warnings
 * result.getWarnings().stream()
 *     .filter(e -> e.errorCode().contains("complex"))
 *     .forEach(warning -> {
 *         logger.warn("Function complexity issue: {}", warning.message());
 *         // Consider refactoring complex functions
 *     });
 * }</pre>
 * 
 * @see BusinessRuleValidator for the parent validation coordinator
 * @see ValidationResult for understanding validation outcomes
 * @author jspinak
 */
@Component
public class FunctionRuleValidator {
    private static final Logger logger = LoggerFactory.getLogger(FunctionRuleValidator.class);

    // Thresholds for complexity metrics
    private static final int MAX_STATEMENTS_PER_FUNCTION = 50;
    private static final int MAX_DEPTH = 5; // Max nesting depth
    private static final int MAX_ACTION_CALLS = 20; // Max number of action calls in a function

    /**
     * Validates function-specific business rules in the DSL model.
     * 
     * <p>This method performs comprehensive analysis of all automation functions
     * defined in the DSL, checking each against established best practices and
     * quality metrics. The validation is thorough but non-blocking - issues are
     * reported as warnings or errors based on severity.</p>
     * 
     * <h3>Validation Process:</h3>
     * <ol>
     *   <li>Validates the DSL model structure</li>
     *   <li>Analyzes each function for complexity metrics</li>
     *   <li>Checks error handling patterns</li>
     *   <li>Validates state management practices</li>
     *   <li>Identifies performance anti-patterns</li>
     * </ol>
     * 
     * @param dslModel Parsed DSL model containing automation function definitions.
     *                 Expected to be a Map with an "automationFunctions" array
     * @return ValidationResult containing all discovered issues categorized by severity:
     *         <ul>
     *           <li>CRITICAL - Invalid model structure</li>
     *           <li>ERROR - Serious issues that may cause runtime failures</li>
     *           <li>WARNING - Quality issues that should be addressed</li>
     *         </ul>
     */
    public ValidationResult validateFunctionRules(Object dslModel) {
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

            // Check function complexity
            validateFunctionComplexity(dsl, result);

            // Check for proper error handling
            validateErrorHandling(dsl, result);

            // Check for proper state management
            validateStateManagement(dsl, result);

            // Check for performance issues
            validatePerformance(dsl, result);

        } catch (ClassCastException e) {
            logger.error("DSL model is not a valid type", e);
            result.addError(new ValidationError(
                    "Invalid DSL model type",
                    "DSL model could not be processed: " + e.getMessage(),
                    ValidationSeverity.CRITICAL
            ));
        } catch (Exception e) {
            logger.error("Error during function rule validation", e);
            result.addError(new ValidationError(
                    "Validation error",
                    "Error validating function rules: " + e.getMessage(),
                    ValidationSeverity.ERROR
            ));
        }

        return result;
    }

    /**
     * Validates function complexity metrics to ensure maintainability.
     * 
     * <p>This method analyzes various complexity metrics for each function and
     * reports violations of established thresholds. Complex functions are harder
     * to understand, test, and debug, so keeping complexity under control is
     * essential for long-term maintenance.</p>
     * 
     * <h3>Complexity Metrics Checked:</h3>
     * <ul>
     *   <li><b>Statement Count</b> - Total number of statements including nested ones</li>
     *   <li><b>Nesting Depth</b> - Maximum depth of nested control structures</li>
     *   <li><b>Action Calls</b> - Number of automation action invocations</li>
     * </ul>
     * 
     * <h3>Why These Metrics:</h3>
     * <ul>
     *   <li>High statement count indicates the function does too much</li>
     *   <li>Deep nesting makes control flow hard to follow</li>
     *   <li>Many action calls suggest the function should be split</li>
     * </ul>
     * 
     * @param dsl The DSL model containing function definitions
     * @param result ValidationResult to add complexity warnings to
     */
    private void validateFunctionComplexity(Map<String, Object> dsl, ValidationResult result) {
        if (!dsl.containsKey("automationFunctions")) {
            return;
        }

        try {
            List<Map<String, Object>> functions = (List<Map<String, Object>>) dsl.get("automationFunctions");

            for (Map<String, Object> function : functions) {
                String functionName = function.containsKey("name") ?
                        (String) function.get("name") : "unknown";

                if (function.containsKey("statements")) {
                    List<Map<String, Object>> statements = (List<Map<String, Object>>) function.get("statements");

                    // Check total statement count
                    int totalStatements = countTotalStatements(statements);
                    if (totalStatements > MAX_STATEMENTS_PER_FUNCTION) {
                        result.addError(new ValidationError(
                                "Function too complex",
                                String.format("Function '%s' has %d statements, which exceeds the recommended maximum of %d",
                                        functionName, totalStatements, MAX_STATEMENTS_PER_FUNCTION),
                                ValidationSeverity.WARNING
                        ));
                    }

                    // Check nesting depth
                    int maxDepth = calculateMaxDepth(statements);
                    if (maxDepth > MAX_DEPTH) {
                        result.addError(new ValidationError(
                                "Excessive nesting",
                                String.format("Function '%s' has a nesting depth of %d, which exceeds the recommended maximum of %d",
                                        functionName, maxDepth, MAX_DEPTH),
                                ValidationSeverity.WARNING
                        ));
                    }

                    // Check number of action calls
                    int actionCalls = countActionCalls(statements);
                    if (actionCalls > MAX_ACTION_CALLS) {
                        result.addError(new ValidationError(
                                "Too many action calls",
                                String.format("Function '%s' makes %d action calls, which exceeds the recommended maximum of %d",
                                        functionName, actionCalls, MAX_ACTION_CALLS),
                                ValidationSeverity.WARNING
                        ));
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Error during function complexity validation", e);
            result.addError(new ValidationError(
                    "Validation error",
                    "Error validating function complexity: " + e.getMessage(),
                    ValidationSeverity.ERROR
            ));
        }
    }

    /**
     * Validates error handling patterns in automation functions.
     * 
     * <p>This method ensures that functions properly handle potential failures,
     * especially when making action calls that could fail due to UI changes,
     * timing issues, or other runtime conditions. Proper error handling is
     * crucial for robust automation.</p>
     * 
     * <h3>Error Handling Checks:</h3>
     * <ul>
     *   <li>Action calls should have result checking</li>
     *   <li>Failed actions should trigger appropriate recovery</li>
     *   <li>Functions should not ignore action failures silently</li>
     * </ul>
     * 
     * <h3>Best Practices Enforced:</h3>
     * <p>Functions that call actions should check the result and handle failures
     * appropriately, either by retrying, falling back to alternatives, or
     * failing gracefully with clear error reporting.</p>
     * 
     * @param dsl The DSL model containing function definitions
     * @param result ValidationResult to add error handling warnings to
     */
    private void validateErrorHandling(Map<String, Object> dsl, ValidationResult result) {
        if (!dsl.containsKey("automationFunctions")) {
            return;
        }

        try {
            List<Map<String, Object>> functions = (List<Map<String, Object>>) dsl.get("automationFunctions");

            for (Map<String, Object> function : functions) {
                String functionName = function.containsKey("name") ?
                        (String) function.get("name") : "unknown";

                if (function.containsKey("statements")) {
                    List<Map<String, Object>> statements = (List<Map<String, Object>>) function.get("statements");

                    // Check for lack of error handling for action calls
                    List<Map<String, Object>> actionCalls = findActionCalls(statements);
                    boolean hasErrorHandling = hasActionErrorHandling(statements);

                    if (!actionCalls.isEmpty() && !hasErrorHandling) {
                        result.addError(new ValidationError(
                                "Insufficient error handling",
                                String.format("Function '%s' makes action calls but doesn't appear to have error handling",
                                        functionName),
                                ValidationSeverity.WARNING
                        ));
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Error during error handling validation", e);
            result.addError(new ValidationError(
                    "Validation error",
                    "Error validating error handling: " + e.getMessage(),
                    ValidationSeverity.ERROR
            ));
        }
    }

    /**
     * Validates state management practices in automation functions.
     * 
     * <p>This method checks that functions interact with application states
     * safely and consistently. Poor state management can lead to race conditions,
     * inconsistent UI states, and difficult-to-debug automation failures.</p>
     * 
     * <h3>State Management Checks:</h3>
     * <ul>
     *   <li>Functions modifying states should verify preconditions</li>
     *   <li>State transitions should be validated before and after</li>
     *   <li>Concurrent state modifications should be avoided</li>
     * </ul>
     * 
     * <h3>Common Issues Detected:</h3>
     * <ul>
     *   <li>Modifying states without checking current state</li>
     *   <li>Assuming state transitions always succeed</li>
     *   <li>Not handling unexpected state conditions</li>
     * </ul>
     * 
     * @param dsl The DSL model containing function definitions  
     * @param result ValidationResult to add state management warnings to
     */
    private void validateStateManagement(Map<String, Object> dsl, ValidationResult result) {
        if (!dsl.containsKey("automationFunctions")) {
            return;
        }

        try {
            List<Map<String, Object>> functions = (List<Map<String, Object>>) dsl.get("automationFunctions");

            for (Map<String, Object> function : functions) {
                String functionName = function.containsKey("name") ?
                        (String) function.get("name") : "unknown";

                if (function.containsKey("statements")) {
                    List<Map<String, Object>> statements = (List<Map<String, Object>>) function.get("statements");

                    // Check for state management issues
                    boolean modifiesStates = containsStateModifications(statements);
                    boolean checksStates = containsStateChecks(statements);

                    // If modifying states but not checking them, might be an issue
                    if (modifiesStates && !checksStates) {
                        result.addError(new ValidationError(
                                "State management concern",
                                String.format("Function '%s' modifies states but doesn't verify state conditions",
                                        functionName),
                                ValidationSeverity.WARNING
                        ));
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Error during state management validation", e);
            result.addError(new ValidationError(
                    "Validation error",
                    "Error validating state management: " + e.getMessage(),
                    ValidationSeverity.ERROR
            ));
        }
    }

    /**
     * Validates performance considerations in automation functions.
     * 
     * <p>This method identifies common performance anti-patterns that can make
     * automation slow, unreliable, or resource-intensive. Early detection of
     * these issues helps maintain fast and efficient automation suites.</p>
     * 
     * <h3>Performance Patterns Checked:</h3>
     * <ul>
     *   <li><b>Repeated Searches</b> - Same image searched multiple times</li>
     *   <li><b>Heavy Operations</b> - Expensive operations like findAll, findColor</li>
     *   <li><b>Inefficient Waits</b> - Fixed waits instead of conditional waits</li>
     * </ul>
     * 
     * <h3>Performance Best Practices:</h3>
     * <ul>
     *   <li>Cache search results when checking the same element multiple times</li>
     *   <li>Use targeted searches instead of scanning entire screen</li>
     *   <li>Prefer conditional waits (wait for element) over fixed delays</li>
     *   <li>Minimize use of computationally expensive operations</li>
     * </ul>
     * 
     * @param dsl The DSL model containing function definitions
     * @param result ValidationResult to add performance warnings to
     */
    private void validatePerformance(Map<String, Object> dsl, ValidationResult result) {
        if (!dsl.containsKey("automationFunctions")) {
            return;
        }

        try {
            List<Map<String, Object>> functions = (List<Map<String, Object>>) dsl.get("automationFunctions");

            for (Map<String, Object> function : functions) {
                String functionName = function.containsKey("name") ?
                        (String) function.get("name") : "unknown";

                if (function.containsKey("statements")) {
                    List<Map<String, Object>> statements = (List<Map<String, Object>>) function.get("statements");

                    // Check for inefficient patterns
                    boolean hasRepeatedSearches = hasRepeatedImageSearches(statements);
                    boolean hasHeavyOperations = hasHeavyOperations(statements);
                    boolean hasUnnecessaryWaits = hasUnnecessaryWaits(statements);

                    if (hasRepeatedSearches) {
                        result.addError(new ValidationError(
                                "Inefficient search pattern",
                                String.format("Function '%s' appears to repeatedly search for the same images",
                                        functionName),
                                ValidationSeverity.WARNING
                        ));
                    }

                    if (hasHeavyOperations) {
                        result.addError(new ValidationError(
                                "Performance concern",
                                String.format("Function '%s' uses computationally expensive operations",
                                        functionName),
                                ValidationSeverity.WARNING
                        ));
                    }

                    if (hasUnnecessaryWaits) {
                        result.addError(new ValidationError(
                                "Inefficient wait pattern",
                                String.format("Function '%s' appears to use fixed waits where conditional waits would be better",
                                        functionName),
                                ValidationSeverity.WARNING
                        ));
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Error during performance validation", e);
            result.addError(new ValidationError(
                    "Validation error",
                    "Error validating performance considerations: " + e.getMessage(),
                    ValidationSeverity.ERROR
            ));
        }
    }

    /**
     * Counts the total number of statements in a function, including nested statements.
     */
    private int countTotalStatements(List<Map<String, Object>> statements) {
        int count = 0;

        for (Map<String, Object> statement : statements) {
            count++;

            String statementType = statement.containsKey("statementType") ?
                    (String) statement.get("statementType") : "";

            // Count nested statements in control structures
            switch (statementType) {
                case "if":
                    if (statement.containsKey("thenStatements")) {
                        count += countTotalStatements((List<Map<String, Object>>) statement.get("thenStatements"));
                    }
                    if (statement.containsKey("elseStatements")) {
                        count += countTotalStatements((List<Map<String, Object>>) statement.get("elseStatements"));
                    }
                    break;

                case "forEach":
                    if (statement.containsKey("statements")) {
                        count += countTotalStatements((List<Map<String, Object>>) statement.get("statements"));
                    }
                    break;
            }
        }

        return count;
    }

    /**
     * Calculates the maximum nesting depth in a function.
     */
    private int calculateMaxDepth(List<Map<String, Object>> statements) {
        return calculateDepthRecursive(statements, 1);
    }

    /**
     * Recursive helper for calculating nesting depth.
     */
    private int calculateDepthRecursive(List<Map<String, Object>> statements, int currentDepth) {
        int maxDepth = currentDepth;

        for (Map<String, Object> statement : statements) {
            String statementType = statement.containsKey("statementType") ?
                    (String) statement.get("statementType") : "";

            // Check nested control structures
            switch (statementType) {
                case "if":
                    int thenDepth = currentDepth;
                    int elseDepth = currentDepth;

                    if (statement.containsKey("thenStatements")) {
                        thenDepth = calculateDepthRecursive(
                                (List<Map<String, Object>>) statement.get("thenStatements"),
                                currentDepth + 1);
                    }

                    if (statement.containsKey("elseStatements")) {
                        elseDepth = calculateDepthRecursive(
                                (List<Map<String, Object>>) statement.get("elseStatements"),
                                currentDepth + 1);
                    }

                    maxDepth = Math.max(maxDepth, Math.max(thenDepth, elseDepth));
                    break;

                case "forEach":
                    if (statement.containsKey("statements")) {
                        int loopDepth = calculateDepthRecursive(
                                (List<Map<String, Object>>) statement.get("statements"),
                                currentDepth + 1);
                        maxDepth = Math.max(maxDepth, loopDepth);
                    }
                    break;
            }
        }

        return maxDepth;
    }

    /**
     * Counts the number of action calls in a function.
     */
    private int countActionCalls(List<Map<String, Object>> statements) {
        int count = 0;

        for (Map<String, Object> statement : statements) {
            String statementType = statement.containsKey("statementType") ?
                    (String) statement.get("statementType") : "";

            // Check for action method calls
            if (statementType.equals("methodCall")) {
                String object = statement.containsKey("object") ?
                        (String) statement.get("object") : "";
                String method = statement.containsKey("method") ?
                        (String) statement.get("method") : "";

                if (object.equals("action") || object.equals("stateTransitionsManagement")) {
                    count++;
                }
            }

            // Check nested statements
            switch (statementType) {
                case "if":
                    if (statement.containsKey("thenStatements")) {
                        count += countActionCalls((List<Map<String, Object>>) statement.get("thenStatements"));
                    }
                    if (statement.containsKey("elseStatements")) {
                        count += countActionCalls((List<Map<String, Object>>) statement.get("elseStatements"));
                    }
                    break;

                case "forEach":
                    if (statement.containsKey("statements")) {
                        count += countActionCalls((List<Map<String, Object>>) statement.get("statements"));
                    }
                    break;
            }
        }

        return count;
    }

    /**
     * Finds all action method calls in a function.
     */
    private List<Map<String, Object>> findActionCalls(List<Map<String, Object>> statements) {
        List<Map<String, Object>> actionCalls = new ArrayList<>();

        for (Map<String, Object> statement : statements) {
            String statementType = statement.containsKey("statementType") ?
                    (String) statement.get("statementType") : "";

            // Check for action method calls
            if (statementType.equals("methodCall")) {
                String object = statement.containsKey("object") ?
                        (String) statement.get("object") : "";

                if (object.equals("action") || object.equals("stateTransitionsManagement")) {
                    actionCalls.add(statement);
                }
            }

            // Check nested statements
            switch (statementType) {
                case "if":
                    if (statement.containsKey("thenStatements")) {
                        actionCalls.addAll(findActionCalls((List<Map<String, Object>>) statement.get("thenStatements")));
                    }
                    if (statement.containsKey("elseStatements")) {
                        actionCalls.addAll(findActionCalls((List<Map<String, Object>>) statement.get("elseStatements")));
                    }
                    break;

                case "forEach":
                    if (statement.containsKey("statements")) {
                        actionCalls.addAll(findActionCalls((List<Map<String, Object>>) statement.get("statements")));
                    }
                    break;
            }
        }

        return actionCalls;
    }

    /**
     * Checks if a function has error handling for action calls.
     */
    private boolean hasActionErrorHandling(List<Map<String, Object>> statements) {
        // Look for if statements that check action results
        for (Map<String, Object> statement : statements) {
            String statementType = statement.containsKey("statementType") ?
                    (String) statement.get("statementType") : "";

            if (statementType.equals("if")) {
                if (statement.containsKey("condition") &&
                        isActionResultCheck((Map<String, Object>) statement.get("condition"))) {
                    return true;
                }
            }

            // Check nested statements
            switch (statementType) {
                case "if":
                    if (statement.containsKey("thenStatements") &&
                            hasActionErrorHandling((List<Map<String, Object>>) statement.get("thenStatements"))) {
                        return true;
                    }
                    if (statement.containsKey("elseStatements") &&
                            hasActionErrorHandling((List<Map<String, Object>>) statement.get("elseStatements"))) {
                        return true;
                    }
                    break;

                case "forEach":
                    if (statement.containsKey("statements") &&
                            hasActionErrorHandling((List<Map<String, Object>>) statement.get("statements"))) {
                        return true;
                    }
                    break;
            }
        }

        return false;
    }

    /**
     * Checks if an expression is checking an action result.
     */
    private boolean isActionResultCheck(Map<String, Object> expression) {
        if (!expression.containsKey("expressionType")) {
            return false;
        }

        String exprType = (String) expression.get("expressionType");

        if (exprType.equals("methodCall")) {
            // Check if it's calling isSuccess() on a result
            String method = expression.containsKey("method") ?
                    (String) expression.get("method") : "";

            if (method.equals("isSuccess")) {
                return true;
            }
        } else if (exprType.equals("binaryOperation")) {
            // Check operands
            if (expression.containsKey("left")) {
                if (isActionResultCheck((Map<String, Object>) expression.get("left"))) {
                    return true;
                }
            }

            if (expression.containsKey("right")) {
                if (isActionResultCheck((Map<String, Object>) expression.get("right"))) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Checks if a function contains state modification operations.
     */
    private boolean containsStateModifications(List<Map<String, Object>> statements) {
        for (Map<String, Object> statement : statements) {
            String statementType = statement.containsKey("statementType") ?
                    (String) statement.get("statementType") : "";

            // Check for state modification method calls
            if (statementType.equals("methodCall")) {
                String object = statement.containsKey("object") ?
                        (String) statement.get("object") : "";
                String method = statement.containsKey("method") ?
                        (String) statement.get("method") : "";

                if (object.equals("stateTransitionsManagement") && method.equals("openState")) {
                    return true;
                }
            }

            // Check nested statements
            switch (statementType) {
                case "if":
                    if (statement.containsKey("thenStatements") &&
                            containsStateModifications((List<Map<String, Object>>) statement.get("thenStatements"))) {
                        return true;
                    }
                    if (statement.containsKey("elseStatements") &&
                            containsStateModifications((List<Map<String, Object>>) statement.get("elseStatements"))) {
                        return true;
                    }
                    break;

                case "forEach":
                    if (statement.containsKey("statements") &&
                            containsStateModifications((List<Map<String, Object>>) statement.get("statements"))) {
                        return true;
                    }
                    break;
            }
        }

        return false;
    }

    /**
     * Checks if a function contains state verification operations.
     */
    private boolean containsStateChecks(List<Map<String, Object>> statements) {
        for (Map<String, Object> statement : statements) {
            String statementType = statement.containsKey("statementType") ?
                    (String) statement.get("statementType") : "";

            // Check for state check method calls
            if (statementType.equals("methodCall")) {
                String object = statement.containsKey("object") ?
                        (String) statement.get("object") : "";
                String method = statement.containsKey("method") ?
                        (String) statement.get("method") : "";

                if (object.equals("stateTransitionsManagement") && 
                    (method.equals("isStateActive") || method.equals("getCurrentState"))) {
                    return true;
                }
            }

            // Also check conditions in if statements
            if (statementType.equals("if") && statement.containsKey("condition")) {
                if (isStateCheckExpression((Map<String, Object>) statement.get("condition"))) {
                    return true;
                }
            }

            // Check nested statements
            switch (statementType) {
                case "if":
                    if (statement.containsKey("thenStatements") &&
                            containsStateChecks((List<Map<String, Object>>) statement.get("thenStatements"))) {
                        return true;
                    }
                    if (statement.containsKey("elseStatements") &&
                            containsStateChecks((List<Map<String, Object>>) statement.get("elseStatements"))) {
                        return true;
                    }
                    break;

                case "forEach":
                    if (statement.containsKey("statements") &&
                            containsStateChecks((List<Map<String, Object>>) statement.get("statements"))) {
                        return true;
                    }
                    break;
            }
        }

        return false;
    }

    /**
     * Checks if an expression is checking a state condition.
     */
    private boolean isStateCheckExpression(Map<String, Object> expression) {
        if (!expression.containsKey("expressionType")) {
            return false;
        }

        String exprType = (String) expression.get("expressionType");

        if (exprType.equals("methodCall")) {
            String object = expression.containsKey("object") ?
                    (String) expression.get("object") : "";
            String method = expression.containsKey("method") ?
                    (String) expression.get("method") : "";

            if (object.equals("stateTransitionsManagement") && method.equals("isStateActive")) {
                return true;
            }
        } else if (exprType.equals("binaryOperation")) {
            // Check operands
            if (expression.containsKey("left")) {
                if (isStateCheckExpression((Map<String, Object>) expression.get("left"))) {
                    return true;
                }
            }

            if (expression.containsKey("right")) {
                if (isStateCheckExpression((Map<String, Object>) expression.get("right"))) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Checks if a function has repeated image searches.
     */
    private boolean hasRepeatedImageSearches(List<Map<String, Object>> statements) {
        // This would require tracking objects used in action.find() calls
        // For simplicity, we'll check for patterns that might indicate repeated searches
        Map<String, Integer> searchCalls = new HashMap<>();

        for (Map<String, Object> statement : statements) {
            if (isImageSearchCall(statement)) {
                String searchTarget = getSearchTarget(statement);
                searchCalls.put(searchTarget, searchCalls.getOrDefault(searchTarget, 0) + 1);
            }

            // Check nested statements
            String statementType = statement.containsKey("statementType") ?
                    (String) statement.get("statementType") : "";

            switch (statementType) {
                case "if":
                    if (statement.containsKey("thenStatements")) {
                        for (Map<String, Object> nestedStatement :
                                (List<Map<String, Object>>) statement.get("thenStatements")) {
                            if (isImageSearchCall(nestedStatement)) {
                                String searchTarget = getSearchTarget(nestedStatement);
                                searchCalls.put(searchTarget, searchCalls.getOrDefault(searchTarget, 0) + 1);
                            }
                        }
                    }
                    break;
            }
        }

        // Check if any image is searched for multiple times
        for (Integer count : searchCalls.values()) {
            if (count > 1) {
                return true;
            }
        }

        return false;
    }

    /**
     * Determines if a statement is an image search call.
     */
    private boolean isImageSearchCall(Map<String, Object> statement) {
        if (!statement.containsKey("statementType") ||
                !statement.get("statementType").equals("methodCall")) {
            return false;
        }

        String object = statement.containsKey("object") ?
                (String) statement.get("object") : "";
        String method = statement.containsKey("method") ?
                (String) statement.get("method") : "";

        return object.equals("action") && method.equals("find");
    }

    /**
     * Gets a search target identifier from a search call.
     */
    private String getSearchTarget(Map<String, Object> statement) {
        // Tries to extract the first argument as the search target
        if (statement.containsKey("arguments")) {
            List<Map<String, Object>> args = (List<Map<String, Object>>) statement.get("arguments");
            if (!args.isEmpty() && args.getFirst() != null) {
                // Use the first argument as the search target
                return args.getFirst().toString();
            }
        }

        // Fallback: return a generic identifier
        return "image-search";
    }

    /**
     * Checks if a function uses computationally expensive operations.
     */
    private boolean hasHeavyOperations(List<Map<String, Object>> statements) {
        for (Map<String, Object> statement : statements) {
            if (isHeavyOperation(statement)) {
                return true;
            }

            // Check nested statements
            String statementType = statement.containsKey("statementType") ?
                    (String) statement.get("statementType") : "";

            switch (statementType) {
                case "if":
                    if (statement.containsKey("thenStatements") &&
                            hasHeavyOperations((List<Map<String, Object>>) statement.get("thenStatements"))) {
                        return true;
                    }
                    if (statement.containsKey("elseStatements") &&
                            hasHeavyOperations((List<Map<String, Object>>) statement.get("elseStatements"))) {
                        return true;
                    }
                    break;

                case "forEach":
                    if (statement.containsKey("statements") &&
                            hasHeavyOperations((List<Map<String, Object>>) statement.get("statements"))) {
                        return true;
                    }
                    break;
            }
        }

        return false;
    }

    /**
     * Determines if a statement uses computationally expensive operations.
     */
    private boolean isHeavyOperation(Map<String, Object> statement) {
        if (!statement.containsKey("statementType") ||
                !statement.get("statementType").equals("methodCall")) {
            return false;
        }

        String object = statement.containsKey("object") ?
                (String) statement.get("object") : "";
        String method = statement.containsKey("method") ?
                (String) statement.get("method") : "";

        // Check for known heavy operations
        if (object.equals("action")) {
            List<String> heavyMethods = Arrays.asList("findAll", "findColor", "findMotion", "findText");
            return heavyMethods.contains(method);
        }

        return false;
    }

    /**
     * Checks if a function uses fixed waits where conditional waits would be better.
     */
    private boolean hasUnnecessaryWaits(List<Map<String, Object>> statements) {
        boolean hasFixedWaits = false;
        boolean usesConditionalWaits = false;

        for (Map<String, Object> statement : statements) {
            if (isFixedWait(statement)) {
                hasFixedWaits = true;
            }

            if (isConditionalWait(statement)) {
                usesConditionalWaits = true;
            }

            // Check nested statements
            String statementType = statement.containsKey("statementType") ?
                    (String) statement.get("statementType") : "";

            switch (statementType) {
                case "if":
                    if (statement.containsKey("thenStatements")) {
                        if (hasFixedWaitNotFollowedByCheck(
                                (List<Map<String, Object>>) statement.get("thenStatements"))) {
                            return true;
                        }
                    }
                    if (statement.containsKey("elseStatements")) {
                        if (hasFixedWaitNotFollowedByCheck(
                                (List<Map<String, Object>>) statement.get("elseStatements"))) {
                            return true;
                        }
                    }
                    break;

                case "forEach":
                    if (statement.containsKey("statements")) {
                        if (hasFixedWaitNotFollowedByCheck(
                                (List<Map<String, Object>>) statement.get("statements"))) {
                            return true;
                        }
                    }
                    break;
            }
        }

        // If using fixed waits but no conditional waits, might be an issue
        return hasFixedWaits && !usesConditionalWaits;
    }

    /**
     * Determines if a statement is a fixed wait.
     */
    private boolean isFixedWait(Map<String, Object> statement) {
        if (!statement.containsKey("statementType")) {
            return false;
        }
        
        String statementType = (String) statement.get("statementType");
        
        // Direct wait statement
        if (statementType.equals("wait")) {
            return true;
        }
        
        // Method call wait
        if (!statementType.equals("methodCall")) {
            return false;
        }

        String object = statement.containsKey("object") ?
                (String) statement.get("object") : "";
        String method = statement.containsKey("method") ?
                (String) statement.get("method") : "";

        return (object.equals("time") || object.equals("Thread")) &&
                (method.equals("wait") || method.equals("sleep"));
    }

    /**
     * Determines if a statement is a conditional wait.
     */
    private boolean isConditionalWait(Map<String, Object> statement) {
        if (!statement.containsKey("statementType") ||
                !statement.get("statementType").equals("methodCall")) {
            return false;
        }

        String object = statement.containsKey("object") ?
                (String) statement.get("object") : "";
        String method = statement.containsKey("method") ?
                (String) statement.get("method") : "";

        // Check for action calls with wait parameters
        if (object.equals("action")) {
            if (statement.containsKey("arguments")) {
                List<Map<String, Object>> args = (List<Map<String, Object>>) statement.get("arguments");

                return !args.isEmpty() && isActionOptionsWithWait(args.getFirst());
            }
        }

        return false;
    }

    /**
     * Determines if an expression is ActionOptions with wait settings.
     */
    private boolean isActionOptionsWithWait(Map<String, Object> expression) {
        if (!expression.containsKey("expressionType")) {
            return false;
        }

        // This is a simplified check - in a real implementation, we'd examine
        // the ActionOptions builder methods for setMaxWait calls
        return expression.get("expressionType").equals("builder");
    }

    /**
     * Checks if the statements contain a fixed wait not followed by a condition check.
     */
    private boolean hasFixedWaitNotFollowedByCheck(List<Map<String, Object>> statements) {
        for (int i = 0; i < statements.size() - 1; i++) {
            if (isFixedWait(statements.get(i))) {
                // Check if the next statement is a condition check
                if (!isConditionCheck(statements.get(i + 1))) {
                    return true;
                }
            }
        }

        // Check last statement
        return !statements.isEmpty() && isFixedWait(statements.getLast());
    }

    /**
     * Determines if a statement is a condition check.
     */
    private boolean isConditionCheck(Map<String, Object> statement) {
        String statementType = statement.containsKey("statementType") ?
                (String) statement.get("statementType") : "";

        if (statementType.equals("if")) {
            return true;
        }

        if (statementType.equals("methodCall")) {
            String object = statement.containsKey("object") ?
                    (String) statement.get("object") : "";
            String method = statement.containsKey("method") ?
                    (String) statement.get("method") : "";

            // Check for find or isStateActive calls
            return (object.equals("action") && method.equals("find")) ||
                    (object.equals("stateTransitionsManagement") && method.equals("isStateActive"));
        }

        return false;
    }
}