// File: io/github/jspinak/brobot/dsl/AutomationFunction.java
package io.github.jspinak.brobot.dsl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AutomationFunction {
    private Integer id;
    private String name;
    private String description;
    private String returnType;
    private List<Parameter> parameters;
    private List<Statement> statements;
}