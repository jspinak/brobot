package io.github.jspinak.brobot.illustratedHistory;

import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.SceneAnalysis;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorCluster;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorInfo;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import org.bytedeco.opencv.opencv_core.*;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.bytedeco.opencv.global.opencv_imgproc.FONT_HERSHEY_SIMPLEX;
import static org.bytedeco.opencv.global.opencv_imgproc.putText;

@Component
public class ClassificationLegend {

    private int sidebarEntryW = 50, sidebarEntryH = 50;
    int labelsPerColumn;

    private Mat initSidebar(Mat screen, int matchesSize) {
        labelsPerColumn = screen.rows() / sidebarEntryH;
        int sidebarW = (matchesSize / labelsPerColumn + 1) * sidebarEntryW;
        return new Mat(screen.rows(), sidebarW, screen.type(), new Scalar(255, 255, 255, 255));
    }

    public Mat draw(Mat screen, SceneAnalysis sceneAnalysis) {
        List<StateImage> imgs = sceneAnalysis.getStateImageObjects();
        Mat sidebar = initSidebar(screen, imgs.size());
        int i=0;
        int x,y;
        for (StateImage img : imgs) {
            x = (i / labelsPerColumn) * sidebarEntryW + 1;
            y = (i % labelsPerColumn) * sidebarEntryH + 1;
            int labelWidth = sidebarEntryW - 2;
            int labelHeight = sidebarEntryH - 2;
            Size sidebarEntrySize = new Size(labelWidth, labelHeight);
            Rect sidebarEntry = new Rect(x, y, labelWidth, labelHeight);
            Mat targetInSidebar = sidebar.apply(sidebarEntry);
            Mat entryBGR = img.getColorCluster().getMat(
                    ColorCluster.ColorSchemaName.BGR, ColorInfo.ColorStat.MEAN, sidebarEntrySize);
            entryBGR.copyTo(targetInSidebar);
            i++;
        }
        return sidebar;
    }

    private void addLabel(StateImage img, Mat mat) {
        String text = img.getName();
        Point position = new Point(170, 280);
        Scalar color = new Scalar(255);
        int font = FONT_HERSHEY_SIMPLEX;
        int scale = 1;
        int thickness = 3;
        putText(mat, "text", position, font, scale, color);
    }
}
