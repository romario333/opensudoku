package cz.romario.opensudoku.gui;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

import cz.romario.opensudoku.game.SudokuCell;
import cz.romario.opensudoku.game.SudokuCellCollection;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class SudokuBoardView extends View {

	private int cellWidth;
	private int cellHeight;
	
	private Paint linePaint;
	private Paint numberPaint;
	private int numberLeft;
	private int numberTop;
	private Paint readonlyPaint;
	private Paint touchedPaint;
	private Paint selectedPaint;
	
	private SudokuCell touchedCell = null;
	private SudokuCell selectedCell = null;
	public boolean readonly = false;
	
	private SudokuCellCollection cells;
	
	private OnCellSelectedListener onCellSelectedListener;
	
	public SudokuBoardView(Context context) {
		super(context);
		
		initWidget();
	}
	
	public SudokuBoardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		initWidget();
	}
	
	public void setCells(SudokuCellCollection cells) {
		this.cells = cells;
		this.invalidate();
	}

	public SudokuCellCollection getCells() {
		return cells;
	}
	
	public void setReadOnly(boolean readonly) {
		this.readonly = readonly;
	}
	
	public boolean getReadOnly() {
		return readonly;
	}
	
	public void setOnCellSelectedListener(OnCellSelectedListener l) {
		onCellSelectedListener = l;
	}
	
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        
        int width;
        int height;
        
        if (widthMode == MeasureSpec.EXACTLY) {
        	// Parent has told us how big to be.
        	width = widthSize;
        } else {
        	// TOOD: jaky dat default?
        	width = 100;
        }
        
        if (heightMode == MeasureSpec.EXACTLY) {
        	// Parent has told us how big to be.
        	height = heightSize;
        } else {
        	// TOOD: jaky dat default?
        	height = 100;
        }
        
        // sudoku will always be square
        // TODO: predpokladam ze vyska px je stejna jako sirka
        int size = Math.min(width, height);
        width = size;
        height = size;
        
        cellWidth = width / 9;
        cellHeight = height / 9;

        // TODO: zohlednovat padding
        setMeasuredDimension(cellWidth * 9 + 1, cellHeight * 9 + 1);
        
        numberPaint.setTextSize(cellHeight * 0.75f);
        // compute offsets in each cell to center the rendered number
        numberLeft = (int) ((cellWidth - numberPaint.measureText("9")) / 2);
        numberTop = (int) ((cellHeight - numberPaint.getTextSize()) / 2);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		int width = getMeasuredWidth();
		int height = getMeasuredHeight();

		// draw cells
		if (cells != null) {
			
			int touchedCellLeft = -1;
			int touchedCellTop = -1;
			
			float numberAscent = numberPaint.ascent();
			for (int x=0; x<9; x++) {
				for (int y=0; y<9; y++) {
					SudokuCell cell = cells.getCell(x, y);
					
					int cellLeft = x * cellWidth;
					int cellTop = y * cellHeight;

					// draw read-only field background
					if (!cell.getEditable()) {
						canvas.drawRect(
								cellLeft, cellTop, 
								cellLeft + cellWidth, cellTop + cellHeight,
								readonlyPaint);
					}
					
					// draw cell Text
					int value = cell.getValue();
					if (value != 0) {
						numberPaint.setColor(cell.getInvalid() ? Color.RED : Color.BLACK);
						canvas.drawText(new Integer(value).toString(),
								cellLeft + numberLeft, 
								cellTop + numberTop - numberAscent, 
								numberPaint);
					}
					
					// draw highlighted cell
					if (cell == selectedCell) {
						canvas.drawRect(
								cellLeft, cellTop, 
								cellLeft + cellWidth, cellTop + cellHeight,
								selectedPaint);
					}
					
					if (cell == touchedCell) {
						touchedCellLeft = cellLeft;
						touchedCellTop = cellTop;
					}
				}
			}
			
			// visually highlight cell under the finger (to cope with touch screen
			// imprecision)
			if (touchedCellLeft != -1) {
				canvas.drawRect(
						touchedCellLeft, 0,
						touchedCellLeft + cellWidth, height,
						touchedPaint);
				canvas.drawRect(
						0, touchedCellTop,
						width, touchedCellTop + cellHeight,
						touchedPaint);
			}

		}
		
		// draw vertical lines
		for (int x=0; x < width; x = x + cellWidth) {
			if (x % 3 == 0) {
				canvas.drawRect(x-1, 0, x+1, height, linePaint);
			} else {
				canvas.drawLine(x, 0, x, height, linePaint);
			}
		}
		
		// draw horizontal lines
		for (int y=0; y < height; y = y + cellHeight) {
			if (y % 3 == 0) {
				canvas.drawRect(0, y-1, width, y+1, linePaint);
			} else {
				canvas.drawLine(0, y, width, y, linePaint);
			}
		}
	}
	
	private void initWidget() {
		setBackgroundColor(Color.WHITE);
		
		linePaint = new Paint();
		linePaint.setColor(Color.BLACK);
		
		numberPaint = new Paint();
		numberPaint.setColor(Color.BLACK);
		numberPaint.setAntiAlias(true);
		
		readonlyPaint = new Paint();
		readonlyPaint.setColor(Color.LTGRAY);

		touchedPaint = new Paint();
		touchedPaint.setColor(Color.rgb(100, 255, 100));
		touchedPaint.setAlpha(100);
		
		selectedPaint = new Paint();
		selectedPaint.setColor(Color.YELLOW);
		selectedPaint.setAlpha(100);
	}
	
	
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		if (!readonly) {
			// TODO: potrebuju podporovat zarizeni s touch displayem s vetsi presnosti nez 1px?
			int x = (int)event.getX();
			int y = (int)event.getY();
			
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_MOVE:
				touchedCell = getCellAtPoint(x, y);
				break;
			case MotionEvent.ACTION_UP:
				touchedCell = null;
				selectedCell = getCellAtPoint(x, y);
				
				if (selectedCell != null && onCellSelectedListener != null) {
					Boolean res = onCellSelectedListener.onCellSelected(selectedCell);
					if (!res) {
						selectedCell = null;
					}
				}
				break;
			case MotionEvent.ACTION_CANCEL:
				touchedCell = null;
				break;
			}
			invalidate();
		}
		
		return true;
	}
	
	private SudokuCell getCellAtPoint(int x, int y) {
//		int xcol = 0;
//		while (x > (xcol * cellWidth)) {
//			xcol++;
//		}
		
		// TODO: zamyslet se, bude vzdy presne?
		int xcol = x / cellWidth;
		int ycol = y / cellHeight;
		
		if(xcol >= 0 && ycol >= 0 && xcol < 9 && ycol < 9) {
			return cells.getCell(xcol, ycol);
		} else {
			return null;
		}
		
	}
	
	public interface OnCellSelectedListener
	{
		boolean onCellSelected(SudokuCell cell);
	}



}
