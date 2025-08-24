package io.github.jspinak.brobot.tools.ml.dataset.io;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.tools.ml.dataset.model.TrainingExample;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads serialized training data from persistent storage.
 * <p>
 * This component handles the deserialization of {@link TrainingExample} objects from
 * a binary file format. It reads the custom serialization format that includes
 * the action vectors, descriptive text, and PNG-encoded screenshots.
 * <p>
 * The data is read from the configured file in the current working directory
 * (default: "trainingdata.dat"). The file format expects an integer count followed
 * by that many serialized TrainingData objects.
 *
 * @see TrainingExample
 * @see TrainingExampleWriter
 */
@Component
public class TrainingExampleReader {

    private static final String DEFAULT_FILENAME = "trainingdata.dat";
    
    private final String filename;
    private List<TrainingExample> trainingData = new ArrayList<>();

    /**
     * Creates a TrainingExampleReader with the default filename.
     */
    public TrainingExampleReader() {
        this(DEFAULT_FILENAME);
    }

    /**
     * Creates a TrainingExampleReader with a custom filename.
     * 
     * @param filename The filename to use for reading training data
     */
    public TrainingExampleReader(String filename) {
        this.filename = filename;
    }

    /**
     * Loads training data from the configured file.
     * <p>
     * This method reads the serialized training data from disk and populates the
     * internal list. The file format consists of an integer indicating the number
     * of TrainingData objects, followed by the serialized objects themselves.
     * <p>
     * Each TrainingData object is printed to console as it's read for debugging
     * purposes. If the file doesn't exist or an error occurs during reading,
     * the exception is printed to stderr and the internal list remains unchanged.
     * <p>
     * Side effects:
     * <ul>
     * <li>Updates the internal trainingData list</li>
     * <li>Prints each loaded TrainingData object to stdout</li>
     * <li>Prints stack trace to stderr if an error occurs</li>
     * </ul>
     */
    public void getDataFromFile() {
        List<TrainingExample> loadedData = new ArrayList<>();

        // Read the data objects from the file using ObjectInputStream
        try (FileInputStream fis = new FileInputStream(filename);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            
            // Read the number of objects from the beginning of the stream
            int size = ois.readInt();
            // Loop over the stream and read each object
            for (int i = 0; i < size; i++) {
                TrainingExample example = (TrainingExample) ois.readObject();
                loadedData.add(example);
                System.out.println("Loaded training example " + (i + 1) + " of " + size);
            }
            System.out.println("Successfully loaded " + size + " training examples from " + filename);
        } catch (IOException e) {
            System.err.println("Error reading training data from " + filename + ": " + e.getMessage());
            throw new RuntimeException("Failed to read training data", e);
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found while deserializing from " + filename + ": " + e.getMessage());
            throw new RuntimeException("Failed to deserialize training data", e);
        }

        this.trainingData = loadedData;
    }

    /**
     * Returns the list of loaded training examples.
     * 
     * @return List of training examples loaded from file
     */
    public List<TrainingExample> getTrainingData() {
        return new ArrayList<>(trainingData);
    }


}
