package io.github.jspinak.brobot.actions.methods.basicactions.find.color;

public class DistSimConversion {

    public static double maxDistance = 441.67;

    public static double convertToSimilarity(double distance) {
        return 1 - distance / maxDistance;
    }

    public static double convertToDistance(double similarity) {
        return (1 - similarity) * maxDistance;
    }

}
