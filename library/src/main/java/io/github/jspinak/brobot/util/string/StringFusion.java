package io.github.jspinak.brobot.util.string;

/**
 * Utility class for intelligently combining strings by removing common prefixes.
 *
 * <p>This class provides string fusion operations that combine two strings while eliminating
 * redundancy in their common prefix. This is useful for creating concise identifiers or labels from
 * related strings.
 *
 * <p>Fusion algorithm:
 *
 * <ol>
 *   <li>Identify the common prefix between two strings
 *   <li>Keep the entire first string
 *   <li>Append only the unique suffix of the second string
 *   <li>Separate with a delimiter (hyphen)
 * </ol>
 *
 * <p>Use cases:
 *
 * <ul>
 *   <li>Creating composite identifiers from hierarchical names
 *   <li>Generating unique keys from similar strings
 *   <li>Building descriptive labels without redundancy
 *   <li>Merging related file or path names
 * </ul>
 *
 * <p>Examples:
 *
 * <ul>
 *   <li>"user_name", "user_email" → "user_name-email"
 *   <li>"com.example", "com.example.app" → "com.example-app"
 *   <li>"test", "testing" → "test-ing"
 * </ul>
 *
 * <p>Thread safety: All methods are stateless and thread-safe.
 */
public class StringFusion {

    /**
     * Fuses two strings by eliminating their common prefix redundancy.
     *
     * <p>Combines string 'a' with the unique suffix of string 'b' that doesn't overlap with their
     * common prefix. The result is separated by a hyphen.
     *
     * <p>Algorithm:
     *
     * <ol>
     *   <li>Find length of common prefix between a and b
     *   <li>Extract suffix of b starting after common prefix
     *   <li>Concatenate: a + "-" + suffix_of_b
     * </ol>
     *
     * <p>Examples:
     *
     * <ul>
     *   <li>fuse("hello", "helloWorld") → "hello-World"
     *   <li>fuse("test", "testCase") → "test-Case"
     *   <li>fuse("abc", "xyz") → "abc-xyz" (no common prefix)
     *   <li>fuse("same", "same") → "same-" (complete overlap)
     * </ul>
     *
     * <p>Edge cases:
     *
     * <ul>
     *   <li>No common prefix: Returns "a-b"
     *   <li>b is prefix of a: Returns "a-"
     *   <li>a is prefix of b: Returns full a plus unique part of b
     *   <li>Identical strings: Returns "a-"
     * </ul>
     *
     * <p>Note: This method does not handle null inputs. Ensure non-null strings are provided to
     * avoid NullPointerException.
     *
     * @param a the first string (preserved entirely in result)
     * @param b the second string (only unique suffix included)
     * @return fused string in format "a-{unique_part_of_b}"
     */
    public static String fuse(String a, String b) {
        int commonLength = commonPrefixLength(a, b); // Find the common prefix length
        // The unique part of 'b' is the substring starting from the common length
        String uniquePartOfB = b.substring(commonLength);
        return a + "-" + uniquePartOfB; // Return the concatenated result
    }

    /**
     * Calculates the length of the common prefix between two strings.
     *
     * <p>Performs character-by-character comparison from the beginning of both strings until a
     * mismatch is found or the shorter string ends.
     *
     * <p>Algorithm:
     *
     * <ol>
     *   <li>Determine the maximum possible common length (shorter string's length)
     *   <li>Compare characters at each position
     *   <li>Stop at first mismatch or end of shorter string
     *   <li>Return count of matching characters
     * </ol>
     *
     * <p>Performance: O(min(a.length, b.length)) - linear in the length of the shorter string.
     *
     * <p>Examples:
     *
     * <ul>
     *   <li>("hello", "help") → 3 ("hel")
     *   <li>("test", "testing") → 4 ("test")
     *   <li>("abc", "xyz") → 0 (no common prefix)
     *   <li>("same", "same") → 4 (complete match)
     * </ul>
     *
     * @param a the first string to compare
     * @param b the second string to compare
     * @return the number of characters in the common prefix
     */
    private static int commonPrefixLength(String a, String b) {
        int minLength = Math.min(a.length(), b.length());
        int commonLength = 0;
        for (int i = 0; i < minLength; i++) {
            if (a.charAt(i) == b.charAt(i)) {
                commonLength++;
            } else {
                break;
            }
        }
        return commonLength;
    }
}
