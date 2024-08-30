package io.github.jspinak.brobot.app.services;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import io.github.jspinak.brobot.app.database.entities.TransitionEntity;

import java.lang.reflect.Method;

@Service
public class TransitionExecutor {
    @Autowired
    private ApplicationContext context;
/*
    public boolean executeTransition(TransitionEntity entity, Action action) {
        try {
            Object transitionInstance = context.getBean(Class.forName(entity.getTransitionClass()));
            Method transitionMethod = transitionInstance.getClass().getDeclaredMethod(entity.getTransitionMethod());
            transitionMethod.setAccessible(true);
            return (boolean) transitionMethod.invoke(transitionInstance);
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute transition", e);
        }
    }

 */
}
