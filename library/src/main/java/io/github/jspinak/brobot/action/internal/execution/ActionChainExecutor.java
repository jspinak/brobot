package io.github.jspinak.brobot.action.internal.execution;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionChainOptions;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.BaseFindOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.find.color.ColorFindOptions;
import io.github.jspinak.brobot.action.internal.service.ActionService;
import io.github.jspinak.brobot.exception.ActionFailedException;
import io.github.jspinak.brobot.exception.BrobotRuntimeException;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.SearchRegions;
import io.github.jspinak.brobot.model.match.Match;

/**
 * Executes a chain of actions according to a specified chaining strategy.
 *
 * <p>ActionChainExecutor is responsible for managing the execution of multiple actions in sequence,
 * where the results of one action influence the execution of the next. It supports different
 * chaining strategies (nested and confirm) that determine how results flow from one action to the
 * next.
 *
 * <p>This component follows the Single Responsibility Principle by focusing solely on chain
 * execution logic, while individual action execution is delegated to the appropriate action
 * implementations.
 *
 * @since 1.0
 */
@Component
public class ActionChainExecutor {

    private final ActionExecution actionExecution;
    private final ActionService actionService;

    public ActionChainExecutor(ActionExecution actionExecution, ActionService actionService) {
        this.actionExecution = actionExecution;
        this.actionService = actionService;
    }

    /**
     * Executes a chain of actions according to the specified chaining strategy.
     *
     * <p>The execution flow depends on the strategy:
     *
     * <ul>
     *   <li>NESTED: Each action searches within the results of the previous action
     *   <li>CONFIRM: Each action validates the results of the previous action
     * </ul>
     *
     * @param chainOptions the configuration for the action chain
     * @param initialResult the initial action result to start the chain
     * @param objectCollections the object collections to use for the initial action
     * @return the final ActionResult after all actions in the chain have executed
     * @throws ActionFailedException if any action in the chain fails
     */
    public ActionResult executeChain(
            ActionChainOptions chainOptions,
            ActionResult initialResult,
            ObjectCollection... objectCollections) {

        // Create a new result that will accumulate all execution history
        ActionResult finalResult = new ActionResult();

        // Execute the initial action
        ActionResult currentResult =
                executeAction(chainOptions.getInitialAction(), initialResult, objectCollections);

        // Store the initial action's result in history
        finalResult.addExecutionRecord(createActionRecord(currentResult));

        // If initial action failed, return immediately
        if (!currentResult.isSuccess()) {
            finalResult.setSuccess(false);
            return finalResult;
        }

        // Execute subsequent actions based on strategy
        for (ActionConfig nextAction : chainOptions.getChainedActions()) {
            currentResult =
                    executeNextInChain(
                            chainOptions.getStrategy(),
                            currentResult,
                            nextAction,
                            objectCollections);

            // Store each action's result in history
            finalResult.addExecutionRecord(createActionRecord(currentResult));

            // If any action fails, stop the chain
            if (!currentResult.isSuccess()) {
                break;
            }
        }

        // Copy final state to the result
        finalResult.setMatchList(currentResult.getMatchList());
        finalResult.setSuccess(currentResult.isSuccess());
        finalResult.setDuration(currentResult.getDuration());
        finalResult.setText(currentResult.getText());
        finalResult.setActiveStates(currentResult.getActiveStates());

        // Copy movements if any
        currentResult.getMovements().forEach(finalResult::addMovement);

        return finalResult;
    }

    /** Executes the next action in the chain based on the chaining strategy. */
    private ActionResult executeNextInChain(
            ActionChainOptions.ChainingStrategy strategy,
            ActionResult previousResult,
            ActionConfig nextAction,
            ObjectCollection... originalCollections) {

        switch (strategy) {
            case NESTED:
                return executeNestedAction(previousResult, nextAction);

            case CONFIRM:
                return executeConfirmingAction(previousResult, nextAction, originalCollections);

            default:
                throw new IllegalArgumentException("Unknown chaining strategy: " + strategy);
        }
    }

    /** Executes an action in NESTED mode where it searches within previous results. */
    private ActionResult executeNestedAction(ActionResult previousResult, ActionConfig nextAction) {
        // Create search regions from previous matches
        List<Region> searchRegions =
                previousResult.getMatchList().stream()
                        .map(Match::getRegion)
                        .collect(Collectors.toList());

        if (searchRegions.isEmpty()) {
            // No regions to search within
            ActionResult emptyResult = new ActionResult();
            emptyResult.setSuccess(false);
            return emptyResult;
        }

        // Configure the next action to search within previous results
        ActionConfig modifiedConfig = modifyConfigForNestedSearch(nextAction, searchRegions);

        // Create new collections containing the regions to search within
        ObjectCollection searchCollection =
                new ObjectCollection.Builder()
                        .withRegions(searchRegions.toArray(new Region[0]))
                        .build();

        return executeAction(modifiedConfig, new ActionResult(), searchCollection);
    }

