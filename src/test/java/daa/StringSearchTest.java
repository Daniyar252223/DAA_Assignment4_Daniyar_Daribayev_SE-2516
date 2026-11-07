package daa;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StringSearchTest {

    private final NaiveSearch naive = new NaiveSearch();

    private final KmpSearch kmp = new KmpSearch();

    @Test
    void lpsReferenceArraysMustBeCorrect() {

        assertArrayEquals(
                new int[]{0, 1, 2, 0, 1, 2, 3, 3},
                kmp.lps("AAACAAAA")
        );

        assertArrayEquals(
                new int[]{0, 0, 1, 2, 0, 1, 2, 3, 4},
                kmp.lps("ABABCABAB")
        );

        assertArrayEquals(
                new int[0],
                kmp.lps("")
        );
    }

    @Test
    void referenceSearchResultsMustBeCorrect() {

        assertEquals(
                List.of(10),
                kmp.search(
                        "ABABDABACDABABCABAB",
                        "ABABCABAB"
                )
        );

        assertEquals(
                List.of(0, 1, 2, 3),
                naive.search("AAAAA", "AA")
        );

        assertEquals(
                List.of(0, 1, 2, 3),
                kmp.search("AAAAA", "AA")
        );

        assertEquals(
                List.of(0, 2, 4),
                naive.search("ABABABA", "ABA")
        );

        assertEquals(
                List.of(0, 2, 4),
                kmp.search("ABABABA", "ABA")
        );
    }

    @Test
    void emptyPatternMustThrowException() {

        assertThrows(
                IllegalArgumentException.class,
                () -> naive.search("ABC", "")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> kmp.search("ABC", "")
        );
    }

    @Test
    void longerPatternMustReturnEmptyList() {

        assertEquals(
                List.of(),
                naive.search("AB", "ABCD")
        );

        assertEquals(
                List.of(),
                kmp.search("AB", "ABCD")
        );
    }

    @Test
    void bothAlgorithmsMustAgreeOnOneHundredRandomTexts() {

        Random random = new Random(42);

        String alphabet = "ABCD";

        for (int test = 0; test < 100; test++) {

            String text = randomString(
                    random,
                    alphabet,
                    random.nextInt(31)
            );

            int patternLength = 1 + random.nextInt(10);

            String pattern = randomString(
                    random,
                    alphabet,
                    patternLength
            );

            assertEquals(
                    naive.search(text, pattern),
                    kmp.search(text, pattern),
                    "Different matches for text: "
                            + text + " and pattern: " + pattern
            );
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