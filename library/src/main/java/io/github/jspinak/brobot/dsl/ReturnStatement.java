// File: io/github/jspinak/brobot/dsl/model/ReturnStatement.java
package io.github.jspinak.brobot.dsl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReturnStatement extends Statement {
    private Expression value;

    // Getters and Setters
    public Expression getValue() { return value; }
    public void setValue(Expression value) { this.value = value; }
}