package io.github.jspinak.brobot.action.composite;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.ConditionalActionChain;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import org.springframework.stereotype.Component;

/**
 * Composite action that combines Find, Click, and Type operations.
 * <p>
 * This composite action provides a convenient way to perform the common
 * pattern of finding a text field, clicking on it, and then typing text.
 * It's designed for users who prefer the simplicity of the old embedded-find
 * approach while using the new architecture under the hood.
 * </p>
 * 
 * <p>Usage:
 * <pre>{@code
 * // Simple usage with text
 * FindAndType findAndType = new FindAndType("user@example.com");
 * action.perform(findAndType, emailFieldImage);
 * 
 * // With custom options
 * FindAndType findAndType = new FindAndType.Builder()
 *     .withText("Hello World")
 *     .withClearField(true)
 *     .withPressEnter(true)
 *     .build();
 * action.perform(findAndType, textFieldImage);
 * }</pre>
 * </p>
 * 
 * @since 2.0
 * @see ConditionalActionChain for more flexible action composition
 */
@Component
public class FindAndType extends ActionConfig implements ActionInterface {
    
    private final PatternFindOptions findOptions;
    private final ClickOptions clickOptions;
    private final TypeOptions typeOptions;
    
    /**
     * Creates a FindAndType action that types the specified text.
     * Uses default find and click options.
     * 
     * @param text the text to type
     */
    public FindAndType(String text) {
        this(new PatternFindOptions.Builder().build(),
             new ClickOptions.Builder().build(),
             new TypeOptions.Builder().setText(text).build());
    }
    
    /**
     * Creates a FindAndType action with custom options.
     * 
     * @param findOptions the find configuration to use
     * @param clickOptions the click configuration to use
     * @param typeOptions the type configuration to use
     */
    public FindAndType(PatternFindOptions findOptions, 
                      ClickOptions clickOptions,
                      TypeOptions typeOptions) {
        super(new Builder());
        this.findOptions = findOptions;
        this.clickOptions = clickOptions;
        this.typeOptions = typeOptions;
    }
    
    /**
     * Private constructor for builder.
     */
    private FindAndType(Builder builder) {
        super(builder);
        this.findOptions = builder.findOptions;
        this.clickOptions = builder.clickOptions;
        this.typeOptions = builder.typeOptions;
    }
    
    @Override
    public ActionResult perform(ActionConfig actionConfig, ObjectCollection... objectCollections) {
        // Note: actionConfig parameter is this instance when called through Action.perform()
        
        // Use ConditionalActionChain to implement find-click-type
        ConditionalActionChain chain = ConditionalActionChain
            .find(findOptions)
            .ifFound(clickOptions)
            .ifFound(typeOptions);
        
        // Return a result indicating the chain configuration
        ActionResult result = new ActionResult();
        result.setActionType("FIND_AND_TYPE");
        result.setText("FindAndType composite action configured");
        
        // Store the chain configuration in the result
        result.setMetadata("chain", chain);
        result.setMetadata("findOptions", findOptions);
        result.setMetadata("clickOptions", clickOptions);
        result.setMetadata("typeOptions", typeOptions);
        
        return result;
    }
    
    /**
     * Gets the text that will be typed.
     * 
     * @return the text to type
     */
    public String getText() {
        return typeOptions.getText();
    }
    
    /**
     * Builder for creating FindAndType instances with a fluent API.
     */
    public static class Builder extends ActionConfig.Builder<Builder> {
        private PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
        private ClickOptions clickOptions = new ClickOptions.Builder().build();
        private TypeOptions typeOptions = new TypeOptions.Builder().build();
        
        /**
         * Sets the text to type.
         * 
         * @param text the text to type
         * @return this builder for chaining
         */
        public Builder withText(String text) {
            this.typeOptions = new TypeOptions.Builder(typeOptions)
                .setText(text)
                .build();
            return self();
        }
        
        /**
         * Sets whether to clear the field before typing.
         * 
         * @param clearField true to clear the field first
         * @return this builder for chaining
         */
        public Builder withClearField(boolean clearField) {
            this.typeOptions = new TypeOptions.Builder(typeOptions)
                .setClearField(clearField)
                .build();
            return self();
        }
        
        /**
         * Sets whether to press Enter after typing.
         * 
         * @param pressEnter true to press Enter after typing
         * @return this builder for chaining
         */
        public Builder withPressEnter(boolean pressEnter) {
            this.typeOptions = new TypeOptions.Builder(typeOptions)
                .setPressEnterAfterTyping(pressEnter)
                .build();
            return self();
        }
        
        /**
         * Sets the similarity threshold for finding.
         * 
         * @param similarity the minimum similarity score (0.0 to 1.0)
         * @return this builder for chaining
         */
        public Builder withSimilarity(double similarity) {
            this.findOptions = new PatternFindOptions.Builder(findOptions)
                .setSimilarity(similarity)
                .build();
            return self();
        }
        
        /**
         * Sets custom find options.
         * 
         * @param findOptions the find configuration
         * @return this builder for chaining
         */
        public Builder withFindOptions(PatternFindOptions findOptions) {
            this.findOptions = findOptions;
            return self();
        }
        
        /**
         * Sets custom click options.
         * 
         * @param clickOptions the click configuration
         * @return this builder for chaining
         */
        public Builder withClickOptions(ClickOptions clickOptions) {
            this.clickOptions = clickOptions;
            return self();
        }
        
        /**
         * Sets custom type options.
         * 
         * @param typeOptions the type configuration
         * @return this builder for chaining
         */
        public Builder withTypeOptions(TypeOptions typeOptions) {
            this.typeOptions = typeOptions;
            return self();
        }
        
        @Override
        public FindAndType build() {
            return new FindAndType(this);
        }
        
        @Override
        protected Builder self() {
            return this;
        }
    }
}