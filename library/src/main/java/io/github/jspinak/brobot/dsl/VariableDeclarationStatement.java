// File: io/github/jspinak/brobot/dsl/model/VariableDeclarationStatement.java
package io.github.jspinak.brobot.dsl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class VariableDeclarationStatement extends Statement {
    private String name;
    private String type;
    private Expression value;
}