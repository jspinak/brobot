package io.github.jspinak.brobot.runner.ui.config.services;

import java.util.function.Function;
import java.util.function.Predicate;
import javafx.scene.control.Control;

import lombok.Builder;
import lombok.Data;

/**
 * Definition of a metadata field type. Describes how to create, validate, and handle a specific
 * field type.
 */
@Data
@Builder
public class MetadataFieldDefinition {

    /** Unique type identifier (e.g., "text", "number", "date", "email") */
    private final String type;

    /** Display name for the field type */
    private final String displayName;

    /** Description of the field type */
    private final String description;

    /** Factory function to create the control */
    private final Function<FieldConfig, Control> controlFactory;

    /** Validator function */
    private final Predicate<Object> validator;

    /** Value converter (from control to storage format) */
    private final Function<Control, String> valueExtractor;

    /** Value setter (from storage format to control) */
    private final ValueSetter valueSetter;

    /** Default value for new fields */
    private final String defaultValue;

    /** Whether this field type supports multiple values */
    private final boolean multiValue;

    /** Maximum length for the field value (0 = unlimited) */
    private final int maxLength;

    /** Configuration for creating a field instance. */
    @Data
    @Builder
    public static class FieldConfig {
        private final String key;
        private final String label;
        private final String prompt;
        private final boolean required;
        private final String initialValue;
        private final Object metadata;
    }

    /** Interface for setting values on controls. */
    @FunctionalInterface
    public interface ValueSetter {
        void setValue(Control control, String value);
    }
}
