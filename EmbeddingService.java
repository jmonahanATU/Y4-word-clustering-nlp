package ie.atu.sw;

import java.io.*;
import java.util.*;

/**
 * Service interface for word embedding operations.
 * Handles loading and managing word vector embeddings.
 *
 * @author Josh monahan
 * @version 1.0
 * @since JDK22
 */
public interface EmbeddingService {
	/**
     * Loads word embeddings from specified file.
     *
     * @param path the file path containing word embeddings
     * @throws IOException if file cannot be read
     */
	void loadEmbeddings(String path) throws IOException;
	

    /**
     * Returns the embedding vector for a word.
     *
     * @param word the word to get embedding for
     * @return double array representing word's vector
     */
    double[] getEmbedding(String word);
    Set<String> getVocabulary();

}
