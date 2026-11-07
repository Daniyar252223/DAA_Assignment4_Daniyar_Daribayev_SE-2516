package daa;

import java.util.Objects;

public class TwoRowsEditDistance implements EditDistanceAlgorithm {

    @Override
    public String name() {
        return "TwoRows";
    }

    @Override
    public int distance(String a, String b, OperationCounter counter) {
        Objects.requireNonNull(a);
        Objects.requireNonNull(b);
        Objects.requireNonNull(counter);

        int n = a.length();
        int m = b.length();

        int[] previous = new int[m + 1];
        int[] current = new int[m + 1];

        for (int j = 0; j <= m; j++) {
            previous[j] = j;
        }

        for (int i = 1; i <= n; i++) {

            current[0] = i;

            for (int j = 1; j <= m; j++) {

                counter.increment();

                int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;

                current[j] = Math.min(
                        Math.min(
                                previous[j] + 1,
                                current[j - 1] + 1
                        ),
                        previous[j - 1] + cost
                );
            }

            int[] temp = previous;
            previous = current;
            current = temp;
        }

        return previous[m];
    }
}