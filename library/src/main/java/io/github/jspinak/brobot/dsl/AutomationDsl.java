// File: io/github/jspinak/brobot/dsl/AutomationDsl.java
package io.github.jspinak.brobot.dsl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AutomationDsl {
    private List<AutomationFunction> automationFunctions;
}