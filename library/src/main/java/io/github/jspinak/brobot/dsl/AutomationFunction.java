// File: io/github/jspinak/brobot/dsl/model/AutomationFunction.java
package io.github.jspinak.brobot.dsl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AutomationFunction {
    private Integer id;
    private String name;
    private String description;
    private String returnType; // Consider Enum: void, boolean, string, int, double, region, matches, stateImage, stateRegion, object
    private List<Parameter> parameters;
    private List<Statement> statements; // Polymorphic list

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getReturnType() { return returnType; }
    public void setReturnType(String returnType) { this.returnType = returnType; }
    public List<Parameter> getParameters() { return parameters; }
    public void setParameters(List<Parameter> parameters) { this.parameters = parameters; }
    public List<Statement> getStatements() { return statements; }
    public void setStatements(List<Statement> statements) { this.statements = statements; }
}