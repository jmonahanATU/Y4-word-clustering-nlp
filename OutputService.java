package ie.atu.sw;

import java.io.IOException;
import java.util.List;

/**
 * Interface for handling file output operations.
 * Defines methods for writing clustering results to files.
 *
 * @author Josh Monahan
 * @version 1.0
 * @since JDK22
 */
public interface OutputService {
	  /**
	    * Writes clustering results to specified output file.
	    *
	    * @param results list of clustering results to write
	    * @param outputFile path to output file
	    * @param searchWord original word that was searched
	    * @throws IOException if writing to file fails
	    */
    void writeResults(List<String> results, String outputFile, String searchWord) throws IOException;


}
