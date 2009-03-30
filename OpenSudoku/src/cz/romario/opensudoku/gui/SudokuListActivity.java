package cz.romario.opensudoku.gui;

import java.util.Date;
import java.util.Formatter;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.SimpleCursorAdapter.ViewBinder;
import cz.romario.opensudoku.db.FolderColumns;
import cz.romario.opensudoku.db.SudokuColumns;
import cz.romario.opensudoku.db.SudokuDatabase;
import cz.romario.opensudoku.game.SudokuCellCollection;
import cz.romario.opensudoku.game.SudokuGame;

public class SudokuListActivity extends ListActivity{

	public static final int MENU_ITEM_INSERT = Menu.FIRST;
    public static final int MENU_ITEM_EDIT = Menu.FIRST + 1;
    public static final int MENU_ITEM_DELETE = Menu.FIRST + 2;
	
	public static final String EXTRAS_FOLDER_ID = "folder_id";
	private static final String TAG = "SudokuListActivity";
	
	private StringBuilder timeText;
	private Formatter timeFormatter;
	
	
	private long folderID;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);
		
		Intent intent = getIntent();
		if (intent.hasExtra(EXTRAS_FOLDER_ID)) {
			folderID = intent.getLongExtra(EXTRAS_FOLDER_ID, 0);
		} else {
			Log.d(TAG, "No 'folder_id' extra provided, exiting.");
			finish();
			return;
		}
		
        // Inform the list we provide context menus for items
        getListView().setOnCreateContextMenuListener(this);

        SudokuDatabase sudokuDB = new SudokuDatabase(this);
        
        Cursor cursor = sudokuDB.getSudokuList(folderID);
        startManagingCursor(cursor);
        
        timeText = new StringBuilder(5);
        timeFormatter = new Formatter(timeText);
        
        // TODO: nasty
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.sudoku_list_item, 
        		cursor, new String[] { SudokuColumns.STATE, SudokuColumns.DATA }, 
        		new int[] { R.id.detail_label, R.id.sudoku_board });
        
		adapter.setViewBinder(new ViewBinder() {
			@Override
			public boolean setViewValue(View view, Cursor c,
					int columnIndex) {
				
            	if (view.getId() == R.id.detail_label) {
                	int state = c.getInt(columnIndex);
                	long time = c.getLong(c.getColumnIndex(SudokuColumns.TIME));
    				
                	String detail = null;
                	switch (state) {
                	case SudokuGame.GAME_STATE_COMPLETED:
                		detail = String.format("Solved (%s)", getTime(time));
                		break;
                	case SudokuGame.GAME_STATE_NOT_STARTED:
                		detail = "Unsolved";
                		break;
                	case SudokuGame.GAME_STATE_PLAYING:
                		detail = String.format("Unsolved (%s)", getTime(time));
                		break;
                	}
                	((TextView)view).setText(detail);
            	} else {
    				String data = c.getString(columnIndex);
    				// TODO: porad by slo jeste zrychlit, nemusim volat initCollection, cist poznamky
    				SudokuCellCollection cells = SudokuCellCollection.deserialize(data);
    				SudokuBoardView board = (SudokuBoardView)view;
    				board.setReadOnly(true);
    				board.setFocusable(false); // TODO: bez toho to nejde selectit, doresit
    				((SudokuBoardView)view).setCells(cells);
            	}
				return true;
			}
		});
        
        setListAdapter(adapter);
	}
	
	private String getTime(long time) {
		timeText.setLength(0);
		timeFormatter.format("%02d:%02d", time / 60000, time / 1000 % 60);
		return timeText.toString();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
        // This is our one standard application action -- inserting a
        // new note into the list.
        menu.add(0, MENU_ITEM_INSERT, 0, "Add sudoku")
                .setShortcut('3', 'a')
                .setIcon(android.R.drawable.ic_menu_add);

        // Generate any additional actions that can be performed on the
        // overall list.  In a normal install, there are no additional
        // actions found here, but this allows other applications to extend
        // our menu with their own actions.
        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                new ComponentName(this, FolderListActivity.class), null, intent, 0, null);

        return true;
		
	}
	
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info;
        try {
             info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return;
        }

        Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
        if (cursor == null) {
            // For some reason the requested item isn't available, do nothing
            return;
        }

        // Setup the menu header
        // TODO: domyslet, jak budu navazovat indexy sloupcu v kurzoru na SudokuColumns
        menu.setHeaderTitle(cursor.getString(1));

        // Add a menu item to delete the note
        menu.add(0, MENU_ITEM_EDIT, 0, "Edit sudoku");
        menu.add(0, MENU_ITEM_DELETE, 1, "Delete sudoku");
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info;
        try {
             info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return false;
        }

        switch (item.getItemId()) {
        case MENU_ITEM_EDIT: {
            // TODO: tady by stacil dialog
        	// Delete the note that the context menu is for
        	Intent i = new Intent(this, SudokuEditActivity.class);
        	i.setAction(Intent.ACTION_EDIT);
        	i.putExtra(SudokuEditActivity.EXTRAS_SUDOKU_ID, info.id);
            startActivity(i);
        }
        // TODO: na delete zatim kaslu
    }
    return false;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_ITEM_INSERT:
            // Launch activity to insert a new item
        	Intent i = new Intent(this, SudokuEditActivity.class);
        	i.setAction(Intent.ACTION_INSERT);
        	i.putExtra(SudokuEditActivity.EXTRAS_FOLDER_ID, folderID);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
	}
	
    // TODO: onPrepareOptionsMenu - menit polozky v menu podle vybraneho itemu
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
//		Intent i = new Intent(this, SudokuDetailActivity.class);
//		i.putExtra(SudokuDetailActivity.EXTRAS_SUDOKU_ID, id);
//		startActivity(i);
		
		Intent i = new Intent(SudokuListActivity.this, SudokuPlayActivity.class);
		i.putExtra(SudokuPlayActivity.EXTRAS_SUDOKU_ID, id);
		startActivity(i);
		
	}
	

}
