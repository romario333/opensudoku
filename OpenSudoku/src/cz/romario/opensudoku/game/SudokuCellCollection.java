package cz.romario.opensudoku.game;

import android.os.Parcel;
import android.os.Parcelable;

public class SudokuCellCollection  implements Parcelable {
	// Cell's data.
	private SudokuCell[][] cells;
	
	// Helper arrays, contains references to the groups of cells, which should contain unique
	// numbers.
	private SudokuCellGroup[] sectors;
	private SudokuCellGroup[] rows;
	private SudokuCellGroup[] columns;
	
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
	
	public static SudokuCellCollection CreateEmpty()
	{
		SudokuCell[][] cells = new SudokuCell[SUDOKU_SIZE][SUDOKU_SIZE];
		
		for (int x=0; x<SUDOKU_SIZE; x++)
		{
			
			for (int y=0; y<SUDOKU_SIZE; y++)
			{
				cells[x][y] = new SudokuCell();
			}
		}
		
		return new SudokuCellCollection(cells);
	}
	
	// TODO: private .ctor
	private SudokuCellCollection(SudokuCell[][] cells)
	{
		this.cells = cells;
		initSudoku();
	}
	
	public SudokuCell getCell(int x, int y) {
		// TODO: assert
		return cells[x][y];
	}
	
	public void validate() {
		for (int x=0; x<SUDOKU_SIZE; x++)
		{
			for (int y=0; y<SUDOKU_SIZE; y++)
			{
				cells[x][y].setInvalid(false);
			}
		}
		
		for (SudokuCellGroup row : rows) {
			row.validate();
		}
		for (SudokuCellGroup column : columns) {
			column.validate();
		}
		for (SudokuCellGroup sector : sectors) {
			sector.validate();
		}
	}
	
	public boolean isCompleted() {
		for (int x=0; x<SUDOKU_SIZE; x++)
		{
			for (int y=0; y<SUDOKU_SIZE; y++)
			{
				SudokuCell cell = cells[x][y]; 
				if (cell.getValue() == 0 || cell.getInvalid()) {
					return false;
				}
			}
		}
		return true;
	}
	
	// constructor for Parcelable
	private SudokuCellCollection(Parcel in) {
		
		cells = new SudokuCell[SUDOKU_SIZE][SUDOKU_SIZE];
		for (int row=0; row<SUDOKU_SIZE; row++) {
			Parcelable[] rowData = (Parcelable[])in.readParcelableArray(SudokuCell.class.getClassLoader());
			for (int col=0; col < rowData.length; col++) {
				cells[row][col] = (SudokuCell)rowData[col];
			}
		}
		initSudoku();
	}

	private void initSudoku() {
		rows = new SudokuCellGroup[SUDOKU_SIZE];
		columns = new SudokuCellGroup[SUDOKU_SIZE];
		sectors = new SudokuCellGroup[SUDOKU_SIZE];

		for (int i=0; i<SUDOKU_SIZE; i++) {
			rows[i] = new SudokuCellGroup();
			columns[i] = new SudokuCellGroup();
			sectors[i] = new SudokuCellGroup();
		}
		
		for (int x=0; x<SUDOKU_SIZE; x++)
		{
			for (int y=0; y<SUDOKU_SIZE; y++)
			{
				rows[y].addCell(cells[x][y]);
				columns[x].addCell(cells[x][y]);
				// TODO: tohle nevim, v debugu tam jsou podezrely hodnoty
				sectors[((y/3) * 3) + (x/3)].addCell(cells[x][y]);
			}
		}
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

		for (int x=0; x<SUDOKU_SIZE; x++)
		{
			for (int y=0; y<SUDOKU_SIZE; y++)
			{
				sb.append(cells[x][y].getValue());
			}
			sb.append("\n");
		}
		sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}
	
	// TODO: temporary
	public void updateFromString(String data, boolean setEditable) {
		String[] lines = data.split("\n");
		
		for (int x=0; x<SUDOKU_SIZE; x++)
		{
			for (int y=0; y<SUDOKU_SIZE; y++)
			{
				// omg, neco mi urcite unika
				int value = Integer.parseInt(new Character(lines[x].charAt(y)).toString());
				cells[x][y].setValue(value);
				if (setEditable && value != 0) {
					cells[x][y].setEditable(false);
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
        for (int x=0; x<SUDOKU_SIZE; x++)
        {
                for (int y=0; y<SUDOKU_SIZE; y++)
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
                        
                        cells[x][y] = cell;
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
        
        for (int x=0; x<SUDOKU_SIZE; x++)
        {
                for (int y=0; y<SUDOKU_SIZE; y++)
                {
                        SudokuCell cell = cells[x][y];
                        sb.append(cell.getValue()).append("|");
                        sb.append(cell.getNotes()).append("|"); 
                        sb.append(cell.getEditable() ? "1" : "0").append("|");
                        sb.append(cell.getInvalid() ? "1" : "0").append("\n");
                }
        }
        
        return sb.toString();
	}
}
