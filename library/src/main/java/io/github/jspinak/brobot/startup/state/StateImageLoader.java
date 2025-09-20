package io.github.jspinak.brobot.startup.state;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.state.StateImage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Handles deferred image loading for State classes after Spring context is fully initialized. This
 * prevents State constructors from trying to load images before the image loading infrastructure is
 * ready.
 *
 * <p>State classes can define a method annotated with @PostStateConstruction that will be called by
 * this loader after all dependencies are ready.
 *
 * @since 1.1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StateImageLoader {

    private final ApplicationContext applicationContext;

    /**
     * Load images for all State beans that have deferred initialization. This should be called in
     * Phase 4 of initialization after all infrastructure is ready.
     *
     * @return true if all state images loaded successfully, false if any failed
     */
    public boolean loadStateImages() {
        log.info("Retrying deferred image loading for State beans...");

        boolean allSuccessful = true;
        int retryCount = 0;
        int successCount = 0;
        int failedCount = 0;

        // Get all beans annotated with @State
        Map<String, Object> stateBeans =
                applicationContext.getBeansWithAnnotation(
                        io.github.jspinak.brobot.annotations.State.class);

        for (Map.Entry<String, Object> entry : stateBeans.entrySet()) {
            String beanName = entry.getKey();
            Object stateBean = entry.getValue();

            try {
                // Check all fields for StateImage objects that might need retry
                Field[] fields = stateBean.getClass().getDeclaredFields();
                for (Field field : fields) {
                    if (StateImage.class.isAssignableFrom(field.getType())) {
                        field.setAccessible(true);
                        StateImage stateImage = (StateImage) field.get(stateBean);

                        if (stateImage != null) {
                            // Check if any patterns need retry
                            List<Pattern> patterns = stateImage.getPatterns();
                            if (patterns != null) {
                                for (Pattern pattern : patterns) {
                                    if (pattern.retryImageLoad()) {
                                        successCount++;
                                        log.debug(
                                                "Successfully loaded deferred image for {} in {}",
                                                field.getName(),
                                                beanName);
                                    } else {
                                        failedCount++;
                                        allSuccessful = false;
                                        log.warn(
                                                "Failed to load deferred image for {} in {}",
                                                field.getName(),
                                                beanName);
                                    }
                                    retryCount++;
                                }
                            }
                        }
                    }
                }

                // Also check for @PostStateConstruction methods (for backward compatibility)
                Method[] methods = stateBean.getClass().getDeclaredMethods();
                for (Method method : methods) {
                    if (method.isAnnotationPresent(PostStateConstruction.class)) {
                        log.debug("Calling {} on state bean {}", method.getName(), beanName);
                        method.setAccessible(true);
                        method.invoke(stateBean);
                        log.debug("Successfully called @PostStateConstruction for {}", beanName);
                    }
                }
            } catch (Exception e) {
                log.error("Error processing state bean {}: {}", beanName, e.getMessage(), e);
                allSuccessful = false;
                failedCount++;
            }
        }

        if (retryCount > 0) {
            log.info(
                    "Retried loading {} deferred images: {} successful, {} failed",
                    retryCount,
                    successCount,
                    failedCount);
        } else {
            log.debug("No deferred images needed retry");
        }

        return allSuccessful;
    }

    /**
     * Check if a specific State bean has completed image loading.
     *
     * @param stateBeanName the name of the State bean
     * @return true if images are loaded, false otherwise
     */
    public boolean isStateImageLoaded(String stateBeanName) {
        try {
            Object stateBean = applicationContext.getBean(stateBeanName);
            // Could add more sophisticated checking here if needed
            return stateBean != null;
        } catch (Exception e) {
            return false;
        }
    }
}
