package io.github.jspinak.brobot.illustratedHistory;

import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.sikuli.script.Match;
import org.springframework.stereotype.Component;

import static org.opencv.imgproc.Imgproc.resize;

@Component
public class Sidebar {

    private int sidebarEntryW = 50, sidebarEntryH = 50;
    int matchesPerColumn;

    private Mat initSidebar(Mat screen, int matchesSize) {
        matchesPerColumn = screen.rows() / sidebarEntryH;
        int sidebarW = (matchesSize / matchesPerColumn + 1) * sidebarEntryW;
        return new Mat(screen.rows(), sidebarW, screen.type(), new Scalar(255, 255, 255));
    }

    public Mat draw(Mat screen, Matches matches) {
        Mat sidebar = initSidebar(screen, matches.size());
        int i=0;
        int x,y;
        for (Match match : matches.getMatches()) {
            x = (i / matchesPerColumn) * sidebarEntryW + 1;
            y = (i % matchesPerColumn) * sidebarEntryH + 1;
            Rect onScreen = new Rect(match.x, match.y, match.w, match.h);
            Mat matchMat = screen.submat(onScreen);
            resize(matchMat, matchMat, new Size(sidebarEntryW-2, sidebarEntryH-2));
            Mat targetInSidebar = sidebar.submat(new Rect(x, y, sidebarEntryW-2, sidebarEntryH-2));
            matchMat.copyTo(targetInSidebar);
            i++;
        }
        return sidebar;
    }
}
