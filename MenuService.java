package ie.atu.sw;

/**
 * Interface defining menu service operations.
 * Handles user interface and menu interactions.
 *
 * @author Josh Monahan
 * @version 1.0
 * @since JDK22
 */
public interface MenuService {
	/**
     * Starts the menu system and processes user input.
     */
   void start();
   /**
    * Displays menu options to the user.
    */
   void showMenu();
   /**
    * Processes user's menu selection.
    */
   void processChoice();
}