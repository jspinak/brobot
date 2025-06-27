// File: io/github/jspinak/brobot/dsl/ForEachStatement.java
package io.github.jspinak.brobot.runner.dsl.statements;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.github.jspinak.brobot.runner.dsl.expressions.Expression;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

/**
 * Represents a for-each loop statement in the Brobot DSL.
 * <p>
 * This statement iterates over a collection, executing a block of statements
 * for each element. A loop variable is declared and bound to each element
 * in turn, allowing the loop body to process each item individually.
 * <p>
 * The loop creates a new scope for each iteration, with the loop variable
 * only accessible within the loop body. The collection expression must
 * evaluate to an iterable type (e.g., list, array).
 * <p>
 * Example in JSON:
 * <pre>
 * {
 *   "statementType": "forEach",
 *   "variable": "item",
 *   "variableType": "string",
 *   "collection": {"expressionType": "variable", "name": "itemList"},
 *   "statements": [
 *     {
 *       "statementType": "methodCall",
 *       "method": "processItem",
 *       "arguments": [{"expressionType": "variable", "name": "item"}]
 *     }
 *   ]
 * }
 * </pre>
 *
 * @see Statement
 * @see VariableDeclarationStatement
 */
@Data
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ForEachStatement extends Statement {
    /**
     * The name of the loop variable that will hold each element.
     * This variable is automatically declared in the loop scope and
     * assigned each element of the collection in turn.
     */
    private String variable;
    
    /**
     * The type of the loop variable.
     * Should match the element type of the collection being iterated.
     * Used for type checking and validation.
     */
    private String variableType;
    
    /**
     * The expression that evaluates to the collection to iterate over.
     * Must evaluate to an iterable type such as a list or array.
     * The collection is evaluated once before the loop begins.
     */
    private Expression collection;
    
    /**
     * The list of statements to execute for each element in the collection.
     * These statements are executed once per element, with the loop variable
     * bound to the current element. Executed within a new scope for each iteration.
     */
    private List<Statement> statements;
}