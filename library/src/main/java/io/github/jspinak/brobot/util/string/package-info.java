/**
 * String manipulation and text processing utilities for automation scenarios.
 * <p>
 * This package provides specialized string handling capabilities tailored for GUI
 * automation and OCR processing. It includes tools for fuzzy string matching,
 * text selection from multiple candidates, filename manipulation, and common
 * string operations needed in automation workflows.
 * 
 * <h2>Core Components</h2>
 * 
 * <h3>StringSimilarity</h3>
 * Fuzzy string matching using Levenshtein distance:
 * <ul>
 *   <li>Calculate similarity scores between strings (0.0 to 1.0)</li>
 *   <li>Edit distance computation with optimization</li>
 *   <li>Case-insensitive comparison options</li>
 *   <li>Useful for OCR result validation</li>
 * </ul>
 * 
 * <h3>TextSelector</h3>
 * Intelligent selection from stochastic text variations:
 * <ul>
 *   <li>RANDOM strategy for quick selection</li>
 *   <li>MOST_SIMILAR strategy using consensus algorithm</li>
 *   <li>Handles multiple OCR readings of same text</li>
 *   <li>Spring component for dependency injection</li>
 * </ul>
 * 
 * <h3>Base64Converter</h3>
 * Binary data encoding for storage and transmission:
 * <ul>
 *   <li>Convert images to Base64 strings</li>
 *   <li>Decode Base64 back to binary data</li>
 *   <li>Support for various image formats</li>
 *   <li>Used in data serialization and storage</li>
 * </ul>
 * 
 * <h3>StringFusion</h3>
 * Intelligent string combination removing redundancy:
 * <ul>
 *   <li>Eliminate common prefix when combining strings</li>
 *   <li>Create concise composite identifiers</li>
 *   <li>Useful for hierarchical naming</li>
 * </ul>
 * 
 * <h3>FilenameExtractor</h3>
 * File path and name manipulation:
 * <ul>
 *   <li>Extract base filename without path or extension</li>
 *   <li>Platform-independent path handling</li>
 *   <li>Null-safe operations</li>
 * </ul>
 * 
 * <h3>RegexPatterns</h3>
 * Regular expression patterns and validation:
 * <ul>
 *   <li>Numeric string validation</li>
 *   <li>Centralized regex patterns</li>
 *   <li>Extensible for future patterns</li>
 * </ul>
 * 
 * <h2>Usage Patterns</h2>
 * 
 * <h3>String Similarity</h3>
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
 * <pre>{@code
 * // Encode image to Base64
 * String encoded = Base64Converter.encode(bufferedImage, "PNG");
 * 
 * // Decode back to BufferedImage
 * BufferedImage decoded = Base64Converter.decode(encoded);
 * }</pre>
 * 
 * <h3>String Fusion</h3>
 * <pre>{@code
 * // Combine hierarchical names
 * String fused = StringFusion.fuse("user_name", "user_email");
 * // Returns: "user_name-email"
 * }</pre>
 * 
 * <h3>Filename Operations</h3>
 * <pre>{@code
 * // Extract base name
 * String base = FilenameExtractor.getFilenameWithoutExtensionAndDirectory(
 *     "/path/to/file.txt");
 * // Returns: "file"
 * }</pre>
 * 
 * <h2>OCR Integration</h2>
 * The string utilities are particularly valuable for OCR processing:
 * <ul>
 *   <li>Handle multiple OCR readings with TextSelector</li>
 *   <li>Validate OCR results using StringSimilarity</li>
 *   <li>Clean and normalize text output</li>
 *   <li>Fuzzy matching for imperfect readings</li>
 * </ul>
 * 
 * <h2>Common Applications</h2>
 * 
 * <h3>Text Recognition</h3>
 * <ul>
 *   <li>OCR result disambiguation</li>
 *   <li>Fuzzy text matching in UI</li>
 *   <li>Typo tolerance in automation</li>
 * </ul>
 * 
 * <h3>Data Processing</h3>
 * <ul>
 *   <li>Image serialization for storage</li>
 *   <li>Filename generation and parsing</li>
 *   <li>Identifier creation from paths</li>
 * </ul>
 * 
 * <h3>Validation</h3>
 * <ul>
 *   <li>Input validation using regex</li>
 *   <li>Text similarity thresholds</li>
 *   <li>Format verification</li>
 * </ul>
 * 
 * <h2>Performance Considerations</h2>
 * <ul>
 *   <li>Levenshtein distance: O(m×n) time, O(min(m,n)) space</li>
 *   <li>Consensus selection: O(n²) for n text variations</li>
 *   <li>Base64 encoding: Linear with data size</li>
 *   <li>All operations optimized for typical string lengths</li>
 * </ul>
 * 
 * <h2>Thread Safety</h2>
 * <ul>
 *   <li>Static utility methods are thread-safe</li>
 *   <li>TextSelector has minor Random instance issue</li>
 *   <li>No shared mutable state in other classes</li>
 * </ul>
 * 
 * @since 1.0.0
 * @see io.github.jspinak.brobot.model.element.Text
 * @see io.github.jspinak.brobot.ocr
 */
package io.github.jspinak.brobot.util.string;