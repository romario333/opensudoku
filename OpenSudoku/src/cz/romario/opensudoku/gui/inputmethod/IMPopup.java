package cz.romario.opensudoku.gui.inputmethod;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import cz.romario.opensudoku.R;
import cz.romario.opensudoku.game.SudokuCell;
import cz.romario.opensudoku.game.SudokuGame;
import cz.romario.opensudoku.gui.HintsQueue;
import cz.romario.opensudoku.gui.SudokuBoardView;
import cz.romario.opensudoku.gui.inputmethod.IMPopupDialog.OnNoteEditListener;
import cz.romario.opensudoku.gui.inputmethod.IMPopupDialog.OnNumberEditListener;

public class IMPopup extends InputMethod {

	private IMPopupDialog mEditCellDialog;
	private SudokuCell mSelectedCell;
	
	private void ensureEditCellDialog() {
		if (mEditCellDialog == null) {
			mEditCellDialog = new IMPopupDialog(mContext);
	        mEditCellDialog.setOnNumberEditListener(onNumberEditListener);
	        mEditCellDialog.setOnNoteEditListener(onNoteEditListener);
		}
		
	}
	
	@Override
	protected void onActivated() {
		mBoard.setAutoHideTouchedCellHint(false);
	}
	
	@Override
	protected void onDeactivated() {
		mBoard.setAutoHideTouchedCellHint(true);
	}
	
	@Override
	protected void onCellTapped(SudokuCell cell){
		mSelectedCell = cell;
		if (cell.isEditable() && mEditCellDialog != null) {
			ensureEditCellDialog();
			mEditCellDialog.updateNumber(cell.getValue());
			mEditCellDialog.updateNote(SudokuCell.getNoteNumbers(cell.getNote()));
			mEditCellDialog.show();
		} else {
			mBoard.hideTouchedCellHint();
		}
	}

	@Override
	public int getNameResID() {
		return R.string.popup;
	}

	@Override
	public int getHelpResID() {
		return R.string.im_popup_hint;
	}
	
	@Override
	public String getAbbrName() {
		// TODO: Icon would be better?
		return mContext.getString(R.string.popup_abbr);
	}
	
	@Override
	protected View createControlPanel() {
		LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		return inflater.inflate(R.layout.im_popup, null);
	}
	
	/**
	 * Occurs when user selects number in EditCellDialog.
	 */
    private OnNumberEditListener onNumberEditListener = new OnNumberEditListener() {
		@Override
		public boolean onNumberEdit(int number) {
    		if (number != -1 && mSelectedCell != null) {
    			mGame.setCellValue(mSelectedCell, number);
    			mBoard.hideTouchedCellHint();
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
				mBoard.hideTouchedCellHint();
			}
			return true;
		}
	};
}
