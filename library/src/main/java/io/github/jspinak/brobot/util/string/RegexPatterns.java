package io.github.jspinak.brobot.util.string;

/**
 * Utility class providing common regular expression patterns and validation methods.
 * <p>
 * This class centralizes frequently used regex patterns and string validation
 * logic to promote consistency and reduce duplication across the codebase.
 * <p>
 * Current capabilities:
 * <ul>
 * <li>Numeric string validation (including decimals)</li>
 * </ul>
 * <p>
 * Design principles:
 * <ul>
 * <li>Static methods for stateless operations</li>
 * <li>Null-safe implementations</li>
 * <li>Performance-conscious patterns</li>
 * <li>Clear method naming for self-documentation</li>
 * </ul>
 * <p>
 * Future expansion possibilities:
 * <ul>
 * <li>Email validation patterns</li>
 * <li>URL/URI validation</li>
 * <li>Phone number formats</li>
 * <li>Date/time patterns</li>
 * <li>Alphanumeric validation</li>
 * </ul>
 * <p>
 * Thread safety: All methods are stateless and thread-safe.
 */
public class RegexPatterns {

    /**
     * Checks if a string contains only numeric characters and decimal points.
     * <p>
     * This method validates strings that represent positive numeric values,
     * including integers and decimal numbers. It uses a simple regex pattern
     * that allows digits (0-9) and decimal points.
     * <p>
     * Pattern details:
     * <ul>
     * <li>Regex: "[0-9.]+"</li>
     * <li>Matches: One or more digits or decimal points</li>
     * <li>No sign validation (positive numbers only)</li>
     * <li>No decimal point position validation</li>
     * </ul>
     * <p>
     * Valid examples:
     * <ul>
     * <li>"123" → true</li>
     * <li>"123.45" → true</li>
     * <li>"0.5" → true</li>
     * <li>".5" → true</li>
     * <li>"123." → true</li>
     * <li>"1.2.3" → true (multiple decimals allowed)</li>
     * </ul>
     * <p>
     * Invalid examples:
     * <ul>
     * <li>null → false</li>
     * <li>"" → false (empty string)</li>
     * <li>"abc" → false</li>
     * <li>"-123" → false (negative sign)</li>
     * <li>"12a3" → false (contains letter)</li>
     * <li>"1,234" → false (comma not allowed)</li>
     * </ul>
     * <p>
     * Limitations:
     * <ul>
     * <li>Allows multiple decimal points (e.g., "1.2.3")</li>
     * <li>No validation of numeric format correctness</li>
     * <li>No support for negative numbers</li>
     * <li>No support for scientific notation</li>
     * <li>No support for thousands separators</li>
     * </ul>
     * <p>
     * Use case: Quick validation for simple positive numeric inputs
     * in user interfaces or configuration files.
     *
     * @param str the string to validate; may be null
     * @return true if the string contains only digits and decimal points, false otherwise
     */
    public static boolean isNumeric(String str){
        return str != null && str.matches("[0-9.]+");
    }

}
