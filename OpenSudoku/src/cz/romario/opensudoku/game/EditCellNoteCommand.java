package cz.romario.opensudoku.game;

public class EditCellNoteCommand implements Command {

	private SudokuCell cell;
	private String note;
	private String oldNote;
	
	public EditCellNoteCommand(SudokuCell cell, String note) {
		this.cell = cell;
		this.note = note;
	}
	
	@Override
	public void execute() {
		oldNote = cell.getNote();
		cell.setNote(note);
	}

	@Override
	public void undo() {
		cell.setNote(oldNote);
	}

}
