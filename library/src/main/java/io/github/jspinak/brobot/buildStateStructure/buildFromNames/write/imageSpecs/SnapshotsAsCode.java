package io.github.jspinak.brobot.buildStateStructure.buildFromNames.write.imageSpecs;

import io.github.jspinak.brobot.datatypes.primitives.match.MatchSnapshot;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import lombok.Getter;
import org.sikuli.script.Match;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Writes Java code for MatchSnapshots for a single StateImageObject.
 *
 * Snapshots are registered for every screenshot. This could produce a lot
 * of Snapshots, and we don't want to add all of them to our code.
 * Here, we set a maximum number of identical, consecutive Snapshots that will be
 * added.
 */
@Component
@Getter
public class SnapshotsAsCode {

    private final int identicalAllowed = 5;
    private String body;
    private boolean addClass;

    public void processSnapshots(StateImageObject image) {
        System.out.println("add snapshot: "+image.getName()+" "+image.getMatchHistory().getSnapshots().size());
        StringBuilder str = new StringBuilder();
        addClass = false;
        int identicalAdded = 0;
        MatchSnapshot lastSnapshot = new MatchSnapshot();
        for (MatchSnapshot snapshot : image.getMatchHistory().getSnapshots()) {
            if (snapshot.hasSameResultsAs(lastSnapshot)) identicalAdded++;
            lastSnapshot = snapshot;
            if (identicalAdded <= identicalAllowed) {
                List<Match> matchList = snapshot.getMatchList();
                if (matchList.size() == 1) {
                    Match m = matchList.get(0);
                    str.append("\n.addSnapshot(").append(m.x).append(", ").append(m.y).append(", ")
                            .append(m.w).append(", ").append(m.h).append(")");
                } else if (matchList.size() > 1) {
                    // MatchSnapshot.Builder needs to be passed the first time as $T
                    str.append("\n.addSnapshot(new ");
                    if (addClass) {
                        str.append("MatchSnapshot.Builder()");
                        str.append("\n\t.setActionOptions(ActionOptions.Find.ALL)");
                    } else {
                        str.append("$T()");
                        str.append("\n\t.setActionOptions($T.ALL)");
                    }
                    for (Match m : matchList) {
                        str.append("\n\t.addMatch(").append(m.x).append(", ").append(m.y).append(", ")
                                .append(m.w).append(", ").append(m.h).append(")");
                    }
                    str.append("\n\t.build())");
                    addClass = true;
                }
            }
        }
        body = str.toString();
    }
}
