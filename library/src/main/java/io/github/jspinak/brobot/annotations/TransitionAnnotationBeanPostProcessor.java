package io.github.jspinak.brobot.annotations;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * BeanPostProcessor that detects and collects beans annotated with @TransitionSet.
 *
 * <p>This is necessary because Spring's getBeansWithAnnotation() method doesn't reliably detect
 * beans with meta-annotations (like @TransitionSet which includes @Component).
 *
 * <p>This processor:
 *
 * <ol>
 *   <li>Intercepts bean creation during Spring's initialization
 *   <li>Checks if the bean's target class has @TransitionSet annotation
 *   <li>Stores references to transition set beans for later processing
 * </ol>
 *
 * <p>This approach is more reliable than getBeansWithAnnotation() for meta-annotations.
 *
 * @since 1.2.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class TransitionAnnotationBeanPostProcessor implements BeanPostProcessor {

    private final Map<String, Object> transitionSetBeans = new ConcurrentHashMap<>();

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName)
            throws BeansException {
        // Get the actual class (handles proxies)
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);

        // Check if the class has @TransitionSet annotation
        TransitionSet transitionSetAnnotation =
                AnnotationUtils.findAnnotation(targetClass, TransitionSet.class);

        if (transitionSetAnnotation != null) {
            log.debug(
                    "TransitionSet bean detected: {} ({})", beanName, targetClass.getSimpleName());
            log.debug(
                    "  State: {}, Name: '{}', Description: '{}'",
                    transitionSetAnnotation.state().getSimpleName(),
                    transitionSetAnnotation.name(),
                    transitionSetAnnotation.description());

            // Store the bean for later processing
            transitionSetBeans.put(beanName, bean);
            log.debug("Stored TransitionSet bean '{}' for later processing", beanName);
        }

        return bean;
    }

    /**
     * Get all collected TransitionSet beans.
     *
     * @return unmodifiable map of bean names to TransitionSet beans
     */
    public Map<String, Object> getTransitionSetBeans() {
        return Collections.unmodifiableMap(transitionSetBeans);
    }

    /** Clear all collected beans. Useful for testing. */
    public void clear() {
        transitionSetBeans.clear();
    }
}
