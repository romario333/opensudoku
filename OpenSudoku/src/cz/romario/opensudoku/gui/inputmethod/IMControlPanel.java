package cz.romario.opensudoku.gui.inputmethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cz.romario.opensudoku.R;
import cz.romario.opensudoku.game.SudokuCell;
import cz.romario.opensudoku.game.SudokuGame;
import cz.romario.opensudoku.gui.SudokuBoardView;
import cz.romario.opensudoku.gui.SudokuBoardView.OnCellTapListener;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.BaseSavedState;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * 
 * 
 * @author EXT91365
 *
 */
public class IMControlPanel extends LinearLayout {

	// TODO: find better names
	public static final int INPUT_METHOD_POPUP = 0;
	public static final int INPUT_METHOD_SIDEBAR_NUMBER = 1;
	public static final int INPUT_METHOD_SIDEBAR_NOTE = 2;
	
	private static final int INPUT_METHODS_COUNT = 3;
	
	private Context mContext;
	private SudokuBoardView mBoard;
	private SudokuGame mGame;
	
	private InputMethod[] mInputMethods;
	private int mActiveMethodIndex = -1;
	
	public IMControlPanel(Context context) {
		super(context);
		mContext = context;
	}
	
	public IMControlPanel(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}
	
	/**
	 * Initializes input method's control panel. Make sure that setGame and setBoard methods are called first.
	 * 
	 * @param context
	 */
	public void initialize() {
		assert mContext != null;
		assert mGame != null;
		assert mBoard != null;
		
		mInputMethods = new InputMethod[INPUT_METHODS_COUNT];
		mInputMethods[INPUT_METHOD_POPUP] = new IMPopup(mContext, mGame, mBoard);
		mInputMethods[INPUT_METHOD_SIDEBAR_NUMBER] = new IMSingleNumberCellValue(mContext, mGame, mBoard);
		mInputMethods[INPUT_METHOD_SIDEBAR_NOTE] = new IMSingleNumberCellNote(mContext, mGame, mBoard);
		
		activateInputMethod(0);
	}
	
	public SudokuBoardView getBoard() {
		return mBoard;
	}
	
	/**
	 * Sets sudoku board for this control panel. Don't forget to call initialize().
	 * 
	 * @param board
	 */
	public void setBoard(SudokuBoardView board) {
		mBoard = board;
		// TODO: only one observer can be registered, implement observer pattern properly
		mBoard.setOnCellTapListener(mOnCellTapListener);
	}
	
	public SudokuGame getGame() {
		return mGame;
	}
	
	/**
	 * Sets game for this control panel.  Don't forget to call initialize().
	 * 
	 * @param game
	 */
	public void setGame(SudokuGame game) {
		mGame = game;
	}
	
	/**
	 * Enables or disables given input method. If input method to disable is active,
	 * the first possible other input method is selected.
	 * 
	 * @param methodID
	 * @param enabled
	 */
	public void setInputMethodEnabled(int methodID, boolean enabled) {
		if (enabled) {
			mInputMethods[methodID].enabled = true;
		} else {
			mInputMethods[methodID].enabled = false;
			
			if (methodID == mActiveMethodIndex) {
				activateInputMethod(0);
			}
		}
	}
	
	
	/**
	 * Activates given input method (see INPUT_METHOD_* constants). If the given method is
	 * not enabled, activates first available method after this method.
	 * 
	 * @param methodID ID of method input to activate.
	 * @return
	 */
	public void activateInputMethod(int methodID) {
		assert methodID >= -1 && methodID < INPUT_METHODS_COUNT;
		
		boolean idFound = false;
		int id = methodID;
		int numOfCycles = 0;
		
		if (id != -1) {
			while (!idFound && numOfCycles <= INPUT_METHODS_COUNT) {
				if (mInputMethods[id].enabled) {
					ensureControlPanel(id);
					idFound = true;
					break;
				}
				
				id++;
				if (id == INPUT_METHODS_COUNT) {
					id = 0;
				}
				numOfCycles++;
			}
		}
		
		if (!idFound) {
			id = -1;
		}
		
		for (int i = 0; i < INPUT_METHODS_COUNT; i++) {
			InputMethod im = mInputMethods[i];
			if (im.isControlPanelCreated()) {
				im.getControlPanel().setVisibility(i == id ? View.VISIBLE : View.GONE);
			}
		}
		
		mActiveMethodIndex = id;
	}
	
	public void activateNextInputMethod() {
		int id = mActiveMethodIndex + 1;
		if (id >= INPUT_METHODS_COUNT) {
			id = 0;
		}
		activateInputMethod(id);
	}
	
	public int getActiveMethodIndex() {
		return mActiveMethodIndex;
	}
	
	private void ensureControlPanel(int methodID) {
		InputMethod im = mInputMethods[methodID];
		if (!im.isControlPanelCreated()) {
			View controlPanel = im.getControlPanel();
			Button switchModeButton = (Button)controlPanel.findViewById(R.id.switch_input_mode);
			if (switchModeButton == null) {
				// TODO: exception
			}
			switchModeButton.setOnClickListener(mSwitchModeListener);
			this.addView(controlPanel, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		}
	}
	
	private OnCellTapListener mOnCellTapListener = new OnCellTapListener() {
		@Override
		public void onCellTap(SudokuCell cell) {
			if (mActiveMethodIndex != -1 && mInputMethods != null) {
				// TODO: tap vs select
				mInputMethods[mActiveMethodIndex].onCellSelected(cell);
			}
		}
	};
	
	private OnClickListener mSwitchModeListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			activateNextInputMethod();
		}
	};
	
	@Override
	protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState, mActiveMethodIndex);
	}
	
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        activateInputMethod(ss.getActiveMethodIndex());
	}
	
    /**
     * Used to save / restore state of time picker
     */
    private static class SavedState extends BaseSavedState {
        private final int mActiveMethodIndex;
    	
    	private SavedState(Parcelable superState, int activeMethodIndex) {
            super(superState);
            mActiveMethodIndex = activeMethodIndex;
        }
        
        private SavedState(Parcel in) {
            super(in);
            mActiveMethodIndex = in.readInt();
        }

        public int getActiveMethodIndex() {
            return mActiveMethodIndex;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(mActiveMethodIndex);
        }

        public static final Parcelable.Creator<SavedState> CREATOR
                = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    	
    }
    

}
