package ie.atu.sw;

import java.util.List;

/**
 * Interface defining clustering service operations.
 * Provides methods for different word clustering algorithms.
 *
 * @author Josh Monahan
 * @version 1.0
 * @since JDK22
 */
public interface ClusteringService {
	/**
     * Builds clusters of similar words using specified algorithm.
     *
     * @param searchWord word to find clusters for
     * @param numThreads number of threads to use
     * @param algorithm clustering algorithm to apply
     * @return List of similar words with their distances
     * @throws Exception if clustering operation fails
     */
    List<String> buildClusters(String searchWord, int numThreads, ClusteringAlgorithm algorithm) throws Exception;
}