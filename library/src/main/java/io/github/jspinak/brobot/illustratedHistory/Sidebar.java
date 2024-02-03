package io.github.jspinak.brobot.illustratedHistory;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.illustratedHistory.draw.DrawHistogram;
import io.github.jspinak.brobot.imageUtils.MatBuilder;
import io.github.jspinak.brobot.imageUtils.MatVisualize;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.Size;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_imgproc.resize;

@Component
public class Sidebar {

    private DrawHistogram drawHistogram;
    private MatVisualize matVisualize;
    private ColumnMatOps columnMatOps;

    private int sidebarEntryW = 50, sidebarEntryH = 50;
    private int spacesBetweenEntries = 4;
    private int matchesPerColumn;
    private int columns;

    public Sidebar(DrawHistogram drawHistogram, MatVisualize matVisualize, ColumnMatOps columnMatOps) {
        this.drawHistogram = drawHistogram;
        this.matVisualize = matVisualize;
        this.columnMatOps = columnMatOps;
    }

    public void drawSidebars(Illustrations illustrations, Matches matches, ActionOptions actionOptions, List<Match> matchList) {
        if (illustrations.getScene() == null) return;
        List<Mat> sidebarEntries = getEntriesForSceneSidebar(illustrations, matches, actionOptions, matchList);
        Mat sidebar = getSidebar(illustrations.getScene(), sidebarEntries, matches, matchList);
        illustrations.setSidebar(sidebar);
    }

    public void mergeSceneAndSidebar(Illustrations illustrations) {
        if (illustrations.getMatchesOnScene() == null) return;
        Mat sceneAndSidebar = new MatBuilder()
                .addHorizontalSubmats(illustrations.getMatchesOnScene(), illustrations.getSidebar())
                .setSpaceBetween(spacesBetweenEntries)
                .build();
        illustrations.setSceneWithMatchesAndSidebar(sceneAndSidebar);
    }

    public void initSidebar(Mat scene, int matchesSize) {
        matchesPerColumn = scene.rows() / (sidebarEntryH + spacesBetweenEntries);
        columns = (int) Math.ceil((double) matchesSize / matchesPerColumn);
    }

    private Mat getSidebar(Mat scene, List<Mat> sidebarEntries, Matches matches, List<Match> matchList) {
        initSidebar(scene, matches.size());
        List<Mat> sidebarColumns = new ArrayList<>();
        for (int i = 0; i < columns; i++) {
            List<Mat> columnEntries = columnMatOps.getColumnEntries(sidebarEntries, i, matchesPerColumn);
            Mat columnMat = columnMatOps.getColumnMat(columnEntries);
            sidebarColumns.add(columnMat);
        }
        return columnMatOps.mergeColumnMats(sidebarColumns, spacesBetweenEntries);
    }

    private List<Mat> getEntriesForSceneSidebar(Illustrations illustrations, Matches matches, ActionOptions actionOptions, List<Match> matchList) {
        List<Mat> sidebarEntries = new ArrayList<>();
        if (actionOptions.getFind() == ActionOptions.Find.MOTION || actionOptions.getFind() == ActionOptions.Find.REGIONS_OF_MOTION) {
            matchList.forEach(m -> sidebarEntries.add(getMatchForSidebar(illustrations, m)));
            return sidebarEntries;
        }
        if (actionOptions.getFind() == ActionOptions.Find.HISTOGRAM) {
            matches.getMatchList().forEach(mO -> {
                Mat matchOnScene = getMatchForSidebar(illustrations, mO);
                sidebarEntries.add(getMatchAndHistogram(matchOnScene, mO));
            });
            return sidebarEntries;
        }
        matches.getMatchList().forEach(m -> sidebarEntries.add(getMatchForSidebar(illustrations, m)));
        return sidebarEntries;
    }

    private Mat getMatchForSidebar(Illustrations illustrations, Match match) {
        if (illustrations.getScene().sizeof() < match.w() * match.h()) return illustrations.getScene();
        Rect rect = new Rect(match.x(), match.y(), match.w(), match.h());
        Mat matchFromScene = illustrations.getScene().apply(rect);
        resize(matchFromScene, matchFromScene, new Size(sidebarEntryW, sidebarEntryH));
        return matchFromScene;
    }

    private Mat getMatchAndHistogram(Mat matchFromScene, Match match) {
        Mat histMat = drawHistogram.draw(sidebarEntryW, sidebarEntryH, match.getHistogram());
        return new MatBuilder()
                .setName("matchAndHist")
                .setSpaceBetween(spacesBetweenEntries)
                .addHorizontalSubmats(matchFromScene, histMat)
                .build();
    }
}
