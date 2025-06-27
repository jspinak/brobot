package io.github.jspinak.brobot.util.file;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.util.image.io.ImageFileUtilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages filename reservations to prevent file conflicts during concurrent operations.
 * <p>
 * This repository tracks filenames that have been allocated but not yet written to disk,
 * ensuring unique filenames across multiple illustration generation processes. It serves
 * as a central coordination point for filename allocation in Brobot's visual documentation
 * system, preventing race conditions and filename collisions.
 * <p>
 * Key features:
 * <ul>
 * <li>In-memory tracking of reserved filenames</li>
 * <li>Automatic numbering for duplicate filename requests</li>
 * <li>Coordination with filesystem checks via {@link ImageFileUtilities}</li>
 * <li>Thread-safe filename generation with incremental indices</li>
 * </ul>
 * <p>
 * Filename generation strategy:
 * <ul>
 * <li>Base format: {prefix}{suffix}</li>
 * <li>Collision format: {prefix}{suffix}_{number}</li>
 * <li>Numbers increment until a free filename is found</li>
 * <li>Checks both filesystem and in-memory reservations</li>
 * </ul>
 * <p>
 * Use cases:
 * <ul>
 * <li>Preventing overwrites during parallel illustration generation</li>
 * <li>Ensuring unique filenames for batch operations</li>
 * <li>Coordinating filename allocation across multiple components</li>
 * <li>Supporting concurrent screenshot and analysis operations</li>
 * </ul>
 * <p>
 * Implementation notes:
 * <ul>
 * <li>Maintains a list of all reserved filenames</li>
 * <li>Tracks highest index per prefix for efficient numbering</li>
 * <li>Does not handle cleanup of reservations after file writing</li>
 * <li>Memory usage grows with number of reservations</li>
 * </ul>
 * <p>
 * Thread safety: This implementation is NOT thread-safe. Concurrent access
 * should be synchronized externally or consider using ConcurrentHashMap
 * and thread-safe collections.
 *
 * @see ImageFileUtilities#fileExists(String)
 * @see HistoryFileNamer
 */
@Component
public class FilenameAllocator {

    private ImageFileUtilities imageUtils;

    /**
     * List of all filenames that have been reserved but may not yet be written.
     * Grows unbounded as filenames are reserved throughout application lifetime.
     */
    private List<String> filenames = new ArrayList<>();
    
    /**
     * Maps filename prefixes to their highest used index number.
     * Used to efficiently generate the next available numbered filename.
     */
    private Map<String, Integer> indices = new HashMap<>();


    public FilenameAllocator(ImageFileUtilities imageUtils) {
        this.imageUtils = imageUtils;
    }

    /**
     * Manually reserves a filename in the repository.
     * <p>
     * Adds a filename to the internal tracking list without generating it.
     * This is useful when filenames are created externally but need to be
     * tracked to prevent conflicts.
     * <p>
     * Side effects: Modifies the internal filename list.
     *
     * @param filename the filename to reserve; should include full path
     */
    public void addFilename(String filename) {
        filenames.add(filename);
    }

    /**
     * Checks if a filename has been reserved in this session.
     * <p>
     * Only checks the in-memory reservation list, not the actual filesystem.
     * Use in conjunction with filesystem checks for complete validation.
     *
     * @param filename the filename to check
     * @return true if the filename has been reserved, false otherwise
     */
    public boolean filenameExists(String filename) {
        return filenames.contains(filename);
    }

    /**
     * Generates and reserves a unique filename with automatic numbering.
     * <p>
     * Creates a filename by combining prefix and suffix, then checks both
     * the filesystem and internal reservations. If conflicts exist, appends
     * an incrementing number until a free filename is found.
     * <p>
     * Algorithm:
     * <ol>
     * <li>Start with {prefix}{suffix}</li>
     * <li>If taken, try {prefix}{suffix}_1, _2, etc.</li>
     * <li>Check both filesystem and reserved names</li>
     * <li>Reserve the first available filename</li>
     * </ol>
     * <p>
     * Side effects:
     * <ul>
     * <li>Updates the indices map with the highest used number</li>
     * <li>Adds the reserved filename to the tracking list</li>
     * </ul>
     * <p>
     * Performance: O(n) where n is the number of existing files with the
     * same prefix. May perform multiple filesystem checks.
     *
     * @param prefix base path and filename prefix (e.g., "/path/to/file_")
     * @param suffix filename suffix including extension (e.g., "screenshot.png")
     * @return unique filename that has been reserved for use
     */
    public String reserveFreePath(String prefix, String suffix) {
        int i = indices.containsKey(prefix) ? indices.get(prefix) + 1 : 0;
        String filename = prefix + suffix;
        while (imageUtils.fileExists(filename) || filenameExists(filename)) {
            i++;
            filename = prefix + suffix + "_" + i;
        }
        indices.put(prefix, i);
        filenames.add(filename);
        return filename;
    }
}
