package cz.romario.opensudoku.game.command;

import android.os.Bundle;
import cz.romario.opensudoku.game.Cell;
import cz.romario.opensudoku.game.CellNote;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class RemoveCellNoteCommand extends AbstractCellCommand {

  private int[] mCellsRowIndexes;
	private int[] mCellsColumnIndexes;
	private CellNote[] mNotes;
	private CellNote[] mOldNotes;

	public RemoveCellNoteCommand(ArrayList<Cell> cells, ArrayList<CellNote> notes) {
		mCellsRowIndexes = new int[cells.size()];
		mCellsColumnIndexes = new int[cells.size()];
		mNotes = new CellNote[notes.size()];

		for (int i=0; i<cells.size(); i++) {
			mCellsRowIndexes[i] = cells.get(i).getRowIndex();
			mCellsColumnIndexes[i] = cells.get(i).getColumnIndex();
			mNotes[i] = notes.get(i);
		}
	}

	RemoveCellNoteCommand() {

	}

	@Override
	void saveState(Bundle outState) {
		super.saveState(outState);
		// TODO
		//outState.putString("notes", mNotes.serialize());
		//outState.putString("oldNotes", mOldNotes.serialize());
	}

	@Override
	void restoreState(Bundle inState) {
		super.restoreState(inState);
		// TODO
		//mNote = CellNote.deserialize(inState.getString("notes"));
		//mOldNote = CellNote.deserialize(inState.getString("oldNotes"));
	}

	@Override
	void execute() {
		mOldNotes = new CellNote[mNotes.length];

		for (int i=0; i<mCellsRowIndexes.length; i++) {
			Cell cell = getCells().getCell(mCellsRowIndexes[i], mCellsColumnIndexes[i]);
			mOldNotes[i] = cell.getNote();
			cell.setNote(mNotes[i]);
		}
	}

	@Override
	void undo() {
		for (int i=0; i<mCellsRowIndexes.length; i++) {
			Cell cell = getCells().getCell(mCellsRowIndexes[i], mCellsColumnIndexes[i]);
			cell.setNote(mOldNotes[i]);
		}
	}
}
