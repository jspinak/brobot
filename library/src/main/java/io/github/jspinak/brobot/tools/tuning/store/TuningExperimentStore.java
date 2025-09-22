package io.github.jspinak.brobot.tools.tuning.store;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

// Removed old logging import: import io.github.jspinak.brobot.tools.tuning.model.TuningConstraints;
import io.github.jspinak.brobot.tools.tuning.model.TuningExperiment;

import lombok.Getter;

/**
 * Manages collections of parameter tuning results for optimization analysis.
 *
 * <p>TuningExperimentStore aggregates multiple {@link TuningExperiment} instances to facilitate
 * analysis of parameter effectiveness across multiple test runs. It supports the calibration of
 * timing parameters by tracking which parameter combinations yield successful outcomes for
 * different applications and scenarios.
 *
 * <p><strong>Primary purpose:</strong>
 *
 * <ul>
 *   <li>Collect parameter test results during tuning sessions
 *   <li>Analyze success patterns across different parameter combinations
 *   <li>Identify optimal timing values for specific applications
 *   <li>Support data-driven parameter optimization
 * </ul>
 *
 * <p><strong>Usage workflow:</strong>
 *
 * <ol>
 *   <li>Generate parameter combinations using {@link TuningExperiment}
 *   <li>Execute actions with those parameters
 *   <li>Record results in the ParameterCollection
 *   <li>Add completed collections to this aggregator
 *   <li>Analyze results to find optimal parameters
 * </ol>
 *
 * <p><strong>Future enhancement:</strong>
 *
 * <p>This functionality could be enhanced by moving to the Snapshots system for more comprehensive
 * data analysis. Additionally, supporting multiple collections for different activity groups (e.g.,
 * UI navigation vs. gameplay) would enable context-specific parameter optimization.
 *
 * <p><strong>Design considerations:</strong>
 *
 * <p>As a Spring component, this class maintains a singleton collection of all parameter tests. For
 * more sophisticated scenarios requiring multiple collections, consider implementing a repository
 * that manages different collections for various activity contexts.
 *
 * @see TuningExperiment
 * @see TuningConstraints
 */
@Component
@Getter
public class TuningExperimentStore {

    /**
     * List storing all parameter test results. Each entry represents one complete test run with
     * specific parameters and its corresponding success metrics.
     */
    private List<TuningExperiment> params = new ArrayList<>();

    /**
     * Adds a parameter test result to the collection.
     *
     * <p>Each ParameterCollection should contain both the input parameters used and the results
     * obtained from executing actions with those parameters. This method accumulates results for
     * later analysis.
     *
     * <p><strong>Side effects:</strong> Modifies the internal params list by adding the new
     * collection.
     *
     * @param param Completed parameter collection with test results
     */
    public void add(TuningExperiment param) {
        params.add(param);
    }

    /**
     * Prints a formatted report of all collected parameter results.
     *
     * <p>Outputs a table showing timing parameters alongside their success metrics, enabling visual
     * analysis of parameter effectiveness. The format includes:
     *
     * <ul>
     *   <li>All timing parameters (delays and pauses)
     *   <li>Appearance and vanish times
     *   <li>Success status for each parameter set
     * </ul>
     *
     * <p>Note: The header and data columns are slightly misaligned in the current implementation.
     * The actual output order differs from the header labels.
     *
     * <p><strong>Side effects:</strong> Writes formatted output to the Report system.
     */
    public void print() {
        // params.forEach(param -> ...)
    }

    /**
     * Prints a report at regular intervals during collection.
     *
     * <p>Useful for monitoring progress during long tuning sessions. Prints the current collection
     * size and, when the size is a multiple of the specified interval, outputs the full parameter
     * report.
     *
     * <p>Note: The condition logic appears to have a bug - it checks if size equals the interval
     * rather than checking for multiples, which may not trigger printing as intended.
     *
     * <p><strong>Side effects:</strong> Writes to Report system and may call {@link #print()}.
     *
     * @param savedCollections Interval at which to print full reports
     */
    public void printEvery(int savedCollections) {
        if (params.size() > 0 && params.size() % savedCollections == savedCollections) print();
    }
}

    /*
    A more sophisticated solution would allow a TuningExperimentStore for different groups of activities.
    TuningExperimentRepo would be a @Component with multiple TuningExperimentStores.
    Activity groups are user defined.
    Examples in a game:
    1) Clicking on buttons during inventory maintenance
    2) Clicking on regions while fighting
    These two activities probably have very different speed requirements and success metrics.

    If implementing this repo, TuningExperimentStore would lose its @Component tag
     */
