package cz.romario.opensudoku;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class Sudoku  implements Parcelable {
	private SudokuCell[][] cells;
	
	private SudokuCellGroup[] sectors;
	private SudokuCellGroup[] rows;
	private SudokuCellGroup[] columns;
	
	public static final int SUDOKU_SIZE = 9;
	
	public static Sudoku CreateDebugGame() {
		return new Sudoku(new SudokuCell[][] {
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
	
	public static Sudoku CreateEmpty()
	{
		SudokuCell[][] cells = new SudokuCell[SUDOKU_SIZE][SUDOKU_SIZE];
		
		for (int x=0; x<SUDOKU_SIZE; x++)
		{
			
			for (int y=0; y<SUDOKU_SIZE; y++)
			{
				cells[x][y] = new SudokuCell();
			}
		}
		
		return new Sudoku(cells);
	}
	
	// TODO: private .ctor
	public Sudoku(SudokuCell[][] cells)
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
	
	// constructor for Parcelable
	private Sudoku(Parcel in) {
		// TODO: ach jo, blba serizalizace, proc nefunguje?
		for (int x=0; x<SUDOKU_SIZE; x++)
		{
			for (int y=0; y<SUDOKU_SIZE; y++)
			{
				cells[x][y] = in.readParcelable(SudokuCell.class.getClassLoader());
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
	public static final Parcelable.Creator<Sudoku> CREATOR = new Parcelable.Creator<Sudoku>() {
		public Sudoku createFromParcel(Parcel in) {
		    return new Sudoku(in);
		}
		
		public Sudoku[] newArray(int size) {
		    return new Sudoku[size];
		}
	};
	
	@Override
	public int describeContents() {
		// TODO nevim k cemu je
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		for (int x=0; x<SUDOKU_SIZE; x++)
		{
			for (int y=0; y<SUDOKU_SIZE; y++)
			{
				dest.writeParcelable(cells[x][y], flags);
			}
		}
		
	}
	
	

}
