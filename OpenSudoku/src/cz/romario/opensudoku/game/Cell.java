/* 
 * Copyright (C) 2009 Roman Masek
 * 
 * This file is part of OpenSudoku.
 * 
 * OpenSudoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * OpenSudoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with OpenSudoku.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package cz.romario.opensudoku.game;

import java.util.StringTokenizer;

/**
 * Sudoku cell. Every cell has value, some notes attached to it and some basic
 * state (whether it is editable and valid).
 *
 * @author romario
 */
public class Cell {
	// if cell is included in collection, here are some additional information 
	// about collection and cell's position in it
	private CellCollection mCellCollection;
	private final Object mCellCollectionLock = new Object();
	private int mRowIndex = -1;
	private int mColumnIndex = -1;
	private CellGroup mSector; // sector containing this cell
	private CellGroup mRow; // row containing this cell
	private CellGroup mColumn; // column containing this cell

	private int mValue;
	private CellNote mNote;
	private CellHint mHint;
	private boolean mEditable;
	private boolean mValid;

	/**
	 * Creates empty editable cell.
	 */
	public Cell() {
		this(0, new CellNote(), true, true);
	}

	/**
	 * Creates empty editable cell containing given value.
	 *
	 * @param value Value of the cell.
	 */
	public Cell(int value) {
		this(value, new CellNote(), true, true);
	}

	private Cell(int value, CellNote note, boolean editable, boolean valid) {
		if (value < 0 || value > 9) {
			throw new IllegalArgumentException("Value must be between 0-9.");
		}

		mValue = value;
		mNote = note;
		mHint = new CellHint(this);
		mEditable = editable;
		mValid = valid;
	}

	/**
	 * Gets cell's row index within {@link CellCollection}.
	 *
	 * @return Cell's row index within CellCollection.
	 */
	public int getRowIndex() {
		return mRowIndex;
	}

	/**
	 * Gets cell's column index within {@link CellCollection}.
	 *
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
	 * @param sector   Reference to sector group in which cell is included.
	 * @param row      Reference to row group in which cell is included.
	 * @param column   Reference to column group in which cell is included.
	 */
	protected void initCollection(CellCollection cellCollection, int rowIndex, int colIndex,
								  CellGroup sector, CellGroup row, CellGroup column) {
		synchronized (mCellCollectionLock) {
			mCellCollection = cellCollection;
		}

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
		if (value < 0 || value > 9) {
			throw new IllegalArgumentException("Value must be between 0-9.");
		}
		mValue = value;
		mHint.isAvailable(value);
		onChange();
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
		onChange();
	}
	
	/**
	* Gets hints attached to the cell.
	*
	* @return Hint attached to the cell.
	*/
	public CellHint getHint() {
		return mHint;
	}

	/**
	 * Returns whether cell can be edited.
	 *
	 * @return True if cell can be edited.
	 */
	public boolean isEditable() {
		return mEditable;
	}

	/**
	 * Sets whether cell can be edited.
	 *
	 * @param editable True, if cell should allow editing.
	 */
	public void setEditable(Boolean editable) {
		mEditable = editable;
		onChange();
	}

	/**
	 * Sets whether cell contains valid value according to sudoku rules.
	 *
	 * @param valid
	 */
	public void setValid(Boolean valid) {
		mValid = valid;
		onChange();
	}

	/**
	 * Returns true, if cell contains valid value according to sudoku rules.
	 *
	 * @return True, if cell contains valid value according to sudoku rules.
	 */
	public boolean isValid() {
		return mValid;
	}


	/**
	 * Creates instance from given <code>StringTokenizer</code>.
	 *
	 * @param data
	 * @return
	 */
	public static Cell deserialize(StringTokenizer data) {
		Cell cell = new Cell();
		cell.setValue(Integer.parseInt(data.nextToken()));
		cell.setNote(CellNote.deserialize(data.nextToken()));
		cell.setEditable(data.nextToken().equals("1"));

		return cell;
	}

	/**
	 * Creates instance from given string (string which has been
	 * created by {@link #serialize(StringBuilder)} or {@link #serialize()} method).
	 * earlier.
	 *
	 * @param note
	 */
	public static Cell deserialize(String cellData) {
		StringTokenizer data = new StringTokenizer(cellData, "|");
		return deserialize(data);
	}


	/**
	 * Appends string representation of this object to the given <code>StringBuilder</code>.
	 * You can later recreate object from this string by calling {@link #deserialize}.
	 *
	 * @param data
	 */
	public void serialize(StringBuilder data) {
		data.append(mValue).append("|");
		if (mNote == null || mNote.isEmpty()) {
			data.append("-").append("|");
		} else {
			mNote.serialize(data);
			data.append("|");
		}
		data.append(mEditable ? "1" : "0").append("|");
	}

	public String serialize() {
		StringBuilder sb = new StringBuilder();
		serialize(sb);
		return sb.toString();
	}

	/**
	 * Notify CellCollection that something has changed.
	 */
	private void onChange() {
		synchronized (mCellCollectionLock) {
			if (mCellCollection != null) {
				mCellCollection.onChange();
			}

		}
	}
}
