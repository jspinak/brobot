package io.github.jspinak.brobot.test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Prevents SikuliX from registering shutdown hooks that cause HeadlessException. This is a more
 * aggressive approach that intercepts Runtime.addShutdownHook calls.
 */
public class SikuliXShutdownPreventer {

    private static boolean installed = false;
    private static final Object lock = new Object();

    /**
     * Install the shutdown hook preventer. This must be called before any SikuliX classes are
     * loaded.
     */
    public static void install() {
        synchronized (lock) {
            if (installed) {
                return;
            }

            try {
                // Prevent SikuliX RunTime from initializing its shutdown hook
                preventSikuliXShutdownHook();

                // Also try to clear any existing shutdown hooks from Runtime
                clearExistingShutdownHooks();

                installed = true;
            } catch (Exception e) {
                // Silently ignore - tests will still run
                System.err.println(
                        "Warning: Could not prevent SikuliX shutdown hooks: " + e.getMessage());
            }
        }
    }

    private static void preventSikuliXShutdownHook() {
        try {
            // Don't set FromCommandLine as it might break normal SikuliX operations
            // Just disable visual features
            System.setProperty("sikuli.Highlight", "false");
            System.setProperty("sikuli.suppress.Java.printout", "true");

            // Try to access SikuliX RunTime class and disable its cleanup
            Class<?> runTimeClass = Class.forName("org.sikuli.script.support.RunTime");

            // Get the instance
            Method getInstance = runTimeClass.getMethod("get");
            Object runtime = getInstance.invoke(null);

            // Try to set various flags to prevent cleanup
            try {
                Field shouldCleanup = runTimeClass.getDeclaredField("shouldCleanUp");
                shouldCleanup.setAccessible(true);
                shouldCleanup.setBoolean(runtime, false);
            } catch (NoSuchFieldException ignored) {
            }

            try {
                Field isTerminating = runTimeClass.getDeclaredField("isTerminating");
                isTerminating.setAccessible(true);
                isTerminating.setBoolean(runtime, true);
            } catch (NoSuchFieldException ignored) {
            }

            // Try to remove the shutdown hook if it's already registered
            try {
                Field hookField = runTimeClass.getDeclaredField("shutdownHook");
                hookField.setAccessible(true);
                Thread hook = (Thread) hookField.get(runtime);
                if (hook != null) {
                    Runtime.getRuntime().removeShutdownHook(hook);
                    hookField.set(runtime, null);
                }
            } catch (Exception ignored) {
            }

        } catch (Exception e) {
            // Class not loaded yet or other issue - that's OK
        }
    }

    private static void clearExistingShutdownHooks() {
        try {
            // This is a hack to remove shutdown hooks that contain "sikuli" or "RunTime"
            Class<?> appHooksClass = Class.forName("java.lang.ApplicationShutdownHooks");
            Field hooksField = appHooksClass.getDeclaredField("hooks");
            hooksField.setAccessible(true);

            @SuppressWarnings("unchecked")
            java.util.IdentityHashMap<Thread, Thread> hooks =
                    (java.util.IdentityHashMap<Thread, Thread>) hooksField.get(null);

            if (hooks != null) {
                // Create a copy to avoid concurrent modification
                var hooksToRemove = new java.util.ArrayList<Thread>();

                for (Thread hook : hooks.keySet()) {
                    String name = hook.getName();
                    String className = hook.getClass().getName();
                    if (name.contains("SikuliX")
                            || name.contains("RunTime")
                            || className.contains("sikuli")
                            || className.contains("RunTime")) {
                        hooksToRemove.add(hook);
                    }
                }

                // Remove identified hooks
                for (Thread hook : hooksToRemove) {
                    try {
                        Runtime.getRuntime().removeShutdownHook(hook);
                    } catch (Exception ignored) {
                        // Hook might not be registered
                    }
                }
            }
        } catch (Exception e) {
            // Can't access shutdown hooks - that's OK
        }
    }

    /** Static initializer to install preventer as early as possible. */
    static {
        if (System.getProperty("java.awt.headless", "false").equals("true")
                || System.getenv("WSL_DISTRO_NAME") != null
                || System.getenv("CI") != null) {
            install();
        }
    }
}
