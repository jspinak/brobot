package io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles;

import lombok.Getter;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;

/**
 * KmeansCluster represents a cluster of points in a k-means analysis corresponding to one of the centers.
 * It holds info for all 3 channels.
 * If it's a BGR cluster, it will have a BGR ColorSchema, otherwise it will be a HSV ColorSchema. There is
 * no need to have both schemas for one cluster, since the cluster was created from a specific color schema.
 */
@Getter
public class KmeansCluster {

    private ColorSchema colorSchema; // BGR or HSV
    private Mat center3d;
    private MatVector masks; // gives us the points that apply to this cluster
    private double[] percentOfPointsInChannel;

    public KmeansCluster(ColorSchema colorSchema, Mat center3d, MatVector masks, double[] percentOfPointsInChannel) {
        this.colorSchema = colorSchema;
        this.center3d = center3d;
        this.masks = masks;
        this.percentOfPointsInChannel = percentOfPointsInChannel;
    }

    public double getPercentOfPointsOverall() {
        return (percentOfPointsInChannel[0] + percentOfPointsInChannel[1] + percentOfPointsInChannel[2]) / 3.0;
    }

    public void print() {
        colorSchema.print();
        System.out.println("KmeansCluster: " + " " + center3d.size().width() + " " + center3d.size().height() + " " +
                //center3d.createIndexer().getDouble(0,0,0) + " " +
                //center3d.createIndexer().getDouble(0,1,0) +
                //center3d.createIndexer().getDouble(0,2,0) +
                percentOfPointsInChannel[0] + " " + percentOfPointsInChannel[1] + " " + percentOfPointsInChannel[2]);
    }
}
