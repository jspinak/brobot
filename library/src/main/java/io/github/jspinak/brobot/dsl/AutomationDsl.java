package io.github.jspinak.brobot.dsl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AutomationDsl {
    private List<AutomationFunction> automationFunctions;

    // Getters and Setters
    public List<AutomationFunction> getAutomationFunctions() {
        return automationFunctions;
    }

    public void setAutomationFunctions(List<AutomationFunction> automationFunctions) {
        this.automationFunctions = automationFunctions;
    }
}
