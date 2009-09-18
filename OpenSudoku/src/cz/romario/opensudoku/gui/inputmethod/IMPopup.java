/* 
 * Copyright (C) 2009 Roman Masek
 * 
 * This file is part of OpenSudoku.
 * 
 * OpenSudoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * OpenSudoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with OpenSudoku.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package cz.romario.opensudoku.gui.inputmethod;

import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import cz.romario.opensudoku.R;
import cz.romario.opensudoku.game.Cell;
import cz.romario.opensudoku.game.CellCollection;
import cz.romario.opensudoku.game.CellNote;
import cz.romario.opensudoku.gui.inputmethod.IMPopupDialog.OnNoteEditListener;
import cz.romario.opensudoku.gui.inputmethod.IMPopupDialog.OnNumberEditListener;

public class IMPopup extends InputMethod {

	private boolean mDisableCompletedValues = true;
	
	private IMPopupDialog mEditCellDialog;
	private Cell mSelectedCell;
	
	public boolean getDisableCompletedValues() {
		return mDisableCompletedValues;
	}
	
	/**
	 * If set to true, buttons for numbers, which occur in {@link CellCollection}
	 * more than {@link CellCollection#SUDOKU_SIZE}-times, will be disabled.
	 * 
	 * @param disableCompletedValues
	 */
	public void setDisableCompletedValues(boolean disableCompletedValues) {
		mDisableCompletedValues = disableCompletedValues;
	}
	
	
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
	protected void onCellTapped(Cell cell){
		mSelectedCell = cell;
		if (cell.isEditable()) {
			ensureEditCellDialog();
			mEditCellDialog.updateNumber(cell.getValue());
			mEditCellDialog.updateNote(cell.getNote().getNotedNumbers());
			mEditCellDialog.enableAllNumbers();
			if (mDisableCompletedValues) {
				Map<Integer, Integer> valuesUseCount = mGame.getCells().getValuesUseCount();
				for (Map.Entry<Integer, Integer> entry : valuesUseCount.entrySet()) {
					mEditCellDialog.setNumberEnabled(entry.getKey(), 
							entry.getValue() < CellCollection.SUDOKU_SIZE);
				}
			}
			mEditCellDialog.show();
		} else {
			mBoard.hideTouchedCellHint();
		}
	}
	
	@Override
	protected void onPause() {
		// release dialog resource (otherwise WindowLeaked exception is logged)
		if (mEditCellDialog != null) {
			mEditCellDialog.cancel();
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
		return mContext.getString(R.string.popup_abbr);
	}
	
	@Override
	protected View createControlPanelView() {
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
				mGame.setCellNote(mSelectedCell, CellNote.fromIntArray(numbers));
				mBoard.hideTouchedCellHint();
			}
			return true;
		}
	};
}
