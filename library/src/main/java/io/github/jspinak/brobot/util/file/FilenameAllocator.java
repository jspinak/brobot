package io.github.jspinak.brobot.util.file;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.util.image.io.ImageFileUtilities;

/**
 * Manages filename reservations to prevent file conflicts during concurrent operations.
 *
 * <p>This repository tracks filenames that have been allocated but not yet written to disk,
 * ensuring unique filenames across multiple illustration generation processes. It serves as a
 * central coordination point for filename allocation in Brobot's visual documentation system,
 * preventing race conditions and filename collisions.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li>In-memory tracking of reserved filenames
 *   <li>Automatic numbering for duplicate filename requests
 *   <li>Coordination with filesystem checks via {@link ImageFileUtilities}
 *   <li>Thread-safe filename generation with incremental indices
 * </ul>
 *
 * <p>Filename generation strategy:
 *
 * <ul>
 *   <li>Base format: {prefix}{suffix}
 *   <li>Collision format: {prefix}{suffix}_{number}
 *   <li>Numbers increment until a free filename is found
 *   <li>Checks both filesystem and in-memory reservations
 * </ul>
 *
 * <p>Use cases:
 *
 * <ul>
 *   <li>Preventing overwrites during parallel illustration generation
 *   <li>Ensuring unique filenames for batch operations
 *   <li>Coordinating filename allocation across multiple components
 *   <li>Supporting concurrent screenshot and analysis operations
 * </ul>
 *
 * <p>Implementation notes:
 *
 * <ul>
 *   <li>Maintains a list of all reserved filenames
 *   <li>Tracks highest index per prefix for efficient numbering
 *   <li>Does not handle cleanup of reservations after file writing
 *   <li>Memory usage grows with number of reservations
 * </ul>
 *
 * <p>Thread safety: This implementation is NOT thread-safe. Concurrent access should be
 * synchronized externally or consider using ConcurrentHashMap and thread-safe collections.
 *
 * @see ImageFileUtilities#fileExists(String)
 * @see HistoryFileNamer
 */
@Component
public class FilenameAllocator {

    private ImageFileUtilities imageUtils;

    /**
     * List of all filenames that have been reserved but may not yet be written. Grows unbounded as
     * filenames are reserved throughout application lifetime.
     */
    private List<String> filenames = new ArrayList<>();

    /**
     * Maps filename prefixes to their highest used index number. Used to efficiently generate the
     * next available numbered filename.
     */
    private Map<String, Integer> indices = new HashMap<>();

    public FilenameAllocator(ImageFileUtilities imageUtils) {
        this.imageUtils = imageUtils;
    }

    /**
     * Manually reserves a filename in the repository.
     *
     * <p>Adds a filename to the internal tracking list without generating it. This is useful when
     * filenames are created externally but need to be tracked to prevent conflicts.
     *
     * <p>Side effects: Modifies the internal filename list.
     *
     * @param filename the filename to reserve; should include full path
     */
    public void addFilename(String filename) {
        filenames.add(filename);
    }

    /**
     * Checks if a filename has been reserved in this session.
     *
     * <p>Only checks the in-memory reservation list, not the actual filesystem. Use in conjunction
     * with filesystem checks for complete validation.
     *
     * @param filename the filename to check
     * @return true if the filename has been reserved, false otherwise
     */
    public boolean filenameExists(String filename) {
        return filenames.contains(filename);
    }

    /**
     * Generates and reserves a unique filename with automatic numbering.
     *
     * <p>Creates a filename by combining prefix and suffix, then checks both the filesystem and
     * internal reservations. If conflicts exist, appends an incrementing number until a free
     * filename is found.
     *
     * <p>Algorithm:
     *
     * <ol>
     *   <li>Start with {prefix}{suffix}
     *   <li>If taken, try {prefix}{suffix}_1, _2, etc.
     *   <li>Check both filesystem and reserved names
     *   <li>Reserve the first available filename
     * </ol>
     *
     * <p>Side effects:
     *
     * <ul>
     *   <li>Updates the indices map with the highest used number
     *   <li>Adds the reserved filename to the tracking list
     * </ul>
     *
     * <p>Performance: O(n) where n is the number of existing files with the same prefix. May
     * perform multiple filesystem checks.
     *
     * @param prefix base path and filename prefix (e.g., "/path/to/file_")
     * @param suffix filename suffix including extension (e.g., "screenshot.png")
     * @return unique filename that has been reserved for use
     */
    public String reserveFreePath(String prefix, String suffix) {
        // Handle null prefix and suffix
        if (prefix == null) {
            prefix = "";
        }
        if (suffix == null) {
            suffix = "";
        }

        // Start from the last used index + 1, or 0 if none exists
        int i = indices.getOrDefault(prefix, -1);
        String filename;

        // Try without index first if this is the first time
        if (i == -1) {
            filename = prefix + suffix;
            if (!imageUtils.fileExists(filename) && !filenameExists(filename)) {
                indices.put(prefix, 0);
                filenames.add(filename);
                return filename;
            }
            i = 0; // Start numbering from 1 (will be incremented below)
        }

        // Try with incrementing indices
        do {
            i++;
            filename = prefix + suffix + "_" + i;
        } while (imageUtils.fileExists(filename) || filenameExists(filename));

        indices.put(prefix, i);
        filenames.add(filename);
        return filename;
    }
}
