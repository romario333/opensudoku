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
			sb.append(c.getString(R.string.no_puzzles));
		} else {
			sb.append(puzzleCount == 1 ? c.getString(R.string.one_puzzle) : c.getString(R.string.n_puzzles, puzzleCount));
			
			if (playingCount != 0 || solvedCount != 0) {
				sb.append(" (");
			}
			
			if (playingCount != 0) {
				sb.append(c.getString(R.string.n_playing, playingCount));
				if (solvedCount != 0) {
					sb.append(", ");
				}
			}
			
			if (solvedCount != 0) {
				if (puzzleCount == solvedCount) {
					sb.append(c.getString(R.string.all_solved));
				}
				
				int unsolvedCount = puzzleCount - solvedCount;
				sb.append(c.getString(R.string.n_unsolved, unsolvedCount));
			}

			if (playingCount != 0 || solvedCount != 0) {
				sb.append(")");
			}
		}
		
		return sb.toString();
		
	}

}
