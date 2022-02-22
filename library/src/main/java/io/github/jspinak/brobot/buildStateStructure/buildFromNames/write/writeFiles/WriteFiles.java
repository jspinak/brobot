package io.github.jspinak.brobot.buildStateStructure.buildFromNames.write.writeFiles;

import com.squareup.javapoet.JavaFile;
import io.github.jspinak.brobot.actions.BrobotSettings;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Creates the folder 'stateStructure'.
 * Creates the State and StateTransitions class files.
 */
@Component
public class WriteFiles {

    public void preparePath() {
        String packageFolders = BrobotSettings.packageName.replace(".","/");
        Path path = Paths.get("src/main/java/"+packageFolders+"/stateStructure");
        try {
            Files.createDirectory(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeToFile(JavaFile stateFile, JavaFile transitionsFile) {
        try {
            Path path = Path.of("src/main/java");
            stateFile.writeTo(path);
            transitionsFile.writeTo(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
