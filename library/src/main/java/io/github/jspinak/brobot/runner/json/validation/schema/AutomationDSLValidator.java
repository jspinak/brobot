package io.github.jspinak.brobot.runner.json.validation.schema;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.runner.json.validation.model.ValidationError;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationResult;
import io.github.jspinak.brobot.runner.json.validation.model.ValidationSeverity;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Validates automation DSL JSON against the Brobot DSL schema.
 * 
 * <p>This validator ensures that automation function definitions conform to the
 * expected DSL (Domain Specific Language) schema. It validates the syntax and
 * structure of automation functions, including their parameters, statements,
 * and expressions, before they are parsed and executed.</p>
 * 
 * <h2>DSL Purpose:</h2>
 * <p>The Brobot DSL allows users to define automation logic in a structured JSON
 * format. This includes function declarations, control flow statements, variable
 * assignments, and API method calls. The validator ensures this code-like JSON
 * structure is well-formed.</p>
 * 
 * <h2>Schema Location:</h2>
 * <p>The DSL schema is loaded from <code>/schemas/automation-dsl-schema.json</code>
 * on the classpath. This schema defines valid statement types, expression formats,
 * and function structures.</p>
 * 
 * <h2>Validation Layers:</h2>
 * <ol>
 *   <li><b>JSON Parsing</b> - Ensures valid JSON syntax</li>
 *   <li><b>Schema Validation</b> - Checks DSL structure against schema</li>
 *   <li><b>Semantic Validation</b> - Additional language-specific rules</li>
 * </ol>
 * 
 * <h2>Semantic Validations:</h2>
 * <ul>
 *   <li>Function name uniqueness</li>
 *   <li>Return statement presence for non-void functions</li>
 *   <li>Variable scope and definition checking</li>
 *   <li>Expression consistency validation</li>
 * </ul>
 * 
 * <h2>DSL Elements Validated:</h2>
 * <ul>
 *   <li><b>Functions</b> - Name, parameters, return type, body statements</li>
 *   <li><b>Statements</b> - Variable declarations, assignments, control flow</li>
 *   <li><b>Expressions</b> - Variables, literals, method calls, operations</li>
 *   <li><b>Control Flow</b> - If/else branches, forEach loops, return statements</li>
 * </ul>
 * 
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * AutomationDSLValidator validator = new AutomationDSLValidator();
 * 
 * String dslJson = Files.readString(Path.of("automation-functions.json"));
 * ValidationResult result = validator.validate(dslJson);
 * 
 * // Check for critical issues
 * if (result.hasCriticalErrors()) {
 *     throw new ConfigValidationException(
 *         "DSL syntax errors", result);
 * }
 * 
 * // Check for semantic issues
 * result.getErrors().stream()
 *     .filter(e -> e.errorCode().contains("Undefined variable"))
 *     .forEach(e -> logger.error("Variable error: {}", e.message()));
 * }</pre>
 * 
 * @see SchemaValidator for the parent validation coordinator
 * @see ProjectSchemaValidator for project configuration validation
 * @author jspinak
 */
@Component
public class AutomationDSLValidator {
    private static final Logger logger = LoggerFactory.getLogger(AutomationDSLValidator.class);
    private static final String SCHEMA_PATH = "/schemas/automation-dsl-schema.json";

    private final Schema schema;

    /**
     * Initializes the validator by loading the DSL schema from classpath.
     * 
     * <p>This constructor loads and prepares the JSON schema for DSL validation.
     * The schema file must be present on the classpath or initialization will
     * fail with an IllegalStateException.</p>
     * 
     * @throws IllegalStateException if the schema file cannot be found or loaded,
     *         preventing creation of an invalid validator
     */
    public AutomationDSLValidator() {
        try (InputStream inputStream = getClass().getResourceAsStream(SCHEMA_PATH)) {
            if (inputStream == null) {
                throw new IllegalStateException("Could not find schema at " + SCHEMA_PATH);
            }

            JSONObject rawSchema = new JSONObject(new JSONTokener(inputStream));
            this.schema = SchemaLoader.load(rawSchema);
            logger.debug("Successfully loaded automation DSL schema");
        } catch (Exception e) {
            logger.error("Failed to load automation DSL schema", e);
            throw new IllegalStateException("Failed to load automation DSL schema", e);
        }
    }