    /** Executes an action in CONFIRM mode where it validates previous results. */
    private ActionResult executeConfirmingAction(
            ActionResult previousResult,
            ActionConfig nextAction,
            ObjectCollection... originalCollections) {

        // Store the original matches that we want to confirm
        List<Match> originalMatches = new ArrayList<>(previousResult.getMatchList());

        // Execute the confirming action on the original collections
        ActionResult confirmResult =
                executeAction(nextAction, new ActionResult(), originalCollections);

        // Filter original matches to keep only those confirmed by the new search
        List<Match> confirmedMatches =
                originalMatches.stream()
                        .filter(
                                originalMatch ->
                                        isMatchConfirmed(
                                                originalMatch, confirmResult.getMatchList()))
                        .collect(Collectors.toList());

        // Update the result with only confirmed matches
        previousResult.setMatchList(confirmedMatches);
        previousResult.setSuccess(!confirmedMatches.isEmpty());

        return previousResult;
    }

    /** Checks if an original match is confirmed by any of the confirming matches. */
    private boolean isMatchConfirmed(Match originalMatch, List<Match> confirmingMatches) {
        Region originalRegion = originalMatch.getRegion();

        return confirmingMatches.stream()
                .anyMatch(
                        confirmMatch -> {
                            Region confirmRegion = confirmMatch.getRegion();
                            // Check if confirm match is within or overlaps with original match
                            return originalRegion.contains(confirmRegion)
                                    || originalRegion.overlaps(confirmRegion);
                        });
    }

    /** Modifies an ActionConfig to search within specific regions for nested searching. */
    private ActionConfig modifyConfigForNestedSearch(
            ActionConfig original, List<Region> searchRegions) {
        // Create a SearchRegions object from the list of regions
        SearchRegions searchRegionsObj = new SearchRegions();
        searchRegionsObj.setRegions(searchRegions);

        // Handle different find option types
        if (original instanceof PatternFindOptions) {
            PatternFindOptions findOptions = (PatternFindOptions) original;
            return new PatternFindOptions.Builder(findOptions)
                    .setSearchRegions(searchRegionsObj)
                    .build();
        }

        if (original instanceof ColorFindOptions) {
            ColorFindOptions colorOptions = (ColorFindOptions) original;
            return new ColorFindOptions.Builder(colorOptions)
                    .setSearchRegions(searchRegionsObj)
                    .build();
        }

        if (original instanceof BaseFindOptions) {
            // Generic handling for other BaseFindOptions implementations
            // This is a fallback - specific implementations should be handled above
            return original;
        }

        // For non-find configs (Click, Type, etc.), search regions don't apply
        // Return the original config unchanged
        return original;
    }

    /** Delegates to ActionExecution to execute a single action. */
    private ActionResult executeAction(
            ActionConfig config, ActionResult result, ObjectCollection... collections) {
        // Get the appropriate action implementation for this config
        Optional<ActionInterface> actionOpt = actionService.getAction(config);

        if (actionOpt.isEmpty()) {
            throw new BrobotRuntimeException(
                    "No action implementation found for " + config.getClass().getSimpleName());
        }

        ActionInterface action = actionOpt.get();

        // Use ActionExecution to manage the lifecycle
        String actionDescription = "Chain execution: " + config.getClass().getSimpleName();
        return actionExecution.perform(action, actionDescription, config, collections);
    }

    /** Creates an ActionRecord from an ActionResult for storage in execution history. */
    private ActionRecord createActionRecord(ActionResult result) {
        ActionRecord record = new ActionRecord();
        record.setMatchList(new ArrayList<>(result.getMatchList()));
        record.setActionSuccess(result.isSuccess());
        record.setResultSuccess(result.isSuccess());
        record.setDuration(result.getDuration().toMillis() / 1000.0); // Convert to seconds
        record.setText(result.getText() != null ? result.getText().toString() : "");
        record.setTimeStamp(result.getStartTime());

        // Extract state name from matches if available
        if (!result.getMatchList().isEmpty()
                && result.getMatchList().get(0).getStateObjectData() != null) {
            record.setStateName(
                    result.getMatchList().get(0).getStateObjectData().getOwnerStateName());
        }

        return record;
    }
}
