package io.github.jspinak.brobot.runner.json.parsing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.github.jspinak.brobot.runner.json.parsing.exception.ConfigurationException;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Provides utilities for navigating and extracting data from JSON structures using path notation.
 * <p>
 * This class simplifies working with complex JSON structures by providing type-safe accessors
 * and path-based navigation. It supports dot notation for accessing nested properties and
 * numeric indices for array elements.
 * <p>
 * Path notation examples:
 * <ul>
 * <li>"field" - Access a top-level field</li>
 * <li>"parent.child" - Access a nested field</li>
 * <li>"array.0" - Access the first element of an array</li>
 * <li>"parent.array.2.field" - Complex nested path with array access</li>
 * </ul>
 * <p>
 * The class provides:
 * <ul>
 * <li>Type-safe getters for primitives (string, int, boolean)</li>
 * <li>Optional variants for graceful handling of missing values</li>
 * <li>Collection utilities for working with arrays</li>
 * <li>Path existence checking</li>
 * </ul>
 *
 * @see JsonNode
 * @see ConfigurationException
 */
@Component
public class JsonPathUtils {

    /**
     * Retrieves a string value from a JSON path.
     * <p>
     * The path must point to a text node. If the node exists but is not textual
     * (e.g., number, boolean, object, array), a ConfigurationException is thrown.
     *
     * @param root The root JSON node to start navigation from
     * @param path The dot-notation path to the value (e.g., "config.name")
     * @return The string value at the specified path
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
     * Retrieves an optional string value from a JSON path.
     * <p>
     * This method provides a null-safe way to access string values. If the path
     * doesn't exist or the value is not a string, an empty Optional is returned
     * instead of throwing an exception.
     *
     * @param root The root JSON node to start navigation from
     * @param path The dot-notation path to the value
     * @return An Optional containing the string value, or empty if not found/not a string
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
     * Navigates to and retrieves a JSON node at the specified path.
     * <p>
     * This is the core navigation method that supports:
     * <ul>
     * <li>Dot notation for object field access</li>
     * <li>Numeric indices for array element access</li>
     * <li>Mixed paths combining objects and arrays</li>
     * </ul>
     * <p>
     * Examples:
     * <ul>
     * <li>"field" - Returns root.field</li>
     * <li>"parent.child" - Returns root.parent.child</li>
     * <li>"items.0.name" - Returns root.items[0].name</li>
     * </ul>
     *
     * @param root The root JSON node to start navigation from
     * @param path The dot-notation path to navigate (empty/null returns root)
     * @return The JsonNode at the specified path
     * @throws ConfigurationException if any part of the path doesn't exist or is invalid
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
     * Checks if a path exists in the JSON structure.
     * <p>
     * This method provides a safe way to check path existence without throwing
     * exceptions. It's useful for conditional logic based on JSON structure.
     *
     * @param root The root JSON node to check
     * @param path The dot-notation path to verify
     * @return true if the complete path exists and is navigable, false otherwise
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
     * Iterates over each element in a JSON array, applying the given consumer.
     * <p>
     * This method provides a functional approach to array processing, allowing
     * side effects for each element without collecting results.
     *
     * @param root The root JSON node containing the array
     * @param arrayPath The dot-notation path to the array
     * @param consumer The operation to perform on each array element
     * @throws ConfigurationException if the path doesn't exist or doesn't point to an array
     */
    public void forEachInArray(JsonNode root, String arrayPath, Consumer<JsonNode> consumer) throws ConfigurationException {
        ArrayNode array = getArray(root, arrayPath);
        for (JsonNode element : array) {
            consumer.accept(element);
        }
    }

    /**
     * Transforms each element in a JSON array into a list of typed objects.
     * <p>
     * This method provides a functional transformation from JSON array elements
     * to a strongly-typed Java list. The mapper function defines how each
     * JsonNode element is converted to the target type.
     *
     * @param <T> The target type for list elements
     * @param root The root JSON node containing the array
     * @param arrayPath The dot-notation path to the array
     * @param mapper The transformation function for each element
     * @return A list containing the transformed elements
     * @throws ConfigurationException if the path doesn't exist or doesn't point to an array
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
     * Finds the first element in a JSON array that matches the given predicate.
     * <p>
     * This method provides a functional search mechanism for JSON arrays,
     * returning the first element that satisfies the predicate condition.
     * The search stops as soon as a match is found (short-circuit evaluation).
     *
     * @param root The root JSON node containing the array
     * @param arrayPath The dot-notation path to the array
     * @param predicate The condition to test each element against
     * @return An Optional containing the first matching element, or empty if no matches
     * @throws ConfigurationException if the path doesn't exist or doesn't point to an array
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
     * Retrieves all field names from a JSON object at the specified path.
     * <p>
     * This method is useful for dynamic JSON processing where field names
     * are not known at compile time. It returns the names in iteration order,
     * which may not be the same as the order in the original JSON text.
     *
     * @param root The root JSON node to navigate from
     * @param objectPath The dot-notation path to the object (empty for root)
     * @return A list of field names present in the object
     * @throws ConfigurationException if the path doesn't exist or doesn't point to an object
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