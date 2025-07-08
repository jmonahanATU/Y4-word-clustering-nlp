package ie.atu.sw;

/**
 * ProgressReporter class for showing operation progress.
 * Implements ProgressService for progress tracking and reporting.
 *
 * @author Josh Monahan
 * @version 1.0
 * @since JDK22
 */
public class ProgressReporter implements ProgressService {
	/**
	* Shows progress of current operation.
	*
	* @param current current step in operation
	* @param total total number of steps
	* @throws InterruptedException if display is interrupted
	*/
    @Override
    public void showProgress(int current, int total) throws InterruptedException {
        System.out.print(ConsoleColour.YELLOW);
        Runner.printProgress(current, total);
        Thread.sleep(10);
        if (current == total) {
            System.out.println(ConsoleColour.RESET);
        }
    }

    /**
    * Displays progress messages to user.
    *
    * @param message progress message to display
    */
    @Override
    public void reportProgress(String message) {
        System.out.println(ConsoleColour.YELLOW + message + ConsoleColour.RESET);
    }
}
