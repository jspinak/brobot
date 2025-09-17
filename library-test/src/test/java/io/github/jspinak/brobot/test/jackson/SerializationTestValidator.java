package io.github.jspinak.brobot.test.jackson;

import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Validates and fixes common serialization issues in test objects. Provides diagnostic information
 * about what fields are missing or problematic.
 */
public class SerializationTestValidator {

    private final ObjectMapper mapper;

    public SerializationTestValidator(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Validates that an object can be serialized and deserialized. Returns a report of any issues
     * found.
     */
    public <T> ValidationReport validateSerialization(T object, Class<T> clazz) {
        ValidationReport report = new ValidationReport(clazz.getSimpleName());

        try {
            // Test serialization
            String json = mapper.writeValueAsString(object);
            report.setSerializable(true);
            report.setSerializedJson(json);

            // Test deserialization
            T deserialized = mapper.readValue(json, clazz);
            report.setDeserializable(true);

            // Compare fields
            validateFields(object, deserialized, report);

        } catch (Exception e) {
            report.addError("Serialization failed: " + e.getMessage());
            report.setException(e);
        }

        return report;
    }

    /** Attempts to fix common issues with an object to make it serializable. */
    public <T> T fixCommonIssues(T object) {
        if (object == null) return null;

        Class<?> clazz = object.getClass();

        // Fix ActionRecord
        if (object instanceof io.github.jspinak.brobot.model.action.ActionRecord) {
            io.github.jspinak.brobot.model.action.ActionRecord record =
                    (io.github.jspinak.brobot.model.action.ActionRecord) object;

            if (record.getMatchList() == null) {
                record.setMatchList(new ArrayList<>());
            }
            if (record.getText() == null) {
                record.setText("");
            }
            if (record.getTimeStamp() == null) {
                record.setTimeStamp(java.time.LocalDateTime.now());
            }
            if (record.getActionConfig() == null) {
                record.setActionConfig(
                        new io.github.jspinak.brobot.action.basic.find.PatternFindOptions.Builder()
                                .build());
            }
        }

        // Fix Match
        if (object instanceof io.github.jspinak.brobot.model.match.Match) {
            io.github.jspinak.brobot.model.match.Match match =
                    (io.github.jspinak.brobot.model.match.Match) object;

            if (match.getRegion() == null) {
                match.setRegion(new io.github.jspinak.brobot.model.element.Region(0, 0, 100, 100));
            }
            if (match.getText() == null) {
                match.setText("");
            }
            if (match.getTimeStamp() == null) {
                match.setTimeStamp(java.time.LocalDateTime.now());
            }
        }

        // Fix ObjectCollection
        if (object instanceof io.github.jspinak.brobot.action.ObjectCollection) {
            io.github.jspinak.brobot.action.ObjectCollection collection =
                    (io.github.jspinak.brobot.action.ObjectCollection) object;

            if (collection.getStateLocations() == null) {
                collection.setStateLocations(new ArrayList<>());
            }
            if (collection.getStateImages() == null) {
                collection.setStateImages(new ArrayList<>());
            }
            if (collection.getStateRegions() == null) {
                collection.setStateRegions(new ArrayList<>());
            }
            if (collection.getStateStrings() == null) {
                collection.setStateStrings(new ArrayList<>());
            }
            if (collection.getMatches() == null) {
                collection.setMatches(new ArrayList<>());
            }
            if (collection.getScenes() == null) {
                collection.setScenes(new ArrayList<>());
            }
        }

        return object;
    }

    /** Analyzes a JSON string to identify missing required fields. */
    public Set<String> findMissingRequiredFields(String json, Class<?> targetClass) {
        Set<String> missingFields = new HashSet<>();

        try {
            JsonNode node = mapper.readTree(json);

            // Check for commonly required fields based on class
            if (targetClass == io.github.jspinak.brobot.model.action.ActionRecord.class) {
                checkField(node, "timeStamp", missingFields);
                checkField(node, "actionConfig", missingFields);
                checkField(node, "matchList", missingFields);
            } else if (targetClass == io.github.jspinak.brobot.model.match.Match.class) {
                checkField(node, "region", missingFields);
                checkField(node, "score", missingFields);
            } else if (targetClass == io.github.jspinak.brobot.action.ObjectCollection.class) {
                checkField(node, "stateLocations", missingFields);
                checkField(node, "stateImages", missingFields);
                checkField(node, "stateRegions", missingFields);
                checkField(node, "stateStrings", missingFields);
            }

        } catch (Exception e) {
            // Invalid JSON
            missingFields.add("INVALID_JSON: " + e.getMessage());
        }

        return missingFields;
    }

    /** Adds missing fields to a JSON string with default values. */
    public String addMissingFields(String json, Class<?> targetClass) {
        try {
            ObjectNode node = (ObjectNode) mapper.readTree(json);

            if (targetClass == io.github.jspinak.brobot.model.action.ActionRecord.class) {
                if (!node.has("timeStamp")) {
                    node.put("timeStamp", java.time.LocalDateTime.now().toString());
                }
                if (!node.has("matchList")) {
                    node.putArray("matchList");
                }
                if (!node.has("text")) {
                    node.put("text", "");
                }
                if (!node.has("actionConfig")) {
                    ObjectNode config = node.putObject("actionConfig");
                    config.put(
                            "@class",
                            "io.github.jspinak.brobot.action.basic.find.PatternFindOptions");
                }
            }

            return mapper.writeValueAsString(node);

        } catch (Exception e) {
            return json; // Return original if we can't fix it
        }
    }

    private void checkField(JsonNode node, String fieldName, Set<String> missingFields) {
        if (!node.has(fieldName) || node.get(fieldName).isNull()) {
            missingFields.add(fieldName);
        }
    }

    private <T> void validateFields(T original, T deserialized, ValidationReport report) {
        // This is a simplified comparison - in reality you'd use reflection
        // or a more sophisticated comparison library
        if (!Objects.equals(original, deserialized)) {
            report.addWarning("Deserialized object differs from original");
        }
    }

    /** Report of validation results. */
    public static class ValidationReport {
        private final String className;
        private boolean serializable = false;
        private boolean deserializable = false;
        private String serializedJson;
        private List<String> errors = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();
        private Exception exception;

        public ValidationReport(String className) {
            this.className = className;
        }

        public boolean isValid() {
            return serializable && deserializable && errors.isEmpty();
        }

        public void addError(String error) {
            errors.add(error);
        }

        public void addWarning(String warning) {
            warnings.add(warning);
        }

        public String generateReport() {
            StringBuilder sb = new StringBuilder();
            sb.append("Validation Report for ").append(className).append("\n");
            sb.append("=====================================\n");
            sb.append("Serializable: ").append(serializable).append("\n");
            sb.append("Deserializable: ").append(deserializable).append("\n");

            if (!errors.isEmpty()) {
                sb.append("\nErrors:\n");
                errors.forEach(e -> sb.append("  - ").append(e).append("\n"));
            }

            if (!warnings.isEmpty()) {
                sb.append("\nWarnings:\n");
                warnings.forEach(w -> sb.append("  - ").append(w).append("\n"));
            }

            if (exception != null) {
                sb.append("\nException: ")
                        .append(exception.getClass().getSimpleName())
                        .append(": ")
                        .append(exception.getMessage())
                        .append("\n");
            }

            if (serializedJson != null && serializedJson.length() < 1000) {
                sb.append("\nSerialized JSON:\n").append(serializedJson).append("\n");
            }

            return sb.toString();
        }

        // Getters and setters
        public boolean isSerializable() {
            return serializable;
        }

        public void setSerializable(boolean serializable) {
            this.serializable = serializable;
        }

        public boolean isDeserializable() {
            return deserializable;
        }

        public void setDeserializable(boolean deserializable) {
            this.deserializable = deserializable;
        }

        public String getSerializedJson() {
            return serializedJson;
        }

        public void setSerializedJson(String json) {
            this.serializedJson = json;
        }

        public List<String> getErrors() {
            return errors;
        }

        public List<String> getWarnings() {
            return warnings;
        }

        public Exception getException() {
            return exception;
        }

        public void setException(Exception e) {
            this.exception = e;
        }
    }
}
