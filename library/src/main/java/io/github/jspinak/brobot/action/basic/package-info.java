/**
 * Basic (atomic) actions that execute GUI automation tasks in a single iteration.
 * 
 * <p>This package contains the fundamental building blocks of GUI automation in Brobot.
 * Each basic action performs a specific, atomic operation that cannot be decomposed into
 * simpler actions. These actions directly interact with the GUI through visual recognition
 * or direct manipulation.</p>
 * 
 * <h2>Action Categories</h2>
 * 
 * <p>Basic actions are organized into functional categories:</p>
 * 
 * <ul>
 *   <li><b>find</b> - Pattern matching and visual recognition actions (Find, FindText, FindColor)</li>
 *   <li><b>click</b> - Mouse click operations at specified locations or on found elements</li>
 *   <li><b>type</b> - Keyboard input simulation (TypeText, KeyDown, KeyUp)</li>
 *   <li><b>mouse</b> - Low-level mouse operations (MoveMouse, MouseDown, MouseUp, ScrollMouseWheel)</li>
 *   <li><b>visual</b> - Region definition and visual feedback actions (DefineRegion, Highlight)</li>
 *   <li><b>wait</b> - Actions that monitor GUI changes over time (WaitVanish, OnChange)</li>
 *   <li><b>classify</b> - Image classification and scene analysis actions</li>
 * </ul>
 * 
 * <h2>Design Principles</h2>
 * 
 * <ul>
 *   <li><b>Single Responsibility</b> - Each action performs one specific task</li>
 *   <li><b>Deterministic Behavior</b> - Given the same inputs and GUI state, actions produce consistent results</li>
 *   <li><b>Minimal Dependencies</b> - Basic actions don't depend on other actions for their core functionality</li>
 *   <li><b>Standardized Interface</b> - All actions implement {@link io.github.jspinak.brobot.action.ActionInterface}</li>
 * </ul>
 * 
 * <h2>Common Patterns</h2>
 * 
 * <p>All basic actions follow these patterns:</p>
 * <ul>
 *   <li>Accept {@link io.github.jspinak.brobot.action.ActionOptions} for configuration</li>
 *   <li>Work with {@link io.github.jspinak.brobot.action.ObjectCollection} for target elements</li>
 *   <li>Return {@link io.github.jspinak.brobot.action.ActionResult} containing execution details</li>
 *   <li>Support both online (live GUI) and offline (mocked) execution modes</li>
 * </ul>
 * 
 * <h2>Example Usage</h2>
 * 
 * <pre>{@code
 * // Find an element on screen
 * Find find = new Find(...);
 * ActionResult result = find.perform(matches, objectCollection);
 * 
 * // Click on a found element
 * Click click = new Click(...);
 * ActionResult clickResult = click.perform(result, objectCollection);
 * 
 * // Type text at current focus
 * TypeText type = new TypeText(...);
 * ActionResult typeResult = type.perform(new ActionResult(), 
 *     new ObjectCollection.Builder().withStrings("Hello World").build());
 * }</pre>
 * 
 * @see io.github.jspinak.brobot.action.ActionInterface
 * @see io.github.jspinak.brobot.action.basic.find.Find
 * @see io.github.jspinak.brobot.action.basic.click.Click
 * @see io.github.jspinak.brobot.action.basic.type.TypeText
 */
package io.github.jspinak.brobot.action.basic;