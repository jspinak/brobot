package io.github.jspinak.brobot.tools.ml.dataset.model;

import lombok.Getter;

import javax.imageio.ImageIO;

import io.github.jspinak.brobot.tools.ml.dataset.io.TrainingExampleReader;
import io.github.jspinak.brobot.tools.ml.dataset.io.TrainingExampleWriter;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Serializable container for machine learning training data.
 * <p>
 * TrainingExample encapsulates all information needed to train a neural network on GUI automation tasks.
 * Each instance represents a single training example consisting of an action vector, descriptive text,
 * and associated screenshots showing the state before and after the action.
 * <p>
 * The class implements custom serialization to efficiently handle {@link BufferedImage} objects,
 * which are marked as transient and manually serialized as PNG byte arrays.
 * <p>
 * <strong>Screenshot flexibility:</strong><br>
 * Screenshots are stored in a list rather than a fixed-size array to support various training
 * scenarios. For example, researchers might want to include multiple "after" screenshots to
 * capture different stages of an action's effect.
 *
 * @see ActionVector
 * @see TrainingExampleWriter
 * @see TrainingExampleReader
 */
@Getter
public class TrainingExample implements Serializable {

    private short[] vector;
    private String text;
    private transient ArrayList<BufferedImage> screenshots;

    /*
    Screenshots are stored in a list instead of an array of size 2. This allows for more flexibility in creating
    training data. For example, the researcher may want to feed 3 "after" screenshots to the neural net with each
    action.
     */
    public TrainingExample(short[] vector, String text, ArrayList<BufferedImage> screenshots) {
        this.vector = vector;
        this.text = text;
        this.screenshots = screenshots;
    }

    /**
     * Builder for creating TrainingExample instances.
     * Provides a fluent interface for constructing training examples with validation.
     */
    public static class Builder {
        private short[] vector;
        private String text;
        private ArrayList<BufferedImage> screenshots = new ArrayList<>();

        /**
         * Sets the action vector for this training example.
         * 
         * @param vector The action vector (must not be null)
         * @return This builder instance for method chaining
         */
        public Builder withVector(short[] vector) {
            if (vector == null) {
                throw new IllegalArgumentException("Vector cannot be null");
            }
            this.vector = Arrays.copyOf(vector, vector.length);
            return this;
        }

        /**
         * Sets the action vector from an ActionVector instance.
         * 
         * @param actionVector The ActionVector to extract data from
         * @return This builder instance for method chaining
         */
        public Builder withActionVector(ActionVector actionVector) {
            if (actionVector == null || actionVector.getVector() == null) {
                throw new IllegalArgumentException("ActionVector cannot be null");
            }
            return withVector(actionVector.getVector());
        }

        /**
         * Sets the descriptive text for this training example.
         * 
         * @param text The action description (must not be null or empty)
         * @return This builder instance for method chaining
         */
        public Builder withText(String text) {
            if (text == null || text.trim().isEmpty()) {
                throw new IllegalArgumentException("Text cannot be null or empty");
            }
            this.text = text;
            return this;
        }

        /**
         * Adds a single screenshot to this training example.
         * 
         * @param screenshot The screenshot to add (must not be null)
         * @return This builder instance for method chaining
         */
        public Builder addScreenshot(BufferedImage screenshot) {
            if (screenshot == null) {
                throw new IllegalArgumentException("Screenshot cannot be null");
            }
            this.screenshots.add(screenshot);
            return this;
        }

        /**
         * Sets all screenshots for this training example.
         * 
         * @param screenshots List of screenshots (must not be null or empty)
         * @return This builder instance for method chaining
         */
        public Builder withScreenshots(ArrayList<BufferedImage> screenshots) {
            if (screenshots == null || screenshots.isEmpty()) {
                throw new IllegalArgumentException("Screenshots list cannot be null or empty");
            }
            this.screenshots = new ArrayList<>(screenshots);
            return this;
        }

        /**
         * Builds the TrainingExample instance.
         * Validates that all required fields have been set.
         * 
         * @return A new TrainingExample instance
         * @throws IllegalStateException if required fields are missing
         */
        public TrainingExample build() {
            if (vector == null) {
                throw new IllegalStateException("Vector must be set before building");
            }
            if (text == null) {
                throw new IllegalStateException("Text must be set before building");
            }
            if (screenshots.isEmpty()) {
                throw new IllegalStateException("At least one screenshot must be added before building");
            }
            return new TrainingExample(vector, text, screenshots);
        }
    }

    // Override the toString method to display the data
    public String toString() {
        return "Vector: " + Arrays.toString(vector) + "\nText: " + text + "\nScreenshots: " + screenshots.size();
    }

    // Implement a custom writeObject method to handle the serialization of the screenshots
    private void writeObject(ObjectOutputStream oos) throws IOException {
        // Write the regular data for this class
        oos.defaultWriteObject();
        // Write the size of the screenshots list
        oos.writeInt(screenshots.size());
        // Write each screenshot as a byte array using ImageIO.write
        for (BufferedImage screenshot : screenshots) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(screenshot, "png", baos);
            baos.flush();
            byte[] imageBytes = baos.toByteArray();
            baos.close();
            // Write the length of the byte array
            oos.writeInt(imageBytes.length);
            // Write the byte array
            oos.write(imageBytes);
        }
    }

    // Implement a custom readObject method to handle the deserialization of the screenshots
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        // Read the regular data for this class
        ois.defaultReadObject();
        // Read the size of the screenshots list
        int size = ois.readInt();
        // Initialize the screenshots list
        screenshots = new ArrayList<>(size);
        // Read each screenshot as a byte array and convert it to a BufferedImage using ImageIO.read
        for (int i = 0; i < size; i++) {
            // Read the length of the byte array
            int length = ois.readInt();
            // Read the byte array
            byte[] imageBytes = new byte[length];
            ois.readFully(imageBytes);
            // Convert the byte array to a BufferedImage
            ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
            BufferedImage screenshot = ImageIO.read(bais);
            bais.close();
            // Add the screenshot to the list
            screenshots.add(screenshot);
        }
    }

}