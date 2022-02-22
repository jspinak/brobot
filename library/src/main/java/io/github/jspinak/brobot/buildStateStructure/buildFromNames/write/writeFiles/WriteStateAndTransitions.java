package io.github.jspinak.brobot.buildStateStructure.buildFromNames.write.writeFiles;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.buildStateStructure.buildFromNames.write.stateSpecs.WriteStateClass;
import io.github.jspinak.brobot.buildStateStructure.buildFromNames.write.transitionSpecs.WriteTransitions;
import io.github.jspinak.brobot.buildStateStructure.buildFromNames.babyStates.BabyState;
import org.springframework.stereotype.Component;

/**
 * Prepares all names (class, package, enum, variable, etc) and calls the methods that
 * write the Java code for both State and StateTransitions classes.
 */
@Component
public class WriteStateAndTransitions {

    private final WriteStateClass writeStateClass;
    private final WriteTransitions writeTransitions;
    private final WriteFiles writeFiles;

    // State values
    private String stateClassName;
    private String stateVariableName;
    private String statePackageName;

    // Enum values
    private String enumName;
    private ClassName enumClass;

    public WriteStateAndTransitions(WriteStateClass writeStateClass, WriteTransitions writeTransitions,
                                    WriteFiles writeFiles) {
        this.writeStateClass = writeStateClass;
        this.writeTransitions = writeTransitions;
        this.writeFiles = writeFiles;
    }

    public void write(BabyState state) {
        String name = state.getName();
        setValues(name);
        JavaFile stateFile = writeStateClass.write(state.getImages(), enumName,
                stateClassName, statePackageName, enumClass, stateVariableName);
        JavaFile transitionsFile = writeTransitions.write(
                stateClassName, statePackageName, enumClass, enumName, stateVariableName,
                state.getImages());
        writeFiles.writeToFile(stateFile, transitionsFile);
    }

    private void setValues(String name) {
        //state
        stateClassName = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        stateVariableName = Character.toLowerCase(name.charAt(0)) + name.substring(1);
        statePackageName = BrobotSettings.packageName + ".stateStructure";
        //enum
        enumName = name.toUpperCase();
        enumClass = ClassName.get(statePackageName+"."+
                stateVariableName+"."+stateClassName, "Name");
    }
}
