package io.github.jspinak.brobot.annotations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BeanPostProcessor that detects and collects beans annotated with @Transition.
 * 
 * This is necessary because Spring's getBeansWithAnnotation() method doesn't
 * reliably detect beans with meta-annotations (like @Transition which includes @Component).
 * 
 * This processor:
 * 1. Intercepts bean creation during Spring's initialization
 * 2. Checks if the bean's target class has @Transition annotation
 * 3. Stores references to transition beans for later processing
 * 
 * This approach is more reliable than getBeansWithAnnotation() for meta-annotations.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class TransitionAnnotationBeanPostProcessor implements BeanPostProcessor {
    
    private final Map<String, Object> transitionBeans = new ConcurrentHashMap<>();
    
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // Get the actual class (handles proxies)
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);
        
        // Check if the class has @Transition annotation
        Transition transitionAnnotation = AnnotationUtils.findAnnotation(targetClass, Transition.class);
        
        if (transitionAnnotation != null) {
            log.debug("Found @Transition annotated bean: {} ({})", beanName, targetClass.getName());
            log.debug("  - From: {}", (Object) transitionAnnotation.from());
            log.debug("  - To: {}", (Object) transitionAnnotation.to());
            log.debug("  - Method: {}", transitionAnnotation.method());
            log.debug("  - Priority: {}", transitionAnnotation.priority());
            
            transitionBeans.put(beanName, bean);
        }
        
        return bean;
    }
    
    /**
     * Get all beans that have been detected with @Transition annotation.
     * 
     * @return Unmodifiable map of transition bean names to bean instances
     */
    public Map<String, Object> getTransitionBeans() {
        return Collections.unmodifiableMap(transitionBeans);
    }
    
    /**
     * Clear the collected transition beans.
     * Useful for testing or resetting state.
     */
    public void clear() {
        transitionBeans.clear();
    }
    
    /**
     * Get the count of detected transition beans.
     * 
     * @return The number of transition beans detected
     */
    public int getTransitionBeanCount() {
        return transitionBeans.size();
    }
}