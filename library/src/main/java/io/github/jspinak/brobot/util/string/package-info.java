/**
 * String manipulation and text processing utilities for automation scenarios.
 *
 * <p>This package provides specialized string handling capabilities tailored for GUI automation and
 * OCR processing. It includes tools for fuzzy string matching, text selection from multiple
 * candidates, filename manipulation, and common string operations needed in automation workflows.
 *
 * <h2>Core Components</h2>
 *
 * <h3>StringSimilarity</h3>
 *
 * Fuzzy string matching using Levenshtein distance:
 *
 * <ul>
 *   <li>Calculate similarity scores between strings (0.0 to 1.0)
 *   <li>Edit distance computation with optimization
 *   <li>Case-insensitive comparison options
 *   <li>Useful for OCR result validation
 * </ul>
 *
 * <h3>TextSelector</h3>
 *
 * Intelligent selection from stochastic text variations:
 *
 * <ul>
 *   <li>RANDOM strategy for quick selection
 *   <li>MOST_SIMILAR strategy using consensus algorithm
 *   <li>Handles multiple OCR readings of same text
 *   <li>Spring component for dependency injection
 * </ul>
 *
 * <h3>Base64Converter</h3>
 *
 * Binary data encoding for storage and transmission:
 *
 * <ul>
 *   <li>Convert images to Base64 strings
 *   <li>Decode Base64 back to binary data
 *   <li>Support for various image formats
 *   <li>Used in data serialization and storage
 * </ul>
 *
 * <h3>StringFusion</h3>
 *
 * Intelligent string combination removing redundancy:
 *
 * <ul>
 *   <li>Eliminate common prefix when combining strings
 *   <li>Create concise composite identifiers
 *   <li>Useful for hierarchical naming
 * </ul>
 *
 * <h3>FilenameExtractor</h3>
 *
 * File path and name manipulation:
 *
 * <ul>
 *   <li>Extract base filename without path or extension
 *   <li>Platform-independent path handling
 *   <li>Null-safe operations
 * </ul>
 *
 * <h3>RegexPatterns</h3>
 *
 * Regular expression patterns and validation:
 *
 * <ul>
 *   <li>Numeric string validation
 *   <li>Centralized regex patterns
 *   <li>Extensible for future patterns
 * </ul>
 *
 * <h2>Usage Patterns</h2>
 *
 * <h3>String Similarity</h3>
 *
 * <pre>{@code
 * // Compare OCR results
 * double score = StringSimilarity.similarity("Hello", "Hallo");
 * // Returns: 0.8 (80% similar)
 *
 * // Get edit distance
 * int edits = StringSimilarity.editDistance("kitten", "sitting");
 * // Returns: 3
 * }</pre>
 *
 * <h3>Text Selection</h3>
 *
 * <pre>{@code
 * @Autowired
 * private TextSelector textSelector;
 *
 * // Select best OCR result
 * Text ocrResults = new Text("Hello", "Hallo", "Hell0");
 * String best = textSelector.getString(Method.MOST_SIMILAR, ocrResults);
 * // Returns: "Hello" (most similar to all others)
 * }</pre>
 *
 * <h3>Base64 Operations</h3>
 *
 * <pre>{@code
 * // Encode image to Base64
 * String encoded = Base64Converter.encode(bufferedImage, "PNG");
 *
 * // Decode back to BufferedImage
 * BufferedImage decoded = Base64Converter.decode(encoded);
 * }</pre>
 *
 * <h3>String Fusion</h3>
 *
 * <pre>{@code
 * // Combine hierarchical names
 * String fused = StringFusion.fuse("user_name", "user_email");
 * // Returns: "user_name-email"
 * }</pre>
 *
 * <h3>Filename Operations</h3>
 *
 * <pre>{@code
 * // Extract base name
 * String base = FilenameExtractor.getFilenameWithoutExtensionAndDirectory(
 *     "/path/to/file.txt");
 * // Returns: "file"
 * }</pre>
 *
 * <h2>OCR Integration</h2>
 *
 * The string utilities are particularly valuable for OCR processing:
 *
 * <ul>
 *   <li>Handle multiple OCR readings with TextSelector
 *   <li>Validate OCR results using StringSimilarity
 *   <li>Clean and normalize text output
 *   <li>Fuzzy matching for imperfect readings
 * </ul>
 *
 * <h2>Common Applications</h2>
 *
 * <h3>Text Recognition</h3>
 *
 * <ul>
 *   <li>OCR result disambiguation
 *   <li>Fuzzy text matching in UI
 *   <li>Typo tolerance in automation
 * </ul>
 *
 * <h3>Data Processing</h3>
 *
 * <ul>
 *   <li>Image serialization for storage
 *   <li>Filename generation and parsing
 *   <li>Identifier creation from paths
 * </ul>
 *
 * <h3>Validation</h3>
 *
 * <ul>
 *   <li>Input validation using regex
 *   <li>Text similarity thresholds
 *   <li>Format verification
 * </ul>
 *
 * <h2>Performance Considerations</h2>
 *
 * <ul>
 *   <li>Levenshtein distance: O(m×n) time, O(min(m,n)) space
 *   <li>Consensus selection: O(n²) for n text variations
 *   <li>Base64 encoding: Linear with data size
 *   <li>All operations optimized for typical string lengths
 * </ul>
 *
 * <h2>Thread Safety</h2>
 *
 * <ul>
 *   <li>Static utility methods are thread-safe
 *   <li>TextSelector has minor Random instance issue
 *   <li>No shared mutable state in other classes
 * </ul>
 *
 * @since 1.0.0
 * @see io.github.jspinak.brobot.model.element.Text
 * @see io.github.jspinak.brobot.ocr
 */
package io.github.jspinak.brobot.util.string;
