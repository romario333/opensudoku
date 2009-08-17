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

import cz.romario.opensudoku.game.Cell;

public class SetCellValueCommand implements Command {

	private Cell mCell;
	private int mValue;
	private int mOldValue;
	
	public SetCellValueCommand(Cell cell, int value) {
		mCell = cell;
		mValue = value;
	}
	
	@Override
	public void execute() {
		mOldValue = mCell.getValue();
		mCell.setValue(mValue);
	}

	@Override
	public void undo() {
		mCell.setValue(mOldValue);
	}

}
