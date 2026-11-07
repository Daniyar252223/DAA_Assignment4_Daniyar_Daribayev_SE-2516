package daa;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class BenchmarkRunner {

    private static final long SEED = 42L;

    private static final int[] RECURSIVE_SIZES = {
            2, 4, 6, 8
    };

    private static final int[] DP_SIZES = {
            100, 200, 400, 800, 1600
    };

    private static final int[] SEARCH_SIZES = {
            1000, 2000, 4000, 8000, 16000
    };

    private static final int RUNS = 3;

    public static void main(String[] args) throws IOException {

        System.setProperty("java.awt.headless", "true");

        Path resultsDirectory = Path.of("results");
        Path plotsDirectory = resultsDirectory.resolve("plots");

        Files.createDirectories(plotsDirectory);

        StringBuilder csv = new StringBuilder();

        csv.append(
                "family,algorithm,input,n,m,run,time_ms,operations\n"
        );

        runEditDistanceExperiments(csv);
        runSearchExperiments(csv);

        Path csvPath = resultsDirectory.resolve("results.csv");

        Files.writeString(
                csvPath,
                csv.toString(),
                StandardCharsets.UTF_8
        );

        PlotGenerator.createPlots(csvPath, plotsDirectory);

        System.out.println("Experiment finished.");
        System.out.println("CSV: " + csvPath.toAbsolutePath());
        System.out.println("Plots: " + plotsDirectory.toAbsolutePath());
    }

    private static void runEditDistanceExperiments(
            StringBuilder csv
    ) {

        List<EditDistanceAlgorithm> algorithms = List.of(
                new RecursiveEditDistance(),
                new TableEditDistance(),
                new TwoRowsEditDistance()
        );

        Map<String, String[]> inputs = createEditInputs();

        for (EditDistanceAlgorithm algorithm : algorithms) {

            int[] sizes = algorithm instanceof RecursiveEditDistance
                    ? RECURSIVE_SIZES
                    : DP_SIZES;

            for (int n : sizes) {

                for (String inputType :
                        List.of("random", "all_mismatch")) {

                    String[] pair = inputs.get(
                            n + "_" + inputType
                    );

                    // Warm-up run is not stored in CSV.
                    algorithm.distance(
                            pair[0],
                            pair[1],
                            new OperationCounter()
                    );

                    for (int run = 1; run <= RUNS; run++) {

                        OperationCounter counter =
                                new OperationCounter();

                        long start = System.nanoTime();

                        algorithm.distance(
                                pair[0],
                                pair[1],
                                counter
                        );

                        long end = System.nanoTime();

                        appendRow(
                                csv,
                                "edit",
                                algorithm.name(),
                                inputType,
                                n,
                                n,
                                run,
                                end - start,
                                counter.getCount()
                        );
                    }
                }
            }
        }
    }

    private static Map<String, String[]> createEditInputs() {

        Map<String, String[]> inputs = new LinkedHashMap<>();

        Random random = new Random(SEED);

        List<Integer> allSizes = new ArrayList<>();

        for (int n : RECURSIVE_SIZES) {
            allSizes.add(n);
        }

        for (int n : DP_SIZES) {
            allSizes.add(n);
        }

        for (int n : allSizes) {

            inputs.put(
                    n + "_random",
                    new String[]{
                            randomString(n, random),
                            randomString(n, random)
                    }
            );

            inputs.put(
                    n + "_all_mismatch",
                    new String[]{
                            repeat('A', n),
                            repeat('B', n)
                    }
            );
        }

        return inputs;
    }

    private static void runSearchExperiments(
            StringBuilder csv
    ) {

        NaiveSearch naive = new NaiveSearch();
        KmpSearch kmp = new KmpSearch();

        Map<String, String[]> inputs = createSearchInputs();

        for (int n : SEARCH_SIZES) {

            for (String inputType :
                    List.of("random", "worst_case")) {

                String[] input = inputs.get(
                        n + "_" + inputType
                );

                runSearchAlgorithm(
                        csv,
                        "Naive",
                        inputType,
                        n,
                        counter -> naive.search(
                                input[0],
                                input[1],
                                counter
                        )
                );

                runSearchAlgorithm(
                        csv,
                        "KMP",
                        inputType,
                        n,
                        counter -> kmp.search(
                                input[0],
                                input[1],
                                counter
                        )
                );
            }
        }
    }

    private static Map<String, String[]> createSearchInputs() {

        Map<String, String[]> inputs = new LinkedHashMap<>();

        Random random = new Random(SEED);

        for (int n : SEARCH_SIZES) {

            int randomPatternLength = Math.max(2, n / 4);

            inputs.put(
                    n + "_random",
                    new String[]{
                            randomString(n, random),
                            randomString(randomPatternLength, random)
                    }
            );

            int worstPatternLength = Math.max(2, n / 2);

            String text = repeat('A', n);

            String pattern =
                    repeat('A', worstPatternLength - 1) + "B";

            inputs.put(
                    n + "_worst_case",
                    new String[]{text, pattern}
            );
        }

        return inputs;
    }

    private static void runSearchAlgorithm(
            StringBuilder csv,
            String algorithmName,
            String inputType,
            int textLength,
            SearchWithCounter search
    ) {

        // Warm-up run is not stored in CSV.
        search.run(new OperationCounter());

        for (int run = 1; run <= RUNS; run++) {

            OperationCounter counter =
                    new OperationCounter();

            long start = System.nanoTime();

            search.run(counter);

            long end = System.nanoTime();

            int patternLength = inputType.equals("worst_case")
                    ? Math.max(2, textLength / 2)
                    : Math.max(2, textLength / 4);

            appendRow(
                    csv,
                    "search",
                    algorithmName,
                    inputType,
                    textLength,
                    patternLength,
                    run,
                    end - start,
                    counter.getCount()
            );
        }
    }

    private static void appendRow(
            StringBuilder csv,
            String family,
            String algorithm,
            String input,
            int n,
            int m,
            int run,
            long elapsedNanos,
            long operations
    ) {

        double timeMs = elapsedNanos / 1_000_000.0;

        csv.append(
                String.format(
                        java.util.Locale.US,
                        "%s,%s,%s,%d,%d,%d,%.6f,%d%n",
                        family,
                        algorithm,
                        input,
                        n,
                        m,
                        run,
                        timeMs,
                        operations
                )
        );
    }

    private static String randomString(
            int length,
            Random random
    ) {

        String alphabet = "ABCD";

        StringBuilder result =
                new StringBuilder(length);

        for (int i = 0; i < length; i++) {

            result.append(
                    alphabet.charAt(
                            random.nextInt(alphabet.length())
                    )
            );
        }

        return result.toString();
    }

    private static String repeat(
            char character,
            int length
    ) {

        return String.valueOf(character).repeat(length);
    }

    @FunctionalInterface
    private interface SearchWithCounter {

        List<Integer> run(OperationCounter counter);
    }
}