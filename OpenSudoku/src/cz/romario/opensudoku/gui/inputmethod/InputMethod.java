package cz.romario.opensudoku.gui.inputmethod;

import android.content.Context;
import android.view.View;
import cz.romario.opensudoku.game.SudokuCell;
import cz.romario.opensudoku.game.SudokuGame;
import cz.romario.opensudoku.gui.SudokuBoardView;
import cz.romario.opensudoku.gui.SudokuBoardView.OnCellTapListener;

/**
 * Base class for several input methods used to edit sudoku contents. 
 * 
 * @author romario
 *
 */
public abstract class InputMethod {
	
	public boolean enabled = true;
	
	private View mControlPanel;
	private int mScreenOrientation;

	public InputMethod(Context context, SudokuGame game, SudokuBoardView board) {
		mScreenOrientation = context.getResources().getConfiguration().orientation;
	}
	
	public boolean isControlPanelCreated() {
		return mControlPanel != null;
	}
	
	public View getControlPanel() {
		if (mControlPanel == null) {
			mControlPanel = createControlPanel(mScreenOrientation);
		}
		onControlPanelCreated(mControlPanel);
		return mControlPanel;
	}
	
	protected abstract View createControlPanel(int screenOrientation);
	
	protected void onControlPanelCreated(View controlPanel) {
		
	}
	
	protected void onActivated() {
		
	}
	
	protected void onDeactivated() {
		
	}
	
	protected void onCellSelected(SudokuCell cell) {
		
	}
	
	
}
