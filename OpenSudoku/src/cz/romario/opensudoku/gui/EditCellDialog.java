package cz.romario.opensudoku.gui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cz.romario.opensudoku.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout.LayoutParams;

public class EditCellDialog {
	private Context context;
	private LayoutInflater inflater;
	// TODO: extend dialog
	private Dialog dialog;
	private TabHost tabHost;
	
	
	// TODO: sladit nazvy
	// buttons from "Select number" tab
	private Map<Integer,Button> numberButtons = new HashMap<Integer, Button>();
	// buttons from "Edit note" tab
	private Map<Integer,ToggleButton> noteNumberButtons = new HashMap<Integer, ToggleButton>();
	// selected numbers on "Edit note" tab
	private Set<Integer> noteSelectedNumbers = new HashSet<Integer>();
	
	private OnNumberEditListener onNumberEditListener;
	private OnNoteEditListener onNoteEditListener;
	
	public EditCellDialog(Context context) {
		this.context = context;
		this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		this.tabHost = createTabView();
		
		// TODO: maybe I should just create my own dialog?
		dialog = new AlertDialog.Builder(context)
        .setView(this.tabHost)
        //.setPositiveButton("Close", closeButtonListener)
       .create();
	}
	
	/**
	 * Registers a callback to be invoked when number is selected.
	 * @param l
	 */
	public void setOnNumberEditListener(OnNumberEditListener l) {
		this.onNumberEditListener = l;
	}
	
	/**
	 * Register a callback to be invoked when note is edited.
	 * @param l
	 */
	public void setOnNoteEditListener(OnNoteEditListener l) {
		this.onNoteEditListener = l;
	}
	
	/**
	 * Returns dialog instance.
	 * @return
	 */
	public Dialog getDialog() {
		return this.dialog;
	}
	
	public void updateNumber(Integer number) {
		for (Button b : this.numberButtons.values()) {
			b.setEnabled(true);
		}
		
		if (number != 0) {
			this.numberButtons.get(number).setEnabled(false);
		}
	}
	
	/**
	 * Updates selected numbers in note.
	 * @param numbers
	 */
	public void updateNote(Integer[] numbers) {
		this.noteSelectedNumbers = new HashSet<Integer>();
		
		if (numbers != null) {
			for (int number : numbers) {
				this.noteSelectedNumbers.add(number);
			}
		}
		
		for (Integer number : noteNumberButtons.keySet()) {
			noteNumberButtons.get(number).setChecked(noteSelectedNumbers.contains(number));
		}
	}
	
	
	/**
	 * Creates view with two tabs, first for number in cell selection, second for
	 * note editing.
	 *  
	 * @return
	 */
	private TabHost createTabView() {
		TabHost tabHost = new TabHost(context);
        tabHost.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        linearLayout.setOrientation(LinearLayout.VERTICAL);
		
		TabWidget tabWidget = new TabWidget(context);
		tabWidget.setId(android.R.id.tabs);
		
		FrameLayout frameLayout = new FrameLayout(context);
		frameLayout.setId(android.R.id.tabcontent);
		
		linearLayout.addView(tabWidget);
		linearLayout.addView(frameLayout);
		tabHost.addView(linearLayout);

		tabHost.setup();

		tabHost.addTab(tabHost.newTabSpec("number")
                .setIndicator("Select number")
                .setContent(new TabHost.TabContentFactory() {
					
                	@Override
					public View createTabContent(String tag) {
						return createEditNumberView();
					}
                	
                }));
		tabHost.addTab(tabHost.newTabSpec("note")
                .setIndicator("Edit note")
                .setContent(new TabHost.TabContentFactory() {
					
                	@Override
					public View createTabContent(String tag) {
						return createEditNoteView();
					}
                	
                }));
		
		return tabHost;
	}
	
