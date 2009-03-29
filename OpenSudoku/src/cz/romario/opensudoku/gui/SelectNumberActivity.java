package cz.romario.opensudoku.gui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

/**
 * This activity allows user to select number to fill in the sudoku cell on screen.
 * 
 * Result is returned in Intent's extras collection with EXTRAS_SELECTED_NUMBER key.
 * Activity retuns integer value of selected number (if user presses Clear button,
 * 0 is returned).
 * 
 * TODO: Ok, tohle nefunguje dobre, cinnost pod timhle je zastavena, takze nefunguje spravne napr. timer.
 * 
 * @author romario
 *
 */
public class SelectNumberActivity extends Activity {

	public static final String EXTRAS_SELECTED_NUMBER = "sel_number";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_LEFT_ICON);
        setContentView(createDialogView());
        getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, 
                android.R.drawable.ic_menu_set_as);
		
	}
	
    /** 
     * Creates dialog body.
     * @return
     */
	private View createDialogView() {
    	LinearLayout dialogBody = new LinearLayout(this);
    	dialogBody.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
    	dialogBody.setOrientation(LinearLayout.VERTICAL);
    	dialogBody.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        
        dialogBody.addView(createSelectNumberView());
        
        
        LinearLayout dialogButtons = new LinearLayout(this);
        dialogButtons.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        dialogButtons.setOrientation(LinearLayout.HORIZONTAL);
        
        dialogBody.addView(dialogButtons);
        
        Button clearButton = new Button(this);
        // TODO: string resources
        clearButton.setText("Clear");
        clearButton.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
        clearButton.setTag(0); // 0 as clear, see OnNumberSelectedListener
        clearButton.setOnClickListener(numberButtonClickListener);
        dialogButtons.addView(clearButton);

        Button cancelButton = new Button(this);
        cancelButton.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
        cancelButton.setText("Cancel");
        cancelButton.setOnClickListener(cancelButtonClickListener);
        dialogButtons.addView(cancelButton);
        
        
        return dialogBody;
    }
	
	/**
     * Creates 3x3 table of number buttons (1 to 9).
     * @return
     */
	private View createSelectNumberView() {
        LinearLayout selectNumberView = new LinearLayout(this);
        selectNumberView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        selectNumberView.setOrientation(LinearLayout.VERTICAL);
        
        for (int x=0; x<3; x++) {
            LinearLayout row = new LinearLayout(this);
            row.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            row.setOrientation(LinearLayout.HORIZONTAL);
            for (int y=0; y<3; y++) {
            	Button numberButton = new Button(this);
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
	
	private OnClickListener numberButtonClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Integer number = (Integer)v.getTag();

			Intent data = new Intent();
			data.putExtra(EXTRAS_SELECTED_NUMBER, number);
			setResult(RESULT_OK, data);
			
			finish();
		}
	};

	private OnClickListener cancelButtonClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			setResult(RESULT_CANCELED);
			
			finish();
		}
	};
}
