package cz.romario.opensudoku.gui.inputmethod;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cz.romario.opensudoku.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
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
	private Context mContext;
	private LayoutInflater mInflater;
	// TODO: extend dialog
	private Dialog mDialog;
	private TabHost mTabHost;
	
	
	// buttons from "Select number" tab
	private Map<Integer,Button> mNumberButtons = new HashMap<Integer, Button>();
	// buttons from "Edit note" tab
	private Map<Integer,ToggleButton> mNoteNumberButtons = new HashMap<Integer, ToggleButton>();
	// selected numbers on "Edit note" tab
	private Set<Integer> mNoteSelectedNumbers = new HashSet<Integer>();
	
	private OnNumberEditListener mOnNumberEditListener;
	private OnNoteEditListener mOnNoteEditListener;
	
	public EditCellDialog(Context context) {
		mContext = context;
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		mTabHost = createTabView();
		
		// TODO: maybe I should just create my own dialog?
		mDialog = new AlertDialog.Builder(context)
		.setView(mTabHost)
        //.setPositiveButton("Close", closeButtonListener)
       .create();
	}
	
	/**
	 * Registers a callback to be invoked when number is selected.
	 * @param l
	 */
	public void setOnNumberEditListener(OnNumberEditListener l) {
		mOnNumberEditListener = l;
	}
	
	/**
	 * Register a callback to be invoked when note is edited.
	 * @param l
	 */
	public void setOnNoteEditListener(OnNoteEditListener l) {
		mOnNoteEditListener = l;
	}
	
	/**
	 * Returns dialog instance.
	 * @return
	 */
	public Dialog getDialog() {
		return mDialog;
	}
	
	public void updateNumber(Integer number) {
		for (Button b : mNumberButtons.values()) {
			b.setEnabled(true);
		}
		
		if (number != 0) {
			mNumberButtons.get(number).setEnabled(false);
		}
	}
	
	/**
	 * Updates selected numbers in note.
	 * @param numbers
	 */
	public void updateNote(Integer[] numbers) {
		mNoteSelectedNumbers = new HashSet<Integer>();
		
		if (numbers != null) {
			for (int number : numbers) {
				mNoteSelectedNumbers.add(number);
			}
		}
		
		for (Integer number : mNoteNumberButtons.keySet()) {
			mNoteNumberButtons.get(number).setChecked(mNoteSelectedNumbers.contains(number));
		}
	}
	
	
	/**
	 * Creates view with two tabs, first for number in cell selection, second for
	 * note editing.
	 *  
	 * @return
	 */
	private TabHost createTabView() {
		TabHost tabHost = new TabHost(mContext);
        //tabHost.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        tabHost.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        
        LinearLayout linearLayout = new LinearLayout(mContext);
        //linearLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        linearLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        linearLayout.setOrientation(LinearLayout.VERTICAL);
		
		TabWidget tabWidget = new TabWidget(mContext);
		tabWidget.setId(android.R.id.tabs);
		
		FrameLayout frameLayout = new FrameLayout(mContext);
		frameLayout.setId(android.R.id.tabcontent);
		
		linearLayout.addView(tabWidget);
		linearLayout.addView(frameLayout);
		tabHost.addView(linearLayout);

		tabHost.setup();

		final View editNumberView = createEditNumberView();
		final View editNoteView = createEditNoteView();
		
		tabHost.addTab(tabHost.newTabSpec("number")
                .setIndicator(mContext.getString(R.string.select_number))
                .setContent(new TabHost.TabContentFactory() {
					
                	@Override
					public View createTabContent(String tag) {
						return editNumberView;
					}
                	
                }));
		tabHost.addTab(tabHost.newTabSpec("note")
                .setIndicator(mContext.getString(R.string.edit_note))
                .setContent(new TabHost.TabContentFactory() {
					
                	@Override
					public View createTabContent(String tag) {
						return editNoteView;
					}
                	
                }));
		
		return tabHost;
	}
	
	/**
     * Creates view for number in cell editing.
     * @return
     */
	private View createEditNumberView() {
		View v = mInflater.inflate(R.layout.edit_cell_select_number, null);
		
		mNumberButtons.put(1, (Button)v.findViewById(R.id.button_1));
		mNumberButtons.put(2, (Button)v.findViewById(R.id.button_2));
		mNumberButtons.put(3, (Button)v.findViewById(R.id.button_3));
		mNumberButtons.put(4, (Button)v.findViewById(R.id.button_4));
		mNumberButtons.put(5, (Button)v.findViewById(R.id.button_5));
		mNumberButtons.put(6, (Button)v.findViewById(R.id.button_6));
		mNumberButtons.put(7, (Button)v.findViewById(R.id.button_7));
		mNumberButtons.put(8, (Button)v.findViewById(R.id.button_8));
		mNumberButtons.put(9, (Button)v.findViewById(R.id.button_9));
		
		for (Integer num : mNumberButtons.keySet()) {
			Button b = mNumberButtons.get(num);
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
		View v = mInflater.inflate(R.layout.edit_cell_edit_note, null);
		
		mNoteNumberButtons.put(1, (ToggleButton)v.findViewById(R.id.button_1));
		mNoteNumberButtons.put(2, (ToggleButton)v.findViewById(R.id.button_2));
		mNoteNumberButtons.put(3, (ToggleButton)v.findViewById(R.id.button_3));
		mNoteNumberButtons.put(4, (ToggleButton)v.findViewById(R.id.button_4));
		mNoteNumberButtons.put(5, (ToggleButton)v.findViewById(R.id.button_5));
		mNoteNumberButtons.put(6, (ToggleButton)v.findViewById(R.id.button_6));
		mNoteNumberButtons.put(7, (ToggleButton)v.findViewById(R.id.button_7));
		mNoteNumberButtons.put(8, (ToggleButton)v.findViewById(R.id.button_8));
		mNoteNumberButtons.put(9, (ToggleButton)v.findViewById(R.id.button_9));
		
		for (Integer num : mNoteNumberButtons.keySet()) {
			ToggleButton b = mNoteNumberButtons.get(num);
			b.setTag(num);
			b.setOnCheckedChangeListener(editNoteCheckedChangeListener);
		}
		
		Button closeButton = (Button)v.findViewById(R.id.button_close);
		closeButton.setOnClickListener(closeButtonListener);
		Button clearButton = (Button)v.findViewById(R.id.button_clear);
		clearButton.setOnClickListener(clearButtonListener);
		
		return v;
    }
	
	/**
	 * Occurs when user selects number in "Select number" tab.
	 */
	private OnClickListener editNumberButtonClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Integer number = (Integer)v.getTag();
			
			if (mOnNumberEditListener != null) {
				mOnNumberEditListener.onNumberEdit(number);
			}
			
			mDialog.dismiss();
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
				mNoteSelectedNumbers.add(number);
			} else {
				mNoteSelectedNumbers.remove(number);
			}
		}
	
	};

	/**
	 * Occurs when user presses "Clear" button.
	 */
	private OnClickListener clearButtonListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			String currentTab = mTabHost.getCurrentTabTag();
			
			if (currentTab.equals("number")) {
				if (mOnNumberEditListener != null) {
					mOnNumberEditListener.onNumberEdit(0); // 0 as clear
				}
				mDialog.dismiss();
			} else {
				for (ToggleButton b : mNoteNumberButtons.values()) {
					b.setChecked(false);
					mNoteSelectedNumbers.remove(b.getTag());
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
			if (mOnNoteEditListener != null) {
				Integer[] numbers = new Integer[mNoteSelectedNumbers.size()];
				mOnNoteEditListener.onNoteEdit(mNoteSelectedNumbers.toArray(numbers));
			}
			mDialog.dismiss();
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
	
	

