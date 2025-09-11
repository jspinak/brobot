/**
 * Core orchestration and coordination for visual pattern finding operations.
 *
 * <p>This package provides the central framework for finding GUI elements through various
 * recognition strategies including image matching, color analysis, and text recognition. It
 * coordinates specialized subsystems while managing the overall find process flow.
 *
 * <h2>Package Organization</h2>
 *
 * <p>The find system is organized into specialized subsystems:
 *
 * <ul>
 *   <li><b>Core Orchestration</b> - Main package classes that coordinate the find process
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.find.pixel pixel}</b> - Color-based
 *       analysis and pixel-level matching
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.find.scene scene}</b> - Scene capture
 *       and pattern matching within scenes
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.find.match match}</b> - Match
 *       processing, adjustment, and collection management
 * </ul>
 *
 * <h2>Core Orchestration Components</h2>
 *
 * <ul>
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.find.IterativePatternFinder}</b> - Main
 *       orchestrator that manages the iterative pattern finding process across scenes and state
 *       images
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.find.SearchRegionResolver}</b> -
 *       Determines optimal search regions based on a sophisticated priority hierarchy
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.find.NonImageObjectConverter}</b> -
 *       Converts non-image state objects (regions, locations) into match results
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.find.OffsetLocationManager}</b> -
 *       Manages location calculations and offset-based positioning
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.find.TargetImageMatchExtractor}</b> -
 *       Bridges scene analysis results to match extraction for specific targets
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.find.DefinedRegionConverter}</b> -
 *       Handles pre-defined regions without pattern matching
 * </ul>
 *
 * <h2>Find Process Flow</h2>
 *
 * <ol>
 *   <li><b>Initialization</b> - Prepare search parameters and regions
 *   <li><b>Region Resolution</b> - Determine search areas via SearchRegionResolver
 *   <li><b>Scene Acquisition</b> - Capture or retrieve scenes for analysis
 *   <li><b>Pattern Matching</b> - Apply appropriate matching strategy:
 *       <ul>
 *         <li>Image template matching via scene subsystem
 *         <li>Color analysis via pixel subsystem
 *         <li>Text recognition via OCR integration
 *         <li>Pre-defined regions via RegionAsMatchConverter
 *       </ul>
 *   <li><b>Match Processing</b> - Adjust, filter, and enhance matches
 *   <li><b>Result Assembly</b> - Compile final ActionResult with all matches
 * </ol>
 *
 * <h2>Architecture Principles</h2>
 *
 * <p>This package implements key principles from model-based GUI automation:
 *
 * <ul>
 *   <li><b>Separation of Concerns</b> - Strategic knowledge (how to find) is separated from domain
 *       knowledge (what to find)
 *   <li><b>Composability</b> - Components can be combined for complex find operations
 *   <li><b>Extensibility</b> - New matching strategies can be added without changing core
 *       orchestration
 *   <li><b>Robustness</b> - Multiple fallback strategies handle GUI variability
 * </ul>
 *
 * <h2>Performance Considerations</h2>
 *
 * <ul>
 *   <li>Search space reduction through intelligent region selection
 *   <li>Early termination strategies (FIRST, EACH) minimize unnecessary work
 *   <li>Efficient memory usage through scene reuse and cleanup
 *   <li>Parallel processing support for multi-target searches
 * </ul>
 *
 * @see io.github.jspinak.brobot.action.basic.find.Find
 * @see io.github.jspinak.brobot.action.ActionOptions
 * @see io.github.jspinak.brobot.model.match.Match
 */
package io.github.jspinak.brobot.action.internal.find;
