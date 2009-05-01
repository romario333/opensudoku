package cz.romario.opensudoku.gui.inputmethod;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import cz.romario.opensudoku.R;
import cz.romario.opensudoku.game.SudokuCell;
import cz.romario.opensudoku.game.SudokuGame;
import cz.romario.opensudoku.gui.SudokuBoardView;

/**
 * This class represents following type of number input workflow: Number buttons are displayed
 * in the sidebar, user selects one number and then fill values by tapping the cells.
 * 
 * @author romario
 *
 */
public abstract class IMSingleNumber extends InputMethod {

	private int mSelectedNumber = 0;
	
	private Context mContext;
	private Handler mGuiHandler;
	private Drawable mSelectedBackground;
	private Map<Integer,Button> mNumberButtons;
	private Map<Integer,Drawable> mNumberButtonsBackgrounds;
	
	
	
	public IMSingleNumber(Context context, SudokuGame game,
			SudokuBoardView board) {
		super(context, game, board);
		
		mContext = context;
		mGuiHandler = new Handler();
	}

	@Override
	protected void onControlPanelCreated(View controlPanel) {
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
		
		mSelectedBackground = mContext.getResources().getDrawable(R.drawable.group_button_selected);
		mNumberButtonsBackgrounds = new HashMap<Integer, Drawable>();
		for (Integer num : mNumberButtons.keySet()) {
			Button b = mNumberButtons.get(num);
			mNumberButtonsBackgrounds.put(num, b.getBackground());
			b.setTag(num);
			b.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Integer num = (Integer)v.getTag();
					mSelectedNumber = mSelectedNumber == num ? 0 : num;
					
					update();
				}
			});
		}
	}
	
	private void update() {
		// TODO: sometimes I change background too early and button stays in pressed state
		// this is just ugly workaround
		mGuiHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				for (Button b : mNumberButtons.values()) {
					if (b.getTag().equals(mSelectedNumber)) {
						b.setBackgroundDrawable(mSelectedBackground);
					} else {
						b.setBackgroundDrawable(mNumberButtonsBackgrounds.get(b.getTag()));
					}
				}
			}
		}, 100);
		
	}

	protected int getSelectedNumber() {
		return mSelectedNumber;
	}
	
	@Override
	protected void onActivated() {
		update();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putInt(getInputMethodName() + ".sel_number", mSelectedNumber);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);
		
		mSelectedNumber = savedInstanceState.getInt(getInputMethodName() + ".sel_number");
		if (isControlPanelCreated()) {
			update();
		}
	}

}
