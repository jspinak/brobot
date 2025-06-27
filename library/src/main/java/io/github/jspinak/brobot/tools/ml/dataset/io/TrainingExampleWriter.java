package io.github.jspinak.brobot.tools.ml.dataset.io;

import lombok.Getter;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.tools.ml.dataset.DatasetManager;
import io.github.jspinak.brobot.tools.ml.dataset.model.ActionVector;
import io.github.jspinak.brobot.tools.ml.dataset.model.TrainingExample;

import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Accumulates and persists training data to disk.
 * <p>
 * This component maintains an in-memory collection of {@link TrainingExample} objects
 * and provides functionality to serialize them to a binary file. The data is saved
 * in a custom format that handles the serialization of BufferedImage objects as
 * PNG-encoded byte arrays.
 * <p>
 * Training data is accumulated through the {@link #addData} method and can be
 * persisted all at once using {@link #saveAllDataToFile}. The output file
 * is created in the current working directory with the configured filename
 * (default: "trainingdata.dat").
 * <p>
 * The file format consists of:
 * <ol>
 * <li>An integer indicating the number of TrainingExample objects</li>
 * <li>The serialized TrainingExample objects in sequence</li>
 * </ol>
 *
 * @see TrainingExample
 * @see TrainingExampleReader
 * @see DatasetManager
 */
@Component
@Getter
public class TrainingExampleWriter {

    private static final String DEFAULT_FILENAME = "trainingdata.dat";
    
    private final String filename;
    private List<TrainingExample> trainingData = new ArrayList<>();

    /**
     * Creates a TrainingExampleWriter with the default filename.
     */
    public TrainingExampleWriter() {
        this(DEFAULT_FILENAME);
    }

    /**
     * Creates a TrainingExampleWriter with a custom filename.
     * 
     * @param filename The filename to use for saving training data
     */
    public TrainingExampleWriter(String filename) {
        this.filename = filename;
    }

    /**
     * Adds a new training example to the in-memory collection.
     * <p>
     * This method creates a new {@link TrainingExample} instance from the provided
     * components and adds it to the internal list. The data is not immediately
     * persisted to disk; call {@link #saveAllDataToFile} to write all accumulated
     * data.
     *
     * @param actionVector The numerical vector representation of the action
     * @param actionText A human-readable description of the action
     * @param screenshots List of screenshots (typically before and after the action)
     */
    public void addData(ActionVector actionVector, String actionText, ArrayList<BufferedImage> screenshots) {
        TrainingExample example = new TrainingExample.Builder()
                .withActionVector(actionVector)
                .withText(actionText)
                .withScreenshots(screenshots)
                .build();
        trainingData.add(example);
    }

    /**
     * Serializes all accumulated training data to disk.
     * <p>
     * This method writes the entire collection of training data to the configured
     * file in the current working directory. The file is created or overwritten
     * if it already exists.
     * <p>
     * The serialization format includes:
     * <ul>
     * <li>The count of TrainingExample objects as an integer</li>
     * <li>Each TrainingExample object serialized in sequence</li>
     * </ul>
     * <p>
     * Side effects:
     * <ul>
     * <li>Creates or overwrites the configured file in the current directory</li>
     * <li>Prints success message to stdout</li>
     * <li>Prints stack trace to stderr if an error occurs</li>
     * </ul>
     */
    public void saveAllDataToFile() {
        if (trainingData.isEmpty()) {
            System.out.println("No training data to save.");
            return;
        }

        // Save the data objects to a file using ObjectOutputStream
        try (FileOutputStream fos = new FileOutputStream(filename);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            
            oos.writeInt(trainingData.size());
            for (TrainingExample td : trainingData) {
                oos.writeObject(td);
            }
            System.out.println("Data objects saved to " + filename);
        } catch (IOException e) {
            System.err.println("Error saving training data to " + filename + ": " + e.getMessage());
            throw new RuntimeException("Failed to save training data", e);
        }
    }

}
