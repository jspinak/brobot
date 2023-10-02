package io.github.jspinak.brobot.stringUtils;

import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

@Component
public class Base64Converter {

    public String convert(String filePath) {
        try {
            // Load the file as bytes
            Path file = Path.of(filePath);
            byte[] fileData = Files.readAllBytes(file);

            // Encode the file data to Base64
            return Base64.getEncoder().encodeToString(fileData);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
