package cz.romario.opensudoku.game;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Collection of sudoku cells. This class in fact represents one sudoku board (9x9).
 * 
 * @author romario
 *
 */
public class SudokuCellCollection  implements Parcelable {
	// Cell's data.
	private SudokuCell[][] cells;
	
	// Helper arrays, contains references to the groups of cells, which should contain unique
	// numbers.
	private SudokuCellGroup[] sectors;
	private SudokuCellGroup[] rows;
	private SudokuCellGroup[] columns;
	
	private List<OnChangeListener> onChangeListeners = new ArrayList<OnChangeListener>();
	
	public static final int SUDOKU_SIZE = 9;
	
	public static SudokuCellCollection CreateDebugGame() {
		return new SudokuCellCollection(new SudokuCell[][] {
			{ new SudokuCell(), new SudokuCell(), new SudokuCell(), new SudokuCell(), new SudokuCell(6), new SudokuCell(), new SudokuCell(), new SudokuCell(), new SudokuCell(9),},
			{ new SudokuCell(), new SudokuCell(4), new SudokuCell(), new SudokuCell(), new SudokuCell(), new SudokuCell(), new SudokuCell(), new SudokuCell(), new SudokuCell(3),},
			{ new SudokuCell(5), new SudokuCell(6), new SudokuCell(), new SudokuCell(), new SudokuCell(8), new SudokuCell(), new SudokuCell(), new SudokuCell(), new SudokuCell(),},
			{ new SudokuCell(), new SudokuCell(), new SudokuCell(7), new SudokuCell(), new SudokuCell(), new SudokuCell(), new SudokuCell(9), new SudokuCell(8), new SudokuCell(),},
			{ new SudokuCell(8), new SudokuCell(), new SudokuCell(), new SudokuCell(9), new SudokuCell(3), new SudokuCell(7), new SudokuCell(), new SudokuCell(), new SudokuCell(2),},
			{ new SudokuCell(6), new SudokuCell(), new SudokuCell(), new SudokuCell(5), new SudokuCell(), new SudokuCell(), new SudokuCell(7), new SudokuCell(), new SudokuCell(),},
			{ new SudokuCell(), new SudokuCell(), new SudokuCell(), new SudokuCell(), new SudokuCell(), new SudokuCell(), new SudokuCell(2), new SudokuCell(5), new SudokuCell(),},
			{ new SudokuCell(), new SudokuCell(2), new SudokuCell(), new SudokuCell(7), new SudokuCell(), new SudokuCell(3), new SudokuCell(), new SudokuCell(), new SudokuCell(),},
			{ new SudokuCell(7), new SudokuCell(), new SudokuCell(1), new SudokuCell(), new SudokuCell(), new SudokuCell(4), new SudokuCell(), new SudokuCell(), new SudokuCell(),},
		});
	}
	
	/**
	 * Creates empty sudoku.
	 * @return
	 */
	public static SudokuCellCollection CreateEmpty()
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
		// TODO: assert
		return cells[rowIndex][colIndex];
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
		for (int r=0; r<SUDOKU_SIZE; r++)
		{
			for (int c=0; c<SUDOKU_SIZE; c++)
			{
				cells[r][c].setInvalid(false);
			}
		}
		
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
	
	/** 
	 * Sets value in given cell.
	 * @param cell
	 * @param value
	 */
	public void setValue(SudokuCell cell, int value) {
		// TODO: check whether is cell editable or not
		if (cell != null && cell.getEditable()) {
			cell.setValue(value);
		}
		onChange();
	}
	
	
	/**
	 * Sets value in cell given by its row and column index.
	 * @param rowIndex
	 * @param columnIndex
	 * @param value
	 */
	public void setValue(int rowIndex, int columnIndex, int value) {
		SudokuCell cell = getCell(rowIndex, columnIndex);
		setValue(cell, value);
	}
	
	// TODO: check that this is ok with Observer pattern and do some performance testing
	// TODO: it seems that generally in android there can be just one listener at a time, why?
	/** 
	 * Registers listener, which will be called, when collection is changed.
	 * Listener will be called synchronously.
	 * @param l
	 */
	public void addOnChangeListener(OnChangeListener l) {
		onChangeListeners.add(l);
	}
	
	public void removeOnChangeListener(OnChangeListener l) {
		onChangeListeners.remove(l);
	}
	
	/**
	 * Fires OnChange event.
	 */
	protected void onChange() {
		for (OnChangeListener l : onChangeListeners) {
			l.onChange();
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
	
	// TODO: Parcelable bych nemel ukladat do databaze, mozna bych se mel uchylit k jinemu
	// druhu serializace (asi do xml)
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
		// TODO nevim k cemu je
		return 0;
	}

	// TODO: k cemu jsou ty flags??
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
	
	// TODO: temporary
	public void updateFromString(String data, boolean setEditable) {
		String[] lines = data.split("\n");
		
		for (int r=0; r<SUDOKU_SIZE; r++)
		{
			for (int c=0; c<SUDOKU_SIZE; c++)
			{
				// omg, neco mi urcite unika
				int value = Integer.parseInt(new Character(lines[r].charAt(c)).toString());
				cells[r][c].setValue(value);
				if (setEditable && value != 0) {
					cells[r][c].setEditable(false);
				}
			}
		}
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

        int pos=1;
        for (int r=0; r<SUDOKU_SIZE; r++)
        {
                for (int c=0; c<SUDOKU_SIZE; c++)
                {
                        String line = lines[pos++];
                        String[] parts = line.split("\\|");
                        
                        if (parts.length != 4) {
                                throw new IllegalArgumentException(String.format("Illegal item: %s", line));
                        }
                        
                        int value = Integer.parseInt(parts[0]);
                        String note = parts[1];
                        boolean editable = parts[2].equals("1");
                        boolean invalid = parts[3].equals("1");
                        
                        SudokuCell cell = new SudokuCell();
                        cell.setValue(value);
                        cell.setNotes(note);
                        cell.setEditable(editable);
                        cell.setInvalid(invalid);
                        
                        cells[r][c] = cell;
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
                        sb.append(cell.getNotes()).append("|"); 
                        sb.append(cell.getEditable() ? "1" : "0").append("|");
                        sb.append(cell.getInvalid() ? "1" : "0").append("\n");
                }
        }
        
        return sb.toString();
	}
	
	public interface OnChangeListener
	{
		boolean onChange();
	}

}
