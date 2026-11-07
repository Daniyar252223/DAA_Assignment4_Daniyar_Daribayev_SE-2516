package daa;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NaiveSearch {

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

        for (int start = 0; start <= n - m; start++) {

            int j = 0;

            while (j < m) {
                counter.increment();

                if (text.charAt(start + j) != pattern.charAt(j)) {
                    break;
                }

                j++;
            }

            if (j == m) {
                matches.add(start);
            }
        }

        return matches;
    }
}