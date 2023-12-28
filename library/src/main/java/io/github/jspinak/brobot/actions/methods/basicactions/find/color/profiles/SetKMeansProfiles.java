package io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.imageUtils.MatOps3d;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.TermCriteria;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.bytedeco.opencv.global.opencv_core.CMP_EQ;
import static org.bytedeco.opencv.global.opencv_core.countNonZero;

@Component
public class SetKMeansProfiles {

    private SetColorCluster setColorCluster;
    private MatOps3d matOps3d;
    private SetAllProfiles setAllProfiles;

    public SetKMeansProfiles(SetColorCluster setColorCluster, MatOps3d matOps3d, SetAllProfiles setAllProfiles) {
        this.setColorCluster = setColorCluster;
        this.matOps3d = matOps3d;
        this.setAllProfiles = setAllProfiles;
    }

    /**
     * Sets the kmeans profiles for the given image, for means from 1 to the max means as specified in the settings.
     * @param img the image to set the profiles for
     */
    public void setProfiles(StateImage img) {
        KmeansProfilesAllSchemas kmeansProfilesAllSchemas = new KmeansProfilesAllSchemas();
        for (int i=1; i<=BrobotSettings.maxKMeansToStoreInProfile; i++) {
            addNewProfiles(kmeansProfilesAllSchemas, img, i);
        }
        img.setKmeansProfilesAllSchemas(kmeansProfilesAllSchemas);
    }

    public void addNewProfiles(KmeansProfilesAllSchemas kmeansProfiles, StateImage img, int means) {
        for (int i=1; i<=BrobotSettings.maxKMeansToStoreInProfile; i++) {
            KmeansProfile kmeansProfilesForBGR = getProfile(
                    img.getOneColumnBGRMat(), means, ColorCluster.ColorSchemaName.BGR);
            kmeansProfiles.addKmeansProfile(ColorCluster.ColorSchemaName.BGR, kmeansProfilesForBGR);
            KmeansProfile kmeansProfilesForHSV = getProfile(
                    img.getOneColumnHSVMat(), means, ColorCluster.ColorSchemaName.HSV);
            kmeansProfiles.addKmeansProfile(ColorCluster.ColorSchemaName.HSV, kmeansProfilesForHSV);
        }
    }

    /**
     * Produces a KmeansProfile given a one-column Mat, the number of means, and the color schema.
     * @param oneColumnMat the images to set the profile for, as a one-column Mat
     * @param kmeans the number of means to use
     * @param schema the color schema to use
     * @return the kmeans profile
     */
    public KmeansProfile getProfile(Mat oneColumnMat, int kmeans, ColorCluster.ColorSchemaName schema) {
        Mat labels = new Mat();
        Mat centers = new Mat();
        double[] compactness = matOps3d.kMeans(oneColumnMat, kmeans, labels, new TermCriteria(), 15, centers);
        List<KmeansCluster> clusters = getKmeansClusters(oneColumnMat, labels, centers, kmeans, schema);
        return new KmeansProfile(schema, kmeans, labels, centers, compactness, clusters);
    }

    private List<KmeansCluster> getKmeansClusters(Mat oneColumnMat, Mat labels, Mat centers,
                                                  int kmeans, ColorCluster.ColorSchemaName schema) {
        List<KmeansCluster> clusters = new ArrayList<>();
        for (int k=0; k<kmeans; k++) { // for each kmeans cluster
            Mat center = centers.row(k);
            // get the masks for this cluster for each channel
            Mat masks = matOps3d.cOmpare(labels, new double[]{k, k, k}, CMP_EQ);
            // get the color profiles for this cluster (one for each channel)
            ColorSchema colorSchema = setColorCluster.getColorSchema(oneColumnMat, masks, schema);
            double[] percentOfPointsInChannel = new double[3];
            for (int c=0; c<3; c++) {
                Mat mask = matOps3d.sPlit(masks).get(c);
                percentOfPointsInChannel[c] = countNonZero(mask) / (double)(labels.total());
            }
            clusters.add(new KmeansCluster(colorSchema, center, matOps3d.sPlit(masks), percentOfPointsInChannel));
        }
        return clusters;
    }

    public void addKMeansIfNeeded(Set<StateImage> allImages, int kMeans) {
        allImages.forEach(img -> {
            KmeansProfilesAllSchemas profiles = img.getKmeansProfilesAllSchemas();
            if (profiles == null) profiles = new KmeansProfilesAllSchemas();
            if (img.getOneColumnBGRMat() == null) setAllProfiles.setMatsAndColorProfiles(img); //initProfileMats.setOneColumnMats(img);
            if (!profiles.containsAll(kMeans)) {
                addNewProfiles(profiles, img, kMeans);
            }
            img.setKmeansProfilesAllSchemas(profiles);
        });
    }
}
