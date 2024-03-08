package io.github.jspinak.brobot.actions.methods.mockOrLiveInterface;

import io.github.jspinak.brobot.actions.Permissions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.MockColor;
import io.github.jspinak.brobot.actions.methods.basicactions.find.histogram.FindHistogramsOneRegionOneImage;
import io.github.jspinak.brobot.actions.methods.basicactions.find.histogram.MockHistogram;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.find.FindInScene;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.text.GetTextWrapper;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.mock.MockFind;
import io.github.jspinak.brobot.mock.MockText;
import io.github.jspinak.brobot.mock.MockTime;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class MockOrLive {
    private final Permissions permissions;
    private final MockFind mockFind;
    private final MockText mockText;
    private final MockColor mockColor; // TODO
    private final MockHistogram mockHistogram;
    private final MockTime mockTime;
    private final FindInScene findInScene;
    private final GetTextWrapper getTextWrapper;
    private final FindHistogramsOneRegionOneImage findHistogramsOneRegionOneImage;

    public MockOrLive(Permissions permissions,
                      MockFind mockFind, MockText mockText, MockColor mockColor, MockHistogram mockHistogram,
                      MockTime mockTime,
                      FindInScene findInScene, GetTextWrapper getTextWrapper,
                      FindHistogramsOneRegionOneImage findHistogramsOneRegionOneImage) {
        this.permissions = permissions;
        this.mockFind = mockFind;
        this.mockText = mockText;
        this.mockColor = mockColor;
        this.mockHistogram = mockHistogram;
        this.mockTime = mockTime;
        this.findInScene = findInScene;
        this.getTextWrapper = getTextWrapper;
        this.findHistogramsOneRegionOneImage = findHistogramsOneRegionOneImage;
    }

    /**
     * Chooses to mock the find or execute it live.
     * In a mock, a pattern can only be found if the corresponding state is active.
     * @param pattern the pattern to find
     * @param scene the scene used as the template
     * @return a list of MatchObject
     */
    public List<Match> findAll(Pattern pattern, Image scene) {
        if (permissions.isMock()) return mockFind.getMatches(pattern);
        return findInScene.findAllInScene(pattern, scene);
    }

    public List<Match> findAllWords(Image scene) {
        if (permissions.isMock()) return mockFind.getWordMatches();
        return findInScene.getWordMatches(scene);
    }

    public void setText(Match match) {
        if (permissions.isMock()) match.setText(mockText.getString(match));
        else getTextWrapper.setText(match);
    }

    /**
     * LocalDateTime is immutable, so the 'now' variable can be directly referenced for a deep copy.
     * @return the current time, either as the real current time or the mocked current time.
     */
    public LocalDateTime now() {
        if (permissions.isMock()) return mockTime.now();
        return LocalDateTime.now();
    }

    public void wait(double seconds) {
        if (permissions.isMock()) mockTime.wait(seconds);
        new Region().sikuli().wait(seconds);
    }

    public List<Match> findHistogram(StateImage stateImage, Mat sceneHSV, List<Region> regions) {
        if (permissions.isMock()) return mockHistogram.getMockHistogramMatches(stateImage, regions);
        return findHistogramsOneRegionOneImage.findAll(regions, stateImage, sceneHSV);
    }

}
