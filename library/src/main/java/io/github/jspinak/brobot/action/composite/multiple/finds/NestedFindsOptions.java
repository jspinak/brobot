package io.github.jspinak.brobot.action.composite.multiple.finds;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;

import lombok.Getter;

/**
 * Configuration for nested find operations.
 *
 * <p>This configuration allows defining multiple find steps that are executed in a nested manner,
 * where each step searches within the results of the previous step.
 *
 * @since 2.0
 */
@Getter
@JsonDeserialize(builder = NestedFindsOptions.Builder.class)
public final class NestedFindsOptions extends ActionConfig {

    private final List<PatternFindOptions> findSteps;

    private NestedFindsOptions(Builder builder) {
        super(builder);
        this.findSteps = new ArrayList<>(builder.findSteps);
    }

    /** Builder for constructing NestedFindsOptions with a fluent API. */
    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder extends ActionConfig.Builder<Builder> {

        @JsonProperty("findSteps")
        private List<PatternFindOptions> findSteps = new ArrayList<>();

        /** Default constructor for creating a new NestedFindsOptions configuration. */
        @JsonCreator
        public Builder() {}

        /**
         * Adds a find step to the nested search sequence.
         *
         * @param findOptions configuration for this find step
         * @return this Builder instance for chaining
         */
        public Builder addFindStep(PatternFindOptions findOptions) {
            this.findSteps.add(findOptions);
            return self();
        }

        /**
         * Sets all find steps at once.
         *
         * @param findSteps list of find configurations
         * @return this Builder instance for chaining
         */
        public Builder setFindSteps(List<PatternFindOptions> findSteps) {
            this.findSteps = new ArrayList<>(findSteps);
            return self();
        }

        @Override
        protected Builder self() {
            return this;
        }

        public NestedFindsOptions build() {
            return new NestedFindsOptions(this);
        }
    }
}
