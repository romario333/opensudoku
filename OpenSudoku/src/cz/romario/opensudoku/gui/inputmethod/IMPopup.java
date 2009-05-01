package cz.romario.opensudoku.gui.inputmethod;

import java.util.zip.Inflater;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import cz.romario.opensudoku.R;
import cz.romario.opensudoku.game.SudokuCell;
import cz.romario.opensudoku.game.SudokuGame;
import cz.romario.opensudoku.gui.SudokuBoardView;
import cz.romario.opensudoku.gui.inputmethod.EditCellDialog.OnNoteEditListener;
import cz.romario.opensudoku.gui.inputmethod.EditCellDialog.OnNumberEditListener;

public class IMPopup extends InputMethod {

	private Context mContext;
	private SudokuGame mGame;
	private SudokuBoardView mBoard;
	
	private EditCellDialog mEditCellDialog;
	private SudokuCell mSelectedCell;
	
	public IMPopup(Context context, SudokuGame game, SudokuBoardView board) {
		super(context, game, board);
		
		mContext = context;
		mGame = game;
		mBoard = board;
		
		mEditCellDialog = new EditCellDialog(mContext);
        mEditCellDialog.setOnNumberEditListener(onNumberEditListener);
        mEditCellDialog.setOnNoteEditListener(onNoteEditListener);

        
	}
	
	@Override
	protected void onCellSelected(SudokuCell cell){
		mSelectedCell = cell;
		if (cell.getEditable() && mEditCellDialog != null) {
			mEditCellDialog.updateNumber(cell.getValue());
			mEditCellDialog.updateNote(SudokuCell.getNoteNumbers(cell.getNote()));
			mEditCellDialog.getDialog().show();
		}
	}

	@Override
	protected View createControlPanel(int screenOrientation) {
		LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		return inflater.inflate(R.layout.input_mode_popup, null);
	}
	
	/**
	 * Occurs when user selects number in EditCellDialog.
	 */
    private OnNumberEditListener onNumberEditListener = new OnNumberEditListener() {
		@Override
		public boolean onNumberEdit(int number) {
    		if (number != -1 && mSelectedCell != null) {
    			mGame.setCellValue(mSelectedCell, number);
				mBoard.postInvalidate();
    		}
			return true;
		}
	};
	
	/**
	 * Occurs when user edits note in EditCellDialog
	 */
	private OnNoteEditListener onNoteEditListener = new OnNoteEditListener() {
		@Override
		public boolean onNoteEdit(Integer[] numbers) {
			if (mSelectedCell != null) {
				mGame.setCellNote(mSelectedCell, SudokuCell.setNoteNumbers(numbers));
				mBoard.postInvalidate();
			}
			return true;
		}
	};
	
}
