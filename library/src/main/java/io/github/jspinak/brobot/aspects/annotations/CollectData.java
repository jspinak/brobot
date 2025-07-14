package io.github.jspinak.brobot.aspects.annotations;

import java.lang.annotation.*;

/**
 * Marks a method for automatic dataset collection for machine learning.
 * 
 * <p>Methods annotated with @CollectData will have their inputs, outputs,
 * and execution context automatically captured by the DatasetCollectionAspect
 * for training machine learning models.</p>
 * 
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * @CollectData(category = "click_accuracy", captureScreenshots = true)
 * public ActionResult performClick(StateObject target) {
 *     // Click action whose data will be collected
 * }
 * 
 * @CollectData(
 *     category = "text_recognition",
 *     features = {"image", "location", "confidence"},
 *     samplingRate = 0.1
 * )
 * public String extractText(Region region) {
 *     // Only collect 10% of executions
 * }
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CollectData {
    
    /**
     * Category for organizing the collected data.
     * Used as subdirectory in the dataset.
     */
    String category() default "general";
    
    /**
     * Specific features to collect.
     * Empty array means collect all available features.
     */
    String[] features() default {};
    
    /**
     * Whether to capture screenshots before/after execution.
     * Default is true.
     */
    boolean captureScreenshots() default true;
    
    /**
     * Whether to capture intermediate states.
     * Useful for multi-step operations.
     * Default is false.
     */
    boolean captureIntermediateStates() default false;
    
    /**
     * Sampling rate for data collection (0.0 to 1.0).
     * 1.0 means collect every execution, 0.1 means collect 10%.
     * Default is 1.0.
     */
    double samplingRate() default 1.0;
    
    /**
     * Maximum number of samples to collect.
     * -1 means no limit.
     * Default is -1.
     */
    int maxSamples() default -1;
    
    /**
     * Whether to collect only successful executions.
     * Default is false (collect both success and failure).
     */
    boolean onlySuccess() default false;
    
    /**
     * Whether to include timing information.
     * Default is true.
     */
    boolean includeTiming() default true;
    
    /**
     * Whether to anonymize sensitive data.
     * Default is true.
     */
    boolean anonymize() default true;
    
    /**
     * Data format for storage.
     */
    DataFormat format() default DataFormat.JSON;
    
    /**
     * Labels to apply to the collected data.
     * Useful for supervised learning.
     */
    String[] labels() default {};
    
    /**
     * Whether to compress the collected data.
     * Default is true.
     */
    boolean compress() default true;
    
    /**
     * Data storage formats
     */
    enum DataFormat {
        /**
         * JSON format (human-readable)
         */
        JSON,
        
        /**
         * CSV format (tabular data)
         */
        CSV,
        
        /**
         * Binary format (efficient storage)
         */
        BINARY,
        
        /**
         * TensorFlow TFRecord format
         */
        TFRECORD,
        
        /**
         * Apache Parquet format
         */
        PARQUET
    }
}