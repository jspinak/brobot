package io.github.jspinak.brobot.debug;

import static io.github.jspinak.brobot.debug.AnsiColor.*;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.state.StateImage;

import lombok.extern.slf4j.Slf4j;

/**
 * AOP interceptor for Find operations to provide debug output. Intercepts Find.perform() calls and
 * provides comprehensive debugging information.
 */
@Slf4j
@Aspect
@Component
@ConditionalOnProperty(name = "brobot.debug.image.enabled", havingValue = "true")
public class FindOperationInterceptor {

    @Autowired(required = false)
    private ImageFindDebugger debugger;

    @jakarta.annotation.PostConstruct
    public void init() {
        log.info(
                "✅ FindOperationInterceptor initialized - Find operations will be intercepted for"
                        + " debugging");
        System.out.println(success("✅ IMAGE FIND DEBUGGER: AOP Interceptor Active"));
    }

    @Autowired(required = false)
    private ImageDebugConfig config;

    private static final ThreadLocal<Integer> depth = ThreadLocal.withInitial(() -> 0);

    /** Intercept Find.perform() operations for debugging. */
    @Around("execution(* io.github.jspinak.brobot.action.basic.find.Find.perform(..))")
    public Object interceptFindPerform(ProceedingJoinPoint joinPoint) throws Throwable {
        log.debug("🔍 INTERCEPTING Find.perform() operation");
        if (config == null || !config.isEnabled() || debugger == null) {
            return joinPoint.proceed();
        }

        long startTime = System.currentTimeMillis();
        boolean isNested = depth.get() > 0;
        depth.set(depth.get() + 1);

        Object[] args = joinPoint.getArgs();
        ActionResult actionResult = null;
        ObjectCollection[] collections = null;

        // Extract arguments
        if (args.length >= 2) {
            actionResult = (ActionResult) args[0];
            collections = (ObjectCollection[]) args[1];
        }

        // Log before execution
        if (config.isLevelEnabled(ImageDebugConfig.DebugLevel.DETAILED) && !isNested) {
            logBeforeFind(collections);
        }

        // Execute the actual find operation
        Object result = null;
        Exception exception = null;
        try {
            result = joinPoint.proceed();
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;

            // Log after execution
            if (!isNested && collections != null && collections.length > 0) {
                PatternFindOptions options = extractOptions(collections[0]);

                // Use the main debugger for comprehensive output
                if (debugger != null && actionResult != null) {
                    debugger.debugFindOperation(collections[0], options, actionResult);
                } else {
                    // Fallback to simple logging
                    logAfterFind(collections[0], actionResult, duration, exception);
                }
            }

            depth.set(depth.get() - 1);
        }

        return result;
    }

    /** Intercept FindPipeline.saveMatchesToStateImages for additional debugging. */
    @Around(
            "execution(*"
                + " io.github.jspinak.brobot.action.basic.find.FindPipeline.saveMatchesToStateImages(..))")
    public Object interceptSaveMatches(ProceedingJoinPoint joinPoint) throws Throwable {
        if (config == null
                || !config.isEnabled()
                || !config.isLevelEnabled(ImageDebugConfig.DebugLevel.FULL)) {
            return joinPoint.proceed();
        }

        Object[] args = joinPoint.getArgs();
        if (args.length >= 2) {
            ActionResult matches = (ActionResult) args[0];
            ObjectCollection[] collections = (ObjectCollection[]) args[1];

            if (config.getConsole().isUseColors()) {
                System.out.println(
                        dim("  [SAVE] Saving " + matches.size() + " matches to StateImages"));
            }
        }

        Object result = joinPoint.proceed();

        if (args.length >= 2) {
            ObjectCollection[] collections = (ObjectCollection[]) args[1];
            for (ObjectCollection col : collections) {
                for (StateImage img : col.getStateImages()) {
                    if (!img.getLastMatchesFound().isEmpty() && config.getConsole().isUseColors()) {
                        System.out.println(
                                success(
                                        "    ✓ "
                                                + img.getName()
                                                + " updated with "
                                                + img.getLastMatchesFound().size()
                                                + " matches"));
                    }
                }
            }
        }

        return result;
    }

    private void logBeforeFind(ObjectCollection[] collections) {
        if (collections == null || collections.length == 0) return;

        StringBuilder log = new StringBuilder();

        if (config.getConsole().isUseColors()) {
            log.append(info("→ FIND START: "));
        } else {
            log.append("FIND START: ");
        }

        for (int i = 0; i < collections.length; i++) {
            ObjectCollection col = collections[i];
            if (i > 0) log.append(", ");

            if (!col.getStateImages().isEmpty()) {
                StateImage img = col.getStateImages().get(0);
                if (config.getConsole().isUseColors()) {
                    log.append(header(img.getName()));
                    if (col.getStateImages().size() > 1) {
                        log.append(dim(" (+" + (col.getStateImages().size() - 1) + " more)"));
                    }
                } else {
                    log.append(img.getName());
                    if (col.getStateImages().size() > 1) {
                        log.append(" (+" + (col.getStateImages().size() - 1) + " more)");
                    }
                }
            }
        }

        System.out.println(log.toString());
    }

    private void logAfterFind(
            ObjectCollection collection, ActionResult result, long duration, Exception exception) {
        if (collection == null || collection.getStateImages().isEmpty()) return;

        StateImage stateImage = collection.getStateImages().get(0);
        StringBuilder log = new StringBuilder();

        if (config.getConsole().isUseColors()) {
            if (exception != null) {
                log.append(error("✗ FIND ERROR: "));
                log.append(stateImage.getName());
                log.append(" - ");
                log.append(exception.getMessage());
            } else if (result != null && result.isSuccess()) {
                log.append(success("✓ FIND SUCCESS: "));
                log.append(stateImage.getName());
                log.append(" (");
                log.append(result.size());
                log.append(" matches, ");
                log.append(duration);
                log.append("ms)");
            } else {
                log.append(warning("○ FIND FAILED: "));
                log.append(stateImage.getName());
                log.append(" (");
                log.append(duration);
                log.append("ms)");
            }
        } else {
            String status =
                    exception != null
                            ? "ERROR"
                            : (result != null && result.isSuccess() ? "SUCCESS" : "FAILED");
            log.append("FIND ");
            log.append(status);
            log.append(": ");
            log.append(stateImage.getName());
            log.append(" (");
            log.append(duration);
            log.append("ms)");
        }

        System.out.println(log.toString());
    }

    private PatternFindOptions extractOptions(ObjectCollection collection) {
        // Options are typically configured on StateImages, not on ObjectCollection
        // For now, return null since options are not directly accessible here
        // The ImageFindDebugger will use default values when options are null
        return null;
    }
}
