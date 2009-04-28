package cz.romario.opensudoku.game;

import java.util.Date;
import java.util.Stack;

import cz.romario.opensudoku.game.command.ClearAllNotesCommand;
import cz.romario.opensudoku.game.command.Command;
import cz.romario.opensudoku.game.command.EditCellNoteCommand;
import cz.romario.opensudoku.game.command.SetCellValueCommand;


import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;

public class SudokuGame implements Parcelable {
	
	public static final int GAME_STATE_PLAYING = 0;
	public static final int GAME_STATE_NOT_STARTED = 1;
	public static final int GAME_STATE_COMPLETED = 2;
	
	private long mId;
	private Date mCreated;
	private int mState;
	private long mTime;
	private Date mLastPlayed;
	private String mNote;
	private SudokuCellCollection mCells;
	
	private OnPuzzleSolvedListener mOnPuzzleSolvedListener;
	// very basic implementation of undo
	private Stack<Command> mUndoStack = new Stack<Command>();
	// Time when current activity has become active. 
	private long mActiveFromTime = -1; 

	private boolean mHighlightWrongVals = true;
	
	public SudokuGame() {
		
	}
	
	public void setOnPuzzleSolvedListener(OnPuzzleSolvedListener l) {
		mOnPuzzleSolvedListener = l;
	}
	
	public void setNote(String note) {
		mNote = note;
		
	}

	public String getNote() {
		return mNote;
	}

	public void setCreated(Date created) {
		mCreated = created;
	}

	public Date getCreated() {
		return mCreated;
	}

	public void setState(int state) {
		mState = state;
	}

	public int getState() {
		return mState;
	}

	/**
	 * Sets time of play in milliseconds.
	 * @param time
	 */
	public void setTime(long time) {
		mTime = time;
	}

	/**
	 * Gets time of game-play in milliseconds. 
	 * @return
	 */
	public long getTime() {
		if (mActiveFromTime != -1) {
			return mTime + SystemClock.uptimeMillis() - mActiveFromTime;
		} else {
			return mTime;
		}
	}

	public void setLastPlayed(Date lastPlayed) {
		mLastPlayed = lastPlayed;
	}

	public Date getLastPlayed() {
		return mLastPlayed;
	}

	public void setCells(SudokuCellCollection cells) {
		mCells = cells;
	}
	
	public SudokuCellCollection getCells() {
		return mCells;
	}

	public void setId(long id) {
		mId = id;
	}

	public long getId() {
		return mId;
	}
	
	/**
	 * Sets value for the given cell. 0 means empty cell.
	 * 
	 * @param cell
	 * @param value
	 */
	public void setCellValue(SudokuCell cell, int value) {
		SetCellValueCommand c = new SetCellValueCommand(cell, value);
		c.execute();
		mUndoStack.push(c);
		
		validate();
		if (isCompleted()) {
			finish();
			if (mOnPuzzleSolvedListener != null) {
				mOnPuzzleSolvedListener.onPuzzleSolved();
			}
		}
	}
	
	/**
	 * Sets note attached to the given cell.
	 * 
	 * @param cell
	 * @param note
	 */
	public void setCellNote(SudokuCell cell, String note) {
		EditCellNoteCommand c = new EditCellNoteCommand(cell, note);
		c.execute();
		mUndoStack.push(c);
	}
	
	/** 
	 * Undo last command.
	 */
	public void undo() {
		// TODO: undo stack should be saved to activity's saved state
		// TODO: redo
		if (!mUndoStack.empty()) {
			Command c = mUndoStack.pop();
			c.undo();
			validate();
		}
	}
	
	public boolean hasSomethingToUndo() {
		return mUndoStack.size() != 0;
	}
	
	/**
	 * Start game-play.
	 */
	public void start() {
		mState = GAME_STATE_PLAYING;
		resume();
	}
	
	public void resume() {
		// reset time we have spent playing so far, so time when activity was not active
		// will not be part of the game play time
		mActiveFromTime = SystemClock.uptimeMillis();
	}
	
	/**
	 * Pauses game-play (for example if activity pauses).
	 */
	public void pause() {
		// save time we have spent playing so far - it will be reseted after resuming 
		mTime += SystemClock.uptimeMillis() - mActiveFromTime;
		mActiveFromTime = -1;
		
		setLastPlayed(new Date(System.currentTimeMillis()));
	}
	
	/**
	 * Finishes game-play. Called when puzzle is solved.
	 */
	private void finish() {
		pause();
		mState = GAME_STATE_COMPLETED;
	}
	
	/**
	 * Resets game.
	 */
	public void reset() {
		for (int r=0; r<SudokuCellCollection.SUDOKU_SIZE; r++) {
			for (int c=0; c<SudokuCellCollection.SUDOKU_SIZE; c++) {
				SudokuCell cell = mCells.getCell(r, c);
				if (cell.getEditable()) {
					cell.setValue(0);
					cell.setNote("");
				}
			}
		}
		validate();
		setTime(0);
		setLastPlayed(new Date(0));
		mState = GAME_STATE_NOT_STARTED;
	}
	
	/**
	 * Returns true, if puzzle is solved. In order to know the current state, you have to
	 * call validate first. 
	 * @return
	 */
	public boolean isCompleted() {
		return mCells.isCompleted();
	}
	
	public void clearAllNotes() {
		ClearAllNotesCommand c = new ClearAllNotesCommand(mCells);
		c.execute();
		mUndoStack.push(c);
	}
	
	public void setHighlightWrongVals(boolean highlightWrongVals) {
		mHighlightWrongVals = highlightWrongVals;
		mCells.markAllCellsAsValid();
		
		if (mHighlightWrongVals) {
			validate();
		}
	}

	public boolean getHighlightWrongVals() {
		return mHighlightWrongVals;
	}
	
	private void validate() {
		if (mHighlightWrongVals) {
			mCells.validate();
		}
	}
	
	// constructor for Parcelable
	private SudokuGame(Parcel in) {
		mId = in.readLong();
		mNote = in.readString();
		mCreated = new Date(in.readLong());
		mState = in.readInt();
		mTime = in.readLong();
		mLastPlayed = new Date(in.readLong());
		
		mCells = (SudokuCellCollection) in.readParcelable(SudokuCellCollection.class.getClassLoader());
	}
	
	public static final Parcelable.Creator<SudokuGame> CREATOR = new Parcelable.Creator<SudokuGame>() {
		public SudokuGame createFromParcel(Parcel in) {
		    return new SudokuGame(in);
		}
		
		public SudokuGame[] newArray(int size) {
		    return new SudokuGame[size];
		}
	};
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(mId);
		dest.writeString(mNote);
		dest.writeLong(mCreated.getTime());
		dest.writeInt(mState);
		dest.writeLong(mTime);
		dest.writeLong(mLastPlayed.getTime());
		dest.writeParcelable(mCells, flags);
	}

	public interface OnPuzzleSolvedListener
	{
		/**
		 * Occurs when puzzle is solved.
		 * 
		 * @return
		 */
		void onPuzzleSolved();
	}
	
}
