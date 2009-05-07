package cz.romario.opensudoku.game.command;

import cz.romario.opensudoku.game.SudokuCell;
import cz.romario.opensudoku.game.SudokuCellNote;

public class EditCellNoteCommand implements Command {

	private SudokuCell mCell;
	private SudokuCellNote mNote;
	private SudokuCellNote mOldNote;
	
	public EditCellNoteCommand(SudokuCell cell, SudokuCellNote note) {
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
