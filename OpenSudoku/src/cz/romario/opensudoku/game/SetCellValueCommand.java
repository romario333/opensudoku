package cz.romario.opensudoku.game;

// TODO: How will I serialize this?
public class SetCellValueCommand implements Command {

	private SudokuCell cell;
	private int value;
	private int oldValue;
	
	public SetCellValueCommand(SudokuCell cell, int value) {
		this.cell = cell;
		this.value = value;
	}
	
	@Override
	public void execute() {
		oldValue = cell.getValue();
		cell.setValue(value);
	}

	@Override
	public void undo() {
		cell.setValue(oldValue);
	}

}
