package io.github.jspinak.brobot.schemaValidation.schema;

import io.github.jspinak.brobot.schemaValidation.model.ValidationError;
import io.github.jspinak.brobot.schemaValidation.model.ValidationResult;
import io.github.jspinak.brobot.schemaValidation.model.ValidationSeverity;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Validates automation DSL JSON against the automation DSL schema.
 */
@Component
public class AutomationDSLValidator {
    private static final Logger logger = LoggerFactory.getLogger(AutomationDSLValidator.class);
    private static final String SCHEMA_PATH = "/schemas/automation-dsl-schema.json";

    private final Schema schema;

    /**
     * Initializes the validator by loading the automation DSL schema.
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
     * Validates the provided JSON against the automation DSL schema.
     *
     * @param jsonString JSON string to validate
     * @return ValidationResult containing any validation errors
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
     * Validates that function names are unique.
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
     * Validates that functions with non-void return types actually return a value.
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
     * Validates that expressions are consistent and well-formed.
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
     * Validates statements recursively, tracking defined variables.
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
     * Validates expressions, checking for undefined variables.
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
