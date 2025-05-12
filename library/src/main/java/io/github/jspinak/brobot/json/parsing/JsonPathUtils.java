package io.github.jspinak.brobot.json.parsing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.jspinak.brobot.json.parsing.exception.ConfigurationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Utility class for working with JSON paths and extracting data from complex JSON structures.
 */
@Component
public class JsonPathUtils {

    /**
     * Get a string value from a JSON path
     *
     * @param root The root JSON node
     * @param path The path to the value (dot notation)
     * @return The string value
     * @throws ConfigurationException if the path doesn't exist or the value is not a string
     */
    public String getString(JsonNode root, String path) throws ConfigurationException {
        JsonNode node = getNode(root, path);
        if (!node.isTextual()) {
            throw new ConfigurationException("Value at path '" + path + "' is not a string");
        }
        return node.asText();
    }

    /**
     * Get an optional string value from a JSON path
     *
     * @param root The root JSON node
     * @param path The path to the value (dot notation)
     * @return An optional string value
     */
    public Optional<String> getOptionalString(JsonNode root, String path) {
        try {
            return Optional.of(getString(root, path));
        } catch (ConfigurationException e) {
            return Optional.empty();
        }
    }

    /**
     * Get an integer value from a JSON path
     *
     * @param root The root JSON node
     * @param path The path to the value (dot notation)
     * @return The integer value
     * @throws ConfigurationException if the path doesn't exist or the value is not an integer
     */
    public int getInt(JsonNode root, String path) throws ConfigurationException {
        JsonNode node = getNode(root, path);
        if (!node.isInt()) {
            throw new ConfigurationException("Value at path '" + path + "' is not an integer");
        }
        return node.asInt();
    }

    /**
     * Get an optional integer value from a JSON path
     *
     * @param root The root JSON node
     * @param path The path to the value (dot notation)
     * @return An optional integer value
     */
    public Optional<Integer> getOptionalInt(JsonNode root, String path) {
        try {
            return Optional.of(getInt(root, path));
        } catch (ConfigurationException e) {
            return Optional.empty();
        }
    }

    /**
     * Get a boolean value from a JSON path
     *
     * @param root The root JSON node
     * @param path The path to the value (dot notation)
     * @return The boolean value
     * @throws ConfigurationException if the path doesn't exist or the value is not a boolean
     */
    public boolean getBoolean(JsonNode root, String path) throws ConfigurationException {
        JsonNode node = getNode(root, path);
        if (!node.isBoolean()) {
            throw new ConfigurationException("Value at path '" + path + "' is not a boolean");
        }
        return node.asBoolean();
    }

    /**
     * Get an optional boolean value from a JSON path
     *
     * @param root The root JSON node
     * @param path The path to the value (dot notation)
     * @return An optional boolean value
     */
    public Optional<Boolean> getOptionalBoolean(JsonNode root, String path) {
        try {
            return Optional.of(getBoolean(root, path));
        } catch (ConfigurationException e) {
            return Optional.empty();
        }
    }

    /**
     * Get a JSON node from a path
     *
     * @param root The root JSON node
     * @param path The path to the node (dot notation)
     * @return The node at the path
     * @throws ConfigurationException if the path doesn't exist
     */
    public JsonNode getNode(JsonNode root, String path) throws ConfigurationException {
        if (path == null || path.isEmpty()) {
            return root;
        }

        String[] parts = path.split("\\.");
        JsonNode current = root;

        for (String part : parts) {
            if (current.isObject()) {
                if (!current.has(part)) {
                    throw new ConfigurationException("Path '" + path + "' does not exist: missing field '" + part + "'");
                }
                current = current.get(part);
            } else if (current.isArray()) {
                try {
                    int index = Integer.parseInt(part);
                    if (index < 0 || index >= current.size()) {
                        throw new ConfigurationException("Path '" + path + "' index out of bounds: " + index);
                    }
                    current = current.get(index);
                } catch (NumberFormatException e) {
                    throw new ConfigurationException("Path '" + path + "' invalid array index: " + part);
                }
            } else {
                throw new ConfigurationException("Path '" + path + "' cannot be navigated at '" + part + "'");
            }
        }

        return current;
    }

