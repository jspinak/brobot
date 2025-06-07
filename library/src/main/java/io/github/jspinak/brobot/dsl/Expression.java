// File: io/github/jspinak/brobot/dsl/Expression.java
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
        property = "expressionType",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = LiteralExpression.class, name = "literal"),
        @JsonSubTypes.Type(value = VariableExpression.class, name = "variable"),
        @JsonSubTypes.Type(value = MethodCallExpression.class, name = "methodCall"),
        @JsonSubTypes.Type(value = BinaryOperationExpression.class, name = "binaryOperation"),
        @JsonSubTypes.Type(value = BuilderExpression.class, name = "builder")
})
public abstract class Expression {
    @JsonProperty("expressionType")
    @Getter
    @Setter
    private String expressionType;
}