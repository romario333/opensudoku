package cz.romario.opensudoku.game;

/**
 * Generic interface for command in application.
 * 
 * @author romario
 *
 */
public interface Command {
	/**
	 * Executes the command.
	 */
	void execute();
	/**
	 * Undo this command.
	 */
	void undo();
}
