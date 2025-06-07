// File: io.github/jspinak/brobot/dsl/BuilderMethod.java
package io.github.jspinak.brobot.dsl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuilderMethod {
    private String method;
    private List<Expression> arguments;
}