package ie.atu.sw;

import java.io.*;
import java.util.*;

/**
 * Menu controller for Word Clustering Application.
 * Handles user interaction and program flow control.
 *
 * @author Josh monahan
 * @version 1.0
 * @since JDK22
 */
// Menu class to handle user interaction
public class Menu implements MenuService{
    private String embeddingFile;
    private String searchWord;
    private String outputFile = "./out.txt";
    private final OutputService outputWriter = new FileOutputWriter();
    private int numThreads = Runtime.getRuntime().availableProcessors();
    private final Scanner scanner = new Scanner(System.in);
    private final WordEmbedder embedder = new WordEmbedder();
    private final ClusterBuilder builder = new ClusterBuilder(embedder);
    private volatile boolean keepRunning = true;
    private final ProgressService progressReporter = new ProgressReporter();
    private ClusteringAlgorithm selectedAlgorithm = ClusteringAlgorithm.NEAREST_NEIGHBOR;

    /**
     * Displays menu options and processes user input.
     * Continues until user selects exit option.
     */
    public void start() {
        while (keepRunning) {
            showMenu();
            processChoice();
        }
    }

    /**
     * Displays the main menu interface to the user.
     */
    @Override
    public void showMenu() {
        System.out.println(ConsoleColour.CYAN_BOLD);
        System.out.println("************************************************************");
        System.out.println("*     ATU - Dept. of Computer Science & Applied Physics    *");
        System.out.println("*                                                          *");
        System.out.println("*            Word Clustering with Virtual Threads          *");
        System.out.println("*                                                          *");
        System.out.println("************************************************************");
        System.out.println("(1) Specify a Word Embedding File");
        System.out.println("(2) Specify a Search Word");
        System.out.println("(3) Specify an Output File (default: ./out.txt)");
        System.out.println("(4) Configure Threads / Clusters");
        System.out.println("(5) Select Clustering Algorithm");
        System.out.println("(6) Build Clusters");
        System.out.println("(7) Quit");

        System.out.print(ConsoleColour.BLACK_BOLD_BRIGHT);
        System.out.print("Select Option [1-6]>");
        System.out.println();
    }

    /**
     * Processes user's menu choice and executes corresponding action.
     */
    @Override
    public void processChoice() {
        try {
            int choice = Integer.parseInt(scanner.nextLine());
            switch (choice) {
                case 1 -> specifyEmbeddingFile();
                case 2 -> specifySearchWord();
                case 3 -> specifyOutputFile();
                case 4 -> configureThreads();
                case 5 -> selectAlgorithm();
                case 6 -> buildClusters();
                case 7 -> quit();
                default -> System.out.println(ConsoleColour.RED + "Invalid choice!" + ConsoleColour.RESET);
            }
        } catch (NumberFormatException e) {
            System.out.println(ConsoleColour.RED + "Please enter a number between 1-6" + ConsoleColour.RESET);
        } catch (Exception e) {
            System.out.println(ConsoleColour.RED + "Error: " + e.getMessage() + ConsoleColour.RESET);
        }
    }

    /**
     * Handles embedding file specification from user input.
     */
    private void specifyEmbeddingFile() {
        System.out.print(ConsoleColour.WHITE_BOLD + "Enter embedding file path> ");
        embeddingFile = scanner.nextLine().trim();
        
        // Try different paths to find the file
        File file = new File(embeddingFile);
        if (!file.exists()) {
            // Try in the src/ie/atu/sw directory
            file = new File("src/ie/atu/sw/" + embeddingFile);
        }
        if (!file.exists()) {
            // Try in the bin/ie/atu/sw directory
            file = new File("bin/ie/atu/sw/" + embeddingFile);
        }
        
        if (!file.exists()) {
            System.out.println(ConsoleColour.RED + "Error: File not found in any directory: " + embeddingFile);
            System.out.println("Tried locations:");
            System.out.println("- " + new File(embeddingFile).getAbsolutePath());
            System.out.println("- " + new File("src/ie/atu/sw/" + embeddingFile).getAbsolutePath());
            System.out.println("- " + new File("bin/ie/atu/sw/" + embeddingFile).getAbsolutePath() + ConsoleColour.RESET);
            return;
        }
        
        if (!file.canRead()) {
            System.out.println(ConsoleColour.RED + "Error: Cannot read file: " + file.getAbsolutePath() + ConsoleColour.RESET);
            return;
        }
        
        embeddingFile = file.getAbsolutePath(); // Store the full path
        System.out.println(ConsoleColour.GREEN + "Successfully set embedding file to: " + embeddingFile + ConsoleColour.RESET);
    }

    /**
     * Gets search word from user input.
     */
    private void specifySearchWord() {
        System.out.print("Enter search word> ");
        searchWord = scanner.nextLine().trim();
    }

