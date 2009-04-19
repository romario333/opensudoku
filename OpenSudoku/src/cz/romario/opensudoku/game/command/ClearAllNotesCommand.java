package cz.romario.opensudoku.game.command;

import cz.romario.opensudoku.game.SudokuCellCollection;

// TODO: complete this
public class ClearAllNotesCommand implements Command {

	private SudokuCellCollection mCells;
	
	
	public ClearAllNotesCommand(SudokuCellCollection cells) {
		this.mCells = cells;
	}
	
	@Override
	public void execute() {
		
	}

	@Override
	public void undo() {
		
	}

}
