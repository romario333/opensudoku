package cz.romario.opensudoku.gui;

import cz.romario.opensudoku.game.SudokuCell;
import cz.romario.opensudoku.game.SudokuCellCollection;
import cz.romario.opensudoku.game.SudokuGame;
import cz.romario.opensudoku.gui.EditCellDialog.OnNoteEditListener;
import cz.romario.opensudoku.gui.EditCellDialog.OnNumberEditListener;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

/**
 *  Sudoku board widget.
 *  
 * @author romario
 *
 */
public class SudokuBoardView extends View {

	public static final int DEFAULT_BOARD_SIZE = 100;
	
	private static final String TAG = "SudokuBoardView";
	
	private float mCellWidth;
	private float mCellHeight;
	
	private Paint mLinePaint;
	private Paint mNumberPaint;
	private Paint mNotePaint;
	private int mNumberLeft;
	private int mNumberTop;
	private Paint mReadonlyPaint;
	private Paint mTouchedPaint;
	private Paint mSelectedPaint;
	
	private SudokuCell mTouchedCell;
	private SudokuCell mSelectedCell;
	public boolean mReadonly = false;
	
	private EditCellDialog mEditCellDialog;
	
	private SudokuGame mGame;
	private SudokuCellCollection mCells;
	
	private int mScreenOrientation = -1;
	
	private OnCellTapListener mOnCellTapListener;
	
	public SudokuBoardView(Context context) {
		super(context);
		initWidget();
	}
	
