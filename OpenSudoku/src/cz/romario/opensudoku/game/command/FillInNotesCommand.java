package cz.romario.opensudoku.game.command;

import java.util.ArrayList;
import java.util.List;

import cz.romario.opensudoku.game.Cell;
import cz.romario.opensudoku.game.CellCollection;
import cz.romario.opensudoku.game.CellGroup;
import cz.romario.opensudoku.game.CellNote;

public class FillInNotesCommand implements Command {

	private CellCollection mCells; 
	private List<NoteEntry> mOldNotes = new ArrayList<NoteEntry>();
	
	
	public FillInNotesCommand(CellCollection cells) {
		mCells = cells;
	}
	
	
	@Override
	public void execute() {
		mOldNotes.clear();
		for (int r = 0; r < CellCollection.SUDOKU_SIZE; r++) {
			for (int c = 0; c < CellCollection.SUDOKU_SIZE; c++) {
				Cell cell = mCells.getCell(r, c);
				mOldNotes.add(new NoteEntry(r, c, cell.getNote()));
				cell.setNote(new CellNote());
				
				CellGroup row = cell.getRow();
				CellGroup column = cell.getColumn();
				CellGroup sector = cell.getSector();
				for (int i = 1; i <= CellCollection.SUDOKU_SIZE; i++) {
					if (!row.contains(i) && !column.contains(i) && !sector.contains(i)) {
						cell.setNote(cell.getNote().addNumber(i));
					}
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
	
	private static class NoteEntry {
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
