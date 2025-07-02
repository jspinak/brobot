/**
 * Keyboard input simulation for text entry and key commands.
 * 
 * <p>This package provides actions for simulating keyboard input, including typing text,
 * pressing individual keys, and executing keyboard shortcuts. These actions enable
 * automation of any task requiring keyboard interaction.</p>
 * 
 * <h2>Key Actions</h2>
 * 
 * <ul>
 *   <li><b>{@link io.github.jspinak.brobot.action.basic.type.TypeText}</b> - 
 *       Types text strings to the focused window or input field</li>
 *   <li><b>{@link io.github.jspinak.brobot.action.basic.type.KeyDown}</b> - 
 *       Presses and holds a key (useful for modifiers like Ctrl, Shift)</li>
 *   <li><b>{@link io.github.jspinak.brobot.action.basic.type.KeyUp}</b> - 
 *       Releases a previously pressed key</li>
 * </ul>
 * 
 * <h2>Features</h2>
 * 
 * <ul>
 *   <li><b>Natural Typing</b> - Configurable delays between keystrokes for human-like typing</li>
 *   <li><b>Special Keys</b> - Support for function keys, arrows, modifiers, and special characters</li>
 *   <li><b>Key Combinations</b> - Execute shortcuts like Ctrl+C, Alt+Tab</li>
 *   <li><b>Batch Processing</b> - Type multiple strings in sequence</li>
 *   <li><b>Focus Management</b> - Ensure correct window/field has focus before typing</li>
 * </ul>
 * 
 * <h2>Typing Process</h2>
 * 
 * <ol>
 *   <li>Verify or establish focus on target input area</li>
 *   <li>Process text string or key sequence</li>
 *   <li>Simulate individual keystrokes with configured timing</li>
 *   <li>Handle special characters and key combinations</li>
 *   <li>Verify completion and return results</li>
 * </ol>
 * 
 * <h2>Configuration Options</h2>
 * 
 * <p>Through {@link io.github.jspinak.brobot.action.basic.type.TypeOptions}:</p>
 * <ul>
 *   <li><b>Typing speed</b> - Delay between keystrokes via setDelay()</li>
 *   <li><b>Modifiers</b> - Set of KeyModifier enums (CTRL, ALT, SHIFT, etc.)</li>
 *   <li><b>Clear before type</b> - Clear existing text before typing</li>
 *   <li><b>Timing control</b> - PauseBeforeBegin and pauseAfterEnd for precise control</li>
 * </ul>
 * 
 * <h2>Example Usage</h2>
 * 
 * <pre>{@code
 * // Simple text typing using ActionConfig (recommended)
 * TypeText typeText = new TypeText(...);
 * 
 * TypeOptions typeOptions = new TypeOptions.Builder()
 *     .setDelay(0.05)  // 50ms between keystrokes
 *     .build();
 * 
 * ObjectCollection text = new ObjectCollection.Builder()
 *     .withStrings("Hello, World!")
 *     .build();
 * 
 * ActionResult result = typeText.perform(typeOptions, text);
 * 
 * // Type with modifiers (e.g., Ctrl+A to select all)
 * TypeOptions ctrlTypeOptions = new TypeOptions.Builder()
 *     .setModifiers(Set.of(KeyModifier.CTRL))
 *     .build();
 * 
 * ObjectCollection selectAllKey = new ObjectCollection.Builder()
 *     .withStrings("a")
 *     .build();
 * 
 * typeText.perform(ctrlTypeOptions, selectAllKey);
 * 
 * // Type with special keys using strings
 * ObjectCollection specialKeys = new ObjectCollection.Builder()
 *     .withStrings("{TAB}Username{TAB}Password{ENTER}")
 *     .build();
 * 
 * typeText.perform(typeOptions, specialKeys);
 * 
 * // Keyboard operations using KeyDownOptions/KeyUpOptions
 * KeyDown keyDown = new KeyDown(...);
 * KeyUp keyUp = new KeyUp(...);
 * 
 * // Hold Shift while typing
 * KeyDownOptions shiftDown = new KeyDownOptions.Builder()
 *     .setKey("SHIFT")
 *     .build();
 * 
 * keyDown.perform(shiftDown, new ObjectCollection.Builder().build());
 * 
 * // Type uppercase letters
 * typeText.perform(typeOptions, new ObjectCollection.Builder()
 *     .withStrings("hello")  // Will be uppercase due to Shift
 *     .build());
 * 
 * // Release Shift
 * KeyUpOptions shiftUp = new KeyUpOptions.Builder()
 *     .setKey("SHIFT")
 *     .build();
 * 
 * keyUp.perform(shiftUp, new ObjectCollection.Builder().build());
 * 
 * // Using convenience utilities
 * TypeOptions slowType = ActionConfigShortcuts.typeSlowly(0.2);  // 200ms delay
 * }</pre>
 * 
 * <h2>Special Key Notation</h2>
 * 
 * <p>Special keys are denoted with curly braces:</p>
 * <ul>
 *   <li>{ENTER}, {TAB}, {ESC}</li>
 *   <li>{F1} through {F12}</li>
 *   <li>{CTRL}, {ALT}, {SHIFT}, {WIN}</li>
 *   <li>{UP}, {DOWN}, {LEFT}, {RIGHT}</li>
 *   <li>{HOME}, {END}, {PGUP}, {PGDN}</li>
 *   <li>{BACKSPACE}, {DELETE}</li>
 * </ul>
 * 
 * <h2>Best Practices</h2>
 * 
 * <ul>
 *   <li>Click on input fields before typing to ensure focus</li>
 *   <li>Use appropriate delays for application responsiveness</li>
 *   <li>Clear fields when replacing text rather than appending</li>
 *   <li>Test special characters across different systems</li>
 *   <li>Consider clipboard operations for large text blocks</li>
 * </ul>
 * 
 * @see io.github.jspinak.brobot.action.basic.type.TypeText
 * @see io.github.jspinak.brobot.action.basic.type.KeyDown
 * @see io.github.jspinak.brobot.action.basic.type.KeyUp
 */
package io.github.jspinak.brobot.action.basic.type;