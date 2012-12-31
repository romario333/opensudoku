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

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ToggleButton;
import cz.romario.opensudoku.R;
import cz.romario.opensudoku.game.Cell;
import cz.romario.opensudoku.game.CellCollection.OnChangeListener;
import cz.romario.opensudoku.game.CellNote;
import cz.romario.opensudoku.game.SudokuGame;
import cz.romario.opensudoku.gui.HintsQueue;
import cz.romario.opensudoku.gui.SudokuBoardView;
import cz.romario.opensudoku.gui.inputmethod.IMControlPanelStatePersister.StateBundle;

public class IMHighlighter extends InputMethod {

	private static final int MODE_EDIT_VALUE = 0;
	private static final int MODE_EDIT_NOTE = 1;
	
	private Cell mSelectedCell;
	private ImageButton mSwitchNumNoteButton;
	
	private int mEditMode = MODE_EDIT_VALUE;
	
	private Map<Integer,Button> mNumberButtons;
	
	@Override
	protected void initialize(Context context, IMControlPanel controlPanel,
			SudokuGame game, SudokuBoardView board, HintsQueue hintsQueue) {
		super.initialize(context, controlPanel, game, board, hintsQueue);
		
		game.getCells().addOnChangeListener(mOnCellsChangeListener);
	}
	
	@Override
	protected View createControlPanelView() {
		LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View controlPanel = inflater.inflate(R.layout.im_highlighter, null);
		
		mNumberButtons = new HashMap<Integer, Button>(); 
		mNumberButtons.put(1, (ToggleButton)controlPanel.findViewById(R.id.button_1));
		mNumberButtons.put(2, (ToggleButton)controlPanel.findViewById(R.id.button_2));
		mNumberButtons.put(3, (ToggleButton)controlPanel.findViewById(R.id.button_3));
		mNumberButtons.put(4, (ToggleButton)controlPanel.findViewById(R.id.button_4));
		mNumberButtons.put(5, (ToggleButton)controlPanel.findViewById(R.id.button_5));
		mNumberButtons.put(6, (ToggleButton)controlPanel.findViewById(R.id.button_6));
		mNumberButtons.put(7, (ToggleButton)controlPanel.findViewById(R.id.button_7));
		mNumberButtons.put(8, (ToggleButton)controlPanel.findViewById(R.id.button_8));
		mNumberButtons.put(9, (ToggleButton)controlPanel.findViewById(R.id.button_9));
		mNumberButtons.put(0, (Button)controlPanel.findViewById(R.id.button_clear));
		
		for (Integer num : mNumberButtons.keySet()) {
			Button b = mNumberButtons.get(num);
			b.setTag(num);
			b.setOnClickListener(mNumberButtonClick);
		}
		
		mSwitchNumNoteButton = (ImageButton)controlPanel.findViewById(R.id.switch_num_note);
		mSwitchNumNoteButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mEditMode = mEditMode == MODE_EDIT_VALUE ? MODE_EDIT_NOTE : MODE_EDIT_VALUE;
				update();
			}
			
		});
		
		return controlPanel;
		
	}

	@Override
	public int getNameResID() {
		return R.string.highlighter;
	}

	@Override
	public int getHelpResID() {
		return R.string.im_highlighter_hint;
	}
	
	@Override
	public String getAbbrName() {
		return mContext.getString(R.string.highlighter_abbr);
	}
	
	@Override
	protected void onActivated() {
		update();
		mSelectedCell = mBoard.getSelectedCell();
	}
	
	@Override
	protected void onCellSelected(Cell cell) {
		mSelectedCell = cell;
		if (mSelectedCell != null) {
			switch (mEditMode) {
				case MODE_EDIT_VALUE: {
					// send user to NumPad input method so they can set value
					// we choose NumPad b/c it needs only one click to set value, as cell is selected
					mControlPanel.activateInputMethod(IMControlPanel.INPUT_METHOD_NUMPAD);
					break;
				}
				case MODE_EDIT_NOTE: {
					int highlightedNumber = mBoard.getHighlightedNumber();
					if (highlightedNumber != 0) {
						CellNote note = mSelectedCell.getNote();
						if ((note != null) && !note.isEmpty())
							mGame.setCellNote(mSelectedCell, note.toggleNumber(highlightedNumber));
					}
					break;
				}
			}
		}
	}
	
	private OnClickListener mNumberButtonClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int selNumber = (Integer)v.getTag();

			int highlightNumber = mBoard.getHighlightedNumber();
			if (selNumber == 0) {
				mBoard.setHighlightedNumber(0);
				getToggle(highlightNumber).setChecked(false);
				
				mBoard.postInvalidate();
			}
			else if (selNumber >= 1 && selNumber <= 9) {
				if (getToggle(selNumber).isChecked()) {
					mBoard.setHighlightedNumber(selNumber);
					if (highlightNumber != 0)
						getToggle(highlightNumber).setChecked(false);
				} else {
					mBoard.setHighlightedNumber(0);
				}
				
				mBoard.postInvalidate();
			}
		}
		
	};
	
	private ToggleButton getToggle(int number) {
		return (ToggleButton) mNumberButtons.get(number);
	}
	
	private OnChangeListener mOnCellsChangeListener = new OnChangeListener() {
		
		@Override
		public void onChange() {
			if (mActive) {
				update();
			}
		}
	};
	
	
	private void update() {
		switch (mEditMode) {
		case MODE_EDIT_NOTE:
			mSwitchNumNoteButton.setImageResource(R.drawable.pencil);
			break;
		case MODE_EDIT_VALUE:
			mSwitchNumNoteButton.setImageResource(R.drawable.pencil_disabled);
			break;
		}
		
		int highlightedNumber = mBoard.getHighlightedNumber();
		for (int num = 1; num <= 9; num++) {
			getToggle(num).setChecked(num == highlightedNumber);
		}
	}
	
	@Override
	protected void onSaveState(StateBundle outState) {
		outState.putInt("editMode", mEditMode);
	}
	
	@Override
	protected void onRestoreState(StateBundle savedState) {
		mEditMode = savedState.getInt("editMode", MODE_EDIT_VALUE);
		if (isInputMethodViewCreated()) {
			update();
		}
	}
}
