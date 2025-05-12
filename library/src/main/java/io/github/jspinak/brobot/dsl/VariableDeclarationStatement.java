// File: io/github/jspinak/brobot/dsl/model/VariableDeclarationStatement.java
package io.github.jspinak.brobot.dsl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VariableDeclarationStatement extends Statement {
    private String name;
    private String type; // Consider Enum: boolean, string, int, double, region, matches, stateImage, stateRegion, actionOptions, objectCollection, object
    private Expression value; // Schema oneOf [expression, builder]. Builder is an expressionType.

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Expression getValue() { return value; }
    public void setValue(Expression value) { this.value = value; }
}