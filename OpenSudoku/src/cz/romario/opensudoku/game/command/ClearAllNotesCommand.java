package cz.romario.opensudoku.game.command;

import java.util.ArrayList;
import java.util.List;

import cz.romario.opensudoku.game.Cell;
import cz.romario.opensudoku.game.CellCollection;
import cz.romario.opensudoku.game.CellNote;

public class ClearAllNotesCommand implements Command {

	private CellCollection mCells; 
	private List<NoteEntry> mOldNotes = new ArrayList<NoteEntry>();
	
	
	public ClearAllNotesCommand(CellCollection cells) {
		mCells = cells;
	}
	
	@Override
	public void execute() {
		mOldNotes.clear();
		for (int r = 0; r < CellCollection.SUDOKU_SIZE; r++) {
			for (int c = 0; c < CellCollection.SUDOKU_SIZE; c++) {
				Cell cell = mCells.getCell(r, c);
				CellNote note = cell.getNote();
				if (!note.isEmpty()) {
					mOldNotes.add(new NoteEntry(r, c, note));
					cell.setNote(null);
				}
			}
		}
	}

	@Override
	public void undo() {
		for (NoteEntry ne : mOldNotes) {
			mCells.getCell(ne.rowIndex, ne.colIndex).setNote(ne.note);
		}
		
	}
	
	private class NoteEntry {
		public int rowIndex;
		public int colIndex;
		public CellNote note;
		
		public NoteEntry(int rowIndex, int colIndex, CellNote note){
			this.rowIndex = rowIndex;
			this.colIndex = colIndex;
			this.note = note;
		}
		
	}
	

}
