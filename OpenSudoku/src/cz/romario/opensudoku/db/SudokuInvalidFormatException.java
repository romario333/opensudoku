package cz.romario.opensudoku.db;

public class SudokuInvalidFormatException extends Exception {

	private static final long serialVersionUID = -5415032786641425594L;

	private String mData;

	public SudokuInvalidFormatException(String data) {
		super("Invalid format of sudoku, ^\\d{81}$ exptected.");
		mData = data;
	}
	
	public String getData() {
		return mData;
	}

}
