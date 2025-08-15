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
 *   <li>Accept {@link io.github.jspinak.brobot.action.ActionConfig} subclasses for type-safe configuration</li>
 *   <li>Work with {@link io.github.jspinak.brobot.action.ObjectCollection} for target elements</li>
 *   <li>Return {@link io.github.jspinak.brobot.action.ActionResult} containing execution details</li>
 *   <li>Support both online (live GUI) and offline (mocked) execution modes</li>
 *   <li>Perform runtime type checking to ensure correct configuration type</li>
 * </ul>
 * 
 * <h2>Example Usage</h2>
 * 
 * <pre>{@code
 * // Find an element on screen with pattern matching
 * PatternFindOptions findOptions = new PatternFindOptions.Builder()
 *     .setSimilarity(0.9)
 *     .setStrategy(PatternFindOptions.Strategy.BEST)
 *     .build();
 * 
 * ActionResult result = new ActionResult();
 * result.setActionConfig(findOptions);
 * find.perform(result, objectCollection);
 * 
 * // Click on a found element with specific timing
 * ClickOptions clickOptions = new ClickOptions.Builder()
 *     .setPauseAfterEnd(0.5)
 *     .build();
 * 
 * result.setActionConfig(clickOptions);
 * click.perform(result, objectCollection);
 * 
 * // Type text with slow speed
 * TypeOptions typeOptions = new TypeOptions.Builder()
 *     .setTypeDelay(0.1)
 *     .build();
 * 
 * result.setActionConfig(typeOptions);
 * type.perform(result, new ObjectCollection.Builder()
 *     .withStrings("Hello World")
 *     .build());
 * }</pre>
 * 
 * @see io.github.jspinak.brobot.action.ActionInterface
 * @see io.github.jspinak.brobot.action.ActionConfig
 * @see io.github.jspinak.brobot.action.basic.find.PatternFindOptions
 * @see io.github.jspinak.brobot.action.basic.click.ClickOptions
 * @see io.github.jspinak.brobot.action.basic.type.TypeOptions
 */
package io.github.jspinak.brobot.action.basic;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
