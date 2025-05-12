// File: io/github/jspinak/brobot/dsl/model/AssignmentStatement.java
package io.github.jspinak.brobot.dsl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AssignmentStatement extends Statement {
    private String variable;
    private Expression value;

    // Getters and Setters
    public String getVariable() { return variable; }
    public void setVariable(String variable) { this.variable = variable; }
    public Expression getValue() { return value; }
    public void setValue(Expression value) { this.value = value; }
}