package daa;

public interface EditDistanceAlgorithm {
    String name();

    int distance(String a, String b, OperationCounter counter);

    default int distance(String a, String b) {
        return distance(a, b, new OperationCounter());
    }
}