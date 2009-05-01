package cz.romario.opensudoku.gui.inputmethod;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import cz.romario.opensudoku.R;
import cz.romario.opensudoku.game.SudokuCell;
import cz.romario.opensudoku.game.SudokuGame;
import cz.romario.opensudoku.gui.SudokuBoardView;

public class IMNumpad extends InputMethod {

	private Context mContext;
	private SudokuGame mGame;
	private SudokuBoardView mBoard;
	private SudokuCell mSelectedCell;
	
	private Map<Integer,Button> mNumberButtons;
	
	public IMNumpad(Context context, SudokuGame game, SudokuBoardView board) {
		super(context, game, board);
		
		mContext = context;
		mGame = game;
		mBoard = board;
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
			b.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					SudokuCell selectedCell = mSelectedCell;
					
					if (selectedCell != null) {
						Integer value = (Integer)v.getTag();
						mGame.setCellValue(selectedCell, value);
						mBoard.moveCellSelectionRight();
						mBoard.postInvalidate();
					}
					
				}
			});
		}
		
		return controlPanel;
		
	}

	@Override
	public String getAbbrName() {
		return "Num\nPad";
	}
	
	@Override
	protected void onActivated() {
		mSelectedCell = mBoard.getSelectedCell();
	}
	
	@Override
	protected void onCellSelected(SudokuCell cell) {
		mSelectedCell = cell;
	}

}
