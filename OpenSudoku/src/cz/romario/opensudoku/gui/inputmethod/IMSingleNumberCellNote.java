package cz.romario.opensudoku.gui.inputmethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.R.attr;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import cz.romario.opensudoku.R;
import cz.romario.opensudoku.game.SudokuCell;
import cz.romario.opensudoku.game.SudokuGame;
import cz.romario.opensudoku.gui.HintsManager;
import cz.romario.opensudoku.gui.SudokuBoardView;

/**
 * Input method "Single number note".
 * 
 * User first selects the number and then taps cell to note this value in it. If no number is
 * selected, tapping the cell clear its note.
 * 
 * @author romario
 *
 */
public class IMSingleNumberCellNote extends IMSingleNumber {

	private Context mContext;
	private SudokuGame mGame;
	private SudokuBoardView mBoard;

	
	public IMSingleNumberCellNote(Context context, SudokuGame game,
			SudokuBoardView board, HintsManager hintsManager) {
		super(context, game, board, hintsManager);
		
		mContext = context;
		mGame = game;
		mBoard = board;
	}

	@Override
	public String getAbbrName() {
		return mContext.getString(R.string.single_number_note_abbr);
	}
	
	@Override
	protected View createControlPanel() {
		LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		return inflater.inflate(R.layout.im_single_number, null);
	}
	
	@Override
	protected void onCellTapped(SudokuCell cell) {
		Integer selNumber = getSelectedNumber();
		
		if (selNumber == 0){
			mGame.setCellNote(cell, null);
		} else if (selNumber > 0 && selNumber <= 9) {
			// TODO: this does not seem very effective
			List<Integer> noteNums = new ArrayList<Integer>();
			
			Integer[] currentNums = SudokuCell.getNoteNumbers(cell.getNote());
			if (currentNums != null) {
				for (Integer n : currentNums) {
					noteNums.add(n);
				}
			}
			
			if (noteNums.contains(selNumber)) {
				noteNums.remove(selNumber);
			} else {
				noteNums.add(selNumber);
			}
			
			Integer[] noteNumsArray = new Integer[noteNums.size()];
			mGame.setCellNote(cell, SudokuCell.setNoteNumbers(noteNums.toArray(noteNumsArray)));
		}
		
		if (selNumber >= 0 && selNumber < 9) {
			mBoard.postInvalidate();
		}
	}
	
	@Override
	protected void onActivated() {
		super.onActivated();
		
		if (!mHintsManager.wasDisplayed("single_number_note_activated")) {
			hint("single_number_note_activated", 
					mContext.getString(R.string.hint_single_number_note_activated), 
					Toast.LENGTH_LONG);
		}
	}

}
