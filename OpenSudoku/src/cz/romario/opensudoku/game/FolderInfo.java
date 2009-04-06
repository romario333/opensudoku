package cz.romario.opensudoku.game;

/**
 * Some information about folder, used in FolderListActivity.
 * 
 * @author EXT91365
 *
 */
public class FolderInfo {
	
	/**
	 * Primary key of folder.
	 */
	public long id;
	
	/**
	 * Name of the folder.
	 */
	public String name;
	
	/**
	 * Total count of puzzles in the folder.
	 */
	public int puzzleCount;

	/**
	 * Count of solved puzzles in the folder.
	 */
	public int solvedCount;
	
	public FolderInfo(long id, String name) {
		this.id = id;
		this.name = name;
	}

}
