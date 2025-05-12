// File: io/github/jspinak/brobot/dsl/model/ForEachStatement.java
package io.github.jspinak.brobot.dsl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ForEachStatement extends Statement {
    private String variable;
    private String variableType; // Optional in schema, consider if needed
    private Expression collection;
    private List<Statement> statements;

    // Getters and Setters
    public String getVariable() { return variable; }
    public void setVariable(String variable) { this.variable = variable; }
    public String getVariableType() { return variableType; }
    public void setVariableType(String variableType) { this.variableType = variableType; }
    public Expression getCollection() { return collection; }
    public void setCollection(Expression collection) { this.collection = collection; }
    public List<Statement> getStatements() { return statements; }
    public void setStatements(List<Statement> statements) { this.statements = statements; }
}