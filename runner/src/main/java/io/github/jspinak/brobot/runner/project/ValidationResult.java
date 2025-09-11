package io.github.jspinak.brobot.runner.project;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Data;

/** Result of project validation containing any issues found. */
@Data
@Builder
public class ValidationResult {

    private boolean valid;

    @Builder.Default private List<ValidationIssue> issues = new ArrayList<>();

    /** Validation issue details. */
    @Data
    @Builder
    public static class ValidationIssue {
        private Severity severity;
        private String category;
        private String message;
        private String field;
        private Object invalidValue;

        public enum Severity {
            ERROR,
            WARNING,
            INFO
        }
    }

    /** Adds an error issue. */
    public void addError(String category, String message) {
        issues.add(
                ValidationIssue.builder()
                        .severity(ValidationIssue.Severity.ERROR)
                        .category(category)
                        .message(message)
                        .build());
        valid = false;
    }

    /** Adds a warning issue. */
    public void addWarning(String category, String message) {
        issues.add(
                ValidationIssue.builder()
                        .severity(ValidationIssue.Severity.WARNING)
                        .category(category)
                        .message(message)
                        .build());
    }

    /** Gets only error issues. */
    public List<ValidationIssue> getErrors() {
        return issues.stream()
                .filter(i -> i.getSeverity() == ValidationIssue.Severity.ERROR)
                .toList();
    }

    /** Gets only warning issues. */
    public List<ValidationIssue> getWarnings() {
        return issues.stream()
                .filter(i -> i.getSeverity() == ValidationIssue.Severity.WARNING)
                .toList();
    }
}
