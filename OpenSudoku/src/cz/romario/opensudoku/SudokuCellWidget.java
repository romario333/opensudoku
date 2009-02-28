package cz.romario.opensudoku;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

public class SudokuCellWidget extends TextView {

	private Paint borderPaint;
	
	public SudokuCellWidget(Context context) {
		super(context);
		initPaints();
	}
	
	public SudokuCellWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		initPaints();
	}
	
	private void initPaints() {
		borderPaint = new Paint();
		borderPaint.setColor(Color.YELLOW);
	}
	
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		int size = Math.min(getMeasuredWidth(), getMeasuredHeight());
		setMeasuredDimension(size, size);
		
		setTextSize(size * 0.75f);
		setGravity(Gravity.CENTER);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		
		// draw border
		int width = getMeasuredWidth()-1;
		int height = getMeasuredHeight()-1;
		canvas.drawLine(0, 0, width, 0, borderPaint);
		canvas.drawLine(width, 0, width, height, borderPaint);
		canvas.drawLine(width, height, 0, height, borderPaint);
		canvas.drawLine(0, height, 0, 0, borderPaint);

		super.onDraw(canvas);
	}
}
