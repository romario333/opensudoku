package cz.romario.opensudoku;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
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
import cz.romario.opensudoku.db.FolderColumns;
import cz.romario.opensudoku.db.SudokuDatabase;

public class FolderListActivity extends ListActivity {
    public static final int MENU_ITEM_INSERT = Menu.FIRST;
    public static final int MENU_ITEM_EDIT = Menu.FIRST + 1;
    public static final int MENU_ITEM_DELETE = Menu.FIRST + 2;
    
    /** The index of the name column in cursor */
    private static final int COLUMN_INDEX_NAME = 1;
    
    private static final String TAG = "FolderListActivity";

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);
		
        // Inform the list we provide context menus for items
        getListView().setOnCreateContextMenuListener(this);

        SudokuDatabase sudokuDB = new SudokuDatabase(this);
        
        Cursor cursor = sudokuDB.getFolderList();
        startManagingCursor(cursor);
        
        
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.folder_list_item, 
        		cursor, new String[] { FolderColumns.NAME }, 
        		new int[] { R.id.name });
        setListAdapter(adapter);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
        // This is our one standard application action -- inserting a
        // new note into the list.
        menu.add(0, MENU_ITEM_INSERT, 0, "Add folder")
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
        menu.setHeaderTitle(cursor.getString(COLUMN_INDEX_NAME));

        // Add a menu item to delete the note
        menu.add(0, MENU_ITEM_EDIT, 0, "Edit folder");
        menu.add(0, MENU_ITEM_DELETE, 1, "Delete folder");
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
        	Intent i = new Intent(this, FolderEditActivity.class);
        	i.setAction(Intent.ACTION_EDIT);
        	i.putExtra(FolderEditActivity.EXTRAS_FOLDER_ID, info.id);
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
        	Intent i = new Intent(this, FolderEditActivity.class);
        	i.setAction(Intent.ACTION_INSERT);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
	}
	
    // TODO: onPrepareOptionsMenu - menit polozky v menu podle vybraneho itemu
	
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent i = new Intent(this, SudokuListActivity.class);
		i.putExtra(SudokuListActivity.EXTRAS_FOLDER_ID, id);
		startActivity(i);
	}

}
