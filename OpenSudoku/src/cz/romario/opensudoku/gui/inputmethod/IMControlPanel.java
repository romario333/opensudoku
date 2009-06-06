package cz.romario.opensudoku.gui.inputmethod;

import java.util.ArrayList;
import java.util.List;

import cz.romario.opensudoku.R;
import cz.romario.opensudoku.game.Cell;
import cz.romario.opensudoku.game.SudokuGame;
import cz.romario.opensudoku.gui.HintsQueue;
import cz.romario.opensudoku.gui.SudokuBoardView;
import cz.romario.opensudoku.gui.SudokuBoardView.OnCellSelectedListener;
import cz.romario.opensudoku.gui.SudokuBoardView.OnCellTappedListener;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * 
 * 
 * @author romario
 *
 */
public class IMControlPanel extends LinearLayout {
	private Context mContext;
	private SudokuBoardView mBoard;
	private SudokuGame mGame;
	private HintsQueue mHintsQueue;
	
	private List<InputMethod> mInputMethods = new ArrayList<InputMethod>();
	private int mActiveMethodIndex = -1;
	
	public IMControlPanel(Context context) {
		super(context);
		mContext = context;
	}
	
	public IMControlPanel(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
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
		mBoard.setOnCellTappedListener(mOnCellTapListener);
		mBoard.setOnCellSelectedListener(mOnCellSelected);
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
	
	public HintsQueue getHintsQueue() {
		return mHintsQueue;
	}
	
	public void setHintsQueue(HintsQueue hintsQueue) {
		mHintsQueue = hintsQueue;
	}
	
	public void addInputMethod(InputMethod im) {
		assert mContext != null;
		assert mGame != null;
		assert mBoard != null;
		
		im.initialize(mContext, mGame, mBoard, mHintsQueue);
		mInputMethods.add(im);
		
		if (mActiveMethodIndex == -1 && im.enabled) {
			activateInputMethod(mInputMethods.size() - 1);
		}
	}
	
	// TODO: this is weird, find better solution
	public void ensureSomethingIsActive() {
		if (mActiveMethodIndex == -1 || !mInputMethods.get(mActiveMethodIndex).enabled) {
			activateInputMethod(0);
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
		assert methodID >= -1 && methodID < mInputMethods.size();
		
		if (mActiveMethodIndex != -1) {
			mInputMethods.get(mActiveMethodIndex).onDeactivated();
		}
		
		boolean idFound = false;
		int id = methodID;
		int numOfCycles = 0;
		
		if (id != -1) {
			while (!idFound && numOfCycles <= mInputMethods.size()) {
				if (mInputMethods.get(id).enabled) {
					ensureControlPanel(id);
					idFound = true;
					break;
				}
				
				id++;
				if (id == mInputMethods.size()) {
					id = 0;
				}
				numOfCycles++;
			}
		}
		
		if (!idFound) {
			id = -1;
		}
		
		for (int i = 0; i < mInputMethods.size(); i++) {
			InputMethod im = mInputMethods.get(i);
			if (im.isControlPanelCreated()) {
				im.getControlPanel().setVisibility(i == id ? View.VISIBLE : View.GONE);
			}
		}
		
		mActiveMethodIndex = id;
		if (mActiveMethodIndex != -1) {
			InputMethod activeMethod = mInputMethods.get(mActiveMethodIndex);
			activeMethod.onActivated();
			
			if (mHintsQueue != null) {
				mHintsQueue.showOneTimeHint(activeMethod.getNameResID(), activeMethod.getHelpResID());
			}
		}
	}
	
	public void activateNextInputMethod() {
		int id = mActiveMethodIndex + 1;
		if (id >= mInputMethods.size()) {
			if (mHintsQueue != null) {
				mHintsQueue.showOneTimeHint(R.string.that_is_all, R.string.im_disable_modes_hint);
			}
			id = 0;
		}
		activateInputMethod(id);
	}
	
	public int getActiveMethodIndex() {
		return mActiveMethodIndex;
	}
	
	public void showHelpForActiveMethod() {
		if (mActiveMethodIndex != -1) {
			InputMethod activeMethod = mInputMethods.get(mActiveMethodIndex);
			activeMethod.onActivated();
			
			mHintsQueue.showHint(activeMethod.getNameResID(), activeMethod.getHelpResID());
		}
	}
	
	/**
	 * This should be called when activity is paused (so Input Methods can do some cleanup,
	 * for example properly dismiss dialogs because of WindowLeaked exception).
	 */
	public void pause() {
		for (InputMethod im : mInputMethods) {
			im.pause();
		}
	}
	
	private void ensureControlPanel(int methodID) {
		InputMethod im = mInputMethods.get(methodID);
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
	
	private OnCellTappedListener mOnCellTapListener = new OnCellTappedListener() {
		@Override
		public void onCellTapped(Cell cell) {
			if (mActiveMethodIndex != -1 && mInputMethods != null) {
				mInputMethods.get(mActiveMethodIndex).onCellTapped(cell);
			}
		}
	};
	
	private OnCellSelectedListener mOnCellSelected = new OnCellSelectedListener() {
		@Override
		public void onCellSelected(Cell cell) {
			if (mActiveMethodIndex != -1 && mInputMethods != null) {
				mInputMethods.get(mActiveMethodIndex).onCellSelected(cell);
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
        return new SavedState(superState, mActiveMethodIndex, mInputMethods);
	}
	
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        activateInputMethod(ss.getActiveMethodIndex());
        ss.restoreInputMethodsState(mInputMethods);
	}
	
    /**
     * Used to save / restore state of time picker
     */
    private static class SavedState extends BaseSavedState {
    	private final int mActiveMethodIndex;
        private final Bundle mInputMethodsState;
    	
    	private SavedState(Parcelable superState, int activeMethodIndex, List<InputMethod> inputMethods) {
            super(superState);
            mActiveMethodIndex = activeMethodIndex;
            
            // TODO: consider thread-safety
            
            mInputMethodsState = new Bundle();
            for (InputMethod im : inputMethods) {
            	im.onSaveInstanceState(mInputMethodsState);
            }
        }
        
        private SavedState(Parcel in) {
            super(in);
            mActiveMethodIndex = in.readInt();
            mInputMethodsState = in.readBundle();
        }

        public int getActiveMethodIndex() {
            return mActiveMethodIndex;
        }
        
        public void restoreInputMethodsState(List<InputMethod> inputMethods) {
        	for (InputMethod im : inputMethods) {
        		im.onRestoreInstanceState(mInputMethodsState);
        	}
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(mActiveMethodIndex);
            dest.writeBundle(mInputMethodsState);
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
