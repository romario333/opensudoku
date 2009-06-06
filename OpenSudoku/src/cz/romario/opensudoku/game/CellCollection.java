package cz.romario.opensudoku.game;

import java.util.StringTokenizer;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Collection of sudoku cells. This class in fact represents one sudoku board (9x9).
 * 
 * @author romario
 *
 */
public class CellCollection  implements Parcelable {
	
	// TODO: An array of ints is a much better than an array of Integers, but this also generalizes to the fact that two parallel arrays of ints are also a lot more efficient than an array of (int,int) objects
	// Cell's data.
	private Cell[][] mCells;
	
	// Helper arrays, contains references to the groups of cells, which should contain unique
	// numbers.
	private CellGroup[] mSectors;
	private CellGroup[] mRows;
	private CellGroup[] mColumns;
	
	public static final int SUDOKU_SIZE = 9;
	
	/**
	 * Creates empty sudoku.
	 * @return
	 */
	public static CellCollection createEmpty()
	{
		Cell[][] cells = new Cell[SUDOKU_SIZE][SUDOKU_SIZE];
		
		for (int r=0; r<SUDOKU_SIZE; r++)
		{
			
			for (int c=0; c<SUDOKU_SIZE; c++)
			{
				cells[r][c] = new Cell();
			}
		}
		
		return new CellCollection(cells);
	}
	
	/**
	 * Generates debug game.
	 * 
	 * @return
	 */
	public static CellCollection createDebugGame() {
		CellCollection debugGame = new CellCollection(new Cell[][] {
                { new Cell(), new Cell(), new Cell(), new Cell(4), new Cell(5), new Cell(6), new Cell(7), new Cell(8), new Cell(9),},
                { new Cell(), new Cell(), new Cell(), new Cell(7), new Cell(8), new Cell(9), new Cell(1), new Cell(2), new Cell(3),},
                { new Cell(), new Cell(), new Cell(), new Cell(1), new Cell(2), new Cell(3), new Cell(4), new Cell(5), new Cell(6),},
                { new Cell(2), new Cell(3), new Cell(4), new Cell(), new Cell(), new Cell(), new Cell(8), new Cell(9), new Cell(1),},
                { new Cell(5), new Cell(6), new Cell(7), new Cell(), new Cell(), new Cell(), new Cell(2), new Cell(3), new Cell(4),},
                { new Cell(8), new Cell(9), new Cell(1), new Cell(), new Cell(), new Cell(), new Cell(5), new Cell(6), new Cell(7),},
                { new Cell(3), new Cell(4), new Cell(5), new Cell(6), new Cell(7), new Cell(8), new Cell(9), new Cell(1), new Cell(2),},
                { new Cell(6), new Cell(7), new Cell(8), new Cell(9), new Cell(1), new Cell(2), new Cell(3), new Cell(4), new Cell(5),},
                { new Cell(9), new Cell(1), new Cell(2), new Cell(3), new Cell(4), new Cell(5), new Cell(6), new Cell(7), new Cell(8),},
        });
		debugGame.markFilledCellsAsNotEditable();
		return debugGame;
	}
	
	public Cell[][] getCells() {
		return mCells;
	}
	
	/**
	 * Wraps given array in this object.
	 * @param cells
	 */
	private CellCollection(Cell[][] cells)
	{
		
		mCells = cells;
		initCollection();
	}
	
	/**
	 * Gets cell at given position.
	 * @param rowIndex
	 * @param colIndex
	 * @return
	 */
	public Cell getCell(int rowIndex, int colIndex) {
		return mCells[rowIndex][colIndex];
	}
	
