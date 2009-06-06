package cz.romario.opensudoku.game.command;

import cz.romario.opensudoku.game.Cell;
import cz.romario.opensudoku.game.CellNote;

public class EditCellNoteCommand implements Command {

	private Cell mCell;
	private CellNote mNote;
	private CellNote mOldNote;
	
	public EditCellNoteCommand(Cell cell, CellNote note) {
		mCell = cell;
		mNote = note;
	}
	
	@Override
	public void execute() {
		mOldNote = mCell.getNote();
		mCell.setNote(mNote);
	}

	@Override
	public void undo() {
		mCell.setNote(mOldNote);
	}

}