    /**
     * Get an array node from a path
     *
     * @param root The root JSON node
     * @param path The path to the array (dot notation)
     * @return The array node
     * @throws ConfigurationException if the path doesn't exist or the node is not an array
     */
    public ArrayNode getArray(JsonNode root, String path) throws ConfigurationException {
        JsonNode node = getNode(root, path);
        if (!node.isArray()) {
            throw new ConfigurationException("Value at path '" + path + "' is not an array");
        }
        return (ArrayNode) node;
    }

    /**
     * Get an optional array node from a path
     *
     * @param root The root JSON node
     * @param path The path to the array (dot notation)
     * @return An optional array node
     */
    public Optional<ArrayNode> getOptionalArray(JsonNode root, String path) {
        try {
            return Optional.of(getArray(root, path));
        } catch (ConfigurationException e) {
            return Optional.empty();
        }
    }

    /**
     * Get an object node from a path
     *
     * @param root The root JSON node
     * @param path The path to the object (dot notation)
     * @return The object node
     * @throws ConfigurationException if the path doesn't exist or the node is not an object
     */
    public ObjectNode getObject(JsonNode root, String path) throws ConfigurationException {
        JsonNode node = getNode(root, path);
        if (!node.isObject()) {
            throw new ConfigurationException("Value at path '" + path + "' is not an object");
        }
        return (ObjectNode) node;
    }

    /**
     * Get an optional object node from a path
     *
     * @param root The root JSON node
     * @param path The path to the object (dot notation)
     * @return An optional object node
     */
    public Optional<ObjectNode> getOptionalObject(JsonNode root, String path) {
        try {
            return Optional.of(getObject(root, path));
        } catch (ConfigurationException e) {
            return Optional.empty();
        }
    }

    /**
     * Check if a path exists in the JSON
     *
     * @param root The root JSON node
     * @param path The path to check
     * @return true if the path exists, false otherwise
     */
    public boolean hasPath(JsonNode root, String path) {
        try {
            getNode(root, path);
            return true;
        } catch (ConfigurationException e) {
            return false;
        }
    }

    /**
     * Process each element in a JSON array with a consumer
     *
     * @param root The root JSON node
     * @param arrayPath The path to the array
     * @param consumer The consumer to process each element
     * @throws ConfigurationException if the path doesn't exist or is not an array
     */
    public void forEachInArray(JsonNode root, String arrayPath, Consumer<JsonNode> consumer) throws ConfigurationException {
        ArrayNode array = getArray(root, arrayPath);
        for (JsonNode element : array) {
            consumer.accept(element);
        }
    }

    /**
     * Map each element in a JSON array to a list
     *
     * @param root The root JSON node
     * @param arrayPath The path to the array
     * @param mapper The function to map each element
     * @param <T> The type to map to
     * @return A list of mapped elements
     * @throws ConfigurationException if the path doesn't exist or is not an array
     */
    public <T> List<T> mapArray(JsonNode root, String arrayPath, Function<JsonNode, T> mapper) throws ConfigurationException {
        ArrayNode array = getArray(root, arrayPath);
        List<T> result = new ArrayList<>();

        for (JsonNode element : array) {
            result.add(mapper.apply(element));
        }

        return result;
    }

    /**
     * Find the first element in an array that matches a predicate
     *
     * @param root The root JSON node
     * @param arrayPath The path to the array
     * @param predicate The predicate to test elements
     * @return The first matching element, or empty if none match
     * @throws ConfigurationException if the path doesn't exist or is not an array
     */
    public Optional<JsonNode> findInArray(JsonNode root, String arrayPath, Function<JsonNode, Boolean> predicate) throws ConfigurationException {
        ArrayNode array = getArray(root, arrayPath);

        for (JsonNode element : array) {
            if (predicate.apply(element)) {
                return Optional.of(element);
            }
        }

        return Optional.empty();
    }

    /**
     * Get a list of field names from an object
     *
     * @param root The root JSON node
     * @param objectPath The path to the object
     * @return A list of field names
     * @throws ConfigurationException if the path doesn't exist or is not an object
     */
    public List<String> getFieldNames(JsonNode root, String objectPath) throws ConfigurationException {
        JsonNode node = getNode(root, objectPath);
        if (!node.isObject()) {
            throw new ConfigurationException("Value at path '" + objectPath + "' is not an object");
        }

        List<String> fieldNames = new ArrayList<>();
        Iterator<String> iterator = node.fieldNames();

        while (iterator.hasNext()) {
            fieldNames.add(iterator.next());
        }

        return fieldNames;
    }
}