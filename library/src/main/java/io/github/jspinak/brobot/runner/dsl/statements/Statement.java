// File: io/github/jspinak/brobot/dsl/model/Statement.java
package io.github.jspinak.brobot.runner.dsl.statements;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Abstract base class for all statements in the Brobot DSL.
 * <p>
 * A statement represents an executable instruction in the DSL that performs
 * an action but does not produce a value (unlike expressions). Statements
 * form the body of automation functions and control the flow of execution.
 * <p>
 * Supported statement types:
 * <ul>
 * <li>Variable declarations - Declare and optionally initialize variables</li>
 * <li>Assignments - Assign values to existing variables</li>
 * <li>Method calls - Invoke methods for their side effects</li>
 * <li>Control flow - If statements and forEach loops</li>
 * <li>Returns - Return values from functions</li>
 * </ul>
 * <p>
 * This class uses Jackson polymorphic deserialization to support parsing different
 * statement types from JSON based on the "statementType" discriminator field.
 *
 * @see VariableDeclarationStatement
 * @see AssignmentStatement
 * @see MethodCallStatement
 * @see IfStatement
 * @see ForEachStatement
 * @see ReturnStatement
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "statementType",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = VariableDeclarationStatement.class, name = "variableDeclaration"),
        @JsonSubTypes.Type(value = AssignmentStatement.class, name = "assignment"),
        @JsonSubTypes.Type(value = IfStatement.class, name = "if"),
        @JsonSubTypes.Type(value = ForEachStatement.class, name = "forEach"),
        @JsonSubTypes.Type(value = ReturnStatement.class, name = "return"),
        @JsonSubTypes.Type(value = MethodCallStatement.class, name = "methodCall")
})
public abstract class Statement {
    /**
     * The discriminator field used by Jackson to determine the concrete type
     * of this statement during JSON deserialization.
     * Valid values: "variableDeclaration", "assignment", "if", "forEach", "return", "methodCall"
     */
    @JsonProperty("statementType")
    @Getter
    @Setter
    private String statementType;
}