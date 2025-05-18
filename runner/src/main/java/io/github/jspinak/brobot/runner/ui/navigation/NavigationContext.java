package io.github.jspinak.brobot.runner.ui.navigation;

import java.util.Optional;

/**
 * Represents contextual data passed during navigation between screens.
 * This allows screens to pass parameters and state information to each other.
 */
public class NavigationContext {
    private final java.util.Map<String, Object> data;

    /**
     * Creates a new NavigationContext with the given data.
     *
     * @param data The data to store in the context
     */
    private NavigationContext(java.util.Map<String, Object> data) {
        this.data = new java.util.HashMap<>(data);
    }

    /**
     * Gets a value from the context.
     *
     * @param key The key for the value
     * @param <T> The type of the value
     * @return An Optional containing the value, or empty if not found or of wrong type
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key) {
        if (key == null || !data.containsKey(key)) {
            return Optional.empty();
        }

        try {
            return Optional.ofNullable((T) data.get(key));
        } catch (ClassCastException e) {
            return Optional.empty();
        }
    }

    /**
     * Gets a value from the context with a default value.
     *
     * @param key The key for the value
     * @param defaultValue The default value to return if the key is not found
     * @param <T> The type of the value
     * @return The value, or the default value if not found or of wrong type
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrDefault(String key, T defaultValue) {
        if (key == null || !data.containsKey(key)) {
            return defaultValue;
        }

        try {
            T value = (T) data.get(key);
            return value != null ? value : defaultValue;
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }

    /**
     * Checks if the context contains a key.
     *
     * @param key The key to check
     * @return true if the context contains the key, false otherwise
     */
    public boolean contains(String key) {
        return key != null && data.containsKey(key);
    }

    /**
     * Gets all keys in the context.
     *
     * @return A set of all keys
     */
    public java.util.Set<String> getKeys() {
        return new java.util.HashSet<>(data.keySet());
    }

    /**
     * Gets the number of items in the context.
     *
     * @return The number of items
     */
    public int size() {
        return data.size();
    }

    /**
     * Checks if the context is empty.
     *
     * @return true if the context is empty, false otherwise
     */
    public boolean isEmpty() {
        return data.isEmpty();
    }

    /**
     * Creates a new NavigationContext.Builder.
     *
     * @return A new Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates an empty NavigationContext.
     *
     * @return An empty NavigationContext
     */
    public static NavigationContext empty() {
        return new NavigationContext(java.util.Collections.emptyMap());
    }

    /**
     * Builder for NavigationContext.
     */
    public static class Builder {
        private final java.util.Map<String, Object> data = new java.util.HashMap<>();

        /**
         * Adds a value to the context.
         *
         * @param key The key for the value
         * @param value The value
         * @return This builder
         */
        public Builder put(String key, Object value) {
            if (key != null) {
                data.put(key, value);
            }
            return this;
        }

        /**
         * Builds the NavigationContext.
         *
         * @return The built NavigationContext
         */
        public NavigationContext build() {
            return new NavigationContext(data);
        }
    }
}