// File: io/github/jspinak/brobot/dsl/model/BuilderExpression.java
package io.github.jspinak.brobot.dsl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BuilderExpression extends Expression {
    private String builderType; // Consider Enum: actionOptions, objectCollection
    private List<BuilderMethod> methods;

    // Getters and Setters
    public String getBuilderType() { return builderType; }
    public void setBuilderType(String builderType) { this.builderType = builderType; }
    public List<BuilderMethod> getMethods() { return methods; }
    public void setMethods(List<BuilderMethod> methods) { this.methods = methods; }
}