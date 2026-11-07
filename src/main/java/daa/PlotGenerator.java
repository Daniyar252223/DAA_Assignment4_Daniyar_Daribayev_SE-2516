package daa;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class PlotGenerator {

    private static final int WIDTH = 1100;
    private static final int HEIGHT = 680;

    private static final int LEFT = 115;
    private static final int RIGHT = 300;
    private static final int TOP = 90;
    private static final int BOTTOM = 105;

    private static final Color[] COLORS = {
            new Color(35, 92, 168),
            new Color(220, 90, 45),
            new Color(35, 145, 95),
            new Color(150, 75, 160)
    };

    private PlotGenerator() {
    }

    public static void createPlots(
            Path csvPath,
            Path outputDirectory
    ) throws IOException {

        List<ResultRow> rows = readCsv(csvPath);

        Files.createDirectories(outputDirectory);

        // Plot 1: Recursive exponential growth.
        createChart(
                outputDirectory.resolve(
                        "recursive_exponential_growth.png"
                ),
                "Recursive Edit Distance: Exponential Growth",
                "String length n (characters)",
                "Recursive calls (count)",
                List.of(
                        makeSeries(
                                rows,
                                "edit",
                                "Recursive",
                                "all_mismatch",
                                "operations"
                        )
                ),
                true
        );

        // Plot 2: Full table and two-row DP.
        createChart(
                outputDirectory.resolve(
                        "edit_distance_dp_operations.png"
                ),
                "Edit Distance DP: Number of Computed Cells",
                "String length n (characters)",
                "Computed DP cells (count)",
                List.of(
                        makeSeries(
                                rows,
                                "edit",
                                "Table",
                                "random",
                                "operations"
                        ),
                        makeSeries(
                                rows,
                                "edit",
                                "TwoRows",
                                "random",
                                "operations"
                        )
                ),
                false
        );

        // Plot 3: Naive and KMP comparisons.
        createChart(
                outputDirectory.resolve(
                        "string_search_worst_case.png"
                ),
                "String Search: Worst-Case Character Comparisons",
                "Text length n (characters)",
                "Character comparisons (count)",
                List.of(
                        makeSeries(
                                rows,
                                "search",
                                "Naive",
                                "worst_case",
                                "operations"
                        ),
                        makeSeries(
                                rows,
                                "search",
                                "KMP",
                                "worst_case",
                                "operations"
                        )
                ),
                true
        );

        // Plot 4: Runtime comparison.
        createChart(
                outputDirectory.resolve(
                        "string_search_runtime.png"
                ),
                "String Search: Median Runtime on Worst-Case Input",
                "Text length n (characters)",
                "Median runtime (milliseconds)",
                List.of(
                        makeSeries(
                                rows,
                                "search",
                                "Naive",
                                "worst_case",
                                "time"
                        ),
                        makeSeries(
                                rows,
                                "search",
                                "KMP",
                                "worst_case",
                                "time"
                        )
                ),
                true
        );
    }

    private static List<ResultRow> readCsv(Path csvPath)
            throws IOException {

        List<String> lines = Files.readAllLines(
                csvPath,
                StandardCharsets.UTF_8
        );

        List<ResultRow> rows = new ArrayList<>();

        for (int i = 1; i < lines.size(); i++) {

            String line = lines.get(i).trim();

            if (line.isEmpty()) {
                continue;
            }

            String[] parts = line.split(",");

            rows.add(
                    new ResultRow(
                            parts[0],
                            parts[1],
                            parts[2],
                            Integer.parseInt(parts[3]),
                            Double.parseDouble(parts[6]),
                            Long.parseLong(parts[7])
                    )
            );
        }

        return rows;
    }

    private static Series makeSeries(
            List<ResultRow> rows,
            String family,
            String algorithm,
            String input,
            String metric
    ) {

        Map<Integer, List<Double>> valuesBySize =
                new TreeMap<>();

        for (ResultRow row : rows) {

            if (row.family.equals(family)
                    && row.algorithm.equals(algorithm)
                    && row.input.equals(input)) {

                double value = metric.equals("time")
                        ? row.timeMs
                        : row.operations;

                valuesBySize
                        .computeIfAbsent(
                                row.n,
                                key -> new ArrayList<>()
                        )
                        .add(value);
            }
        }

        List<Point> points = new ArrayList<>();

        for (Map.Entry<Integer, List<Double>> entry
                : valuesBySize.entrySet()) {

            points.add(
                    new Point(
                            entry.getKey(),
                            median(entry.getValue())
                    )
            );
        }

        String label = algorithm + " / " + input;

        return new Series(label, points);
    }

    private static double median(List<Double> values) {

        List<Double> sorted = new ArrayList<>(values);

        sorted.sort(Comparator.naturalOrder());

        int middle = sorted.size() / 2;

        if (sorted.size() % 2 == 1) {
            return sorted.get(middle);
        }

        return (
                sorted.get(middle - 1)
                        + sorted.get(middle)
        ) / 2.0;
    }

    private static void createChart(
            Path output,
            String title,
            String xLabel,
            String yLabel,
            List<Series> seriesList,
            boolean logarithmicY
    ) throws IOException {

        List<Point> allPoints = new ArrayList<>();

        for (Series series : seriesList) {
            allPoints.addAll(series.points);
        }

        allPoints.removeIf(
                point -> !Double.isFinite(point.y)
                        || (logarithmicY && point.y <= 0)
        );

        if (allPoints.isEmpty()) {
            throw new IllegalStateException(
                    "There is no data for chart: " + title
            );
        }

        int minX = allPoints.stream()
                .mapToInt(point -> point.x)
                .min()
                .orElse(0);

        int maxX = allPoints.stream()
                .mapToInt(point -> point.x)
                .max()
                .orElse(1);

        if (minX == maxX) {
            maxX = minX + 1;
        }

        double minY = allPoints.stream()
                .mapToDouble(point -> point.y)
                .min()
                .orElse(1);

        double maxY = allPoints.stream()
                .mapToDouble(point -> point.y)
                .max()
                .orElse(1);

        if (!logarithmicY) {

            minY = 0;
            maxY *= 1.10;

            if (maxY == 0) {
                maxY = 1;
            }

        } else {

            minY = Math.pow(
                    10,
                    Math.floor(Math.log10(minY))
            );

            maxY = Math.pow(
                    10,
                    Math.ceil(Math.log10(maxY))
            );

            if (minY == maxY) {
                maxY *= 10;
            }
        }

        BufferedImage image = new BufferedImage(
                WIDTH,
                HEIGHT,
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D g = image.createGraphics();

        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        int plotWidth = WIDTH - LEFT - RIGHT;
        int plotHeight = HEIGHT - TOP - BOTTOM;

        int plotBottom = TOP + plotHeight;

        g.setFont(
                new Font("SansSerif", Font.BOLD, 23)
        );

        g.setColor(Color.BLACK);
        g.drawString(title, LEFT, 42);

        g.setFont(
                new Font("SansSerif", Font.PLAIN, 15)
        );

        g.setColor(new Color(225, 225, 225));

        if (logarithmicY) {

            int firstPower = (int) Math.floor(
                    Math.log10(minY)
            );

            int lastPower = (int) Math.ceil(
                    Math.log10(maxY)
            );

            for (int power = firstPower;
                 power <= lastPower;
                 power++) {

                double value = Math.pow(10, power);

                int y = mapY(
                        value,
                        minY,
                        maxY,
                        plotBottom,
                        plotHeight,
                        true
                );

                g.drawLine(
                        LEFT,
                        y,
                        LEFT + plotWidth,
                        y
                );

                g.setColor(Color.DARK_GRAY);

                g.drawString(
                        formatNumber(value),
                        LEFT - 70,
                        y + 5
                );

                g.setColor(new Color(225, 225, 225));
            }

        } else {

            for (int tick = 0; tick <= 5; tick++) {

                double value = maxY * tick / 5.0;

                int y = mapY(
                        value,
                        minY,
                        maxY,
                        plotBottom,
                        plotHeight,
                        false
                );

                g.drawLine(
                        LEFT,
                        y,
                        LEFT + plotWidth,
                        y
                );

                g.setColor(Color.DARK_GRAY);

                g.drawString(
                        String.format(
                                java.util.Locale.US,
                                "%,.0f",
                                value
                        ),
                        LEFT - 65,
                        y + 5
                );

                g.setColor(new Color(225, 225, 225));
            }
        }

        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2f));

        g.drawLine(LEFT, TOP, LEFT, plotBottom);

        g.drawLine(
                LEFT,
                plotBottom,
                LEFT + plotWidth,
                plotBottom
        );

        g.setFont(new Font("SansSerif", Font.PLAIN, 13));

        List<Integer> xValues = allPoints.stream()
                .map(point -> point.x)
                .distinct()
                .sorted()
                .toList();

        for (int xValue : xValues) {

            int x = mapX(
                    xValue,
                    minX,
                    maxX,
                    LEFT,
                    plotWidth
            );

            String label = String.valueOf(xValue);

            int labelWidth = g.getFontMetrics()
                    .stringWidth(label);

            g.setColor(Color.DARK_GRAY);

            g.drawString(
                    label,
                    x - labelWidth / 2,
                    plotBottom + 25
            );

            g.setColor(Color.BLACK);

            g.drawLine(
                    x,
                    plotBottom,
                    x,
                    plotBottom + 5
            );
        }

        g.setFont(
                new Font("SansSerif", Font.PLAIN, 17)
        );

        g.drawString(
                xLabel,
                LEFT + plotWidth / 2 - 120,
                HEIGHT - 32
        );

        g.rotate(-Math.PI / 2);

        g.drawString(
                yLabel,
                -TOP - plotHeight / 2 - 100,
                32
        );

        g.rotate(Math.PI / 2);

        for (int s = 0; s < seriesList.size(); s++) {

            Series series = seriesList.get(s);

            Color color = COLORS[s % COLORS.length];

            g.setColor(color);
            g.setStroke(new BasicStroke(3f));

            Point previous = null;

            for (Point point : series.points) {

                if (logarithmicY && point.y <= 0) {
                    continue;
                }

                int x = mapX(
                        point.x,
                        minX,
                        maxX,
                        LEFT,
                        plotWidth
                );

                int y = mapY(
                        point.y,
                        minY,
                        maxY,
                        plotBottom,
                        plotHeight,
                        logarithmicY
                );

                if (previous != null) {

                    int previousX = mapX(
                            previous.x,
                            minX,
                            maxX,
                            LEFT,
                            plotWidth
                    );

                    int previousY = mapY(
                            previous.y,
                            minY,
                            maxY,
                            plotBottom,
                            plotHeight,
                            logarithmicY
                    );

                    g.drawLine(
                            previousX,
                            previousY,
                            x,
                            y
                    );
                }

                g.fillOval(x - 6, y - 6, 12, 12);

                previous = point;
            }

            int legendY = TOP + 35 + s * 34;

            g.setColor(color);
            g.setStroke(new BasicStroke(3f));

            g.drawLine(
                    LEFT + plotWidth + 35,
                    legendY,
                    LEFT + plotWidth + 75,
                    legendY
            );

            g.fillOval(
                    LEFT + plotWidth + 50,
                    legendY - 5,
                    10,
                    10
            );

            g.setColor(Color.BLACK);

            g.drawString(
                    series.label,
                    LEFT + plotWidth + 88,
                    legendY + 5
            );
        }

        if (logarithmicY) {

            g.setColor(Color.DARK_GRAY);

            g.setFont(
                    new Font("SansSerif", Font.ITALIC, 13)
            );

            g.drawString(
                    "Logarithmic Y-axis",
                    LEFT + 10,
                    TOP - 15
            );
        }

        g.dispose();

        ImageIO.write(
                image,
                "png",
                output.toFile()
        );
    }

    private static int mapX(
            int x,
            int minX,
            int maxX,
            int left,
            int width
    ) {

        return left + (int) Math.round(
                (x - minX) * 1.0 / (maxX - minX) * width
        );
    }

    private static int mapY(
            double y,
            double minY,
            double maxY,
            int bottom,
            int height,
            boolean logarithmic
    ) {

        double transformedY = logarithmic
                ? Math.log10(y)
                : y;

        double transformedMin = logarithmic
                ? Math.log10(minY)
                : minY;

        double transformedMax = logarithmic
                ? Math.log10(maxY)
                : maxY;

        double ratio = (
                transformedY - transformedMin
        ) / (
                transformedMax - transformedMin
        );

        return bottom - (int) Math.round(
                ratio * height
        );
    }


    private static String formatNumber(double value) {

        if (value > 0 && value < 1) {
            return String.format(
                    java.util.Locale.US,
                    "%.1f",
                    value
            );
        }

        if (value >= 1_000_000) {
            return String.format(
                    java.util.Locale.US,
                    "%.0fM",
                    value / 1_000_000
            );
        }

        if (value >= 1_000) {
            return String.format(
                    java.util.Locale.US,
                    "%.0fk",
                    value / 1_000
            );
        }

        return String.format(
                java.util.Locale.US,
                "%.0f",
                value
        );
    }

    private record ResultRow(
            String family,
            String algorithm,
            String input,
            int n,
            double timeMs,
            long operations
    ) {
    }

    private record Point(
            int x,
            double y
    ) {
    }

    private record Series(
            String label,
            List<Point> points
    ) {
    }
}