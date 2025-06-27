package io.github.jspinak.brobot.tools.logging.dto;

import lombok.Data;

/**
 * Data Transfer Object representing the result of a state image detection attempt.
 * This DTO provides a serialization-friendly representation of 
 * {@link io.github.jspinak.brobot.tools.logging.model.StateImageLogData} for use in
 * API responses and client communication.
 * 
 * <p>This simple DTO maintains the same structure as its model counterpart,
 * as both are already minimal data containers. The separation ensures that
 * API contracts remain stable even if the internal model evolves.</p>
 * 
 * <h3>Usage Context:</h3>
 * <p>StateImageLogDTO instances are typically found in collections within
 * {@link LogDataDTO}, representing multiple image detection attempts that
 * occurred during a single logged operation. This is particularly useful for:</p>
 * <ul>
 *   <li>Debugging why certain states were or weren't detected</li>
 *   <li>Analyzing pattern matching reliability</li>
 *   <li>Optimizing image recognition thresholds</li>
 * </ul>
 * 
 * @see io.github.jspinak.brobot.tools.logging.model.StateImageLogData for the corresponding model
 * @see LogDataDTO for the parent DTO that contains collections of these results
 */
@Data
public class StateImageLogDTO {
    /**
     * The unique identifier of the state image that was searched for.
     * References a state image in the application's pattern library.
     */
    private Long stateImageId;
    
    /**
     * Result of the detection attempt.
     * true if the image was found on screen, false otherwise.
     */
    private boolean found;
}

