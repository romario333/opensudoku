package cz.romario.opensudoku.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

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
	private boolean mInvalid;
	
	public Cell() {
		this (0, new CellNote(), true, false);
	}
	
	// TODO: fakt tohle nekde pouzivam?
	public Cell(int value) {
		this(value, new CellNote(), true, false);
		mValue = value;
	}
	
	public Cell(int value, CellNote note, boolean editable, boolean invalid) {
		mValue = value;
		mNote = note;
		mEditable = editable;
		mInvalid = invalid;
	}
	
	/**
	 * Gets cell's row index within <code>CellCollection</code>.
	 * @return 
	 */
	public int getRowIndex() {
		return mRowIndex;
	}
	
	/**
	 * Gets cell's column index within <code>CellColection</code>.
	 * @return
	 */
	public int getColumnIndex() {
		return mColumnIndex;
	}
	
	/**
	 * Called when <code>Cell</code> is added to <code>CellCollection</code>.  
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
	 * @return
	 */
	public CellGroup getSector() {
		return mSector;
	}
	
	/**
	 * Returns row containing this cell.
	 * 
	 * 
	 * 
	 * 
	 * @return
	 */
	public CellGroup getRow() {
		return mRow;
	}

	/**
	 * Returns column containing this cell.
	 * 
	 * @return
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
	 * Gets cell's value. Value can be 1-9 or 0 if cell should be empty.
	 * @return
	 */
	public int getValue() {
		return mValue;
	}


	/**
	 * Gets note attached to the cell.
	 * 
	 * @return
	 */
	public CellNote getNote() {
		return mNote;
	}
	
	/**
	 * Sets note attached to the cell
	 * @param note
	 */
	public void setNote(CellNote note) {
		mNote = note;
	}
	
	public boolean isEditable() {
		return mEditable;
	}
	
	public void setEditable(Boolean editable) {
		mEditable = editable;
	}
	
	public void setInvalid(Boolean invalid) {
		mInvalid = invalid;
	}

	public boolean isInvalid() {
		return mInvalid;
	}
	
	// constructor for Parcelable
	private Cell(Parcel in) {
		mValue = in.readInt();
		setNote(CellNote.deserialize(in.readString()));
		mEditable = (Boolean)in.readValue(null);
		mInvalid = (Boolean)in.readValue(null);
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
		dest.writeValue(mInvalid);
	}
}
