package cz.romario.opensudoku.game.command;

import cz.romario.opensudoku.game.SudokuCell;

public class SetCellValueCommand implements Command {

	private SudokuCell mCell;
	private int mValue;
	private int mOldValue;
	
	public SetCellValueCommand(SudokuCell cell, int value) {
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