	/**
     * Creates view for number in cell editing.
     * @return
     */
	private View createEditNumberView() {
		View v = inflater.inflate(R.layout.edit_cell_select_number, null);
		
		numberButtons.put(1, (Button)v.findViewById(R.id.button_1));
		numberButtons.put(2, (Button)v.findViewById(R.id.button_2));
		numberButtons.put(3, (Button)v.findViewById(R.id.button_3));
		numberButtons.put(4, (Button)v.findViewById(R.id.button_4));
		numberButtons.put(5, (Button)v.findViewById(R.id.button_5));
		numberButtons.put(6, (Button)v.findViewById(R.id.button_6));
		numberButtons.put(7, (Button)v.findViewById(R.id.button_7));
		numberButtons.put(8, (Button)v.findViewById(R.id.button_8));
		numberButtons.put(9, (Button)v.findViewById(R.id.button_9));
		
		for (Integer num : numberButtons.keySet()) {
			Button b = numberButtons.get(num);
			b.setTag(num);
			b.setOnClickListener(editNumberButtonClickListener);
		}
		
		Button closeButton = (Button)v.findViewById(R.id.button_close);
		closeButton.setOnClickListener(closeButtonListener);
		Button clearButton = (Button)v.findViewById(R.id.button_clear);
		clearButton.setOnClickListener(clearButtonListener);
		
		return v;
    }

	
	/**
     * Creates view for note editing.
     * @return
     */
	private View createEditNoteView() {
		View v = inflater.inflate(R.layout.edit_cell_edit_note, null);
		
		noteNumberButtons.put(1, (ToggleButton)v.findViewById(R.id.button_1));
		noteNumberButtons.put(2, (ToggleButton)v.findViewById(R.id.button_2));
		noteNumberButtons.put(3, (ToggleButton)v.findViewById(R.id.button_3));
		noteNumberButtons.put(4, (ToggleButton)v.findViewById(R.id.button_4));
		noteNumberButtons.put(5, (ToggleButton)v.findViewById(R.id.button_5));
		noteNumberButtons.put(6, (ToggleButton)v.findViewById(R.id.button_6));
		noteNumberButtons.put(7, (ToggleButton)v.findViewById(R.id.button_7));
		noteNumberButtons.put(8, (ToggleButton)v.findViewById(R.id.button_8));
		noteNumberButtons.put(9, (ToggleButton)v.findViewById(R.id.button_9));
		
		for (Integer num : noteNumberButtons.keySet()) {
			ToggleButton b = noteNumberButtons.get(num);
			b.setTag(num);
			b.setOnCheckedChangeListener(editNoteCheckedChangeListener);
		}
		
		Button closeButton = (Button)v.findViewById(R.id.button_close);
		closeButton.setOnClickListener(closeButtonListener);
		Button clearButton = (Button)v.findViewById(R.id.button_clear);
		clearButton.setOnClickListener(clearButtonListener);
		
		return v;
        
//		
//		TableLayout editNumberView = new TableLayout(context);
//        
//        // create 3x3 table with numbers 1 - 9
//        for (int x=0; x<3; x++) {
//        	TableRow row = new TableRow(context);
//            for (int y=0; y<3; y++) {
//            	ToggleButton numberButton = new ToggleButton(context);
//            	numberButton.setWidth(NUMBER_BUTTON_WIDTH);
//            	numberButton.setHeight(NUMBER_BUTTON_HEIGHT);
//            	
//            	Integer number = (x * 3) + (y + 1);
//            	
//            	numberButton.setTextOn(number.toString());
//            	numberButton.setTextOff(number.toString());
//            	numberButton.setChecked(this.noteSelectedNumbers.contains(number));
//            	numberButton.setTag(number);
//            	numberButton.setOnCheckedChangeListener(editNoteCheckedChangeListener);
//            	
//            	noteNumberButtons.put(number, numberButton);
//            	
//            	row.addView(numberButton);
//            }
//            editNumberView.addView(row);
//        }
//        
//        // Add clear button to the end
//        TableRow row = new TableRow(context);
//        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams();
//        layoutParams.column = 2;
//        layoutParams.topMargin = CLEAR_BUTTON_MARGIN;
//        row.addView(createClearButton(), layoutParams);
//        editNumberView.addView(row);
//        
//        editNumberView.setShrinkAllColumns(true);
//        
//        return editNumberView;
    }
	
//	private View createClearButton() {
//        Button clearButton = new Button(context);
//        clearButton.setText("Clear");
//        clearButton.setWidth(NUMBER_BUTTON_WIDTH);
//        clearButton.setOnClickListener(clearButtonListener);
//		return clearButton;
//	}
	
	/**
	 * Occurs when user selects number in "Select number" tab.
	 */
	private OnClickListener editNumberButtonClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Integer number = (Integer)v.getTag();
			
			if (onNumberEditListener != null) {
				onNumberEditListener.onNumberEdit(number);
			}
			
			dialog.dismiss();
		}
	};

	/**
	 * Occurs when user checks or unchecks number in "Edit note" tab.
	 */
	private OnCheckedChangeListener editNoteCheckedChangeListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			Integer number = (Integer)buttonView.getTag();
			if (isChecked) {
				noteSelectedNumbers.add(number);
			} else {
				noteSelectedNumbers.remove(number);
			}
		}
	
	};

	/**
	 * Occurs when user presses "Clear" button.
	 */
	private OnClickListener clearButtonListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			String currentTab = tabHost.getCurrentTabTag();
			
			if (currentTab.equals("number")) {
				if (onNumberEditListener != null) {
					onNumberEditListener.onNumberEdit(0); // 0 as clear
				}
				dialog.dismiss();
			} else {
				for (ToggleButton b : noteNumberButtons.values()) {
					b.setChecked(false);
					noteSelectedNumbers.remove(b.getTag());
				}
			}
		}
	};

	/**
	 * Occurs when user presses "Close" button.
	 */
	private OnClickListener closeButtonListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (onNoteEditListener != null) {
				Integer[] numbers = new Integer[noteSelectedNumbers.size()];
				onNoteEditListener.onNoteEdit(noteSelectedNumbers.toArray(numbers));
			}
			dialog.dismiss();
		}
	};
	
	/**
	 * Interface definition for a callback to be invoked, when user selects number, which
	 * should be entered in the sudoku cell.
	 * 
	 * @author romario
	 *
	 */
	public interface OnNumberEditListener
	{
		boolean onNumberEdit(int number);
	}
	
	/**
	 * Interface definition for a callback to be invoked, when user selects new note
	 * content.
	 * 
	 * @author romario
	 *
	 */
	public interface OnNoteEditListener
	{
		boolean onNoteEdit(Integer[] number);
	}
}
	
	

