package io.github.jspinak.brobot.actions.actionExecution.manageTrainingData;

import io.github.jspinak.brobot.datatypes.trainingData.TrainingData;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class ReadTrainingData {

    private List<TrainingData> trainingData = new ArrayList<>();

    public void getDataFromFile() {
        List<TrainingData> trainingData = new ArrayList<>();

        // Read the data objects from the file using ObjectInputStream
        try {
            FileInputStream fis = new FileInputStream("trainingdata.dat");
            ObjectInputStream ois = new ObjectInputStream(fis);
            // Read the number of objects from the beginning of the stream
            int size = ois.readInt();
            // Loop over the stream and read each object
            for (int i = 0; i < size; i++) {
                trainingData.add((TrainingData) ois.readObject());
                System.out.println(trainingData);
            }
            ois.close();
            fis.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        this.trainingData = trainingData;
    }


}
