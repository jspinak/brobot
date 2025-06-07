// File: io.github.jspinak/brobot/dsl/BuilderExpression.java
package io.github.jspinak.brobot.dsl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuilderExpression extends Expression {
    private String builderType;
    private List<BuilderMethod> methods;
}