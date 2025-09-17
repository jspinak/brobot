package io.github.jspinak.brobot.test.locks;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.parallel.ResourceLock;

/**
 * Indicates that a test requires exclusive access to shared file system resources. Tests with this
 * annotation will not run in parallel with other file-system-locked tests.
 *
 * <p>Use this for: - Tests that write to shared directories (not @TempDir) - Tests that modify
 * configuration files - Tests that access shared image directories - Tests that manipulate the
 * ImagePath
 *
 * <p>Note: Tests using @TempDir don't need this lock as they have isolated directories.
 *
 * <p>Example:
 *
 * <pre>{@code
 * @Test
 * @FileSystemLock
 * void testSharedImageDirectory() {
 *     // This test has exclusive file system access
 * }
 * }</pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ResourceLock("FILE_SYSTEM")
public @interface FileSystemLock {}
