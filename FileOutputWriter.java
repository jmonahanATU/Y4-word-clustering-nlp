package ie.atu.sw;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * FileOutputWriter class for handling file output operations.
 * Implements OutputService interface for result file writing.
 *
 * @author Josh Monahan
 * @version 1.0
 * @since JDK22
 */
public class FileOutputWriter implements OutputService{
	/**
	 * Writes clustering results to output file.
	 *
	 * @param results list of clustering results to write
	 * @throws IOException if writing to file fails
	 */
	@Override
    public void writeResults(List<String> results, String outputFile, String searchWord) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            writer.println("Search Results for: " + searchWord);
            writer.println("----------------------------------------");
            for (String result : results) {
                writer.println(result);
            }
            System.out.println(ConsoleColour.GREEN + "Results written to " + outputFile + ConsoleColour.RESET);
        }
    }
}