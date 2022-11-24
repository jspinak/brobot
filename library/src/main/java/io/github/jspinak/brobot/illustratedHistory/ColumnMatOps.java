package io.github.jspinak.brobot.illustratedHistory;

import io.github.jspinak.brobot.imageUtils.MatBuilder;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ColumnMatOps {

    public Mat getColumnMat(List<Mat> imagesMats, int matchesPerColumn, int columns, int spacesBetweenEntries) {
        List<Mat> columnMats = new ArrayList<>();
        for (int i=0; i<columns; i++) {
            List<Mat> columnMatsEntries = getColumnEntries(imagesMats, i, matchesPerColumn);
            Mat columnMat = mergeColumnMats(columnMatsEntries, spacesBetweenEntries);
            columnMats.add(columnMat);
        }
        return mergeColumnMats(columnMats, spacesBetweenEntries);
    }

    public Mat mergeColumnMats(List<Mat> columnMats, int spacesBetweenEntries) {
        return new MatBuilder()
                .setName("sidebar")
                .setSpaceBetween(spacesBetweenEntries)
                .addHorizontalSubmats(columnMats)
                .build();
    }

    public List<Mat> getColumnEntries(List<Mat> entries, int column, int matchesPerColumn) {
        List<Mat> sidebarEntries = new ArrayList<>();
        for (int i = 0; i < matchesPerColumn; i++) {
            int matchIndex = column * matchesPerColumn + i;
            if (matchIndex == entries.size()) break;
            sidebarEntries.add(entries.get(matchIndex));
        }
        return sidebarEntries;
    }

    public Mat getColumnMat(List<Mat> entries, int column, int matchesPerColumn) {
        List<Mat> columnEntries = getColumnEntries(entries, column, matchesPerColumn);
        return getColumnMat(columnEntries);
    }

    public Mat getColumnMat(List<Mat> entriesInColumn) {
        Mat sidebar = new MatBuilder()
                .setName("sidebar")
                .setSpaceBetween(4)
                .addVerticalSubmats(entriesInColumn)
                .build();
        return sidebar;
    }
}
