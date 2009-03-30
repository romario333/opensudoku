package cz.romario.opensudoku.game;

import java.io.Serializable;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * Sudoku cell.
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
	private SudokuCellGroup sector;
	private SudokuCellGroup row;
	private SudokuCellGroup column;
	
	private int value;
	private String notes = "";
	private Boolean editable = false;
	private Boolean invalid = false;
	
	
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
	 * Called when SudokuCell is added to collection. 
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


	public SudokuCell() {
		notes = "";
		editable = true;
		invalid = false;
	}
	
	// TODO: tohle je spis pro muj debug
	public SudokuCell(int value) {
		this.value = value;
		editable = false;
		invalid = false;
	}
	
	public SudokuCellGroup getSector() {
		return sector;
	}
	
	public SudokuCellGroup getRow() {
		return row;
	}

	public SudokuCellGroup getColumn() {
		return column;
	}
	
	public void setValue(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	// TODO: note by byl hezci nazev
	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getNotes() {
		return notes;
	}
	
	public boolean hasNote() {
		return notes != null && notes != "";
	}

	/**
	 * Returns content of note as array of numbers.
	 * @return
	 */
	public Integer[] getNoteNumbers() {
		String note = getNotes();
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
	 * Creates content of note from array of numbers.
	 * @param numbers
	 */
	public void setNoteNumbers(Integer[] numbers) {
		StringBuffer sb = new StringBuffer();
		
		for (Integer number : numbers) {
			sb.append(number).append(",");
		}
		
		setNotes(sb.toString());
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
		notes = in.readString();
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
		dest.writeString(notes);
		dest.writeValue(editable);
		dest.writeValue(invalid);
	}
}
