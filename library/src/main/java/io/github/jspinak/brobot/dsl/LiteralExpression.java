// File: io/github/jspinak/brobot/dsl/model/LiteralExpression.java
package io.github.jspinak.brobot.dsl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LiteralExpression extends Expression {
    private String valueType; // Consider Enum: boolean, string, integer, double, null
    private Object value; // Can be Boolean, String, Integer, Double, or null.
}