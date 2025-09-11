/**
 * Internal options classes for backward compatibility.
 *
 * <p>This package contains legacy configuration classes that are maintained for backward
 * compatibility but are not part of the public API. New applications should use the modern
 * ActionConfig architecture instead.
 *
 * <p><strong>Important:</strong> Classes in this package are internal implementation details and
 * may be changed or removed in future versions without notice. They should not be used in new code.
 *
 * <p>For new development, use these public API alternatives:
 *
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.action.basic.click.ClickOptions} for click actions
 *   <li>{@link io.github.jspinak.brobot.action.basic.find.PatternFindOptions} for pattern finding
 *   <li>{@link io.github.jspinak.brobot.action.basic.type.TypeOptions} for typing
 *   <li>{@link io.github.jspinak.brobot.action.basic.region.DefineRegionOptions} for region
 *       definition
 *   <li>Other specific ActionConfig implementations in the public API
 * </ul>
 *
 * @since 2.0
 */
package io.github.jspinak.brobot.action.internal.options;
