package com.generated.app;

public class HebrewMatcher {

    public enum MatchResult {
        PERFECT,
        TYPO_ACCEPTABLE,
        WRONG
    }

    /**
     * Cleans text: trims, lowers, removes punctuation, ignores vowels (niqqud).
     */
    public static String clean(String s) {
        if (s == null) return "";
        // remove niqqud (unicode 0591 to 05C7)
        String cleaned = s.replaceAll("[\u0591-\u05C7]", "");
        // remove punctuation, quotes, double spaces
        cleaned = cleaned.replaceAll("[\\.,\\?!\\-\"'\״\׳\n\r]", " ");
        cleaned = cleaned.replaceAll("\\s+", " ").trim();
        return cleaned.toLowerCase();
    }

    /**
     * Matches the user input against allowed synonyms.
     */
    public static MatchResult checkAnswer(String userInput, String standardAnswer) {
        String cleanUser = clean(userInput);
        if (cleanUser.isEmpty()) {
            return MatchResult.WRONG;
        }

        String[] synonyms = standardAnswer.split("[,;/]");
        boolean nearMatchFound = false;

        for (String rawSyn : synonyms) {
            String syn = clean(rawSyn);
            if (syn.isEmpty()) continue;

            // Direct equal
            if (cleanUser.equals(syn)) {
                return MatchResult.PERFECT;
            }

            // Prefix variations: 'ל' (infinitive prefix) or 'ה' (definite article)
            if (cleanUser.length() > 3 && syn.length() > 3) {
                String uNoPrefix = (cleanUser.startsWith("ל") || cleanUser.startsWith("ה")) ? cleanUser.substring(1) : cleanUser;
                String sNoPrefix = (syn.startsWith("ל") || syn.startsWith("ה")) ? syn.substring(1) : syn;
                if (uNoPrefix.equals(sNoPrefix)) {
                    return MatchResult.PERFECT;
                }
            }

            // Typo tolerance: Levenshtein distance <= 1 for length >= 4
            int dist = levenshtein(cleanUser, syn);
            if (syn.length() >= 4 && dist <= 1) {
                nearMatchFound = true;
            } else if (syn.length() >= 7 && dist <= 2) {
                nearMatchFound = true;
            }
        }

        if (nearMatchFound) {
            return MatchResult.TYPO_ACCEPTABLE;
        }

        return MatchResult.WRONG;
    }

    public static int levenshtein(String a, String b) {
        int[] costs = new int[b.length() + 1];
        for (int j = 0; j < costs.length; j++)
            costs[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= b.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]),
                        a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[b.length()];
    }
}
