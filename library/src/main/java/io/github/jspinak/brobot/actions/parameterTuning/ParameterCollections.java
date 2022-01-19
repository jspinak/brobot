package io.github.jspinak.brobot.actions.parameterTuning;

import io.github.jspinak.brobot.reports.Report;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * ParameterCollections help Brobot calibrate specific parameters with respect
 * to success conditions. For example, the effectiveness of clicking may require
 * different pauses for different applications.
 *
 * CAN BE BETTER ANALYZED USING SNAPSHOTS. MOVE FUNCTIONALITY TO SNAPSHOTS.
 */
@Component
@Getter
public class ParameterCollections {

    private List<ParameterCollection> params = new ArrayList<>();

    public void add(ParameterCollection param) {
        params.add(param);
    }

    public void print() {
        Report.println("clickDelay pauseBeforeMouseDown pauseAfterMouseUp moveMouseDelay | appear vanish success");
        params.forEach(param -> Report.format(
                "%.2f %.2f %.2f %.02f %s \n",
                param.getPauseAfterMouseDown(),
                param.getPauseBeforeMouseDown(),
                param.getPauseAfterMouseUp(),
                param.getMoveMouseDelay(),
                param.getTimeToAppear(),
                param.getTimeToVanish(),
                param.isSuccess()));
                //param.getMovementDuringClick())); // not yet implemented
    }

    public void printEvery(int savedCollections) {
        Report.println("params list size = "+params.size());
        if (params.size() > 0 && params.size() % savedCollections == savedCollections) print();
    }

}

    /*
    A more sophisticated solution would allow ParameterCollections for different groups of activities.
    ParameterCollectionsRepo would be a @Component with multiple ParameterCollections
    Activity groups are user defined.
    Examples in a game:
    1) Clicking on buttons during inventory maintenance
    2) Clicking on regions while fighting
    These two activities probably have very different speed requirements and success metrics.

    If implementing this repo, ParameterCollections would lose its @Component tag
     */
