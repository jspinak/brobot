// File: io/github/jspinak/brobot/dsl/model/BinaryOperationExpression.java
package io.github.jspinak.brobot.dsl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BinaryOperationExpression extends Expression {
    private String operator; // Consider Enum for operators
    private Expression left;
    private Expression right;

    // Getters and Setters
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public Expression getLeft() { return left; }
    public void setLeft(Expression left) { this.left = left; }
    public Expression getRight() { return right; }
    public void setRight(Expression right) { this.right = right; }
}