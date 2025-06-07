// File: io/github/jspinak/brobot/dsl/model/Statement.java
package io.github.jspinak.brobot.dsl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "statementType",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = VariableDeclarationStatement.class, name = "variableDeclaration"),
        @JsonSubTypes.Type(value = AssignmentStatement.class, name = "assignment"),
        @JsonSubTypes.Type(value = IfStatement.class, name = "if"),
        @JsonSubTypes.Type(value = ForEachStatement.class, name = "forEach"),
        @JsonSubTypes.Type(value = ReturnStatement.class, name = "return"),
        @JsonSubTypes.Type(value = MethodCallStatement.class, name = "methodCall")
})
public abstract class Statement {
    @JsonProperty("statementType")
    @Getter
    @Setter
    private String statementType;
}