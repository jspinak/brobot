// File: io/github/jspinak/brobot/dsl/model/IfStatement.java
package io.github.jspinak.brobot.dsl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IfStatement extends Statement {
    private Expression condition;
    private List<Statement> thenStatements;
    private List<Statement> elseStatements;

    // Getters and Setters
    public Expression getCondition() { return condition; }
    public void setCondition(Expression condition) { this.condition = condition; }
    public List<Statement> getThenStatements() { return thenStatements; }
    public void setThenStatements(List<Statement> thenStatements) { this.thenStatements = thenStatements; }
    public List<Statement> getElseStatements() { return elseStatements; }
    public void setElseStatements(List<Statement> elseStatements) { this.elseStatements = elseStatements; }
}