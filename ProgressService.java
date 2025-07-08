package ie.atu.sw;

/**
 * Interface defining progress reporting operations.
 * Handles progress updates and user feedback.
 *
 * @author Josh Monahan
 * @version 1.0
 * @since JDK22
 */
public interface ProgressService {
	/**
     * Shows progress of current operation.
     *
     * @param current current progress value
     * @param total total progress value
     * @throws InterruptedException if progress display is interrupted
     */
    void showProgress(int current, int total) throws InterruptedException;
    /**
     * Reports a progress message to the user.
     *
     * @param message progress message to display
     */
    void reportProgress(String message);
}
