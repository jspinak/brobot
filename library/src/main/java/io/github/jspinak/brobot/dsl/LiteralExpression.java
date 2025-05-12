// File: io/github/jspinak/brobot/dsl/model/LiteralExpression.java
package io.github.jspinak.brobot.dsl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LiteralExpression extends Expression {
    private String valueType; // Consider Enum: boolean, string, integer, double, null
    private Object value; // Can be Boolean, String, Integer, Double, or null. Jackson handles numeric types.

    // Getters and Setters
    public String getValueType() { return valueType; }
    public void setValueType(String valueType) { this.valueType = valueType; }
    public Object getValue() { return value; }
    public void setValue(Object value) { this.value = value; }
}