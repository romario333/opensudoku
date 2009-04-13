package cz.romario.opensudoku.game;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Collection of sudoku cells. This class in fact represents one sudoku board (9x9).
 * 
 * @author romario
 *
 */
public class SudokuCellCollection  implements Parcelable {
	
	// TODO: An array of ints is a much better than an array of Integers, but this also generalizes to the fact that two parallel arrays of ints are also a lot more efficient than an array of (int,int) objects
	// Cell's data.
	private SudokuCell[][] cells;
	
	// Helper arrays, contains references to the groups of cells, which should contain unique
	// numbers.
	private SudokuCellGroup[] sectors;
	private SudokuCellGroup[] rows;
	private SudokuCellGroup[] columns;
	
	public static final int SUDOKU_SIZE = 9;
	
	/**
	 * Creates empty sudoku.
	 * @return
	 */
	public static SudokuCellCollection createEmpty()
	{
		SudokuCell[][] cells = new SudokuCell[SUDOKU_SIZE][SUDOKU_SIZE];
		
		for (int r=0; r<SUDOKU_SIZE; r++)
		{
			
			for (int c=0; c<SUDOKU_SIZE; c++)
			{
				cells[r][c] = new SudokuCell();
			}
		}
		
		return new SudokuCellCollection(cells);
	}

	// TODO: SudokuCellCollection should be collection, implement Enumerable or something like that
	public SudokuCell[][] getCells() {
		return cells;
	}
	
	/**
	 * Wraps given array in this object.
	 * @param cells
	 */
	private SudokuCellCollection(SudokuCell[][] cells)
	{
		
		this.cells = cells;
		initCollection();
	}
	
	/**
	 * Gets cell at given position.
	 * @param rowIndex
	 * @param colIndex
	 * @return
	 */
	public SudokuCell getCell(int rowIndex, int colIndex) {
		return cells[rowIndex][colIndex];
	}
	
	public void markAllCellsAsValid() {
		for (int r=0; r<SUDOKU_SIZE; r++)
		{
			for (int c=0; c<SUDOKU_SIZE; c++)
			{
				cells[r][c].setInvalid(false);
			}
		}
	}
	
	/**
	 * Validates numbers in collection according to the sudoku rules. Cells with invalid
	 * values are marked - you can use getInvalid method of cell to find out whether cell
	 * contains valid value.
	 * 
	 * @return True if validation is successful. 
	 */
	public boolean validate() {
		boolean valid = true;
		
		// first set all cells as valid
		markAllCellsAsValid();
		
		// run validation in groups
		for (SudokuCellGroup row : rows) {
			if (!row.validate()) {
				valid = false;
			}
		}
		for (SudokuCellGroup column : columns) {
			if (!column.validate()) {
				valid = false;
			}
		}
		for (SudokuCellGroup sector : sectors) {
			if (!sector.validate()) {
				valid = false;
			}
		}

		return valid;
	}
	
	public boolean isCompleted() {
		for (int r=0; r<SUDOKU_SIZE; r++)
		{
			for (int c=0; c<SUDOKU_SIZE; c++)
			{
				SudokuCell cell = cells[r][c]; 
				if (cell.getValue() == 0 || cell.getInvalid()) {
					return false;
				}
			}
		}
		return true;
	}
	
//	/** 
//	 * Sets value in given cell.
//	 * @param cell
//	 * @param value
//	 */
//	public void setValue(SudokuCell cell, int value) {
//		if (cell != null && cell.getEditable()) {
//			cell.setValue(value);
//		}
//	}
	
	
//	/**
//	 * Sets value in cell given by its row and column index.
//	 * @param rowIndex
//	 * @param columnIndex
//	 * @param value
//	 */
//	public void setValue(int rowIndex, int columnIndex, int value) {
//		SudokuCell cell = getCell(rowIndex, columnIndex);
//		setValue(cell, value);
//	}
	
//	/**
//	 * Sets note for given cell from array of numbers and fires change event. 
//	 * @param cell
//	 * @param numbers
//	 */
//	public void setNoteNumbers(SudokuCell cell, Integer[] numbers) {
//		if (cell != null) {
//			cell.setNoteNumbers(numbers);
//			onChange();
//		}
//	}

	/**
	 * Marks all cells as editable.
	 */
	public void markAllCellsAsEditable() {
		// TODO: iterator
		for (int r=0; r<SUDOKU_SIZE; r++) {
			for (int c=0; c<SUDOKU_SIZE; c++){
				SudokuCell cell = cells[r][c];
				cell.setEditable(true);
			}
		}
	}
	
	/**
	 * Marks all filled cells (cells with value other than 0) as not editable.
	 */
	public void markFilledCellsAsNotEditable() {
		// TODO: iterator
		for (int r=0; r<SUDOKU_SIZE; r++) {
			for (int c=0; c<SUDOKU_SIZE; c++){
				SudokuCell cell = cells[r][c];
				cell.setEditable(cell.getValue() == 0);
			}
		}
	}
	
