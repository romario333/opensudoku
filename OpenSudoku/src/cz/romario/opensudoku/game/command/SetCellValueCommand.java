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
import cz.romario.opensudoku.game.Cell;
import cz.romario.opensudoku.game.CellNote;

public class SetCellValueCommand extends AbstractCellCommand {

	private int mCellRow;
	private int mCellColumn;
	private int mValue;
	private int mOldValue;
	private CellNote mOldNote;

	public SetCellValueCommand(Cell cell, int value) {
		mCellRow = cell.getRowIndex();
		mCellColumn = cell.getColumnIndex();
		mValue = value;
	}

	SetCellValueCommand() {

	}

	@Override
	void saveState(Bundle outState) {
		super.saveState(outState);

		outState.putInt("cellRow", mCellRow);
		outState.putInt("cellColumn", mCellColumn);
		outState.putInt("value", mValue);
		outState.putInt("oldValue", mOldValue);
		outState.putString("oldNote", mOldNote.serialize());
	}

	@Override
	void restoreState(Bundle inState) {
		super.restoreState(inState);

		mCellRow = inState.getInt("cellRow");
		mCellColumn = inState.getInt("cellColumn");
		mValue = inState.getInt("value");
		mOldValue = inState.getInt("oldValue");
		mOldNote = CellNote.deserialize(inState.getString("oldNote"));
	}

	@Override
	protected void execute() {
		Cell cell = getCells().getCell(mCellRow, mCellColumn);
		mOldValue = cell.getValue();
		mOldNote = cell.getNote();
		cell.setNote(new CellNote());
		cell.setValue(mValue);
	}

	@Override
	protected void undo() {
		Cell cell = getCells().getCell(mCellRow, mCellColumn);
		cell.setValue(mOldValue);
		cell.setNote(mOldNote);
	}

}
