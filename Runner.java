package ie.atu.sw;

/**
 * Main runner class for the Word Clustering Application.
 * Initializes and starts the word clustering system using virtual threads.
 *
 * @author Josh Monahan
 * @version 1.0
 * @since JDK22
 */

//Main Runner class using the provided code
public class Runner {
	 /**
     * Entry point for the Word Clustering Application.
     * Creates and starts the menu system.
     *
     * @param args Command line arguments (not used)
     * @throws Exception if application fails to start
     */
 public static void main(String[] args) throws Exception {
     Menu menu = new Menu();
     menu.start();
 }

 public static void printProgress(int index, int total) {
     if (index > total) return;
     int size = 50;
     char done = '█';
     char todo = '░';
     
     int complete = (100 * index) / total;
     int completeLen = size * complete / 100;
     
     StringBuilder sb = new StringBuilder();
     sb.append("[");
     for (int i = 0; i < size; i++) {
         sb.append((i < completeLen) ? done : todo);
     }
     
     System.out.print("\r" + sb + "] " + complete + "%");
     
     if (index == total) System.out.println("\n");
 }
}