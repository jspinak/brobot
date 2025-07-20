package io.github.jspinak.brobot.runner.ui.components.table.services;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.TextField;
import org.springframework.stereotype.Service;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Service for managing table filtering operations.
 *
 * @param <T> The type of items in the table
 */
@Service
public class TableFilterService<T> {
    
    private final ObjectProperty<Function<T, String>> searchProvider = new SimpleObjectProperty<>();
    private FilteredList<T> filteredItems;
    private TextField searchField;
    private Runnable onFilterChanged;
    
    /**
     * Initializes the filter service with the data.
     *
     * @param items The observable list of items
     * @return The filtered list
     */
    public FilteredList<T> initializeFiltering(ObservableList<T> items) {
        filteredItems = new FilteredList<>(items);
        return filteredItems;
    }
    
    /**
     * Sets the search field and binds the filter.
     *
     * @param searchField The search field
     */
    public void setSearchField(TextField searchField) {
        this.searchField = searchField;
        
        // Setup search field listener
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            updateFilter();
            if (onFilterChanged != null) {
                onFilterChanged.run();
            }
        });
    }
    
    /**
     * Sets the callback for when the filter changes.
     *
     * @param onFilterChanged The callback
     */
    public void setOnFilterChanged(Runnable onFilterChanged) {
        this.onFilterChanged = onFilterChanged;
    }
    
    /**
     * Sets the search provider function that extracts searchable text from an item.
     *
     * @param searchProvider The search provider function
     */
    public void setSearchProvider(Function<T, String> searchProvider) {
        this.searchProvider.set(searchProvider);
        updateFilter();
    }
    
    /**
     * Gets the search provider function.
     *
     * @return The search provider function
     */
    public Function<T, String> getSearchProvider() {
        return searchProvider.get();
    }
    
    /**
     * Gets the search provider property.
     *
     * @return The search provider property
     */
    public ObjectProperty<Function<T, String>> searchProviderProperty() {
        return searchProvider;
    }
    
    /**
     * Updates the filter based on the search text.
     */
    private void updateFilter() {
        if (searchField == null || filteredItems == null) {
            return;
        }
        
        String searchText = searchField.getText().toLowerCase();
        Function<T, String> provider = searchProvider.get();
        
        if (searchText.isEmpty()) {
            filteredItems.setPredicate(null);
        } else if (provider != null) {
            filteredItems.setPredicate(item -> {
                String searchString = provider.apply(item);
                return searchString != null && searchString.toLowerCase().contains(searchText);
            });
        }
    }
    
    /**
     * Sets a custom filter predicate.
     *
     * @param predicate The filter predicate
     */
    public void setFilter(Predicate<T> predicate) {
        if (filteredItems != null) {
            filteredItems.setPredicate(predicate);
            if (onFilterChanged != null) {
                onFilterChanged.run();
            }
        }
    }
    
    /**
     * Clears the filter.
     */
    public void clearFilter() {
        if (searchField != null) {
            searchField.clear();
        }
        if (filteredItems != null) {
            filteredItems.setPredicate(null);
        }
        if (onFilterChanged != null) {
            onFilterChanged.run();
        }
    }
    
    /**
     * Gets the current filter predicate.
     *
     * @return The filter predicate
     */
    public Predicate<? super T> getFilterPredicate() {
        return filteredItems != null ? filteredItems.getPredicate() : null;
    }
    
    /**
     * Gets the filtered items.
     *
     * @return The filtered list
     */
    public FilteredList<T> getFilteredItems() {
        return filteredItems;
    }
    
    /**
     * Gets the current search text.
     *
     * @return The search text
     */
    public String getSearchText() {
        return searchField != null ? searchField.getText() : "";
    }
    
    /**
     * Sets the search text programmatically.
     *
     * @param text The search text
     */
    public void setSearchText(String text) {
        if (searchField != null) {
            searchField.setText(text);
        }
    }
    
    /**
     * Combines multiple predicates with AND logic.
     *
     * @param predicates The predicates to combine
     * @return The combined predicate
     */
    @SafeVarargs
    public final Predicate<T> combinePredicatesAnd(Predicate<T>... predicates) {
        return item -> {
            for (Predicate<T> predicate : predicates) {
                if (!predicate.test(item)) {
                    return false;
                }
            }
            return true;
        };
    }
    
    /**
     * Combines multiple predicates with OR logic.
     *
     * @param predicates The predicates to combine
     * @return The combined predicate
     */
    @SafeVarargs
    public final Predicate<T> combinePredicatesOr(Predicate<T>... predicates) {
        return item -> {
            for (Predicate<T> predicate : predicates) {
                if (predicate.test(item)) {
                    return true;
                }
            }
            return false;
        };
    }
}