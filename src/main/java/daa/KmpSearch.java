package daa;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class KmpSearch {

    public int[] lps(String pattern) {
        Objects.requireNonNull(pattern);

        return buildLps(pattern, new OperationCounter());
    }

    private int[] buildLps(
            String pattern,
            OperationCounter counter
    ) {
        int[] lps = new int[pattern.length()];

        int length = 0;
        int i = 1;

        while (i < pattern.length()) {

            counter.increment();

            if (pattern.charAt(i) == pattern.charAt(length)) {

                length++;
                lps[i] = length;
                i++;

            } else if (length > 0) {

                length = lps[length - 1];

            } else {

                lps[i] = 0;
                i++;
            }
        }

        return lps;
    }

    public List<Integer> search(String text, String pattern) {
        return search(text, pattern, new OperationCounter());
    }

    public List<Integer> search(
            String text,
            String pattern,
            OperationCounter counter
    ) {
        Objects.requireNonNull(text);
        Objects.requireNonNull(pattern);
        Objects.requireNonNull(counter);

        if (pattern.isEmpty()) {
            throw new IllegalArgumentException("Pattern cannot be empty");
        }

        List<Integer> matches = new ArrayList<>();

        int n = text.length();
        int m = pattern.length();

        if (m > n) {
            return matches;
        }

        int[] lps = buildLps(pattern, counter);

        int i = 0;
        int j = 0;

        while (i < n) {

            counter.increment();

            if (text.charAt(i) == pattern.charAt(j)) {

                i++;
                j++;

                if (j == m) {
                    matches.add(i - j);
                    j = lps[j - 1];
                }

            } else if (j > 0) {

                j = lps[j - 1];

            } else {

                i++;
            }
        }

        return matches;
    }
}
