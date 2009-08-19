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

import java.util.ArrayList;
import java.util.Collections;
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
	public static final int INPUT_METHOD_POPUP = 0;
	public static final int INPUT_METHOD_SINGLE_NUMBER = 1;
	public static final int INPUT_METHOD_NUMPAD = 2;
	
	private Context mContext;
	private SudokuBoardView mBoard;
	// TODO: why does control panel need access tu SudokuGame? CellCollection should be enough.
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
	
	// TODO: 
	/**
	 * Activates first enabled input method. If such method does not exists, nothing
	 * happens.
	 */
	public void activateFirstInputMethod() {
		ensureInputMethods();
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
		if (methodID < -1 || methodID >= mInputMethods.size()) {
			throw new IllegalArgumentException(String.format("Invalid method id: %s.", methodID));
		}
		
		ensureInputMethods();
		
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
		ensureInputMethods();
		
		int id = mActiveMethodIndex + 1;
		if (id >= mInputMethods.size()) {
			if (mHintsQueue != null) {
				mHintsQueue.showOneTimeHint(R.string.that_is_all, R.string.im_disable_modes_hint);
			}
			id = 0;
		}
		activateInputMethod(id);
	}
	
	/**
	 * Returns input method object by its ID (see INPUT_METHOD_* constants).
	 * 
	 * @param methodId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends InputMethod> T getInputMethod(int methodId) {
		ensureInputMethods();
		
		return (T)mInputMethods.get(methodId);
	}
	
	public List<InputMethod> getInputMethods() {
		return Collections.unmodifiableList(mInputMethods);
	}
	
	public int getActiveMethodIndex() {
		return mActiveMethodIndex;
	}
	
	public void showHelpForActiveMethod() {
		ensureInputMethods();
		
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
	
	/**
	 * Ensures that all input method objects are created.
	 */
	private void ensureInputMethods() {
		synchronized (mInputMethods) {
			if (mInputMethods.size() == 0) {
				addInputMethod(INPUT_METHOD_POPUP, new IMPopup());
				addInputMethod(INPUT_METHOD_SINGLE_NUMBER, new IMSingleNumber());
				addInputMethod(INPUT_METHOD_NUMPAD, new IMNumpad());
			}
		}
	}
	
	private void addInputMethod(int methodIndex, InputMethod im) {
		if (mContext == null ) throw new IllegalStateException("Context is not set.");
		if (mGame == null ) throw new IllegalStateException("Game is not set. Call setGame() first.");
		if (mBoard == null ) throw new IllegalStateException("Board is not set. Call setBoard() first.");
		
		im.initialize(mContext, mGame, mBoard, mHintsQueue);
		mInputMethods.add(methodIndex, im);
	}

	/**
	 * Ensures that control panel for given input method is created.
	 * 
	 * @param methodID
	 */
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
     * Used to save / restore state of control panel.
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