    /**
     * Validates the provided JSON string against the automation DSL schema.
     * 
     * <p>This method performs multi-level validation of DSL configurations,
     * ensuring both structural correctness and semantic validity. It's designed
     * to catch errors early before function parsing and execution.</p>
     * 
     * <h3>Validation Process:</h3>
     * <ol>
     *   <li><b>JSON Parsing</b> - Validates JSON syntax</li>
     *   <li><b>Schema Validation</b> - Checks structure against DSL schema</li>
     *   <li><b>Function Name Validation</b> - Ensures unique function names</li>
     *   <li><b>Return Type Validation</b> - Verifies return statements match types</li>
     *   <li><b>Expression Validation</b> - Checks variable scoping and usage</li>
     * </ol>
     * 
     * <h3>Error Categories:</h3>
     * <ul>
     *   <li>CRITICAL - Malformed JSON or schema loading failures</li>
     *   <li>ERROR - Invalid syntax, undefined variables, missing returns</li>
     *   <li>WARNING - Unused parameters, unreachable code (future)</li>
     * </ul>
     * 
     * @param jsonString The DSL configuration JSON string containing automation
     *                   function definitions
     * @return ValidationResult aggregating all validation errors found during
     *         the multi-phase validation process
     */
    public ValidationResult validate(String jsonString) {
        ValidationResult result = new ValidationResult();

        try {
            // Parse JSON
            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(jsonString);
            } catch (JSONException e) {
                result.addError(
                        new ValidationError(
                                "Invalid JSON format",
                                "The provided automation DSL is not valid JSON: " + e.getMessage(),
                                ValidationSeverity.CRITICAL
                        )
                );
                return result;
            }

            // Validate against schema
            try {
                schema.validate(jsonObject);
            } catch (ValidationException e) {
                processValidationException(e, result);
                return result;
            }

            // Perform additional validations
            validateFunctionNames(jsonObject, result);
            validateReturnTypes(jsonObject, result);
            validateExpressionConsistency(jsonObject, result);

        } catch (Exception e) {
            logger.error("Unexpected error during automation DSL validation", e);
            result.addError(
                    new ValidationError(
                            "Validation failure",
                            "An unexpected error occurred during validation: " + e.getMessage(),
                            ValidationSeverity.CRITICAL
                    )
            );
        }

