package io.github.jspinak.brobot.actions.methods.basicactions.find.contours;

import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import lombok.Getter;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.sikuli.script.Match;

import java.util.*;

import static org.bytedeco.opencv.global.opencv_core.sumElems;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

@Getter
public class Contours {

    private Mat scoreThresholdDist;
    private Mat scores;

    /*
    With a Find.COLOR action, only the target images are classified and appear in this Mat.
     */
    private Mat bgrFromClassification2d;
    private List<Region> searchRegions = new ArrayList<>();
    private int minArea;
    private int maxArea;
    private MatVector opencvContours;
    private List<Rect> unmodifiedContours = new ArrayList<>();
    private List<Rect> contours = new ArrayList<>();
    private Map<Integer, Match> matchMap = new HashMap<>();

    public List<Rect> getContours() {
        contours = new ArrayList<>();
        unmodifiedContours = new ArrayList<>();
        opencvContours = new MatVector();
        for (Region region : searchRegions) {
            region.w = Math.min(region.w, bgrFromClassification2d.cols() - region.x); // prevent out of bounds
            region.h = Math.min(region.h, bgrFromClassification2d.rows() - region.y); // prevent out of bounds
            contours.addAll(getContoursInRegion(region));
        }
        return contours;
    }

    public List<Rect> getContoursInRegion(Region region) {
        List<Rect> rectsInRegion = new ArrayList<>();
        Mat regionalClassMat = new Mat(bgrFromClassification2d, region.getJavaCVRect());
        Mat gray = new Mat(regionalClassMat.size(), regionalClassMat.type());
        if (bgrFromClassification2d.channels() == 3) cvtColor(regionalClassMat, gray, COLOR_BGR2GRAY);
        else gray = regionalClassMat;
        MatVector regionalContours = new MatVector();
        findContours(gray, regionalContours, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
        opencvContours.put(regionalContours);
        for (int i = 0; i < regionalContours.size(); i++) {
            Rect baseRect = boundingRect(regionalContours.get(i));
            Rect rect = new Rect(baseRect.x() + region.getX(), baseRect.y() + region.getY(), baseRect.width(), baseRect.height());
            rectsInRegion.add(rect);
            unmodifiedContours.add(rect);
        }
        return rectsInRegion;
    }

    private boolean minAreaOK(Rect contour, int minArea) {
        if (minArea <= 0) return true;
        return contour.width() * contour.height() >= minArea;
    }

    /*
    It's not enough to exclude contours that are too big. We need to break them up into smaller contours and
    include these smaller contours in the list of contours to be returned.
     */
    private boolean maxAreaOK(Rect contour, int maxArea) {
        if (maxArea < 0) return true;
        if (maxArea == 0) return false;
        return contour.width() * contour.height() <= maxArea;
    }

    private List<Rect> partitionLargeContour(Rect rect, int maxArea) {
        List<Rect> contours = new ArrayList<>();
        if (maxAreaOK(rect, maxArea)) {
            contours.add(rect);
            return contours;
        }
        // the last contours may overlap with the next-to-last contours, but will not exceed the boundaries of the contour
        int startX = rect.x();
        int startY = rect.y();
        int endX = rect.x() + rect.width();
        int endY = rect.y() + rect.height();
        int x = startX;
        int y = startY;
        while (x < endX) {
            int width = Math.min(maxArea, endX - x);
            int height = Math.min(maxArea, endY - y);
            Rect newRect = new Rect(x, y, width, height);
            contours.add(newRect);
            x += width;
            if (x >= endX) {
                x = startX;
                y += height;
            }
            if (y >= endY) break;
        }
        return contours;
    }

    public List<Match> getMatches() {
        int m = 0;
        for (int i=0; i<contours.size(); i++) {
            Rect contour = contours.get(i);
            if (minAreaOK(contour, minArea)) {
                List<Rect> partitionedContours = partitionLargeContour(contour, maxArea);
                for (Rect partitionedContour : partitionedContours) {
                    double score = getContourScore(partitionedContour);
                    if (score > 0) { // score is 0 if the contour is not a match
                        Match match = new Match(new Region(partitionedContour), score);
                        matchMap.put(m, match);
                        m++;
                    }
                }
            }
        }
        return matchMap.values().stream().sorted(Comparator.comparing(Match::getScore).reversed()).toList();
    }

    /*
    Scores are determined by the average distance to the threshold values,
    for all pixels in the contour. The size is most important for CLASSIFY and MOTION actions, but
    for FIND actions, the score is most important.

    The best results are obtained when the minScore is set to 0. This creates a match for all areas of the Mat
    and takes a lot longer to process, but looks at the score of every pixel in the Mat.
     */
    private double getContourScore(Rect rect) {
        if (scoreThresholdDist == null) return rect.area(); // with the Motion action, there is no scoreThresholdDist
        Mat boundingMat = new Mat(scoreThresholdDist, rect);
        double totalScore = 0;
        for (int i=0; i<3; i++) {
            Scalar score = sumElems(boundingMat);
            double sum = score.get(i);
            long totalCellsInContour = boundingMat.total();
            double average = sum / totalCellsInContour;
            if (i==0) average *= 2; // hue weight relative to saturation and value
            if (average == 0) return 0; // if any channel is 0, the contour is not a match
            totalScore += average;
        }
        return totalScore;
    }

    public Mat getMatchAsMatInScene(int index, Mat scene) {
        Match m = matchMap.get(index);
        if (m == null) return null;
        return new Mat(scene, new Region(m).getJavaCVRect());
    }

    public Match getContourAsMatch(Mat contour) {
        Rect rect = boundingRect(contour);
        double score = getContourScore(rect);
        return new Match(new Region(rect), score);
    }

    public static class Builder {
        private Mat scoreThresholdDist;
        private Mat scores;
        private Mat bgrFromClassification2d;
        private List<Region> searchRegions = new ArrayList<>();
        private int minArea;
        private int maxArea;
        private MatVector opencvContours;
        private final List<Rect> unmodifiedContours = new ArrayList<>();
        private final List<Rect> contours = new ArrayList<>();
        private final Map<Integer, Match> matchMap = new HashMap<>();

        public Builder setScoreThresholdDist(Mat scoreThresholdDist) {
            this.scoreThresholdDist = scoreThresholdDist;
            return this;
        }

        public Builder setScores(Mat scores) {
            this.scores = scores;
            return this;
        }

        public Builder setBgrFromClassification2d(Mat bgrFromClassification2d) {
            this.bgrFromClassification2d = bgrFromClassification2d.clone();
            return this;
        }

        public Builder setSearchRegions(List<Region> searchRegions) {
            this.searchRegions = searchRegions;
            return this;
        }

        public Builder setMinArea(int minArea) {
            this.minArea = minArea;
            return this;
        }

        public Builder setMaxArea(int maxArea) {
            this.maxArea = maxArea;
            return this;
        }

        public Contours build() {
            Contours contours = new Contours();
            contours.scoreThresholdDist = scoreThresholdDist;
            contours.scores = scores;
            contours.bgrFromClassification2d = bgrFromClassification2d;
            contours.searchRegions = searchRegions;
            contours.minArea = minArea;
            contours.maxArea = maxArea;
            contours.opencvContours = opencvContours;
            contours.unmodifiedContours = unmodifiedContours;
            contours.contours = this.contours;
            contours.matchMap = matchMap;
            contours.getContours();
            return contours;
        }
    }
}
