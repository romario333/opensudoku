package cz.romario.opensudoku.gui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import cz.romario.opensudoku.db.SudokuDatabase;

public class FolderEditActivity extends Activity {

	private static final String TAG = "FolderEditActivity";
	
	public static final String EXTRAS_FOLDER_ID = "folder_id";
	
	// The different distinct states the activity can be run in.
    private static final int STATE_EDIT = 0;
    private static final int STATE_INSERT = 1;

    private int state;
    private long folderID;
    
    private SudokuDatabase sudokuDB;
    private TextView folderNameText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
        String action = intent.getAction();
        if (Intent.ACTION_EDIT.equals(action)) {
            // Requested to edit: set that state, and the data being edited.
            state = STATE_EDIT;
            if (intent.hasExtra(EXTRAS_FOLDER_ID)) {
	            folderID = intent.getLongExtra(EXTRAS_FOLDER_ID, 0);
            } else {
            	throw new IllegalArgumentException(String.format("Cannot find extra with key '%s'.", EXTRAS_FOLDER_ID));
            }
        } else if (Intent.ACTION_INSERT.equals(action)) {
        	state = STATE_INSERT;
        	folderID = 0;
        } else {
            // Whoops, unknown action!  Bail.
            Log.e(TAG, "Unknown action, exiting.");
            finish();
            return;
        }
        
        sudokuDB = new SudokuDatabase(this);
        
		setContentView(R.layout.folder_edit);

		Button buttonSave = (Button)findViewById(R.id.button_save);
		Button buttonCancel = (Button)findViewById(R.id.button_cancel);
		folderNameText = (TextView)findViewById(R.id.folder_name);
		
		buttonSave.setOnClickListener(buttonSaveClickListener);
		buttonCancel.setOnClickListener(buttonCancelClickListener);
	}

	private OnClickListener buttonSaveClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			String name = folderNameText.getText().toString();
			
			switch (state) {
			case STATE_EDIT:
				sudokuDB.updateFolder(folderID, name);
				break;
			case STATE_INSERT:
				sudokuDB.insertFolder(name);
				break;
			}
			
			finish();
		}
		
	};

	private OnClickListener buttonCancelClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			finish();
		}
		
	};
	
}
