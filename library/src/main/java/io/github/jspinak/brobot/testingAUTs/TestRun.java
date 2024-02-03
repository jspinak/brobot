package io.github.jspinak.brobot.testingAUTs;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Brobot's AUT testing strategy is to go through all states looking for missing
 * images or faulty transitions. A single TestRun should be sufficient to test the entire
 * application.
 *
 * All states must be connected in a functioning
 * model-based automation application, although there may not be sufficient cycles
 * for one run to visit all states. In this case, multiple TestRuns are needed to
 * proof the entire application. A meta solution would be to add cycles to the model that
 * allow the automation to reach the start states again. This might involve adding transitions
 * that close and open the application. With transitions to the start states,
 * the Brobot application can proof all states in one run and is preferable
 * to manually restarting the application and selecting new paths to test.
 *
 * The TestRun class can provide test meta information to a noSQL database like
 * Elasticsearch. Indexing elements to elasticsearch would require keeping track of the test
 * instance, stored as BrobotSettings.testIteration.
 * Alternatively, the timestamp could be used to identify the test instance.
 */
@Getter
@Setter
public class TestRun {

    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Set<String> startStates;
    private Set<String> endStates;
    private String recordingFilename;

    /*
    Each Image can have various png files. This should be specified in the backend
    Spring Boot application along with the locations of the files in the cloud.
     */
    public TestRun(String description, LocalDateTime startTime, Set<String> startStates) {
        this.description = description;
        this.startTime = startTime;
        this.startStates = startStates;
    }

}
