// File: io/github/jspinak/brobot/dsl/model/Statement.java
package io.github.jspinak.brobot.dsl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY, // Use EXISTING_PROPERTY if statementType is a field in subtypes
        property = "statementType",
        visible = true // Makes statementType also deserializable into a field if needed
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
    // The 'statementType' field from JSON will be used by Jackson to determine the subtype.
    // If you want to access it in your POJOs, you can declare it here or in subtypes.
    // If 'visible = true' in @JsonTypeInfo, Jackson can map it if a field exists.
    @JsonProperty("statementType")
    private String statementType;

    public String getStatementType() {
        return statementType;
    }

    public void setStatementType(String statementType) {
        this.statementType = statementType;
    }
}