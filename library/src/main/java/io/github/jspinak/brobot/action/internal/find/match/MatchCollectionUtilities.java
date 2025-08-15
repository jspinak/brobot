package io.github.jspinak.brobot.action.internal.find.match;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.find.BaseFindOptions;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.match.Match;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Utility operations for managing collections of Match objects.
 * <p>
 * This component provides basic operations for manipulating match collections,
 * including adding matches to results and limiting the number of matches based
 * on action options. These operations are commonly used across various find
 * actions to manage and constrain match results.
 * 
 * @see Match
 * @see ActionResult
 * @see ActionConfig
 */
@Component
public class MatchCollectionUtilities {

    /**
     * Adds all matches from a list to an ActionResult.
     * <p>
     * This method transfers each match from the provided list into the
     * ActionResult's match collection. The ActionResult is modified by
     * this operation.
     * 
     * @param matchList The list of matches to add. Must not be null.
     * @param matches The ActionResult to which matches will be added. This object
     *                is modified by the method.
     */
    public void addMatchListToMatches(List<Match> matchList, ActionResult matches) {
        matchList.forEach(matches::add);
    }

    /**
     * Limits the number of matches in an ActionResult based on action options.
     * <p>
     * This method truncates the match list to contain at most the number of
     * matches specified in the action options. The method assumes that the
     * matches are already sorted by relevance, with the best matches at the
     * beginning of the list.
     * 
     * <p>Important: The matches must be sorted before calling this method,
     * as it simply takes the first N matches from the list.</p>
     * 
     * @param matches The ActionResult containing matches to limit. Must be sorted
     *                with best matches first. This object is modified by the method.
     * @param actionConfig Configuration specifying the maximum number of matches
     *                      to retain. If this value is 0 or negative, no limiting occurs.
     */
    public void limitNumberOfMatches(ActionResult matches, ActionConfig actionConfig) {
        int maxMatches = 1; // default
        if (actionConfig instanceof BaseFindOptions) {
            BaseFindOptions findOptions = (BaseFindOptions) actionConfig;
            maxMatches = findOptions.getMaxMatchesToActOn();
        }
        if (maxMatches <= 0) return;
        if (matches.size() <= maxMatches) return;
        List<Match> matchList = matches.getMatchList();
        matches.setMatchList(matchList.subList(0, maxMatches));
    }

}