    /**
     * Sets output file path for results.
     */
    private void specifyOutputFile() {
        System.out.print("Enter output file path (default: ./out.txt)> ");
        String input = scanner.nextLine().trim();
        if (!input.isEmpty()) outputFile = input;
    }

    /**
     * Configures number of threads for processing.
     */
    private void configureThreads() {
        System.out.print("Enter number of threads> ");
        try {
            int threads = Integer.parseInt(scanner.nextLine().trim());
            if (threads > 0) numThreads = threads;
            else System.out.println(ConsoleColour.RED + "Please enter a positive number" + ConsoleColour.RESET);
        } catch (NumberFormatException e) {
            System.out.println(ConsoleColour.RED + "Invalid number format" + ConsoleColour.RESET);
        }
    }
    
    /**
    * Allows user to select clustering algorithm to use.
    * Displays available algorithms and processes user selection.
    */
    private void selectAlgorithm() {
        System.out.println("\nAvailable Algorithms:");
        System.out.println("1) Nearest Neighbor");
        System.out.println("2) K-Means");
        System.out.println("3) Hierarchical");
        
        System.out.print("Select algorithm [1-3]> ");
        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            switch(choice) {
                case 1 -> selectedAlgorithm = ClusteringAlgorithm.NEAREST_NEIGHBOR;
                case 2 -> selectedAlgorithm = ClusteringAlgorithm.K_MEANS;
                case 3 -> selectedAlgorithm = ClusteringAlgorithm.HIERARCHICAL;
                default -> {
                    System.out.println(ConsoleColour.RED + "Invalid choice. Using default (Nearest Neighbor)" + ConsoleColour.RESET);
                    selectedAlgorithm = ClusteringAlgorithm.NEAREST_NEIGHBOR;
                }
            }
            System.out.println(ConsoleColour.GREEN + "Algorithm set to: " + selectedAlgorithm + ConsoleColour.RESET);
        } catch (NumberFormatException e) {
            System.out.println(ConsoleColour.RED + "Invalid input. Using default (Nearest Neighbor)" + ConsoleColour.RESET);
            selectedAlgorithm = ClusteringAlgorithm.NEAREST_NEIGHBOR;
        }
    }


    /**
    * Builds word clusters using selected algorithm.
    * Loads embeddings, processes clusters, and writes results.
    *
    * @throws Exception if clustering operation fails
    */
    private void buildClusters() throws Exception {
        if (embeddingFile == null || searchWord == null) {
            System.out.println(ConsoleColour.RED + "Please specify both embedding file and search word first" + ConsoleColour.RESET);
            return;
        }

        try {
            long startTime = System.currentTimeMillis();
            
            // Load embeddings
            System.out.println(ConsoleColour.YELLOW + "Loading embeddings from: " + embeddingFile + ConsoleColour.RESET);
            embedder.loadEmbeddings(embeddingFile);
            System.out.println(ConsoleColour.GREEN + "Successfully loaded embeddings" + ConsoleColour.RESET);

            // Build clusters using virtual threads
            System.out.println(ConsoleColour.YELLOW + "Building clusters using " + selectedAlgorithm + " algorithm..." + ConsoleColour.RESET);
            List<String> results = builder.buildClusters(searchWord, numThreads, selectedAlgorithm );

            if (results.isEmpty()) {
                System.out.println(ConsoleColour.RED + "No results found for word: " + searchWord + ConsoleColour.RESET);
                System.out.println(ConsoleColour.YELLOW + "Try a different word." + ConsoleColour.RESET);
                return;
            }

            // Show progress
            System.out.println(ConsoleColour.YELLOW + "Processing results..." + ConsoleColour.RESET);
            showProgress();

            // Write results
            outputWriter.writeResults(results, outputFile, searchWord);
            
            long endTime = System.currentTimeMillis();
            System.out.println(ConsoleColour.BLUE + "Total processing time: " + (endTime - startTime) + "ms" + ConsoleColour.RESET);

        } catch (IOException e) {
            System.out.println(ConsoleColour.RED + "Error reading embeddings file: " + e.getMessage() + ConsoleColour.RESET);
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println(ConsoleColour.RED + "Error processing clusters: " + e.getMessage() + ConsoleColour.RESET);
            e.printStackTrace();
        }
    }
    
    


    /**
     * Shows progress bar during processing.
     *
     * @throws InterruptedException if progress display is interrupted
     */
    private void showProgress() throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            progressReporter.showProgress(i + 1, 100);
        }
    }
    
    /**
     * Exits the application with goodbye message.
     */
    private void quit() {
        System.out.println(ConsoleColour.GREEN + "Thank you for using Word Clustering. Goodbye!" + ConsoleColour.RESET);
        keepRunning = false;
    }
}