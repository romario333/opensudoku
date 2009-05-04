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
public class SudokuCell implements Parcelable {
	// if cell is included in collection, here are some information about cell's position
	private int mRowIndex = -1;
	private int mColumnIndex = -1;
	private SudokuCellGroup mSector; // sector containing this cell
	private SudokuCellGroup mRow; // row containing this cell
	private SudokuCellGroup mColumn; // column containing this cell
	
	private int mValue;
	private boolean mEditable = false;
	private boolean mInvalid = false;
	//private int[] mNoteNumbers = new int[10];
	private Set<Integer> mNoteNumbers = new HashSet<Integer>();
	
	public SudokuCell() {
		mEditable = true;
		mInvalid = false;
	}
	
	public SudokuCell(int value) {
		this();
		mValue = value;
	}
	
	/**
	 * Gets cell's row index within SudokuCellCollection.
	 * @return
	 */
	public int getRowIndex() {
		return mRowIndex;
	}
	
	/**
	 * Gets cell's column index within SudokuCellColection.
	 * @return
	 */
	public int getColumnIndex() {
		return mColumnIndex;
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
	public SudokuCellGroup getSector() {
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
	public SudokuCellGroup getRow() {
		return mRow;
	}

	/**
	 * Returns column containing this cell.
	 * 
	 * @return
	 */
	public SudokuCellGroup getColumn() {
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

	public Collection<Integer> getNoteNumbers() {
		return mNoteNumbers;		
	}
	
	/**
	 * Sets note attached to the cell.
	 * 
	 * @param notes
	 */
	public void setNote(String note) {
		if (note == null || note.equals(""))
		{
			mNoteNumbers.clear();
//			for (int i=1; i<mNoteNumbers.length; i++) {
//				mNoteNumbers[i] = 0;
//			}
		} else {
			mNoteNumbers.clear();

			StringTokenizer tokenizer = new StringTokenizer(note, ",");
	        while (tokenizer.hasMoreTokens()) {
	        	mNoteNumbers.add(Integer.parseInt(tokenizer.nextToken()));
//	            mNoteNumbers[Integer.parseInt(tokenizer.nextToken())] = 1;
	        }
		}
	}

	/**
	 * Gets note attached to the cell.
	 * 
	 * @return
	 */
	public String getNote() {
		StringBuffer sb = new StringBuffer();
		
//		for (int i=1; i<mNoteNumbers.length; i++) {
//			if (mNoteNumbers[i] == 1) {
//				sb.append(i).append(",");
//			}
//		}
		
		for (Integer i : mNoteNumbers) {
			sb.append(i).append(",");
		}
		
		return sb.toString();
	}
	
	// TODO: think again about SudokuCell's interface concerning notes, also take into account
	// EditCellNoteCommand and especially the fact, that SudokuBoard onDraw needs to know
	// which numbers are noted
	public static String numberListToNoteString(Collection<Integer> numbers) {
		StringBuffer sb = new StringBuffer();
		for (Integer num : numbers) {
			sb.append(num).append(",");
		}
		return sb.toString();
	}
	public static String numberListToNoteString(Integer[] numbers) {
		StringBuffer sb = new StringBuffer();
		for (Integer num : numbers) {
			sb.append(num).append(",");
		}
		return sb.toString();
	}
	
	/**
	 * Returns true, if cell has some note attached to it.
	 * 
	 * @return
	 */
	public boolean hasNote() {
		return mNoteNumbers.size() != 0;
//		for (int i=1; i<mNoteNumbers.length; i++) {
//			if(mNoteNumbers[i] == 1)
//				return true;
//		}
//		return false;
	}

	public void setNoteNumber(int number, boolean isSet) {
		if (isSet) {
			mNoteNumbers.add(number);
		} else {
			mNoteNumbers.remove(new Integer(number));
		}

//		mNoteNumbers[number] = isSet ? 1 : 0;
	}
	
	// TODO: ugly quick fix
	public Collection<Integer> toggleNoteNumber(int number) {
		Set<Integer> nums = new HashSet<Integer>();
		
		for (Integer n : mNoteNumbers) {
			nums.add(n);
		}
		
		Integer n = new Integer(number);
		if (nums.contains(n)) {
			nums.remove(n);
		} else {
			nums.add(n);
		}
		return nums;
			
		
//		if (mNoteNumbers[number] == 1) {
//			mNoteNumbers[number] = 0;
//		} else {
//			mNoteNumbers[number] = 1;
//		}
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
	private SudokuCell(Parcel in) {
		mValue = in.readInt();
		setNote(in.readString());
		mEditable = (Boolean)in.readValue(null);
		mInvalid = (Boolean)in.readValue(null);
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
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(mValue);
		dest.writeString(getNote());
		dest.writeValue(mEditable);
		dest.writeValue(mInvalid);
	}
}
