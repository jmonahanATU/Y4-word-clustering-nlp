package ie.atu.sw;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Builds word clusters using different clustering algorithms.
 * Supports Nearest Neighbor, K-Means, and Hierarchical clustering.
 *
 * @author Josh monahan
 * @version 1.0
 * @since JDK22
 */
public class ClusterBuilder implements ClusteringService {
	
	private final EmbeddingService embeddingService;

	public ClusterBuilder(EmbeddingService embeddingService) {
		this.embeddingService = embeddingService;
	}

	 /**
     * Creates clusters based on specified algorithm.
     *
     * @param searchWord the word to find clusters for
     * @param numThreads number of virtual threads to use
     * @param algorithm clustering algorithm to apply
     * @return List of similar words with distances
     * @throws Exception if clustering fails
     */
	@Override
	public List<String> buildClusters(String searchWord, int numThreads, ClusteringAlgorithm algorithm)
			throws Exception {
		switch (algorithm) {
		case NEAREST_NEIGHBOR -> {
			return buildNearestNeighbor(searchWord, numThreads);
		}
		case K_MEANS -> {
			return buildKMeans(searchWord, numThreads);
		}
		case HIERARCHICAL -> {
			return buildHierarchical(searchWord, numThreads);
		}
		default -> throw new IllegalArgumentException("Unknown algorithm: " + algorithm);
		}
	}

	/**
	 * Implements Nearest Neighbor clustering algorithm.
	 *
	 * @param searchWord word to find neighbors for
	 * @param numThreads number of threads to use
	 * @return List of nearest neighbors with distances
	 * @throws Exception if clustering fails
	 */
	private List<String> buildNearestNeighbor(String searchWord, int numThreads) throws Exception {
		double[] searchEmbedding = embeddingService.getEmbedding(searchWord);
		if (searchEmbedding == null) {
			throw new IllegalArgumentException("Search word not found in embeddings");
		}

		List<WordDistance> distances = Collections.synchronizedList(new ArrayList<>());

		try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
			List<Future<?>> futures = new ArrayList<>();

			for (String word : embeddingService.getVocabulary()) {
				if (word.equals(searchWord))
					continue;

				futures.add(executor.submit(() -> {
					double[] embedding = embeddingService.getEmbedding(word);
					double distance = computeDistance(searchEmbedding, embedding);
					distances.add(new WordDistance(word, distance));
				}));
			}

			for (Future<?> future : futures) {
				future.get();
			}
		}

