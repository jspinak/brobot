package io.github.jspinak.brobot.actions.methods.basicactions.find.contours;

import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import lombok.Getter;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.Scalar;

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
    private int minArea; // the smallest area allowed for a contour
    private int maxArea; // the largest area allowed for a contour. The default of -1 allows for all sizes.
    private MatVector opencvContours; // the original contours, saved as Mat masks of the specified regions. Rect coordinates are region specific.
    private List<Rect> screenAdjustedContours = new ArrayList<>(); // the original contours with screen coordinates (instead of region coordinates).
    private List<Rect> contours = new ArrayList<>(); // contours contained in other contours are removed, too small or too large contours are removed or modified, etc.
    private Map<Integer, Match> matchMap = new HashMap<>(); // score and match. score is the area when not comparing pixels.

    public void setContours() {
        screenAdjustedContours = new ArrayList<>();
        opencvContours = new MatVector();
        for (Region region : searchRegions) {
            region.setW(Math.min(region.w(), bgrFromClassification2d.cols() - region.x())); // prevent out of bounds
            region.setH(Math.min(region.h(), bgrFromClassification2d.rows() - region.y())); // prevent out of bounds
            MatVector regionalContours = getRegionalContourMats(region);
            opencvContours.put(regionalContours);
            screenAdjustedContours.addAll(getScreenCoordinateRects(region, opencvContours));
        }
        contours = new ArrayList<>();
        screenAdjustedContours.forEach(rect -> contours.add(new Rect(rect)));
        contours = getContoursNotContained(contours);
        matchMap = getMatchMap(contours, minArea, maxArea);
        //System.out.println("Contours: setContours. sizes of lists = " + opencvContours.size() + " " + screenAdjustedContours.size() + " " + contours.size());
    }

    private List<Rect> getContoursNotContained(List<Rect> contours) {
        List<Rect> notContainedRects = new ArrayList<>();
        for (Rect rect : contours) {
            if (isUniqueRect(rect)) notContainedRects.add(rect);
        }
        return notContainedRects;
    }

    private boolean isUniqueRect(Rect rect) {
        for (Rect contour : contours) {
            if (rect != contour && new Region(contour).contains(rect)) return false;
        }
        return true;
    }

    private MatVector getRegionalContourMats(Region region) {
        Mat regionalClassMat = new Mat(bgrFromClassification2d, region.getJavaCVRect());
        Mat gray = new Mat(regionalClassMat.size(), regionalClassMat.type());
        if (bgrFromClassification2d.channels() == 3) cvtColor(regionalClassMat, gray, COLOR_BGR2GRAY);
        else gray = regionalClassMat;
        MatVector regionalContours = new MatVector();
        findContours(gray, regionalContours, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
        return regionalContours;
    }

    public List<Rect> getScreenCoordinateRects(Region region, MatVector regionalContours) {
        List<Rect> rectsWithScreenCoordinates = new ArrayList<>();
        for (int i = 0; i < regionalContours.size(); i++) {
            Rect baseRect = boundingRect(regionalContours.get(i));
            Rect rect = new Rect(baseRect.x() + region.x(), baseRect.y() + region.y(), baseRect.width(), baseRect.height());
            rectsWithScreenCoordinates.add(rect);
        }
        return rectsWithScreenCoordinates;
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
        return contour.width() * contour.height() <= maxArea;
    }

    private List<Rect> partitionLargeContour(Rect rect, int maxArea) {
        List<Rect> contours = new ArrayList<>();
        if (maxArea == 0) return contours;
        if (maxAreaOK(rect, maxArea)) { // if ok, just return the Rect
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

    /**
     * Sorts by largest score. Score can include pixel comparisons, or look at size only. The largest score, or
     * largest size Match, is returned first.
     * @return a list of Match objects.
     */
    public List<Match> getMatchList() {
        return matchMap.values().stream().sorted(Comparator.comparing(Match::getScore).reversed()).toList();
    }

    public Map<Integer, Match> getMatchMap(List<Rect> contours, int minArea, int maxArea) {
        Map<Integer, Match> matchMap = new HashMap<>();
        if (maxArea == 0) return matchMap;
        int m = 0;
        for (Rect contour : contours) {
            if (minAreaOK(contour, minArea)) {
                List<Rect> partitionedContours = partitionLargeContour(contour, maxArea);
                for (Rect partitionedContour : partitionedContours) {
                    double score = getContourScore(partitionedContour);
                    if (score > 0) { // score is 0 if the contour is not a match
                        Match match = new Match.Builder()
                                .setRegion(new Region(partitionedContour))
                                .build();
                        matchMap.put(m, match);
                        m++;
                    }
                }
            }
        }
        return matchMap;
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
        return new Mat(scene, m.getRegion().getJavaCVRect());
    }

    public Match getContourAsMatch(Mat contour) {
        Rect rect = boundingRect(contour);
        double score = getContourScore(rect);
        return new Match.Builder()
                .setRegion(rect)
                .setSimScore(score)
                .build();
    }

    public static class Builder {
        private Mat scoreThresholdDist;
        private Mat scores;
        private Mat bgrFromClassification2d;
        private List<Region> searchRegions = new ArrayList<>();
        private int minArea;
        private int maxArea = -1;
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

        public Builder setSearchRegions(Region... regions) {
            this.searchRegions.addAll(List.of(regions));
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
            contours.screenAdjustedContours = unmodifiedContours;
            contours.contours = this.contours;
            contours.matchMap = matchMap;
            contours.setContours();
            return contours;
        }
    }
}
