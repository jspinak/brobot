package io.github.jspinak.brobot.runner.ui.log.services;

import io.github.jspinak.brobot.runner.ui.log.models.LogEntry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Service for filtering log entries based on various criteria.
 * Provides predicate builders for different filter types.
 */
@Slf4j
@Service
public class LogFilterService {
    
    /**
     * Filter criteria container.
     */
    public static class FilterCriteria {
        private String searchText;
        private boolean useRegex;
        private boolean caseSensitive;
        private String logType;
        private LogEntry.LogLevel minLevel;
        private LocalDate startDate;
        private LocalDate endDate;
        private String source;
        private boolean includeExceptions;
        
        // Builder pattern for easy construction
        public static FilterCriteriaBuilder builder() {
            return new FilterCriteriaBuilder();
        }
        
        public static class FilterCriteriaBuilder {
            private FilterCriteria criteria = new FilterCriteria();
            
            public FilterCriteriaBuilder searchText(String searchText) {
                criteria.searchText = searchText;
                return this;
            }
            
            public FilterCriteriaBuilder useRegex(boolean useRegex) {
                criteria.useRegex = useRegex;
                return this;
            }
            
            public FilterCriteriaBuilder caseSensitive(boolean caseSensitive) {
                criteria.caseSensitive = caseSensitive;
                return this;
            }
            
            public FilterCriteriaBuilder logType(String logType) {
                criteria.logType = logType;
                return this;
            }
            
            public FilterCriteriaBuilder minLevel(LogEntry.LogLevel minLevel) {
                criteria.minLevel = minLevel;
                return this;
            }
            
            public FilterCriteriaBuilder dateRange(LocalDate startDate, LocalDate endDate) {
                criteria.startDate = startDate;
                criteria.endDate = endDate;
                return this;
            }
            
            public FilterCriteriaBuilder source(String source) {
                criteria.source = source;
                return this;
            }
            
            public FilterCriteriaBuilder includeExceptions(boolean includeExceptions) {
                criteria.includeExceptions = includeExceptions;
                return this;
            }
            
            public FilterCriteria build() {
                return criteria;
            }
        }
    }
    
    /**
     * Creates a composite predicate based on the filter criteria.
     */
    public Predicate<LogEntry> createFilter(FilterCriteria criteria) {
        List<Predicate<LogEntry>> predicates = new ArrayList<>();
        
        // Search text filter
        if (criteria.searchText != null && !criteria.searchText.trim().isEmpty()) {
            predicates.add(createSearchFilter(criteria.searchText, criteria.useRegex, criteria.caseSensitive));
        }
        
        // Type filter
        if (criteria.logType != null && !criteria.logType.equals("ALL")) {
            predicates.add(createTypeFilter(criteria.logType));
        }
        
        // Level filter
        if (criteria.minLevel != null) {
            predicates.add(createLevelFilter(criteria.minLevel));
        }
        
        // Date range filter
        if (criteria.startDate != null || criteria.endDate != null) {
            predicates.add(createDateRangeFilter(criteria.startDate, criteria.endDate));
        }
        
        // Source filter
        if (criteria.source != null && !criteria.source.trim().isEmpty()) {
            predicates.add(createSourceFilter(criteria.source));
        }
        
        // Exception filter
        if (!criteria.includeExceptions) {
            predicates.add(entry -> !entry.hasException());
        }
        
        // Combine all predicates
        return predicates.stream()
                .reduce(entry -> true, Predicate::and);
    }
    
    /**
     * Creates a search text filter.
     */
    private Predicate<LogEntry> createSearchFilter(String searchText, boolean useRegex, boolean caseSensitive) {
        if (useRegex) {
            try {
                Pattern pattern = caseSensitive ? 
                    Pattern.compile(searchText) : 
                    Pattern.compile(searchText, Pattern.CASE_INSENSITIVE);
                
                return entry -> {
                    String text = getSearchableText(entry);
                    return pattern.matcher(text).find();
                };
            } catch (PatternSyntaxException e) {
                log.warn("Invalid regex pattern: {}", searchText, e);
                // Fall back to simple contains
                return createSimpleSearchFilter(searchText, caseSensitive);
            }
        } else {
            return createSimpleSearchFilter(searchText, caseSensitive);
        }
    }
    
    /**
     * Creates a simple contains search filter.
     */
    private Predicate<LogEntry> createSimpleSearchFilter(String searchText, boolean caseSensitive) {
        String search = caseSensitive ? searchText : searchText.toLowerCase();
        
        return entry -> {
            String text = getSearchableText(entry);
            String searchIn = caseSensitive ? text : text.toLowerCase();
            return searchIn.contains(search);
        };
    }
    
    /**
     * Gets all searchable text from a log entry.
     */
    private String getSearchableText(LogEntry entry) {
        StringBuilder sb = new StringBuilder();
        
        if (entry.getMessage() != null) {
            sb.append(entry.getMessage()).append(" ");
        }
        
        if (entry.getSource() != null) {
            sb.append(entry.getSource()).append(" ");
        }
        
        if (entry.getDetails() != null) {
            sb.append(entry.getDetails()).append(" ");
        }
        
        if (entry.getType() != null) {
            sb.append(entry.getType()).append(" ");
        }
        
        if (entry.hasException() && entry.getExceptionStackTrace() != null) {
            sb.append(entry.getExceptionStackTrace());
        }
        
        return sb.toString();
    }
    
    /**
     * Creates a type filter.
     */
    private Predicate<LogEntry> createTypeFilter(String type) {
        return entry -> type.equalsIgnoreCase(entry.getType());
    }
    
