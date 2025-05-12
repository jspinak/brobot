// File: io/github/jspinak/brobot/dsl/model/BuilderMethod.java
package io.github.jspinak.brobot.dsl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BuilderMethod {
    private String method;
    private List<Expression> arguments;

    // Getters and Setters
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public List<Expression> getArguments() { return arguments; }
    public void setArguments(List<Expression> arguments) { this.arguments = arguments; }
}