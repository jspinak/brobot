package io.github.jspinak.brobot.runner.ui.services.logs;

import java.util.function.Predicate;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import lombok.extern.slf4j.Slf4j;

/** Service for handling log filtering logic. */
@Slf4j
public class LogFilterService {

    private final FilteredList<LogEntryViewModel> filteredLogs;

    // Current filter criteria
    private String searchText = "";
    private String levelFilter = "All Levels";
    private String typeFilter = "All Types";

    public LogFilterService(ObservableList<LogEntryViewModel> logEntries) {
        this.filteredLogs = new FilteredList<>(logEntries);
        applyFilters();
    }

    /** Sets the search text filter. */
    public void setSearchText(String searchText) {
        this.searchText = searchText != null ? searchText.toLowerCase() : "";
        applyFilters();
    }

    /** Sets the level filter. */
    public void setLevelFilter(String levelFilter) {
        this.levelFilter = levelFilter != null ? levelFilter : "All Levels";
        applyFilters();
    }

    /** Sets the type filter. */
    public void setTypeFilter(String typeFilter) {
        this.typeFilter = typeFilter != null ? typeFilter : "All Types";
        applyFilters();
    }

    /** Clears all filters. */
    public void clearFilters() {
        searchText = "";
        levelFilter = "All Levels";
        typeFilter = "All Types";
        applyFilters();
    }

    /** Applies all active filters. */
    private void applyFilters() {
        Predicate<LogEntryViewModel> combinedFilter =
                entry -> {
                    // Search filter
                    if (!searchText.isEmpty()) {
                        String message = entry.getMessage().toLowerCase();
                        if (!message.contains(searchText)) {
                            return false;
                        }
                    }

                    // Level filter
                    if (!levelFilter.equals("All Levels")) {
                        if (!entry.getLevel().equals(levelFilter)) {
                            return false;
                        }
                    }

                    // Type filter
                    if (!typeFilter.equals("All Types")) {
                        if (!entry.getType().equals(typeFilter)) {
                            return false;
                        }
                    }

                    return true;
                };

        filteredLogs.setPredicate(combinedFilter);

        log.debug(
                "Filters applied - Search: '{}', Level: {}, Type: {}, Results: {}",
                searchText,
                levelFilter,
                typeFilter,
                filteredLogs.size());
    }

    /** Gets the filtered logs list. */
    public FilteredList<LogEntryViewModel> getFilteredLogs() {
        return filteredLogs;
    }

    /** Gets the current number of filtered entries. */
    public int getFilteredCount() {
        return filteredLogs.size();
    }

    /** Gets the total number of entries (before filtering). */
    public int getTotalCount() {
        return filteredLogs.getSource().size();
    }
}
