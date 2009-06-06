package cz.romario.opensudoku.gui.inputmethod;

import android.content.Context;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import cz.romario.opensudoku.R;
import cz.romario.opensudoku.game.Cell;
import cz.romario.opensudoku.game.SudokuGame;
import cz.romario.opensudoku.gui.HintsQueue;
import cz.romario.opensudoku.gui.SudokuBoardView;

/**
 * Base class for several input methods used to edit sudoku contents. 
 * 
 * @author romario
 *
 */
public abstract class InputMethod {
	
	public boolean enabled = true;
	
	protected Context mContext;
	protected SudokuGame mGame;
	protected SudokuBoardView mBoard;
	protected HintsQueue mHintsQueue;
	
	private String mInputMethodName;
	protected View mControlPanel;
	 

	public InputMethod() {
		
	}
	
	protected void initialize(Context context, SudokuGame game, SudokuBoardView board, HintsQueue hintsQueue) {
		mContext = context;
		mGame = game;
		mBoard = board;
		mHintsQueue = hintsQueue;
		mInputMethodName = this.getClass().getSimpleName();
	}
	
	public boolean isControlPanelCreated() {
		return mControlPanel != null;
	}
	
	public View getControlPanel() {
		if (mControlPanel == null) {
			mControlPanel = createControlPanel();
			View switchModeView = mControlPanel.findViewById(R.id.switch_input_mode);
			if (switchModeView == null) {
				// TODO: exception + check that it is button
			}
			Button switchModeButton = (Button) switchModeView;
			switchModeButton.setText(getAbbrName());
			// TODO: color from resources
			switchModeButton.getBackground().setColorFilter(new LightingColorFilter(Color.CYAN, 0));
			onControlPanelCreated(mControlPanel);
		}
		
		return mControlPanel;
	}
	
	/**
	 * This should be called when activity is paused (so InputMethod can do some cleanup,
	 * for example properly dismiss dialogs because of WindowLeaked exception).
	 */
	public void pause() {
		onPause();
	}
	
	protected void onPause() {
		
	}
	
	/**
	 * This should be unique name of input method.
	 * 
	 * @return
	 */
	protected String getInputMethodName() {
		return mInputMethodName;
	}
	
	public abstract int getNameResID();
	
	public abstract int getHelpResID();
	
	/**
	 * Gets abbreviated name of input method, which will be displayed on input method switch button.
	 * 
	 * @return
	 */
	public abstract String getAbbrName();
	
	protected abstract View createControlPanel();
	
	protected void onControlPanelCreated(View controlPanel) {
		
	}
	
	protected void onActivated() {
		
	}
	
	protected void onDeactivated() {
		
	}
	
	/**
	 * Called when cell is selected. Please note that cell selection can
	 * change without direct user interaction.
	 * 
	 * @param cell
	 */
	protected void onCellSelected(Cell cell) {
		
	}
	
	/**
	 * Called when cell is tapped.
	 * 
	 * @param cell
	 */
	protected void onCellTapped(Cell cell) {
		
	}
	
	protected void onSaveInstanceState(Bundle outState) {
		
	}
	
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		
	}
}