	/**
	 * Clears all notes attached to cells in the collection.
	 */
	public void clearAllNotes() {
		// TODO: iterator
		for (int r=0; r<SUDOKU_SIZE; r++) {
			for (int c=0; c<SUDOKU_SIZE; c++){
				SudokuCell cell = cells[r][c];
				cell.setNote(null);
			}
		}
	}
	
	/**
	 * Initializes collection, initialization has two steps:
	 * 1) Groups of cells which must contain unique numbers are created.
	 * 2) Row and column index for each cell is set.
	 */
	private void initCollection() {
		rows = new SudokuCellGroup[SUDOKU_SIZE];
		columns = new SudokuCellGroup[SUDOKU_SIZE];
		sectors = new SudokuCellGroup[SUDOKU_SIZE];

		for (int i=0; i<SUDOKU_SIZE; i++) {
			rows[i] = new SudokuCellGroup();
			columns[i] = new SudokuCellGroup();
			sectors[i] = new SudokuCellGroup();
		}
		
		for (int r=0; r<SUDOKU_SIZE; r++)
		{
			for (int c=0; c<SUDOKU_SIZE; c++)
			{
				SudokuCell cell = cells[r][c];
				
				cell.initCollection(r, c,
						sectors[((c/3) * 3) + (r/3)],
						rows[c],
						columns[r]
						);
			}
		}
	}

	/**
	 * Contructor because of Parcelable support.
	 * 
	 * @param in
	 */
	private SudokuCellCollection(Parcel in) {
		
		cells = new SudokuCell[SUDOKU_SIZE][SUDOKU_SIZE];
		for (int row=0; row<SUDOKU_SIZE; row++) {
			Parcelable[] rowData = (Parcelable[])in.readParcelableArray(SudokuCell.class.getClassLoader());
			for (int col=0; col < rowData.length; col++) {
				cells[row][col] = (SudokuCell)rowData[col];
			}
		}
		initCollection();
	}
	
	public static final Parcelable.Creator<SudokuCellCollection> CREATOR = new Parcelable.Creator<SudokuCellCollection>() {
		public SudokuCellCollection createFromParcel(Parcel in) {
		    return new SudokuCellCollection(in);
		}
		
		public SudokuCellCollection[] newArray(int size) {
		    return new SudokuCellCollection[size];
		}
	};
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		for (SudokuCell[] cols : cells) {
			dest.writeParcelableArray(cols, flags);
		}
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		for (int r=0; r<SUDOKU_SIZE; r++)
		{
			for (int c=0; c<SUDOKU_SIZE; c++)
			{
				sb.append(cells[r][c].getValue());
			}
			sb.append("\n");
		}
		sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}
	
	// TODO: find some standard way of serialization
	/**
	 * Creates instance of sudoku collection from String, which was earlier created
	 * by writeToString method.
	 */
	public static SudokuCellCollection deserialize(String data) {
		SudokuCell[][] cells = new SudokuCell[SUDOKU_SIZE][SUDOKU_SIZE];

        String[] lines = data.split("\n");
        if (lines.length == 0) {
            throw new IllegalArgumentException("Cannot deserialize Sudoku, data corrupted.");
        }
        
        if (!lines[0].equals("version: 1")) {
            throw new IllegalArgumentException(String.format("Unknown version of data: %s", lines[0]));
        }
        
        StringTokenizer tokenizer = new StringTokenizer(lines[1], "|");
        int r = 0, c = 0;
        while (tokenizer.hasMoreTokens() && r < 9) {
            SudokuCell cell = new SudokuCell();
            cell.setValue(Integer.parseInt(tokenizer.nextToken()));
            String note = tokenizer.nextToken(); 
            if (!note.equals("-")) {
            	cell.setNote(note);
            }
            cell.setEditable(tokenizer.nextToken().equals("1"));
            
            cells[r][c] = cell;
            c++;
            
            if (c == 9) {
            	r++;
            	c = 0;
            }
        }

        return new SudokuCellCollection(cells);
	}
	
	/**
	 * Writes collection to String. You can later recreate the object instance
	 * by calling createFromString method.
	 * @return
	 */
	public String serialize() {
		StringBuffer sb = new StringBuffer();
		sb.append("version: 1\n");
        
        for (int r=0; r<SUDOKU_SIZE; r++)
        {
                for (int c=0; c<SUDOKU_SIZE; c++)
                {
                        SudokuCell cell = cells[r][c];
                        sb.append(cell.getValue()).append("|");
                        if (cell.getNote() == null || cell.getNote().equals("")) {
                        	sb.append("-").append("|");
                        } else {
                        	sb.append(cell.getNote()).append("|");
                        }
                        sb.append(cell.getEditable() ? "1" : "0").append("|");
                }
        }
        
        return sb.toString();
	}
}