	public SudokuBoardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		initWidget();
	}
	
	public void setGame(SudokuGame game) {
		mGame = game;
		setCells(game.getCells());
	}

	public void setCells(SudokuCellCollection cells) {
		mCells = cells;
		if (!mReadonly) {
			mSelectedCell = mCells.getCell(0, 0); // first cell will be selected by default
		}
		invalidate();
	}
	
	public SudokuCellCollection getCells() {
		return mCells;
	}
	
	public SudokuCell getSelectedCell() {
		return mSelectedCell;
	}
	
	public void setReadOnly(boolean readonly) {
		mReadonly = readonly;
	}
	
	public boolean getReadOnly() {
		return mReadonly;
	}
	
	public void setOnCellTapListener(OnCellTapListener l) {
		mOnCellTapListener = l;
	}
	
	private void initWidget() {
		setFocusable(true);
		setFocusableInTouchMode(true);
		
		setBackgroundColor(Color.WHITE);
		
		mLinePaint = new Paint();
		mLinePaint.setColor(Color.BLACK);
		
		mNumberPaint = new Paint();
		mNumberPaint.setColor(Color.BLACK);
		mNumberPaint.setAntiAlias(true);

		mNotePaint = new Paint();
		mNotePaint.setColor(Color.BLACK);
		mNotePaint.setAntiAlias(true);
		
		mReadonlyPaint = new Paint();
		mReadonlyPaint.setColor(Color.LTGRAY);

		mTouchedPaint = new Paint();
		mTouchedPaint.setColor(Color.rgb(50, 50, 255));
		//touchedPaint.setColor(Color.rgb(100, 255, 100));
		mTouchedPaint.setAlpha(100);
		
		mSelectedPaint = new Paint();
		mSelectedPaint.setColor(Color.YELLOW);
		mSelectedPaint.setAlpha(100);
	}

	/**
	 * Ensures that editCellDialog exists and is properly initialized.
	 * 
	 * @return
	 */
	private void ensureEditCellDialog() {
		if (mEditCellDialog == null) {
			if (mScreenOrientation == -1) {
				mScreenOrientation = getResources().getConfiguration().orientation;
			}
			
			// TODO: EditCellDialog is not ready for landscape
			if (mScreenOrientation != Configuration.ORIENTATION_LANDSCAPE) {
				mEditCellDialog = new EditCellDialog(getContext());
		        mEditCellDialog.setOnNumberEditListener(onNumberEditListener);
		        mEditCellDialog.setOnNoteEditListener(onNoteEditListener);
			}
		}
		
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        
        
//        Log.d(TAG, "widthMode=" + getMeasureSpecModeString(widthMode));
//        Log.d(TAG, "widthSize=" + widthSize);
//        Log.d(TAG, "heightMode=" + getMeasureSpecModeString(heightMode));
//        Log.d(TAG, "heightSize=" + heightSize);
        
        int width = -1, height = -1;
        if (widthMode == MeasureSpec.EXACTLY) {
        	width = widthSize;
        } else {
        	width = DEFAULT_BOARD_SIZE;
        	if (widthMode == MeasureSpec.AT_MOST && width > widthSize ) {
        		width = widthSize;
        	}
        }
        if (heightMode == MeasureSpec.EXACTLY) {
        	height = heightSize;
        } else {
        	height = DEFAULT_BOARD_SIZE;
        	if (heightMode == MeasureSpec.AT_MOST && height > heightSize ) {
        		height = heightSize;
        	}
        }
        
        if (widthMode != MeasureSpec.EXACTLY) {
        	width = height;
        }
        
        if (heightMode != MeasureSpec.EXACTLY) {
        	height = width;
        }
        
    	if (widthMode == MeasureSpec.AT_MOST && width > widthSize ) {
    		width = widthSize;
    	}
    	if (heightMode == MeasureSpec.AT_MOST && height > heightSize ) {
    		height = heightSize;
    	}
        
    	mCellWidth = (width - getPaddingLeft() - getPaddingRight()) / 9.0f;
        mCellHeight = (height - getPaddingTop() - getPaddingBottom()) / 9.0f;

        setMeasuredDimension(width, height);
        
        mNumberPaint.setTextSize(mCellHeight * 0.75f);
        mNotePaint.setTextSize(mCellHeight / 3f);
        // compute offsets in each cell to center the rendered number
        mNumberLeft = (int) ((mCellWidth - mNumberPaint.measureText("9")) / 2);
        mNumberTop = (int) ((mCellHeight - mNumberPaint.getTextSize()) / 2);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		// some notes:
		// Drawable has its own draw() method that takes your Canvas as an arguement
		
//		int width = getMeasuredWidth();
//		int height = getMeasuredHeight();
		// TODO: padding?
		int width = getWidth() - getPaddingLeft() - getPaddingRight();
		int height = getHeight() - getPaddingTop() - getPaddingBottom();
		
		int paddingLeft = getPaddingLeft();
		int paddingTop = getPaddingTop();
		
		// draw cells
		int cellLeft, cellTop;
		if (mCells != null) {
			
			// TODO: why?
			float numberAscent = mNumberPaint.ascent();
			float noteAscent = mNotePaint.ascent();
			float noteWidth = mCellWidth / 3f;
			for (int row=0; row<9; row++) {
				for (int col=0; col<9; col++) {
					SudokuCell cell = mCells.getCell(row, col);
					
					cellLeft = Math.round((col * mCellWidth) + paddingLeft);
					cellTop = Math.round((row * mCellHeight) + paddingTop);

					// draw read-only field background
					if (!cell.getEditable()) {
						canvas.drawRect(
								cellLeft, cellTop, 
								cellLeft + mCellWidth, cellTop + mCellHeight,
								mReadonlyPaint);
					}
					
					// draw cell Text
					int value = cell.getValue();
					if (value != 0) {
						mNumberPaint.setColor(cell.getInvalid() ? Color.RED : Color.BLACK);
						canvas.drawText(new Integer(value).toString(),
								cellLeft + mNumberLeft, 
								Math.round(cellTop) + mNumberTop - numberAscent, 
								mNumberPaint);
					} else {
						
						if (cell.hasNote()) {
							Integer[] numbers = getNoteNumbers(cell.getNote());
							if (numbers != null) {
								for (Integer number : numbers) {
									if (number >= 1 && number <= 9) {
										int n = number - 1;
										int c = n % 3;
										int r = n / 3;
										canvas.drawText(number.toString(), cellLeft + c*noteWidth + 2, cellTop - noteAscent + r*noteWidth - 1, mNotePaint);
									}
								}
							}
						}
					}
					
					
						
				}
			}
			
			// highlight selected cell
			if (!mReadonly && mSelectedCell != null) {
				cellLeft = Math.round(mSelectedCell.getColumnIndex() * mCellWidth);
				cellTop = Math.round(mSelectedCell.getRowIndex() * mCellHeight);
				canvas.drawRect(
						cellLeft, cellTop, 
						cellLeft + mCellWidth, cellTop + mCellHeight,
						mSelectedPaint);
			}
			
			// visually highlight cell under the finger (to cope with touch screen
			// imprecision)
			if (mTouchedCell != null) {
				cellLeft = Math.round(mTouchedCell.getColumnIndex() * mCellWidth);
				cellTop = Math.round(mTouchedCell.getRowIndex() * mCellHeight);
				canvas.drawRect(
						cellLeft, 0,
						cellLeft + mCellWidth, height,
						mTouchedPaint);
				canvas.drawRect(
						0, cellTop,
						width, cellTop + mCellHeight,
						mTouchedPaint);
			}

		}
		
		// draw vertical lines
		for (int c=0; c <= 9; c++) {
			float x = c * mCellWidth;
			if (c % 3 == 0) {
				canvas.drawRect(x-1, 0, x+1, height, mLinePaint);
			} else {
				canvas.drawLine(x, 0, x, height, mLinePaint);
			}
		}
		
		// draw horizontal lines
		for (int r=0; r <= 9; r++) {
			float y = r * mCellHeight;
			if (r % 3 == 0) {
				canvas.drawRect(0, y-1, width, y+1, mLinePaint);
			} else {
				canvas.drawLine(0, y, width, y, mLinePaint);
			}
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		if (!mReadonly) {
			int x = (int)event.getX();
			int y = (int)event.getY();
			
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_MOVE:
				mTouchedCell = getCellAtPoint(x, y);
				break;
			case MotionEvent.ACTION_UP:
				mSelectedCell = getCellAtPoint(x, y);
				
				boolean selectNumberShowed = false;
				if (mSelectedCell != null) {
					if (mOnCellTapListener != null) {
						mOnCellTapListener.onCellTap(mSelectedCell);
					}
					ensureEditCellDialog();
					if (mSelectedCell.getEditable() && mEditCellDialog != null) {
						mEditCellDialog.updateNumber(mSelectedCell.getValue());
						mEditCellDialog.updateNote(getNoteNumbers(mSelectedCell.getNote()));
						mEditCellDialog.getDialog().show();
						selectNumberShowed = true;
					}
				}
				
				// If select number dialog wasn't showed, clear touched cell highlight, if dialog
				// is visible, highlight will be cleared after dialog is dismissed.
				if (!selectNumberShowed) {
					mTouchedCell = null;
				}
				break;
			case MotionEvent.ACTION_CANCEL:
				mTouchedCell = null;
				break;
			}
			invalidate();
		}
		
		return !mReadonly;
	}
	
	// TODO: do I really need this?
	@Override
	public boolean onTrackballEvent(MotionEvent event) {
    	// Actually, just let these come through as D-pad events.
    	return false;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (!mReadonly) {
			switch (keyCode) {
				case KeyEvent.KEYCODE_DPAD_UP:
					return moveCellSelection(0, -1);
				case KeyEvent.KEYCODE_DPAD_RIGHT:
					return moveCellSelection(1, 0);
				case KeyEvent.KEYCODE_DPAD_DOWN:
					return moveCellSelection(0, 1);
				case KeyEvent.KEYCODE_DPAD_LEFT:
					return moveCellSelection(-1, 0);
				case KeyEvent.KEYCODE_0:
				case KeyEvent.KEYCODE_SPACE:
				case KeyEvent.KEYCODE_DEL:
					// clear value in selected cell
					if (mSelectedCell != null) {
						setCellValue(mSelectedCell, 0);
						moveCellSelectionRight();
					}
					return true;
			}
			
			if (keyCode >= KeyEvent.KEYCODE_1 && keyCode <= KeyEvent.KEYCODE_9) {
				// enter request number in cell
				int selectedNumber = keyCode - KeyEvent.KEYCODE_0;
				setCellValue(mSelectedCell, selectedNumber);
				moveCellSelectionRight();
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Occurs when user selects number in EditCellDialog.
	 */
    private OnNumberEditListener onNumberEditListener = new OnNumberEditListener() {
		@Override
		public boolean onNumberEdit(int number) {
    		SudokuCell selectedCell = getSelectedCell();
    		if (number != -1) {
                // set cell number selected by user
				setCellValue(selectedCell, number);
				mTouchedCell = null;
				invalidate();
    		}
			return true;
		}
	};
	
	/**
	 * Occurs when user edits note in EditCellDialog
	 */
	private OnNoteEditListener onNoteEditListener = new OnNoteEditListener() {
		@Override
		public boolean onNoteEdit(Integer[] numbers) {
			SudokuCell selectedCell = getSelectedCell();
			if (selectedCell != null) {
				setCellNote(selectedCell, setNoteNumbers(numbers));
				mTouchedCell = null;
				invalidate();
			}
			return true;
		}
	};
	
	private void setCellValue(SudokuCell cell, int value) {
		if (cell.getEditable()) {
			if (mGame != null) {
				mGame.setCellValue(cell, value);
			} else {
				cell.setValue(value);
			}
		}
	}
	
	private void setCellNote(SudokuCell cell, String note) {
		if (cell.getEditable()) {
			if (mGame != null) {
				mGame.setCellNote(cell, note);
			} else {
				cell.setNote(note);
			}
		}
	}
	
	
	/**
	 * Moves selected cell by one cell to the right. If edge is reached, selection
	 * skips on beginning of another line. 
	 */
	private void moveCellSelectionRight() {
		if (!moveCellSelection(1, 0)) {
			int selRow = mSelectedCell.getRowIndex();
			selRow++;
			if (!moveCellSelectionTo(selRow, 0)) {
				moveCellSelectionTo(0, 0);
			}
		}
	}
	
	/**
	 * Moves selected by vx cells right and vy cells down. vx and vy can be negative. Returns true,
	 * if new cell is selected.
	 * 
	 * @param vx Horizontal offset, by which move selected cell.
	 * @param vy Vertical offset, by which move selected cell.
	 */
	private boolean moveCellSelection(int vx, int vy) {
		int newRow = 0;
		int newCol = 0;
		
		if (mSelectedCell != null) {
			newRow = mSelectedCell.getRowIndex() + vy;
			newCol = mSelectedCell.getColumnIndex() + vx;
		}
		
		return moveCellSelectionTo(newRow, newCol);
	}
	
	
	/**
	 * Moves selection to the cell given by row and column index.
	 * @param row Row index of cell which should be selected.
	 * @param col Columnd index of cell which should be selected.
	 * @return True, if cell was successfuly selected.
	 */
	private boolean moveCellSelectionTo(int row, int col) {
		if(col >= 0 && col < SudokuCellCollection.SUDOKU_SIZE 
				&& row >= 0 && row < SudokuCellCollection.SUDOKU_SIZE) {
			mSelectedCell = mCells.getCell(row, col);
			postInvalidate();
			return true;
		}
		
		return false;
	}
	
	/**
	 * Get cell at given screen coordinates. Returns null if no cell is found.
	 * @param x
	 * @param y
	 * @return
	 */
	private SudokuCell getCellAtPoint(int x, int y) {
		// TODO: this is not nice, col/row vs x/y
		
		int row = (int) (y / mCellHeight);
		int col = (int) (x / mCellWidth);
		
		if(col >= 0 && col < SudokuCellCollection.SUDOKU_SIZE 
				&& row >= 0 && row < SudokuCellCollection.SUDOKU_SIZE) {
			return mCells.getCell(row, col);
		} else {
			return null;
		}
	}
	
	/**
	 * Returns content of note as array of numbers. Note is expected to be
	 * in format "n,n,n".
	 * 
	 * @return
	 */
	private Integer[] getNoteNumbers(String note) {
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
	 * TODO: find better name for this method
	 * 
	 * @param numbers
	 */
	private String setNoteNumbers(Integer[] numbers) {
		StringBuffer sb = new StringBuffer();
		
		for (Integer number : numbers) {
			sb.append(number).append(",");
		}
		
		return sb.toString();
	}
	
	
	public interface OnCellTapListener
	{
		/**
		 * Called when a cell is tapped (by finger).
		 * @param cell
		 * @return
		 */
		void onCellTap(SudokuCell cell);
	}

	private String getMeasureSpecModeString(int mode) {
		String modeString = null;
		switch (mode) {
		case MeasureSpec.AT_MOST:
			modeString = "MeasureSpec.AT_MOST";
			break;
		case MeasureSpec.EXACTLY:
			modeString = "MeasureSpec.EXACTLY";
			break;
		case MeasureSpec.UNSPECIFIED:
			modeString = "MeasureSpec.UNSPECIFIED";
			break;
		}
		
		if (modeString == null)
			modeString = new Integer(mode).toString();
		
		return modeString;
	}
	


}
