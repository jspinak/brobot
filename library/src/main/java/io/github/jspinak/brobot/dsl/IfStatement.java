// File: io/github/jspinak/brobot/dsl/model/IfStatement.java
package io.github.jspinak.brobot.dsl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class IfStatement extends Statement {
    private Expression condition;
    private List<Statement> thenStatements;
    private List<Statement> elseStatements;
}