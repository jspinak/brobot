package io.github.jspinak.brobot.actions.methods.sikuliWrappers.text;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.mock.MockText;
import org.sikuli.script.OCR;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;

/**
 * Wrapper class for GetText, handles real and mock text queries.
 * Finds text in the Regions of MatchObjects and saves them to the Text
 * variable in Matches.
 */
@Component
public class GetTextWrapper {

    private final MockText mockText;

    public GetTextWrapper(MockText mockText) {
        this.mockText = mockText;
    }

    /*
      No new text is added to existing Match objects in mocks.
      In a mock, the first time text is retrieved for an Image
      has a certain likelihood of finding text based on the Image MatchHistory.
      Repeating this process enough times would make it almost certain to find text,
      thus rendering the inherent probability in the MatchHistory irrelevant.
    */
    public void getAllText(Matches matches) {
        matches.getMatchList().forEach(match -> setText(matches, match));
    }

    private void setText(Matches matches, Match match) {
        String str = getTextFromMatch(match);
        if (str.isEmpty()) return;
        matches.addString(str);
        match.setText(str);
    }

    public String getTextFromMatch(Match match) {
        if (BrobotSettings.mock) return mockText.getString(match);
        return match.getText();
    }

    /**
     * Reads all text in a Mat and returns a String
     * @param match should contain the Mat to read
     * @return all text as a String
     */
    public String getText(Match match) {
        BufferedImage bi = match.getImage().getBufferedImage();
        if (bi == null) return "";
        //OCR.Options myOptions = new OCR.Options().asLine();
        return OCR.readText(bi); //, myOptions);
    }

    /**
     * Reads all text in a Mat and sets the Match object's Text field
     * @param match should contain the Mat to read
     */
    public void setText(Match match) {
        String text = getText(match);
        match.setText(text);
    }
}
