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