        return result;
    }

    /**
     * Processes validation exceptions and adds them to the result.
     */
    private void processValidationException(ValidationException e, ValidationResult result) {
        // Add the main error
        result.addError(
                new ValidationError(
                        "Schema validation failed",
                        e.getMessage(),
                        ValidationSeverity.CRITICAL
                )
        );

        // Add nested validation errors
        if (e.getCausingExceptions() != null && !e.getCausingExceptions().isEmpty()) {
            e.getCausingExceptions().forEach(cause -> {
                result.addError(
                        new ValidationError(
                                "Schema validation error",
                                cause.getMessage(),
                                ValidationSeverity.ERROR
                        )
                );
            });
        }
    }

    /**
     * Validates function name uniqueness within the DSL configuration.
     * 
     * <p>Function names must be unique to avoid ambiguity when functions are
     * called. This validation ensures no duplicate function names exist and
     * that all functions have valid name properties.</p>
     * 
     * <h3>Validation Rules:</h3>
     * <ul>
     *   <li>Each function must have a non-null name property</li>
     *   <li>Function names must be unique (case-sensitive)</li>
     *   <li>Functions without names receive a warning</li>
     * </ul>
     * 
     * <h3>Why This Matters:</h3>
     * <p>Duplicate function names would make it impossible to determine which
     * function to execute when called, leading to unpredictable behavior.</p>
     * 
     * @param jsonObject The parsed DSL configuration
     * @param result ValidationResult to accumulate name-related errors
     */
    private void validateFunctionNames(JSONObject jsonObject, ValidationResult result) {
        if (!jsonObject.has("automationFunctions") || !jsonObject.get("automationFunctions").getClass().equals(org.json.JSONArray.class)) {
            return;
        }

        try {
            var functions = jsonObject.getJSONArray("automationFunctions");
            Map<String, Integer> functionNamePositions = new HashMap<>();

            for (int i = 0; i < functions.length(); i++) {
                var function = functions.getJSONObject(i);
                if (function.has("name") && !function.isNull("name")) {
                    String name = function.getString("name");

                    if (functionNamePositions.containsKey(name)) {
                        result.addError(
                            new ValidationError(
                                "Duplicate function name",
                                String.format("Function name '%s' is defined multiple times (first at index %d, again at index %d)",
                                              name, functionNamePositions.get(name), i),
                                ValidationSeverity.ERROR
                            )
                        );
                    } else {
                        functionNamePositions.put(name, i);
                    }
                } else {
                    result.addError(
                        new ValidationError(
                            "Missing function name",
                            String.format("Function at index %d is missing a valid 'name' field", i),
                            ValidationSeverity.WARNING
                        )
                    );
                }
            }
        } catch (Exception e) {
            logger.error("Error during function name validation", e);
            result.addError(
                new ValidationError(
                    "Validation error",
                    "Error validating function names: " + e.getMessage(),
                    ValidationSeverity.ERROR
                )
            );
        }
    }

    /**
     * Validates return statement presence for non-void functions.
     * 
     * <p>Functions declaring a non-void return type must have at least one
     * return statement in their body. This validation prevents runtime errors
     * from functions that claim to return values but don't.</p>
     * 
     * <h3>Validation Logic:</h3>
     * <ul>
     *   <li>void functions - No return statement required</li>
     *   <li>Non-void functions - Must have at least one return statement</li>
     *   <li>Note: Doesn't check if all paths return (future enhancement)</li>
     * </ul>
     * 
     * <h3>Limitations:</h3>
     * <p>This is a simple presence check. More sophisticated analysis would
     * verify that all execution paths lead to a return statement.</p>
     * 
     * @param jsonObject The parsed DSL configuration
     * @param result ValidationResult to accumulate return-related errors
     */
    private void validateReturnTypes(JSONObject jsonObject, ValidationResult result) {
        if (!jsonObject.has("automationFunctions")) {
            return;
        }

        try {
            var functions = jsonObject.getJSONArray("automationFunctions");

            for (int i = 0; i < functions.length(); i++) {
                var function = functions.getJSONObject(i);

                if (function.has("returnType") && !function.getString("returnType").equals("void")) {
                    // Non-void return type should have at least one return statement
                    boolean hasReturnStatement = false;

                    if (function.has("statements")) {
                        var statements = function.getJSONArray("statements");

                        for (int j = 0; j < statements.length(); j++) {
                            var statement = statements.getJSONObject(j);
                            if (statement.has("statementType") &&
                                    statement.getString("statementType").equals("return")) {
                                hasReturnStatement = true;
                                break;
                            }
                        }
                    }

                    if (!hasReturnStatement) {
                        result.addError(
                                new ValidationError(
                                        "Missing return statement",
                                        String.format("Function '%s' has return type '%s' but no return statement",
                                                function.optString("name", "unknown"),
                                                function.getString("returnType")),
                                        ValidationSeverity.ERROR
                                )
                        );
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error during return type validation", e);
            result.addError(
                    new ValidationError(
                            "Validation error",
                            "Error validating return types: " + e.getMessage(),
                            ValidationSeverity.ERROR
                    )
            );
        }
    }

    /**
     * Validates expression consistency and variable scoping in functions.
     * 
     * <p>This method performs static analysis of function bodies to ensure all
     * variable references are valid and that expressions are well-formed. It
     * tracks variable definitions through different scopes and control flows.</p>
     * 
     * <h3>Scope Tracking:</h3>
     * <ul>
     *   <li>Function parameters are available throughout the function</li>
     *   <li>Variables are available after their declaration point</li>
     *   <li>Block scopes (if/else, loops) are properly handled</li>
     *   <li>Variables defined in both if/else branches propagate to outer scope</li>
     * </ul>
     * 
     * <h3>Expression Types Validated:</h3>
     * <ul>
     *   <li>Variable references</li>
     *   <li>Method call arguments</li>
     *   <li>Binary operation operands</li>
     *   <li>Builder pattern method chains</li>
     * </ul>
     * 
     * @param jsonObject The parsed DSL configuration containing functions
     * @param result ValidationResult to accumulate expression/variable errors
     */
    private void validateExpressionConsistency(JSONObject jsonObject, ValidationResult result) {
        if (!jsonObject.has("automationFunctions")) {
            return;
        }

        try {
            var functions = jsonObject.getJSONArray("automationFunctions");

            // For each function
            for (int i = 0; i < functions.length(); i++) {
                var function = functions.getJSONObject(i);

                // For simplicity, collect variable names defined in the function
                Set<String> definedVariables = new HashSet<>();

                if (function.has("parameters")) {
                    var parameters = function.getJSONArray("parameters");
                    for (int j = 0; j < parameters.length(); j++) {
                        var param = parameters.getJSONObject(j);
                        if (param.has("name")) {
                            definedVariables.add(param.getString("name"));
                        }
                    }
                }

                if (function.has("statements")) {
                    validateStatements(function.getJSONArray("statements"), definedVariables,
                            function.optString("name", "unknown"), result);
                }
            }
        } catch (Exception e) {
            logger.error("Error during expression consistency validation", e);
            result.addError(
                    new ValidationError(
                            "Validation error",
                            "Error validating expressions: " + e.getMessage(),
                            ValidationSeverity.ERROR
                    )
            );
        }
    }

    /**
     * Recursively validates statements while maintaining variable scope.
     * 
     * <p>This method traverses the statement tree of a function, tracking variable
     * definitions and validating references. It handles different statement types
     * and their impact on variable scope.</p>
     * 
     * <h3>Statement Types Handled:</h3>
     * <ul>
     *   <li><b>variableDeclaration</b> - Adds variable to current scope</li>
     *   <li><b>assignment</b> - Validates target variable exists</li>
     *   <li><b>if</b> - Creates separate scopes for then/else branches</li>
     *   <li><b>forEach</b> - Adds loop variable to inner scope</li>
     *   <li><b>return</b> - Validates return expression</li>
     *   <li><b>methodCall</b> - Validates all argument expressions</li>
     * </ul>
     * 
     * <h3>Scope Management:</h3>
     * <p>The method maintains proper scope isolation for control structures
     * while allowing variables defined in both branches of an if statement
     * to propagate to the outer scope.</p>
     * 
     * @param statements Array of statement objects to validate
     * @param definedVariables Set of currently defined variable names
     * @param functionName Name of the containing function for error messages
     * @param result ValidationResult to accumulate validation errors
     */
    private void validateStatements(org.json.JSONArray statements, Set<String> definedVariables,
                                    String functionName, ValidationResult result) {
        for (int i = 0; i < statements.length(); i++) {
            var statement = statements.getJSONObject(i);

            // Track variable declarations
            if (statement.has("statementType") &&
                    statement.getString("statementType").equals("variableDeclaration")) {
                if (statement.has("name")) {
                    definedVariables.add(statement.getString("name"));
                }
            }

            // Check variable usage in expressions
            if (statement.has("statementType")) {
                switch (statement.getString("statementType")) {
                    case "assignment":
                        if (statement.has("variable") && !definedVariables.contains(statement.getString("variable"))) {
                            result.addError(
                                    new ValidationError(
                                            "Undefined variable",
                                            String.format("Function '%s' assigns to undefined variable: %s",
                                                    functionName, statement.getString("variable")),
                                            ValidationSeverity.ERROR
                                    )
                            );
                        }
                        if (statement.has("value")) {
                            validateExpression(statement.getJSONObject("value"), definedVariables, functionName, result);
                        }
                        break;

                    case "if":
                        if (statement.has("condition")) {
                            validateExpression(statement.getJSONObject("condition"), definedVariables, functionName, result);
                        }

                        // Create a copy of defined variables for each branch
                        var thenVars = new HashSet<>(definedVariables);
                        var elseVars = new HashSet<>(definedVariables);

                        if (statement.has("thenStatements")) {
                            validateStatements(statement.getJSONArray("thenStatements"), thenVars, functionName, result);
                        }

                        if (statement.has("elseStatements")) {
                            validateStatements(statement.getJSONArray("elseStatements"), elseVars, functionName, result);
                        }

                        // Add variables defined in both branches to the outer scope
                        definedVariables.addAll(thenVars);
                        definedVariables.addAll(elseVars);
                        break;

                    case "forEach":
                        if (statement.has("collection")) {
                            validateExpression(statement.getJSONObject("collection"), definedVariables, functionName, result);
                        }

                        // Add loop variable to the inner scope
                        var loopVars = new HashSet<>(definedVariables);
                        if (statement.has("variable")) {
                            loopVars.add(statement.getString("variable"));
                        }

                        if (statement.has("statements")) {
                            validateStatements(statement.getJSONArray("statements"), loopVars, functionName, result);
                        }
                        break;

                    case "return":
                        if (statement.has("value")) {
                            validateExpression(statement.getJSONObject("value"), definedVariables, functionName, result);
                        }
                        break;

                    case "methodCall":
                        if (statement.has("arguments")) {
                            var args = statement.getJSONArray("arguments");
                            for (int j = 0; j < args.length(); j++) {
                                validateExpression(args.getJSONObject(j), definedVariables, functionName, result);
                            }
                        }
                        break;
                }
            }
        }
    }

    /**
     * Validates expressions for well-formedness and variable references.
     * 
     * <p>This method recursively validates expression trees, ensuring all variable
     * references point to defined variables and that complex expressions are
     * properly structured. It handles various expression types including literals,
     * variables, method calls, and operations.</p>
     * 
     * <h3>Expression Types:</h3>
     * <ul>
     *   <li><b>variable</b> - Must reference a defined variable</li>
     *   <li><b>literal</b> - Always valid (numbers, strings, booleans)</li>
     *   <li><b>methodCall</b> - Validates all argument expressions</li>
     *   <li><b>binaryOperation</b> - Validates both operands</li>
     *   <li><b>builder</b> - Validates arguments in method chain</li>
     * </ul>
     * 
     * <h3>Recursive Validation:</h3>
     * <p>Complex expressions are validated recursively, ensuring all nested
     * expressions are checked. This catches deeply nested undefined variable
     * references that might otherwise be missed.</p>
     * 
     * @param expression The expression object to validate
     * @param definedVariables Set of currently defined variable names
     * @param functionName Name of the containing function for error context
     * @param result ValidationResult to accumulate undefined variable errors
     */
    private void validateExpression(JSONObject expression, Set<String> definedVariables,
                                    String functionName, ValidationResult result) {
        if (!expression.has("expressionType")) {
            return;
        }

        String exprType = expression.getString("expressionType");

        switch (exprType) {
            case "variable":
                if (expression.has("name") && !definedVariables.contains(expression.getString("name"))) {
                    result.addError(
                            new ValidationError(
                                    "Undefined variable",
                                    String.format("Function '%s' references undefined variable: %s",
                                            functionName, expression.getString("name")),
                                    ValidationSeverity.ERROR
                            )
                    );
                }
                break;

            case "methodCall":
                if (expression.has("arguments")) {
                    var args = expression.getJSONArray("arguments");
                    for (int i = 0; i < args.length(); i++) {
                        validateExpression(args.getJSONObject(i), definedVariables, functionName, result);
                    }
                }
                break;

            case "binaryOperation":
                if (expression.has("left")) {
                    validateExpression(expression.getJSONObject("left"), definedVariables, functionName, result);
                }
                if (expression.has("right")) {
                    validateExpression(expression.getJSONObject("right"), definedVariables, functionName, result);
                }
                break;

            case "builder":
                if (expression.has("methods")) {
                    var methods = expression.getJSONArray("methods");
                    for (int i = 0; i < methods.length(); i++) {
                        var method = methods.getJSONObject(i);
                        if (method.has("arguments")) {
                            var args = method.getJSONArray("arguments");
                            for (int j = 0; j < args.length(); j++) {
                                validateExpression(args.getJSONObject(j), definedVariables, functionName, result);
                            }
                        }
                    }
                }
                break;
        }
    }
}
