package cz.romario.opensudoku.gui.inputmethod;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import cz.romario.opensudoku.R;
import cz.romario.opensudoku.game.SudokuCell;
import cz.romario.opensudoku.game.SudokuGame;
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
	
	
	public IMSingleNumberCellValue(Context context, SudokuGame game,
			SudokuBoardView board) {
		super(context, game, board);
		
		mContext = context;
		mGame = game;
		mBoard = board;
	}

	@Override
	public String getAbbrName() {
		return "Num";
	}
	
	@Override
	protected View createControlPanel() {
		LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		return inflater.inflate(R.layout.im_single_number, null);
	}

	@Override
	protected void onCellSelected(SudokuCell cell) {
		int selNumber = getSelectedNumber();
		
		if (selNumber >= 0 && selNumber <= 9) {
			mGame.setCellValue(cell, selNumber);
			mBoard.postInvalidate();
		}
	}

}
