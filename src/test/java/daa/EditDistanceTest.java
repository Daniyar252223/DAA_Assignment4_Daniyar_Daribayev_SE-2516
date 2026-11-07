package daa;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EditDistanceTest {

    private final List<EditDistanceAlgorithm> algorithms =
            List.of(
                    new RecursiveEditDistance(),
                    new TableEditDistance(),
                    new TwoRowsEditDistance()
            );

    @Test
    void referenceValuesMustBeCorrect() {

        for (EditDistanceAlgorithm algorithm : algorithms) {

            assertEquals(
                    3,
                    algorithm.distance("kitten", "sitting")
            );

            assertEquals(
                    2,
                    algorithm.distance("flaw", "lawn")
            );

            assertEquals(
                    5,
                    algorithm.distance("intention", "execution")
            );

            assertEquals(
                    3,
                    algorithm.distance("", "abc")
            );

            assertEquals(
                    0,
                    algorithm.distance("abc", "abc")
            );
        }
    }

    @Test
    void emptyStringsMustWork() {

        for (EditDistanceAlgorithm algorithm : algorithms) {

            assertEquals(0, algorithm.distance("", ""));

            assertEquals(4, algorithm.distance("", "data"));

            assertEquals(4, algorithm.distance("data", ""));
        }
    }

    @Test
    void allVersionsMustAgreeOnOneHundredRandomInputs() {

        Random random = new Random(42);

        String alphabet = "ABCD";

        for (int test = 0; test < 100; test++) {

            String a = randomString(
                    random,
                    alphabet,
                    random.nextInt(7)
            );

            String b = randomString(
                    random,
                    alphabet,
                    random.nextInt(7)
            );

            int expected = algorithms.get(1).distance(a, b);

            for (EditDistanceAlgorithm algorithm : algorithms) {

                assertEquals(
                        expected,
                        algorithm.distance(a, b),
                        "Different answer for strings: " + a + " and " + b
                );
            }
        }
    }

    private String randomString(
            Random random,
            String alphabet,
            int length
    ) {

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < length; i++) {

            result.append(
                    alphabet.charAt(
                            random.nextInt(alphabet.length())
                    )
            );
        }

        return result.toString();
    }
}