package cz.romario.opensudoku.game;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Sudoku cell. Every cell has value, some notes attached to it and some basic
 * state (whether it is editable and valid).
 * 
 * Implements Parcelable, however references to sector, row and column are not serialized.
 * 
 * @author romario
 *
 */
public class SudokuCell implements Parcelable {
	// if cell is included in collection, here are some information about cell's position
	private int rowIndex = -1;
	private int columnIndex = -1;
	private SudokuCellGroup sector; // sector to which cell belongs
	private SudokuCellGroup row; // row to which cell belongs
	private SudokuCellGroup column; // column to which cell belongs
	
	private int value;
	private String note = "";
	private Boolean editable = false;
	private Boolean invalid = false;
	
	public SudokuCell() {
		note = "";
		editable = true;
		invalid = false;
	}
	
	/**
	 * Gets cell's row index within SudokuCellCollection.
	 * @return
	 */
	public int getRowIndex() {
		return rowIndex;
	}
	
	/**
	 * Gets cell's column index within SudokuCellColection.
	 * @return
	 */
	public int getColumnIndex() {
		return columnIndex;
	}
	
	/**
	 * Called when SudokuCell is added to SudokuCellCollection.  
	 * 
	 * @param rowIndex Cell's row index within collection.
	 * @param colIndex Cell's column index within collection.
	 * @param sector Reference to sector group in which cell is included.
	 * @param row Reference to row group in which cell is included.
	 * @param column Reference to column group in which cell is included. 
	 */
	protected void initCollection(int rowIndex, int colIndex, SudokuCellGroup sector, SudokuCellGroup row, SudokuCellGroup column) {
		
		
		this.rowIndex = rowIndex;
		this.columnIndex = colIndex;
		this.sector = sector;
		this.row = row;
		this.column = column;
		
		sector.addCell(this);
		row.addCell(this);
		column.addCell(this);
	}
	
	/**
	 * Returns sector to which this cell belongs. Sector is 3x3 group of cells.
	 * 
	 * @return
	 */
	public SudokuCellGroup getSector() {
		return sector;
	}
	
	/**
	 * TODO: This sounds weird, correct it before commit: Return row to which this cell belongs.
	 * 
	 * 
	 * @return
	 */
	public SudokuCellGroup getRow() {
		return row;
	}

	/**
	 * Returns column to which this cell belongs.
	 * 
	 * @return
	 */
	public SudokuCellGroup getColumn() {
		return column;
	}
	
	/**
	 * Sets cell's value. Value can be 1-9 or 0 if cell should be empty. 
	 * 
	 * @param value 1-9 or 0 if cell should be empty.
	 */
	public void setValue(int value) {
		assert value >= 0 && value < 10;
		
		this.value = value;
	}

	/**
	 * Gets cell's value. Value can be 1-9 or 0 if cell should be empty.
	 * @return
	 */
	public int getValue() {
		return value;
	}

	/**
	 * Sets note attached to the cell.
	 * 
	 * @param notes
	 */
	public void setNote(String notes) {
		this.note = notes;
	}

	/**
	 * Gets note attached to the cell.
	 * 
	 * @return
	 */
	public String getNote() {
		return note;
	}
	
	/**
	 * Returns true, if cell has some note attached to it.
	 * 
	 * @return
	 */
	public boolean hasNote() {
		return note != null && note != "";
	}

	/**
	 * Returns content of note as array of numbers. Note is expected to be
	 * in format "n,n,n".
	 * 
	 * @return
	 */
	public Integer[] getNoteNumbers() {
		String note = getNote();
		if (note == null || note.equals(""))
			return null;
		
		String[] numberStrings = note.split(",");
		Integer[] numbers = new Integer[numberStrings.length];
		for (int i=0; i<numberStrings.length; i++) {
			numbers[i] = Integer.parseInt(numberStrings[i]);
		}
		
		return numbers;
	}
	
	/**
	 * Creates content of note from array of numbers. Note will be stored
	 * in "n,n,n" format.
	 * 
	 * @param numbers
	 */
	public void setNoteNumbers(Integer[] numbers) {
		StringBuffer sb = new StringBuffer();
		
		for (Integer number : numbers) {
			sb.append(number).append(",");
		}
		
		setNote(sb.toString());
	}

	public Boolean getEditable() {
		return editable;
	}
	
	public void setEditable(Boolean editable) {
		this.editable = editable;
	}
	
	public void setInvalid(Boolean invalid) {
		this.invalid = invalid;
	}

	public Boolean getInvalid() {
		return invalid;
	}
	
	// constructor for Parcelable
	private SudokuCell(Parcel in) {
		value = in.readInt();
		note = in.readString();
		editable = (Boolean)in.readValue(null);
		invalid = (Boolean)in.readValue(null);
	}

	public static final Parcelable.Creator<SudokuCell> CREATOR = new Parcelable.Creator<SudokuCell>() {
		public SudokuCell createFromParcel(Parcel in) {
		    return new SudokuCell(in);
		}
		
		public SudokuCell[] newArray(int size) {
		    return new SudokuCell[size];
		}
	};
	
	@Override
	public int describeContents() {
		// TODO nevim k cemu je
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(value);
		dest.writeString(note);
		dest.writeValue(editable);
		dest.writeValue(invalid);
	}
}
