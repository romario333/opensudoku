package cz.romario.opensudoku.game;

import cz.romario.opensudoku.R;
import android.content.Context;

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
	
	/**
	 * Count of puzzles in "playing" state in the folder.
	 */
	public int playingCount;
	
	public FolderInfo(long id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public String getDetail(Context c) {
		StringBuilder sb = new StringBuilder();
		
		if (puzzleCount == 0) {
			// no puzzles in folder
			sb.append(c.getString(R.string.no_puzzles));
		} else {
			// there are some puzzles
			sb.append(puzzleCount == 1 ? c.getString(R.string.one_puzzle) : c.getString(R.string.n_puzzles, puzzleCount));
			
			int unsolvedCount = puzzleCount - solvedCount;
			
			// if there are any playing or unsolved puzzles, add info about them
			if (playingCount != 0 || unsolvedCount != 0) {
				sb.append(" (");
				
				if (playingCount != 0) {
					sb.append(c.getString(R.string.n_playing, playingCount));
					if (unsolvedCount != 0) {
						sb.append(", ");
					}
				}
				
				if (unsolvedCount != 0) {
					sb.append(c.getString(R.string.n_unsolved, unsolvedCount));
				}
				
				sb.append(")");
			}
			
			// maybe all puzzles are solved?
			if (unsolvedCount == 0 && puzzleCount != 0) {
					sb.append(" (").append(c.getString(R.string.all_solved)).append(")");
			}
			
		}
		
		return sb.toString();
		
	}

}
