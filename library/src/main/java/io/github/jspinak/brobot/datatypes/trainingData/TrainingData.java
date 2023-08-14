package io.github.jspinak.brobot.datatypes.trainingData;

import lombok.Getter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

@Getter
public class TrainingData implements Serializable {

    private short[] vector;
    private String text;
    private transient ArrayList<BufferedImage> screenshots;

    /*
    Screenshots are stored in a list instead of an array of size 2. This allows for more flexibility in creating
    training data. For example, the researcher may want to feed 3 "after" screenshots to the neural net with each
    action.
     */
    public TrainingData(short[] vector, String text, ArrayList<BufferedImage> screenshots) {
        this.vector = vector;
        this.text = text;
        this.screenshots = screenshots;
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
