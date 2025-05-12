// File: io/github/jspinak/brobot/dsl/model/Parameter.java
package io.github.jspinak.brobot.dsl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Parameter {
    private String name;
    private String type; // Consider Enum: boolean, string, int, double, region, matches, stateImage, stateRegion, object

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}