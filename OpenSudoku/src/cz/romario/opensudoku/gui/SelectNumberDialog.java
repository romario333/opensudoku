package cz.romario.opensudoku.gui;

import cz.romario.opensudoku.game.SudokuCell;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class SelectNumberDialog {
	private Context context;
	private Dialog dialog;
	
	private OnNumberSelectListener onNumberSelectListener;
	
	public SelectNumberDialog(Context context) {
		this.context = context;
		
		// TODO: maybe I should just create my own dialog, this is just quick hack
		dialog = new AlertDialog.Builder(context)
		.setIcon(android.R.drawable.ic_menu_set_as)
        .setTitle("Select number")
        .setView(createSelectNumberView())
        .setPositiveButton("Clear", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	// 0 as clear
    			if (onNumberSelectListener != null) {
    				onNumberSelectListener.onNumberSelect(0);
    			}
            }
        })
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                /* User clicked No so do some stuff */
            }
        })
       .create();
	}
	
	/**
	 * Registers a callback to be invoked when number is selected.
	 * @param l
	 */
	public void setOnNumberSelectListener(OnNumberSelectListener l) {
		this.onNumberSelectListener = l;
	}
	
	/**
	 * Returns dialog instance.
	 * @return
	 */
	public Dialog getDialog() {
		return this.dialog;
	}
	
	/**
     * Creates 3x3 table of number buttons (1 to 9).
     * @return
     */
	private View createSelectNumberView() {
        LinearLayout selectNumberView = new LinearLayout(context);
        selectNumberView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        selectNumberView.setOrientation(LinearLayout.VERTICAL);
        
        
        
        for (int x=0; x<3; x++) {
            LinearLayout row = new LinearLayout(context);
            row.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            row.setOrientation(LinearLayout.HORIZONTAL);
            for (int y=0; y<3; y++) {
            	Button numberButton = new Button(context);
            	numberButton.setWidth(0);
            	numberButton.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
            	
            	int number = (x * 3) + (y + 1);
            	
            	numberButton.setText(new Integer(number).toString());
            	numberButton.setTag(number);
            	numberButton.setOnClickListener(numberButtonClickListener);
            	
            	row.addView(numberButton);
            }
            selectNumberView.addView(row);
        }
        
        return selectNumberView;
    }
	
	/**
	 * Occurs when user selects some number or presses the clear button
	 * (in which case 0 will be sent).
	 */
	private OnClickListener numberButtonClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Integer number = (Integer)v.getTag();
			
			if (onNumberSelectListener != null) {
				onNumberSelectListener.onNumberSelect(number);
			}
			
			dialog.dismiss();
		}
	};

	/**
	 * Occurs when user cancels number selection.
	 */
	private OnClickListener cancelButtonClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			dialog.dismiss();
		}
	};
	
	public interface OnNumberSelectListener
	{
		/**
		 * Called when a cell is tapped (by finger).
		 * @param cell
		 * @return
		 */
		boolean onNumberSelect(int number);
	}
	
	
}
