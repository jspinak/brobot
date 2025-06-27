/**
 * Internal wrappers and utilities for keyboard and text operations.
 * 
 * <p>This package provides the internal implementation layer for text input,
 * keyboard control, and text extraction operations. These wrappers handle
 * the low-level details of keyboard simulation, OCR integration, and
 * platform-specific text handling.</p>
 * 
 * <h2>Text Operation Wrappers</h2>
 * 
 * <ul>
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.text.TypeTextWrapper}</b> - 
 *       Low-level text typing with timing control</li>
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.text.KeyDownWrapper}</b> - 
 *       Individual key press operations</li>
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.text.KeyUpWrapper}</b> - 
 *       Individual key release operations</li>
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.text.GetTextWrapper}</b> - 
 *       OCR text extraction from screen regions</li>
 * </ul>
 * 
 * <h2>Text Input Features</h2>
 * 
 * <h3>Typing Characteristics</h3>
 * <ul>
 *   <li>Natural typing speed variations</li>
 *   <li>Character-by-character or paste simulation</li>
 *   <li>Special character handling</li>
 *   <li>Unicode support</li>
 * </ul>
 * 
 * <h3>Key Operations</h3>
 * <ul>
 *   <li>Individual key press/release control</li>
 *   <li>Modifier key combinations (Ctrl, Alt, Shift)</li>
 *   <li>Function keys and special keys</li>
 *   <li>Key repeat simulation</li>
 * </ul>
 * 
 * <h3>Text Extraction</h3>
 * <ul>
 *   <li>OCR integration for text recognition</li>
 *   <li>Multiple OCR engine support</li>
 *   <li>Text preprocessing for better recognition</li>
 *   <li>Confidence scoring and validation</li>
 * </ul>
 * 
 * <h2>Platform Handling</h2>
 * 
 * <ul>
 *   <li>Cross-platform key code mapping</li>
 *   <li>Layout-specific adjustments</li>
 *   <li>IME (Input Method Editor) support</li>
 *   <li>Clipboard integration options</li>
 * </ul>
 * 
 * <h2>Special Key Notation</h2>
 * 
 * <p>Special keys use consistent notation:</p>
 * <ul>
 *   <li>{ENTER}, {TAB}, {ESC} - Common control keys</li>
 *   <li>{F1} through {F12} - Function keys</li>
 *   <li>{CTRL}, {ALT}, {SHIFT}, {WIN} - Modifier keys</li>
 *   <li>{UP}, {DOWN}, {LEFT}, {RIGHT} - Arrow keys</li>
 *   <li>{HOME}, {END}, {PGUP}, {PGDN} - Navigation keys</li>
 * </ul>
 * 
 * <h2>Implementation Examples</h2>
 * 
 * <h3>Text Typing</h3>
 * <pre>{@code
 * // Internal wrapper usage
 * String text = "Hello, World!";
 * int delayBetweenKeys = 50; // milliseconds
 * 
 * TypeTextWrapper.typeText(text, delayBetweenKeys);
 * 
 * // With special keys
 * String withSpecial = "Username{TAB}Password{ENTER}";
 * TypeTextWrapper.typeTextWithSpecialKeys(withSpecial, delayBetweenKeys);
 * }</pre>
 * 
 * <h3>Key Combinations</h3>
 * <pre>{@code
 * // Ctrl+A (Select All)
 * KeyDownWrapper.pressKey(Key.CTRL);
 * TypeTextWrapper.typeKey("a");
 * KeyUpWrapper.releaseKey(Key.CTRL);
 * 
 * // Alt+Tab
 * KeyDownWrapper.pressKey(Key.ALT);
 * TypeTextWrapper.typeKey(Key.TAB);
 * KeyUpWrapper.releaseKey(Key.ALT);
 * }</pre>
 * 
 * <h3>Text Extraction</h3>
 * <pre>{@code
 * // Extract text from region
 * Region textRegion = new Region(100, 100, 300, 50);
 * OCRConfig config = new OCRConfig()
 *     .withLanguage("eng")
 *     .withPreprocessing(true);
 * 
 * String extractedText = GetTextWrapper.getText(textRegion, config);
 * double confidence = GetTextWrapper.getConfidence();
 * }</pre>
 * 
 * <h2>Error Handling</h2>
 * 
 * <ul>
 *   <li>Graceful handling of unsupported characters</li>
 *   <li>Recovery from focus loss</li>
 *   <li>OCR failure fallbacks</li>
 *   <li>Timeout protection for long operations</li>
 * </ul>
 * 
 * <h2>Performance Considerations</h2>
 * 
 * <ul>
 *   <li>Batch typing for improved speed</li>
 *   <li>OCR result caching</li>
 *   <li>Preprocessing optimization</li>
 *   <li>Minimal key event generation</li>
 * </ul>
 * 
 * <h2>Testing Support</h2>
 * 
 * <ul>
 *   <li>Mock keyboard for unit tests</li>
 *   <li>Recorded keystroke playback</li>
 *   <li>OCR result simulation</li>
 *   <li>Deterministic timing modes</li>
 * </ul>
 * 
 * @see io.github.jspinak.brobot.action.basic.type.TypeText
 * @see io.github.jspinak.brobot.action.basic.type.KeyDown
 * @see io.github.jspinak.brobot.action.basic.type.KeyUp
 */
package io.github.jspinak.brobot.action.internal.text;