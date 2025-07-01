/**
 * Temporary migration utilities for transitioning from ActionOptions to ActionConfig.
 * <p>
 * This package contains helper classes and utilities to facilitate the migration
 * from the legacy monolithic ActionOptions to the new type-safe ActionConfig
 * hierarchy. These utilities are designed to ease the transition process and
 * should be removed once the migration is complete.
 * <p>
 * <strong>Key components:</strong>
 * <ul>
 * <li>{@link io.github.jspinak.brobot.action.migration.ActionConfigMigrationHelper} - 
 *     Provides convenience methods for common migration scenarios</li>
 * </ul>
 * <p>
 * <strong>Migration approach:</strong>
 * <ol>
 * <li>Use the ActionOptionsAdapter to convert existing ActionOptions instances</li>
 * <li>Gradually replace ActionOptions creation with specific ActionConfig builders</li>
 * <li>Update tests and dependent code to use the new API directly</li>
 * <li>Remove migration utilities once all code is updated</li>
 * </ol>
 * 
 * @deprecated This entire package is temporary and will be removed after migration
 * @since 2.0
 */
@Deprecated
package io.github.jspinak.brobot.action.migration;