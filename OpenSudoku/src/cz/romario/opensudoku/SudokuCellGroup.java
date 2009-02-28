package cz.romario.opensudoku;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;



public class SudokuCellGroup {
	private SudokuCell[] cells = new SudokuCell[Sudoku.SUDOKU_SIZE];
	private int pos = 0;
	
	public void addCell(SudokuCell cell) {
		cells[pos] = cell;
		pos++;
	}
	
	public void validate() {
		// TODO: asi bude pomale
		Map<Integer, List<SudokuCell>> cellsByValue = 
			new HashMap<Integer, List<SudokuCell>>();
		
		for (int i=0; i<cells.length; i++) {
			int value = cells[i].getValue();
			
			if (!cellsByValue.containsKey(value)) {
				cellsByValue.put(value, new ArrayList<SudokuCell>());
			}
			cellsByValue.get(value).add(cells[i]);
		}
		
		for (Entry<Integer, List<SudokuCell>> cellsForValue : cellsByValue.entrySet()) {
			if (cellsForValue.getValue() != null && cellsForValue.getValue().size() > 1) {
				for (SudokuCell cell : cellsForValue.getValue()) {
					cell.setInvalid(true);
				}
			}
		}
	}
	

}
