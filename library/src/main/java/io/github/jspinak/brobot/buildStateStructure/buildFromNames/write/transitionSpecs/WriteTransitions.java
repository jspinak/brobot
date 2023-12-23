package io.github.jspinak.brobot.buildStateStructure.buildFromNames.write.transitionSpecs;

import com.squareup.javapoet.*;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import org.springframework.stereotype.Component;

import javax.lang.model.element.Modifier;
import java.util.HashSet;
import java.util.Set;

/**
 * Writes Java code for the StateTransitions class. This class coordinates the
 * preparation of all parts of the Java code.
 */
@Component
public class WriteTransitions {

    private TransitionDependencyInjection transitionDependencyInjection;
    private TransitionMethods transitionMethods;
    private TransitionsJavaFile transitionsJavaFile;

    public WriteTransitions(TransitionDependencyInjection transitionDependencyInjection,
                            TransitionMethods transitionMethods, TransitionsJavaFile transitionsJavaFile) {
        this.transitionDependencyInjection = transitionDependencyInjection;
        this.transitionMethods = transitionMethods;
        this.transitionsJavaFile = transitionsJavaFile;
    }

    public JavaFile write(String baseClassName, String packageName,
                          ClassName enumClass, String enumName, String stateClassVar) {
        return write(baseClassName, packageName, enumClass, enumName, stateClassVar, new HashSet<>());
    }

    public JavaFile write(String baseClassName, String packageName,
                          ClassName enumClass, String enumName, String stateClassVar,
                          Set<StateImage> images) {
        FieldSpec commonActions = transitionDependencyInjection.getCommonActions();
        ClassName className = ClassName.get(packageName+"."+stateClassVar, baseClassName);
        FieldSpec stateClass = transitionDependencyInjection.getInjectedClass(stateClassVar, className);
        MethodSpec finishTransition = transitionMethods.getFinishTransition(enumName);
        MethodSpec constructor = transitionMethods.getConstructor(
                className, stateClassVar, enumName, finishTransition, packageName, images);
        transitionMethods.getEnumNames().put(enumName, enumClass);

        TypeSpec transitionsClass = TypeSpec.classBuilder(baseClassName+"Transitions")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Component.class)
                .addField(commonActions)
                .addField(stateClass)
                .addMethod(constructor)
                .addMethod(finishTransition)
                .build();

        return transitionsJavaFile.makeFile(packageName, stateClassVar, transitionsClass, transitionMethods.getEnumNames());
    }
}
