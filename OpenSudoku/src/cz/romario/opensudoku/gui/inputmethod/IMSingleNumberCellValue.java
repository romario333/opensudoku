package cz.romario.opensudoku.gui.inputmethod;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import cz.romario.opensudoku.R;
import cz.romario.opensudoku.game.SudokuCell;
import cz.romario.opensudoku.game.SudokuGame;
import cz.romario.opensudoku.gui.HintsManager;
import cz.romario.opensudoku.gui.SudokuBoardView;

/**
 * Input method "Single number".
 * 
 * User first selects the number and then taps cell to fill value in it. If no number is
 * selected, tapping the cell clear its value.
 * 
 * @author romario
 *
 */
public class IMSingleNumberCellValue extends IMSingleNumber {
	private Context mContext;
	private SudokuGame mGame;
	private SudokuBoardView mBoard;
	
	private int mTapCount = 0;
	
	
	public IMSingleNumberCellValue(Context context, SudokuGame game,
			SudokuBoardView board, HintsManager hintsManager) {
		super(context, game, board, hintsManager);
		
		mContext = context;
		mGame = game;
		mBoard = board;
	}

	@Override
	public String getAbbrName() {
		return mContext.getString(R.string.single_number_value_abbr);
	}
	
	@Override
	protected View createControlPanel() {
		LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		return inflater.inflate(R.layout.im_single_number, null);
	}

	@Override
	protected void onCellTapped(SudokuCell cell) {
		int selNumber = getSelectedNumber();
		
		if (selNumber >= 0 && selNumber <= 9) {
			mGame.setCellValue(cell, selNumber);
			mBoard.postInvalidate();

			if (!mHintsManager.wasDisplayed("single_number_value_cell_tapped")) {
				mTapCount++;
				if (mTapCount == 2) {
					hint("single_number_value_cell_tapped", 
							mContext.getString(R.string.hint_single_number_few_nums_entered, selNumber),
							Toast.LENGTH_LONG);				
				}
			}
		}
	}

}