	public void markAllCellsAsValid() {
		for (int r=0; r<SUDOKU_SIZE; r++)
		{
			for (int c=0; c<SUDOKU_SIZE; c++)
			{
				mCells[r][c].setValid(true);
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
		for (CellGroup row : mRows) {
			if (!row.validate()) {
				valid = false;
			}
		}
		for (CellGroup column : mColumns) {
			if (!column.validate()) {
				valid = false;
			}
		}
		for (CellGroup sector : mSectors) {
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
				Cell cell = mCells[r][c]; 
				if (cell.getValue() == 0 || !cell.isValid()) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Marks all cells as editable.
	 */
	public void markAllCellsAsEditable() {
		// TODO: implement iterator? (consider also performance)
		for (int r=0; r<SUDOKU_SIZE; r++) {
			for (int c=0; c<SUDOKU_SIZE; c++){
				Cell cell = mCells[r][c];
				cell.setEditable(true);
			}
		}
	}
	
	/**
	 * Marks all filled cells (cells with value other than 0) as not editable.
	 */
	public void markFilledCellsAsNotEditable() {
		for (int r=0; r<SUDOKU_SIZE; r++) {
			for (int c=0; c<SUDOKU_SIZE; c++){
				Cell cell = mCells[r][c];
				cell.setEditable(cell.getValue() == 0);
			}
		}
	}
	
	/**
	 * Initializes collection, initialization has two steps:
	 * 1) Groups of cells which must contain unique numbers are created.
	 * 2) Row and column index for each cell is set.
	 */
	private void initCollection() {
		mRows = new CellGroup[SUDOKU_SIZE];
		mColumns = new CellGroup[SUDOKU_SIZE];
		mSectors = new CellGroup[SUDOKU_SIZE];

		for (int i=0; i<SUDOKU_SIZE; i++) {
			mRows[i] = new CellGroup();
			mColumns[i] = new CellGroup();
			mSectors[i] = new CellGroup();
		}
		
		for (int r=0; r<SUDOKU_SIZE; r++)
		{
			for (int c=0; c<SUDOKU_SIZE; c++)
			{
				Cell cell = mCells[r][c];
				
				cell.initCollection(r, c,
						mSectors[((c/3) * 3) + (r/3)],
						mRows[c],
						mColumns[r]
						);
			}
		}
	}

	/**
	 * Contructor because of Parcelable support.
	 * 
	 * @param in
	 */
	private CellCollection(Parcel in) {
		
		mCells = new Cell[SUDOKU_SIZE][SUDOKU_SIZE];
		for (int row=0; row<SUDOKU_SIZE; row++) {
			Parcelable[] rowData = (Parcelable[])in.readParcelableArray(Cell.class.getClassLoader());
			for (int col=0; col < rowData.length; col++) {
				mCells[row][col] = (Cell)rowData[col];
			}
		}
		initCollection();
	}
	
	public static final Parcelable.Creator<CellCollection> CREATOR = new Parcelable.Creator<CellCollection>() {
		public CellCollection createFromParcel(Parcel in) {
		    return new CellCollection(in);
		}
		
		public CellCollection[] newArray(int size) {
		    return new CellCollection[size];
		}
	};
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		for (Cell[] cols : mCells) {
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
				sb.append(mCells[r][c].getValue());
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
	public static CellCollection deserialize(String data) {
		Cell[][] cells = new Cell[SUDOKU_SIZE][SUDOKU_SIZE];

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
            Cell cell = new Cell();
            cell.setValue(Integer.parseInt(tokenizer.nextToken()));
            String note = tokenizer.nextToken(); 
            if (!note.equals("-")) {
            	// TODO: asi kazdy z techle objektu by mel mit serialize a deserialize, udelej z toho interface
            	cell.setNote(CellNote.deserialize(note));
            }
            cell.setEditable(tokenizer.nextToken().equals("1"));
            
            cells[r][c] = cell;
            c++;
            
            if (c == 9) {
            	r++;
            	c = 0;
            }
        }

        return new CellCollection(cells);
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
                        Cell cell = mCells[r][c];
                        sb.append(cell.getValue()).append("|");
                        if (cell.getNote() == null || cell.getNote().equals("")) {
                        	sb.append("-").append("|");
                        } else {
                        	sb.append(cell.getNote().serialize()).append("|");
                        }
                        sb.append(cell.isEditable() ? "1" : "0").append("|");
                }
        }
        
        return sb.toString();
	}
}
