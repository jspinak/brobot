// File: io/github/jspinak/brobot/dsl/model/MethodCallStatement.java
package io.github.jspinak.brobot.dsl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MethodCallStatement extends Statement {
    private String object;
    private String method;
    private List<Expression> arguments;

    // Getters and Setters
    public String getObject() { return object; }
    public void setObject(String objectValue) { this.object = objectValue; } // Renamed to avoid conflict
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public List<Expression> getArguments() { return arguments; }
    public void setArguments(List<Expression> arguments) { this.arguments = arguments; }
}