package cz.romario.opensudoku.game;

// TODO: complete
public class ClearAllNotesCommand implements Command {

	private SudokuCellCollection cells;
	
	
	public ClearAllNotesCommand(SudokuCellCollection cells) {
		this.cells = cells;
	}
	
	@Override
	public void execute() {
		
		
	}

	@Override
	public void undo() {
		// TODO Auto-generated method stub
		
	}

}