		return distances.stream().sorted(Comparator.comparingDouble(WordDistance::distance)).limit(5)
				.map(wd -> String.format("%s (distance: %.4f) [Nearest Neighbor]", wd.word(), wd.distance()))
				.collect(Collectors.toList());
	}

	
	/**
	 * Implements K-Means clustering algorithm.
	 *
	 * @param searchWord word to cluster around
	 * @param numThreads number of threads to use
	 * @return List of words in same cluster with distances
	 * @throws Exception if clustering fails
	 */
	private List<String> buildKMeans(String searchWord, int numThreads) throws Exception {
	    double[] searchEmbedding = embeddingService.getEmbedding(searchWord);
	    if (searchEmbedding == null) {
	        throw new IllegalArgumentException("Search word not found in embeddings");
	    }

	    // Increase K to get more diverse clusters
	    final int K = 10;
	    List<double[]> centroids = initializeCentroids(K);
	    Map<Integer, List<String>> clusters = new HashMap<>();
	    boolean converged = false;
	    int maxIterations = 100;
	    int iteration = 0;

	    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
	        while (!converged && iteration < maxIterations) {
	            // Initialize clusters
	            clusters.clear();
	            for (int i = 0; i < K; i++) {
	                clusters.put(i, new ArrayList<>());
	            }

	            // Assign words to nearest centroid
	            List<Future<?>> futures = new ArrayList<>();
	            Set<String> vocabulary = embeddingService.getVocabulary();

	            for (String word : vocabulary) {
	                futures.add(executor.submit(() -> {
	                    double[] embedding = embeddingService.getEmbedding(word);
	                    int nearestCentroid = findNearestCentroid(embedding, centroids);
	                    synchronized (clusters) {
	                        clusters.get(nearestCentroid).add(word);
	                    }
	                }));
	            }

	            for (Future<?> future : futures) {
	                future.get();
	            }

	            // Update centroids and check for convergence
	            converged = !updateCentroids(centroids, clusters);
	            iteration++;
	        }
	    }

	    // Find which cluster contains our search word
	    int searchCluster = -1;
	    for (Map.Entry<Integer, List<String>> entry : clusters.entrySet()) {
	        if (entry.getValue().contains(searchWord)) {
	            searchCluster = entry.getKey();
	            break;
	        }
	    }

	    if (searchCluster == -1) {
	        return Collections.emptyList();
	    }

	    // Get the most representative words from the cluster
	    return clusters.get(searchCluster).stream()
	        .filter(word -> !word.equals(searchWord))
	        .map(word -> {
	            double[] embedding = embeddingService.getEmbedding(word);
	            double distance = computeDistance(searchEmbedding, embedding);
	            return new WordDistance(word, distance);
	        })
	        .sorted(Comparator.comparingDouble(WordDistance::distance))
	        .limit(5)
	        .map(wd -> String.format("%s (distance: %.4f) [K-Means Cluster]", wd.word(), wd.distance()))
	        .collect(Collectors.toList());
	}
	
	
	/**
	* Implements hierarchical clustering algorithm to find similar words.
	* Groups words into hierarchical clusters based on their vector similarities.
	*
	* @param searchWord word to find clusters for
	* @param numThreads number of threads to use for parallel processing
	* @return List of words from the most relevant cluster with distances
	* @throws Exception if clustering operation fails
	*/
	private List<String> buildHierarchical(String searchWord, int numThreads) throws Exception {
		double[] searchEmbedding = embeddingService.getEmbedding(searchWord);
		if (searchEmbedding == null) {
			throw new IllegalArgumentException("Search word not found in embeddings");
		}

		 // Get nearest 1000 words to our search word first
	    List<WordDistance> nearestWords = embeddingService.getVocabulary().stream()
	        .map(word -> new WordDistance(word, 
	            computeDistance(searchEmbedding, embeddingService.getEmbedding(word))))
	        .sorted(Comparator.comparingDouble(WordDistance::distance))
	        .limit(500)
	        .collect(Collectors.toList());

	    // Create initial clusters from these words
	    Map<String, List<String>> clusters = new HashMap<>();
	    for (WordDistance wd : nearestWords) {
	        clusters.put(wd.word(), new ArrayList<>(List.of(wd.word())));
	    }
	    
	    
		try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
			// Merge closest clusters until we have desired number
			while (clusters.size() > 5) {
				double minDistance = Double.MAX_VALUE;
				String cluster1 = null, cluster2 = null;

				for (String c1 : clusters.keySet()) {
					for (String c2 : clusters.keySet()) {
						if (c1.equals(c2))
							continue;

						double dist = calculateClusterDistance(clusters.get(c1), clusters.get(c2), embeddingService);

						if (dist < minDistance) {
							minDistance = dist;
							cluster1 = c1;
							cluster2 = c2;
						}
					}
				}

				// Merge the closest clusters
				if (cluster1 != null && cluster2 != null) {
					List<String> merged = new ArrayList<>(clusters.get(cluster1));
					merged.addAll(clusters.get(cluster2));
					clusters.remove(cluster2);
					clusters.put(cluster1, merged);
				}
			}
		}

		// Find cluster containing search word
		List<String> targetCluster = null;
		for (List<String> cluster : clusters.values()) {
			if (cluster.contains(searchWord)) {
				targetCluster = cluster;
				break;
			}
		}

		if (targetCluster == null) {
			return Collections.emptyList();
		}

		// Return closest words from the target cluster
		return targetCluster.stream().filter(word -> !word.equals(searchWord)).map(word -> {
			double[] embedding = embeddingService.getEmbedding(word);
			double distance = computeDistance(searchEmbedding, embedding);
			return new WordDistance(word, distance);
		}).sorted(Comparator.comparingDouble(WordDistance::distance)).limit(5)
				.map(wd -> String.format("%s (distance: %.4f) [Hierarchical Group]", wd.word(), wd.distance()))
				.collect(Collectors.toList());
	}
	
	/**
	 * Initializes random centroids for K-means clustering.
	 *
	 * @param k number of centroids to initialize
	 * @return list of initial centroid vectors
	 */
	private List<double[]> initializeCentroids(int k) {
        List<double[]> centroids = new ArrayList<>();
        List<String> words = new ArrayList<>(embeddingService.getVocabulary());
        Random random = new Random();
        
        for (int i = 0; i < k; i++) {
            String randomWord = words.get(random.nextInt(words.size()));
            centroids.add(embeddingService.getEmbedding(randomWord).clone());
        }
        return centroids;
    }
	
	/**
	 * Helper method to find nearest centroid for K-Means clustering.
	 *
	 * @param embedding word vector to check
	 * @param centroids list of centroid vectors
	 * @return index of nearest centroid
	 */
	 private int findNearestCentroid(double[] embedding, List<double[]> centroids) {
	        int nearest = 0;
	        double minDistance = Double.MAX_VALUE;
	        
	        for (int i = 0; i < centroids.size(); i++) {
	            double distance = computeDistance(embedding, centroids.get(i));
	            if (distance < minDistance) {
	                minDistance = distance;
	                nearest = i;
	            }
	        }
	        return nearest;
	    }


	 /**
	  * Updates centroid positions in K-Means clustering.
	  *
	  * @param centroids current centroid positions
	  * @param clusters current word clusters
	  * @return true if centroids changed position
	  */
	 private boolean updateCentroids(List<double[]> centroids, Map<Integer, List<String>> clusters) {
	        boolean changed = false;
	        
	        for (int i = 0; i < centroids.size(); i++) {
	            List<String> clusterWords = clusters.get(i);
	            if (clusterWords.isEmpty()) continue;
	            
	            double[] newCentroid = new double[centroids.get(i).length];
	            for (String word : clusterWords) {
	                double[] embedding = embeddingService.getEmbedding(word);
	                for (int j = 0; j < embedding.length; j++) {
	                    newCentroid[j] += embedding[j];
	                }
	            }
	            
	            for (int j = 0; j < newCentroid.length; j++) {
	                newCentroid[j] /= clusterWords.size();
	            }
	            
	            if (!Arrays.equals(centroids.get(i), newCentroid)) {
	                changed = true;
	                centroids.set(i, newCentroid);
	            }
	        }
	        
	        return changed;
	    }

	 /**
	  * Calculates distance between two clusters.
	  *
	  * @param cluster1 first cluster of words
	  * @param cluster2 second cluster of words
	  * @param embeddingService service to get word embeddings
	  * @return average distance between clusters
	  */
	private double calculateClusterDistance(List<String> cluster1, List<String> cluster2,
			EmbeddingService embeddingService) {
		double totalDistance = 0;
		int pairs = 0;

		for (String word1 : cluster1) {
			double[] emb1 = embeddingService.getEmbedding(word1);
			for (String word2 : cluster2) {
				double[] emb2 = embeddingService.getEmbedding(word2);
				totalDistance += computeDistance(emb1, emb2);
				pairs++;
			}
		}

		return pairs > 0 ? totalDistance / pairs : Double.MAX_VALUE;
	}

	/**
	 * Calculates Euclidean distance between two word vectors.
	 *
	 * @param v1 first word vector
	 * @param v2 second word vector
	 * @return distance between vectors
	 */
	private double computeDistance(double[] v1, double[] v2) {
		double sum = 0.0;
		for (int i = 0; i < v1.length; i++) {
			sum += Math.pow(v1[i] - v2[i], 2);
		}
		return Math.sqrt(sum);
	}
}

record WordDistance(String word, double distance) {
}