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

package cz.romario.opensudoku.game.command;

import android.os.Bundle;

/**
 * Generic interface for command in application.
 *
 * @author romario
 */
public abstract class AbstractCommand {

	public static AbstractCommand newInstance(String commandClass) {
		if (commandClass.equals(ClearAllNotesCommand.class.getSimpleName())) {
			return new ClearAllNotesCommand();
		} else if (commandClass.equals(EditCellNoteCommand.class.getSimpleName())) {
			return new EditCellNoteCommand();
		} else if (commandClass.equals(FillInNotesCommand.class.getSimpleName())) {
			return new FillInNotesCommand();
		} else if (commandClass.equals(SetCellValueCommand.class.getSimpleName())) {
			return new SetCellValueCommand();
		} else {
			throw new IllegalArgumentException(String.format("Unknown command class '%s'.", commandClass));
		}
	}

	private boolean mIsCheckpoint;
	private int mNestDepth;

	void saveState(Bundle outState) {
		outState.putBoolean("isCheckpoint", mIsCheckpoint);
		outState.putInt("nestDepth", mNestDepth);
	}

	void restoreState(Bundle inState) {
		mIsCheckpoint = inState.getBoolean("isCheckpoint");
		mNestDepth = inState.getInt("nestDepth");
	}

	public boolean isCheckpoint() {
		return mIsCheckpoint;
	}

	public void setCheckpoint(boolean isCheckpoint) {
		mIsCheckpoint = isCheckpoint;
	}

	public String getCommandClass() {
		return getClass().getSimpleName();
	}

	protected int getNestDepth() {
		return mNestDepth;
	}

	/**
	 * Executes the command after saving the nesting level.
	 */
	protected void execute(int nestDepth) {
		mNestDepth = nestDepth;
		execute();
	}

	/**
	 * Executes the command.
	 */
	protected abstract void execute();

	/**
	 * Undo this command.
	 */
	protected abstract void undo();

}
