package daa;

import java.util.Objects;

public class RecursiveEditDistance implements EditDistanceAlgorithm {

    @Override
    public String name() {
        return "Recursive";
    }

    @Override
    public int distance(String a, String b, OperationCounter counter) {
        Objects.requireNonNull(a);
        Objects.requireNonNull(b);
        Objects.requireNonNull(counter);

        return solve(a, a.length(), b, b.length(), counter);
    }

    private int solve(String a, int i, String b, int j,
                      OperationCounter counter) {

        counter.increment();

        if (i == 0) {
            return j;
        }

        if (j == 0) {
            return i;
        }

        int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;

        int deletion = solve(a, i - 1, b, j, counter) + 1;

        int insertion = solve(a, i, b, j - 1, counter) + 1;

        int replacement = solve(a, i - 1, b, j - 1, counter) + cost;

        return Math.min(
                Math.min(deletion, insertion),
                replacement
        );
    }
}