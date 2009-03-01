package cz.romario.opensudoku;

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
public class SudokuCell implements Parcelable, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4469146085003371681L;
	private SudokuSector sector;
	private SudokuRow row;
	private SudokuColumn column;
	
	private int value;
	private String notes = "";
	private Boolean editable = false;
	private Boolean invalid = false;


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
	
	// constructor for Parcelable
	private SudokuCell(Parcel in) {
		value = in.readInt();
		notes = in.readString();
		editable = (Boolean)in.readValue(null);
		invalid = (Boolean)in.readValue(null);
	}

	
	public SudokuSector getSector() {
		return sector;
	}
	
	protected void setSector(SudokuSector sector) {
		this.sector = sector;
	}

	public SudokuRow getRow() {
		return row;
	}

	protected void setRow(SudokuRow row) {
		this.row = row;
	}
	
	public SudokuColumn getColumn() {
		return column;
	}
	
	protected void setColumn(SudokuColumn column) {
		this.column = column;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getNotes() {
		return notes;
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
