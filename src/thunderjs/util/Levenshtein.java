package thunderjs.util;

import java.util.Collection;

/**
 * Utility class for calculating Levenshtein Distance and finding suggestions for typos.
 */
public class Levenshtein {

    /**
     * Calculates the Levenshtein Distance between two strings.
     * Uses an O(min(N, M)) space-optimized dynamic programming approach.
     */
    public static int distance(String a, String b) {
        if (a == null && b == null) return 0;
        if (a == null) return b.length();
        if (b == null) return a.length();

        // Swap to make sure b is shorter to save memory
        if (a.length() < b.length()) {
            String temp = a;
            a = b;
            b = temp;
        }

        int[] dp = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) {
            dp[j] = j;
        }

        for (int i = 1; i <= a.length(); i++) {
            int prev = dp[0];
            dp[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int temp = dp[j];
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[j] = prev;
                } else {
                    dp[j] = Math.min(prev + 1, Math.min(dp[j] + 1, dp[j - 1] + 1));
                }
                prev = temp;
            }
        }

        return dp[b.length()];
    }

    /**
     * Finds the closest string from a collection of candidates to the target.
     * Returns the closest candidate only if the distance is <= 2. Otherwise returns null.
     */
    public static String closest(String target, Collection<String> candidates) {
        if (target == null || candidates == null || candidates.isEmpty()) {
            return null;
        }

        String bestMatch = null;
        int minDistance = Integer.MAX_VALUE;

        for (String candidate : candidates) {
            if (candidate == null) continue;
            int dist = distance(target, candidate);
            if (dist < minDistance) {
                minDistance = dist;
                bestMatch = candidate;
            }
        }

        int threshold = target.length() <= 3 ? 1 : 2;
        if (minDistance <= threshold) {
            return bestMatch;
        }
        return null;
    }
}
