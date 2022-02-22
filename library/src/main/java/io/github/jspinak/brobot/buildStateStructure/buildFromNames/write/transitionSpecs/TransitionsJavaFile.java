package io.github.jspinak.brobot.buildStateStructure.buildFromNames.write.transitionSpecs;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Writes Java code for the StateTransitions class, including static imports
 * for all enums corresponding to the target States of Transitions.
 */
@Component
public class TransitionsJavaFile {

    public JavaFile makeFile(String packageName, String stateClassVar, TypeSpec transitionsClass,
                             Map<String, ClassName> enms) {
        JavaFile.Builder javaFile = JavaFile.builder(packageName+"."+stateClassVar, transitionsClass);
        for (Map.Entry<String, ClassName> enm : enms.entrySet()) {
            /* __example__
              import static com.example.demo.stateStructure.class.Class.Name.CLASS;
             */
            javaFile.addStaticImport(enm.getValue(), enm.getKey());
        }
        return javaFile.build();
    }
}