    /**
     * Creates a minimum level filter.
     */
    private Predicate<LogEntry> createLevelFilter(LogEntry.LogLevel minLevel) {
        return entry -> entry.getLevel().ordinal() >= minLevel.ordinal();
    }
    
    /**
     * Creates a date range filter.
     */
    private Predicate<LogEntry> createDateRangeFilter(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate != null ? 
            startDate.atStartOfDay() : LocalDateTime.MIN;
        LocalDateTime endDateTime = endDate != null ? 
            endDate.atTime(LocalTime.MAX) : LocalDateTime.MAX;
        
        return entry -> {
            LocalDateTime timestamp = entry.getTimestamp();
            return !timestamp.isBefore(startDateTime) && !timestamp.isAfter(endDateTime);
        };
    }
    
    /**
     * Creates a source filter.
     */
    private Predicate<LogEntry> createSourceFilter(String source) {
        String lowerSource = source.toLowerCase();
        return entry -> entry.getSource() != null && 
                       entry.getSource().toLowerCase().contains(lowerSource);
    }
    
    /**
     * Creates a quick filter for common scenarios.
     */
    public Predicate<LogEntry> createQuickFilter(QuickFilterType type) {
        switch (type) {
            case ERRORS_ONLY:
                return entry -> entry.getLevel() == LogEntry.LogLevel.ERROR || 
                               entry.getLevel() == LogEntry.LogLevel.FATAL;
                
            case WARNINGS_AND_ABOVE:
                return entry -> entry.getLevel().ordinal() >= LogEntry.LogLevel.WARNING.ordinal();
                
            case TODAY_ONLY:
                LocalDate today = LocalDate.now();
                return createDateRangeFilter(today, today);
                
            case LAST_HOUR:
                LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
                return entry -> !entry.getTimestamp().isBefore(oneHourAgo);
                
            case WITH_EXCEPTIONS:
                return LogEntry::hasException;
                
            case STATE_TRANSITIONS:
                return entry -> "STATE".equals(entry.getType()) || 
                               "STATE_TRANSITION".equals(entry.getType());
                
            case ACTIONS_ONLY:
                return entry -> "ACTION".equals(entry.getType());
                
            default:
                return entry -> true;
        }
    }
    
    /**
     * Quick filter types for common scenarios.
     */
    public enum QuickFilterType {
        ALL,
        ERRORS_ONLY,
        WARNINGS_AND_ABOVE,
        TODAY_ONLY,
        LAST_HOUR,
        WITH_EXCEPTIONS,
        STATE_TRANSITIONS,
        ACTIONS_ONLY
    }
    
    /**
     * Creates a highlighted search filter that marks matching text.
     * Returns a predicate that also populates highlight ranges.
     */
    public SearchResult createHighlightedSearchFilter(String searchText, boolean useRegex, boolean caseSensitive) {
        Pattern pattern;
        
        try {
            if (useRegex) {
                pattern = caseSensitive ? 
                    Pattern.compile(searchText) : 
                    Pattern.compile(searchText, Pattern.CASE_INSENSITIVE);
            } else {
                String quoted = Pattern.quote(searchText);
                pattern = caseSensitive ? 
                    Pattern.compile(quoted) : 
                    Pattern.compile(quoted, Pattern.CASE_INSENSITIVE);
            }
        } catch (PatternSyntaxException e) {
            log.warn("Invalid search pattern: {}", searchText, e);
            return new SearchResult(entry -> true, new ArrayList<>());
        }
        
        List<HighlightRange> highlights = new ArrayList<>();
        
        Predicate<LogEntry> predicate = entry -> {
            String text = getSearchableText(entry);
            java.util.regex.Matcher matcher = pattern.matcher(text);
            
            boolean found = false;
            while (matcher.find()) {
                found = true;
                highlights.add(new HighlightRange(
                    entry.getId(),
                    matcher.start(),
                    matcher.end()
                ));
            }
            
            return found;
        };
        
        return new SearchResult(predicate, highlights);
    }
    
    /**
     * Result of a highlighted search.
     */
    public static class SearchResult {
        private final Predicate<LogEntry> predicate;
        private final List<HighlightRange> highlights;
        
        public SearchResult(Predicate<LogEntry> predicate, List<HighlightRange> highlights) {
            this.predicate = predicate;
            this.highlights = highlights;
        }
        
        public Predicate<LogEntry> getPredicate() {
            return predicate;
        }
        
        public List<HighlightRange> getHighlights() {
            return highlights;
        }
    }
    
    /**
     * Represents a text range to highlight.
     */
    public static class HighlightRange {
        private final String entryId;
        private final int start;
        private final int end;
        
        public HighlightRange(String entryId, int start, int end) {
            this.entryId = entryId;
            this.start = start;
            this.end = end;
        }
        
        public String getEntryId() {
            return entryId;
        }
        
        public int getStart() {
            return start;
        }
        
        public int getEnd() {
            return end;
        }
    }
    
    /**
     * Gets all unique log types from a list of entries.
     */
    public List<String> getUniqueTypes(List<LogEntry> entries) {
        return entries.stream()
                .map(LogEntry::getType)
                .distinct()
                .sorted()
                .toList();
    }
    
    /**
     * Gets all unique sources from a list of entries.
     */
    public List<String> getUniqueSources(List<LogEntry> entries) {
        return entries.stream()
                .map(LogEntry::getSource)
                .filter(source -> source != null)
                .distinct()
                .sorted()
                .toList();
    }
}