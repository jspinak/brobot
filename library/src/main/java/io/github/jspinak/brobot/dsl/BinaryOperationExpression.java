// File: io/github/jspinak/brobot/dsl/BinaryOperationExpression.java
package io.github.jspinak.brobot.dsl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BinaryOperationExpression extends Expression {
    private String operator;
    private Expression left;
    private Expression right;
}