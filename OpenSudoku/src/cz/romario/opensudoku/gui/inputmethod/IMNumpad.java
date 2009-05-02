package cz.romario.opensudoku.gui.inputmethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import cz.romario.opensudoku.R;
import cz.romario.opensudoku.game.SudokuCell;
import cz.romario.opensudoku.game.SudokuGame;
import cz.romario.opensudoku.gui.HintsQueue;
import cz.romario.opensudoku.gui.SudokuBoardView;

public class IMNumpad extends InputMethod {

	private static final int MODE_EDIT_VALUE = 0;
	private static final int MODE_EDIT_NOTE = 1;
	
	private Context mContext;
	private SudokuGame mGame;
	private SudokuBoardView mBoard;
	private SudokuCell mSelectedCell;
	private Button mSwitchNumNoteButton;

	private int mSelectedNumber = -1;
	private int mEditMode;
	
	private Map<Integer,Button> mNumberButtons;
	
	public IMNumpad(Context context, SudokuGame game, SudokuBoardView board, HintsQueue hintsQueue) {
		super(context, game, board, hintsQueue);
		
		mContext = context;
		mGame = game;
		mBoard = board;
		
		mEditMode = MODE_EDIT_VALUE;
	}

	@Override
	protected View createControlPanel() {
		LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View controlPanel = inflater.inflate(R.layout.im_numpad, null);
		
		mNumberButtons = new HashMap<Integer, Button>(); 
		mNumberButtons.put(1, (Button)controlPanel.findViewById(R.id.button_1));
		mNumberButtons.put(2, (Button)controlPanel.findViewById(R.id.button_2));
		mNumberButtons.put(3, (Button)controlPanel.findViewById(R.id.button_3));
		mNumberButtons.put(4, (Button)controlPanel.findViewById(R.id.button_4));
		mNumberButtons.put(5, (Button)controlPanel.findViewById(R.id.button_5));
		mNumberButtons.put(6, (Button)controlPanel.findViewById(R.id.button_6));
		mNumberButtons.put(7, (Button)controlPanel.findViewById(R.id.button_7));
		mNumberButtons.put(8, (Button)controlPanel.findViewById(R.id.button_8));
		mNumberButtons.put(9, (Button)controlPanel.findViewById(R.id.button_9));
		mNumberButtons.put(0, (Button)controlPanel.findViewById(R.id.button_clear));
		
		for (Integer num : mNumberButtons.keySet()) {
			Button b = mNumberButtons.get(num);
			b.setTag(num);
			b.setOnClickListener(mNumberButtonClick);
		}
		
		mSwitchNumNoteButton = (Button)controlPanel.findViewById(R.id.switch_num_note);
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
		return R.string.numpad;
	}

	@Override
	public int getHelpResID() {
		return R.string.im_numpad_hint;
	}
	
	@Override
	public String getAbbrName() {
		return mContext.getString(R.string.numpad_abbr);
	}
	
	@Override
	protected void onActivated() {
		update();
		
		mSelectedCell = mBoard.getSelectedCell();
	}
	
	@Override
	protected void onCellSelected(SudokuCell cell) {
		mSelectedCell = cell;
	}
	
	private OnClickListener mNumberButtonClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int selNumber = (Integer)v.getTag();
			SudokuCell selCell = mSelectedCell;
			
			switch (mEditMode) {
			case MODE_EDIT_NOTE:
				if (selNumber >= 0 && selNumber <= 9) {
					// TODO: this does not seem very effective
					List<Integer> noteNums = new ArrayList<Integer>();
					
					Integer[] currentNums = SudokuCell.getNoteNumbers(selCell.getNote());
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
					mGame.setCellNote(selCell, SudokuCell.setNoteNumbers(noteNums.toArray(noteNumsArray)));
					mBoard.postInvalidate();
				}
				break;
			case MODE_EDIT_VALUE:
				if (selNumber >= 0 && selNumber <= 9) {
					mGame.setCellValue(selCell, selNumber);
					mBoard.moveCellSelectionRight();
					mBoard.postInvalidate();
				}
				break;
			}
		}
		
	};
	
	private void update() {
		switch (mEditMode) {
		case MODE_EDIT_NOTE:
			mSwitchNumNoteButton.setText("Note");
			break;
		case MODE_EDIT_VALUE:
			mSwitchNumNoteButton.setText("Num");
			break;
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putInt(getInputMethodName() + ".edit_mode", mEditMode);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);
		
		mEditMode = savedInstanceState.getInt(getInputMethodName() + ".edit_mode");
		if (isControlPanelCreated()) {
			update();
		}
	}
}
