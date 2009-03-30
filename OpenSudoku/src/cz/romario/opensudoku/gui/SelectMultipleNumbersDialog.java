package cz.romario.opensudoku.gui;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout.LayoutParams;

/**
 * Wrapper around dialog which allows user to select multiple numbers from
 * 0 to 9 by finger.
 * 
 * @author romario
 *
 */
public class SelectMultipleNumbersDialog {
	private Context context;
	private Dialog dialog;
	private Set<Integer> selectedNumbers = new HashSet<Integer>();
	private Map<Integer,ToggleButton> numberButtons = new HashMap<Integer, ToggleButton>();
	
	private OnNumbersSelectListener onNumbersSelectListener;
	
	public SelectMultipleNumbersDialog(Context context) {
		this.context = context;

		this.dialog = new AlertDialog.Builder(context)
		.setIcon(android.R.drawable.ic_menu_set_as)
        .setTitle("Select number")
        .setView(createSelectMultipleNumbersView())
        .setPositiveButton("Close", closeButtonClickListener)
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                // user clicked Cancel, do nothing
            }
        })
       .create();		
		// TODO: maybe I should just create my own dialog, this is just quick hack
	}
	
	/**
	 * Registers a callback to be invoked when number is selected.
	 * @param l
	 */
	public void setOnNumbersSelectListener(OnNumbersSelectListener l) {
		this.onNumbersSelectListener = l;
	}

	/**
	 * Gets dialog instance.
	 * @param numbers Set of numbers which should be toggled on.
	 * @return
	 */
	public Dialog getDialog() {
		return dialog;
	}
	
	public void updateNumbers(Integer[] numbers) {
		this.selectedNumbers = new HashSet<Integer>();
		
		if (numbers != null) {
			for (int number : numbers) {
				this.selectedNumbers.add(number);
			}
		}
		
		for (Integer number : numberButtons.keySet()) {
			numberButtons.get(number).setChecked(selectedNumbers.contains(number));
		}
	}
	
	/**
     * Creates 3x3 table of number buttons (1 to 9).
     * @return
     */
	private View createSelectMultipleNumbersView() {
        LinearLayout selectNumberView = new LinearLayout(context);
        selectNumberView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        selectNumberView.setOrientation(LinearLayout.VERTICAL);
        
        
        
        for (int x=0; x<3; x++) {
            LinearLayout row = new LinearLayout(context);
            row.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            row.setOrientation(LinearLayout.HORIZONTAL);
            for (int y=0; y<3; y++) {
            	ToggleButton numberButton = new ToggleButton(context);
            	numberButton.setWidth(0);
            	numberButton.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
            	
            	Integer number = (x * 3) + (y + 1);
            	
            	numberButton.setTextOn(number.toString());
            	numberButton.setTextOff(number.toString());
            	numberButton.setChecked(this.selectedNumbers.contains(number));
            	numberButton.setTag(number);
            	numberButton.setOnCheckedChangeListener(numberCheckedChangeListener);
            	
            	numberButtons.put(number, numberButton);
            	
            	row.addView(numberButton);
            }
            selectNumberView.addView(row);
        }
        
        return selectNumberView;
    }
	
	/**
	 * Occurs when user checks or unchecks number.
	 */
	private OnCheckedChangeListener numberCheckedChangeListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			Integer number = (Integer)buttonView.getTag();
			if (isChecked) {
				selectedNumbers.add(number);
			} else {
				selectedNumbers.remove(number);
			}
		}
	
	};

	
	/**
	 * Occurs when user confirms selection.
	 */
	private DialogInterface.OnClickListener closeButtonClickListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			if (onNumbersSelectListener != null) {
				Integer[] numbers = new Integer[selectedNumbers.size()];
				onNumbersSelectListener.onNumbersSelect(selectedNumbers.toArray(numbers));
			}
		}
	};
	
	public interface OnNumbersSelectListener
	{
		/**
		 * Called when numbers selection is complete.
		 * @param numbers Selected numbers.
		 * @return
		 */
		boolean onNumbersSelect(Integer[] numbers);
	}
	

}
