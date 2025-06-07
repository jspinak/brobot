// File: io/github/jspinak/brobot/dsl/model/MethodCallExpression.java
package io.github.jspinak.brobot.dsl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MethodCallExpression extends Expression {
    private String object;
    private String method;
    private List<Expression> arguments;
}