// File: io/github/jspinak/brobot/dsl/ForEachStatement.java
package io.github.jspinak.brobot.dsl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ForEachStatement extends Statement {
    private String variable;
    private String variableType;
    private Expression collection;
    private List<Statement> statements;
}