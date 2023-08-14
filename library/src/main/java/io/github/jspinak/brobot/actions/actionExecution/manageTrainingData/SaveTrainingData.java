package io.github.jspinak.brobot.actions.actionExecution.manageTrainingData;

import io.github.jspinak.brobot.datatypes.trainingData.ActionVector;
import io.github.jspinak.brobot.datatypes.trainingData.TrainingData;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

@Component
@Getter
public class SaveTrainingData {

    private List<TrainingData> trainingData = new ArrayList<>();

    public void addData(ActionVector actionVector, String actionText, ArrayList<BufferedImage> screenshots) {
        trainingData.add(new TrainingData(actionVector.getVector(), actionText, screenshots));
    }

    public void saveAllDataToFile() {

        // Save the data objects to a file using ObjectOutputStream
        try {
            FileOutputStream fos = new FileOutputStream("trainingdata.dat");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeInt(trainingData.size());
            for (TrainingData td : trainingData) {
                oos.writeObject(td);
            }
            oos.close();
            fos.close();
            System.out.println("Data objects saved to trainingdata.dat");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
