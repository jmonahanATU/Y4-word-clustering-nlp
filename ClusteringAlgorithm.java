package ie.atu.sw;

/**
 * Represents available clustering algorithms.
 * Provides options for different word clustering approaches.
 *
 * @author Josh Monahan
 * @version 1.0
 * @since JDK22
 */
public enum ClusteringAlgorithm {
	 /** Direct similarity comparison */
    NEAREST_NEIGHBOR("Nearest Neighbor"),
    /** Centroid-based clustering */
    K_MEANS("K-Means"),
    /** Hierarchical relationship clustering */
    HIERARCHICAL("Hierarchical");

    private final String description;

    ClusteringAlgorithm(String description) {
        this.description = description;
    }

    /**
     * Returns human-readable description of algorithm.
     *
     * @return String description of the algorithm
     */
    @Override
    public String toString() {
        return description;
    }
}
