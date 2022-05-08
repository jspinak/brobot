package io.github.jspinak.brobot.actions.methods.sikuliWrappers.text;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchObject;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.mock.MockText;
import org.springframework.stereotype.Component;

/**
 * Wrapper class for GetText, handles real and mock text queries.
 * Finds text in the Regions of MatchObjects and saves them to the Text
 * variable in Matches.
 */
@Component
public class GetTextWrapper {

    private MockText mockText;

    public GetTextWrapper(MockText mockText) {
        this.mockText = mockText;
    }

    /*
      No new text is added to existing MatchObjects in mocks.
      In a mock, the first time text is retrieved for an Image
      has a certain likelihood of finding text based on the Image MatchHistory.
      Repeating this process enough times would make it almost certain to find text,
      thus rendering the inherent probability in the MatchHistory irrelevant.
    */
    public void getAllText(Matches matches) {
        matches.getMatchObjects().forEach(mO -> addString(matches, mO));
    }

    private void addString(Matches matches, MatchObject matchObject) {
        String str = getTextFromMatch(matchObject);
        if (str.isEmpty()) return;
        matches.addString(str);
        matchObject.addString(str);
        matches.getDanglingSnapshots().addString(matchObject.getStateObject(), str);
    }

    public String getTextFromMatch(MatchObject matchObject) {
        if (BrobotSettings.mock) return mockText.getString(matchObject.getStateObject());
        return matchObject.getMatch().text();
    }
}
