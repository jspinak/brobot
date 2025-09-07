package io.github.jspinak.brobot.annotations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BeanPostProcessor that detects beans annotated with @State during bean creation.
 * This is more reliable than using applicationContext.getBeansWithAnnotation() 
 * which can miss proxied beans or beans with meta-annotations.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class StateAnnotationBeanPostProcessor implements BeanPostProcessor {
    
    private final Map<String, Object> stateBeans = new ConcurrentHashMap<>();
    
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // Get the actual class (not the proxy)
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);
        
        // Check if the class has @State annotation
        State stateAnnotation = AnnotationUtils.findAnnotation(targetClass, State.class);
        
        if (stateAnnotation != null) {
            log.debug("Found @State annotated bean: {} ({})", beanName, targetClass.getName());
            stateBeans.put(beanName, bean);
        }
        
        return bean;
    }
    
    /**
     * Get all beans that have been detected with @State annotation.
     * @return Map of bean name to bean instance
     */
    public Map<String, Object> getStateBeans() {
        return new ConcurrentHashMap<>(stateBeans);
    }
    
    /**
     * Clear the collected state beans (useful for testing).
     */
    public void clear() {
        stateBeans.clear();
    }
}