package cz.romario.opensudoku.game;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Sudoku cell. Every cell has value, some notes attached to it and some basic
 * state (whether it is editable and valid).
 * 
 * Implements <code>Parcelable</code>, however references to sector, row and column are not serialized.
 * 
 * @author romario
 *
 */
public class Cell implements Parcelable {
	// if cell is included in collection, here are some information about cell's position
	private int mRowIndex = -1;
	private int mColumnIndex = -1;
	private CellGroup mSector; // sector containing this cell
	private CellGroup mRow; // row containing this cell
	private CellGroup mColumn; // column containing this cell
	
	private int mValue;
	private CellNote mNote;
	private boolean mEditable;
	private boolean mValid;
	
	/**
	 * Creates empty editable cell.
	 */
	public Cell() {
		this (0, new CellNote(), true, true);
	}
	
	/**
	 * Creates empty editable cell containing given value.
	 * @param value Value of the cell.
	 */
	public Cell(int value) {
		this(value, new CellNote(), true, true);
		mValue = value;
	}
	
	private Cell(int value, CellNote note, boolean editable, boolean valid) {
		mValue = value;
		mNote = note;
		mEditable = editable;
		mValid = valid;
	}
	
	/**
	 * Gets cell's row index within {@link CellCollection}.
	 * @return Cell's row index within CellCollection.
	 */
	public int getRowIndex() {
		return mRowIndex;
	}
	
	/**
	 * Gets cell's column index within {@link CellCollection}.
	 * @return Cell's column index within CellColection.
	 */
	public int getColumnIndex() {
		return mColumnIndex;
	}
	
	/**
	 * Called when <code>Cell</code> is added to {@link CellCollection}.  
	 * 
	 * @param rowIndex Cell's row index within collection.
	 * @param colIndex Cell's column index within collection.
	 * @param sector Reference to sector group in which cell is included.
	 * @param row Reference to row group in which cell is included.
	 * @param column Reference to column group in which cell is included. 
	 */
	protected void initCollection(int rowIndex, int colIndex, CellGroup sector, CellGroup row, CellGroup column) {
		mRowIndex = rowIndex;
		mColumnIndex = colIndex;
		mSector = sector;
		mRow = row;
		mColumn = column;
		
		sector.addCell(this);
		row.addCell(this);
		column.addCell(this);
	}
	
	/**
	 * Returns sector containing this cell. Sector is 3x3 group of cells.
	 * 
	 * @return Sector containing this cell.
	 */
	public CellGroup getSector() {
		return mSector;
	}
	
	/**
	 * Returns row containing this cell.
	 * 
	 * @return Row containing this cell.
	 */
	public CellGroup getRow() {
		return mRow;
	}

	/**
	 * Returns column containing this cell.
	 * 
	 * @return Column containing this cell.
	 */
	public CellGroup getColumn() {
		return mColumn;
	}
	
	/**
	 * Sets cell's value. Value can be 1-9 or 0 if cell should be empty. 
	 * 
	 * @param value 1-9 or 0 if cell should be empty.
	 */
	public void setValue(int value) {
		assert value >= 0 && value < 10;
		
		mValue = value;
	}

	/**
	 * Gets cell's value. Value can be 1-9 or 0 if cell is empty.
	 * 
	 * @return Cell's value. Value can be 1-9 or 0 if cell is empty.
	 */
	public int getValue() {
		return mValue;
	}


	/**
	 * Gets note attached to the cell.
	 * 
	 * @return Note attached to the cell.
	 */
	public CellNote getNote() {
		return mNote;
	}
	
	/**
	 * Sets note attached to the cell
	 * 
	 * @param note Note attached to the cell
	 */
	public void setNote(CellNote note) {
		mNote = note;
	}
	
	/**
	 * Returns whether cell can be edited.
	 * @return True if cell can be edited.
	 */
	public boolean isEditable() {
		return mEditable;
	}
	
	/**
	 * Sets whether cell can be edited.
	 * @param editable True, if cell should allow editing.
	 */
	public void setEditable(Boolean editable) {
		mEditable = editable;
	}
	
	/**
	 * Sets whether cell contains valid value according to sudoku rules.
	 * 
	 * @param valid
	 */
	public void setValid(Boolean valid) {
		mValid = valid;
	}

	/**
	 * Returns true, if cell contains valid value according to sudoku rules.
	 * 
	 * @return True, if cell contains valid value according to sudoku rules.
	 */
	public boolean isValid() {
		return mValid;
	}
	
	// constructor for Parcelable
	private Cell(Parcel in) {
		mValue = in.readInt();
		setNote(CellNote.deserialize(in.readString()));
		mEditable = (Boolean)in.readValue(null);
		mValid = (Boolean)in.readValue(null);
	}

	public static final Parcelable.Creator<Cell> CREATOR = new Parcelable.Creator<Cell>() {
		public Cell createFromParcel(Parcel in) {
		    return new Cell(in);
		}
		
		public Cell[] newArray(int size) {
		    return new Cell[size];
		}
	};
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(mValue);
		dest.writeString(mNote.serialize());
		dest.writeValue(mEditable);
		dest.writeValue(mValid);
	}
}
