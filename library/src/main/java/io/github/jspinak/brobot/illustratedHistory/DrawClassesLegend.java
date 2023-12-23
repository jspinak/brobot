package io.github.jspinak.brobot.illustratedHistory;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.SetProfileMats;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.imageUtils.MatBuilder;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DrawClassesLegend {
    private SetProfileMats setProfileMats;
    private ColumnMatOps columnMatOps;

    private int spacesBetweenEntries = 4;

    private int entryW = 40, entryH = 40;

    int matchesPerColumn;
    int columns;

    public DrawClassesLegend(SetProfileMats setProfileMats, ColumnMatOps columnMatOps) {
        this.setProfileMats = setProfileMats;
        this.columnMatOps = columnMatOps;
    }

    public void initSidebar(Mat scene, int numberOfImages) {
        matchesPerColumn = scene.rows() / (entryH + spacesBetweenEntries);
        columns = (int) Math.ceil((double) numberOfImages / matchesPerColumn);
    }

    /*
    The classes legend shows the images files comprising each Brobot Image and the Image's kMeans centers.
     */
    public void drawLegend(Illustrations illustrations, List<StateImage> imgs, ActionOptions actionOptions) {
        if (illustrations.getScene() == null) return;
        initSidebar(illustrations.getScene(), imgs.size());
        List<Mat> imgEntries = getAllImageEntries(imgs);
        List<Mat> kmeansEntries = getAllColorEntries(imgs, actionOptions);
        Mat legend = getImagesAndKmeansCentersColumns(imgEntries, kmeansEntries);
        illustrations.setLegend(legend);
    }

    public void mergeClassesAndLegend(Illustrations illustrations) {
        if (illustrations.getMatchesOnClasses() == null) return;
        Mat classesAndLegend = new MatBuilder()
                .addHorizontalSubmats(illustrations.getMatchesOnClasses(), illustrations.getLegend())
                .setSpaceBetween(spacesBetweenEntries)
                .build();
        illustrations.setClassesWithMatchesAndLegend(classesAndLegend);
    }

    /*
    We want a column of images and a corresponding column of kMeans centers, until all Images are represented.
     */
    private Mat getImagesAndKmeansCentersColumns(List<Mat> imgs, List<Mat> kmeansCenters) {
        List<Mat> imgKmeansColumns = new ArrayList<>();
        for (int i=0; i<columns; i++) {
            Mat imgColumn = columnMatOps.getColumnMat(imgs, i, matchesPerColumn);
            Mat kmeansColumn = columnMatOps.getColumnMat(kmeansCenters, i, matchesPerColumn);
            Mat imgAndKmeans = new MatBuilder()
                    .addHorizontalSubmats(imgColumn, kmeansColumn)
                    .setSpaceBetween(spacesBetweenEntries)
                    .build();
            imgKmeansColumns.add(imgAndKmeans);
        }
        return columnMatOps.mergeColumnMats(imgKmeansColumns, spacesBetweenEntries * 2);
    }

    private List<Mat> getAllImageEntries(List<StateImage> imgs) {
        List<Mat> sidebarEntries = new ArrayList<>();
        for (StateImage img : imgs) {
            Mat entry = getImagesEntryForClassesLegend(img);
            sidebarEntries.add(entry);
        }
        return sidebarEntries;
    }

    private Mat getImagesEntryForClassesLegend(StateImage img) {
        return new MatBuilder()
                .setName("classes legend")
                .addHorizontalSubmats(setProfileMats.getImagesMat(img))
                .setSpaceBetween(spacesBetweenEntries)
                .build();
    }

    private List<Mat> getAllColorEntries(List<StateImage> imgs, ActionOptions actionOptions) {
        List<Mat> sidebarEntries = new ArrayList<>();
        for (StateImage img : imgs) {
            Mat entry = getKmeansCenterEntry(img, actionOptions);
            sidebarEntries.add(entry);
        }
        return sidebarEntries;
    }

    private Mat getKmeansCenterEntry(StateImage img, ActionOptions actionOptions) {
        return new MatBuilder()
                .setName("classes legend")
                .addHorizontalSubmats(setProfileMats.getProfilesMat(img, actionOptions))
                .setSpaceBetween(spacesBetweenEntries)
                .build();
    }
}
