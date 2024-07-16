package io.github.jspinak.brobot.stringUtils;

public class FuseStrings {

    public static String fuse(String a, String b) {
        int commonLength = commonPrefixLength(a, b); // Find the common prefix length
        // The unique part of 'b' is the substring starting from the common length
        String uniquePartOfB = b.substring(commonLength);
        return a + "-" + uniquePartOfB; // Return the concatenated result
    }

    // Helper method to find the common prefix length
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
