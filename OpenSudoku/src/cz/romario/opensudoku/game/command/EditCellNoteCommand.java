package cz.romario.opensudoku.game.command;

import cz.romario.opensudoku.game.SudokuCell;

public class EditCellNoteCommand implements Command {

	private SudokuCell mCell;
	private String mNote;
	private String mOldNote;
	
	public EditCellNoteCommand(SudokuCell cell, String note) {
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
