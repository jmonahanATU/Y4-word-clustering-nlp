package ie.atu.sw;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * Manages word embeddings and vector operations.
 * Loads and provides access to word vectors.
 *
 * @author Josh monahan
 * @version 1.0
 * @since JDK22
 */
public class WordEmbedder implements EmbeddingService {
    private Map<String, double[]> embeddings = new HashMap<>();
    
    /**
     * Loads word embeddings from a file.
     *
     * @param filename path to embeddings file
     * @throws IOException if file cannot be read
     */
    public void loadEmbeddings(String filename) throws IOException {
        System.out.println(ConsoleColour.YELLOW + "Starting to load embeddings from: " + filename);
        
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filename))) {
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null) {
                if (count % 1000 == 0) {
                    System.out.println("Processed " + count + " words...");
                }
                String[] parts = line.split(",");
                if (parts.length > 1) {
                    String word = parts[0];
                    double[] embedding = new double[parts.length - 1];
                    for (int i = 1; i < parts.length; i++) {
                        embedding[i-1] = Double.parseDouble(parts[i]);
                    }
                    embeddings.put(word, embedding);
                    count++;
                }
            }
            System.out.println(ConsoleColour.GREEN + "Successfully loaded " + count + " word embeddings" + ConsoleColour.RESET);
        } catch (Exception e) {
            System.out.println(ConsoleColour.RED + "Error loading embeddings: " + e.getMessage() + ConsoleColour.RESET);
            throw e;
        }
    }

    /**
     * Gets the embedding vector for a specified word.
     *
     * @param word word to get embedding for
     * @return double array representing word's vector, null if word not found
     */
    public double[] getEmbedding(String word) {
        return embeddings.get(word);
    }

    /**
     * Gets all words in the vocabulary.
     *
     * @return Set of all words in the embedding vocabulary
     */
    public Set<String> getVocabulary() {
        return embeddings.keySet();
    }
}