package cz.romario.opensudoku.gui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.SimpleCursorAdapter.ViewBinder;
import cz.romario.opensudoku.R;
import cz.romario.opensudoku.db.FolderColumns;
import cz.romario.opensudoku.db.SudokuDatabase;
import cz.romario.opensudoku.game.FolderInfo;

public class FolderListActivity extends ListActivity {
    
	public static final int MENU_ITEM_ADD = Menu.FIRST;
    public static final int MENU_ITEM_RENAME = Menu.FIRST + 1;
    public static final int MENU_ITEM_DELETE = Menu.FIRST + 2;
    public static final int MENU_ITEM_ABOUT = Menu.FIRST + 3;
    public static final int MENU_ITEM_DEBUG = Menu.FIRST + 4;
    
    private static final String TAG = "FolderListActivity";
    
    //private Handler mGuiHandler;
    //private TaskQueue mBackgroundTaskQueue;
    private Cursor mCursor;
    private SudokuDatabase mSudokuDB;
    
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.folder_list);
		
		setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);
		
		//mGuiHandler = new Handler();
		//mBackgroundTaskQueue = new TaskQueue();
		
		mSudokuDB = new SudokuDatabase(this);
		
		// Inform the list we provide context menus for items
        getListView().setOnCreateContextMenuListener(this);

        // TODO: it is important that only getFolderList is called on this instance of SudokuDatabase.
        mCursor = new SudokuDatabase(this).getFolderList();
		startManagingCursor(mCursor);
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.folder_list_item,
				mCursor, new String[] { FolderColumns.NAME, FolderColumns._ID},
				new int[] { R.id.name, R.id.detail});

		adapter.setViewBinder(new ViewBinder() {
			@Override
			public boolean setViewValue(View view, Cursor c, int columnIndex) {

				switch (view.getId()) {
				case R.id.name:
					((TextView)view).setText(c.getString(columnIndex));
					break;
				case R.id.detail:
					final long folderID = c.getLong(columnIndex);
					final TextView detailView = (TextView)view;
					//final Handler guiHandler = mGuiHandler;
					// TODO: lazy load detail or improve detail loading performance
					String detail = mSudokuDB.getFolderInfo(folderID).getDetail(FolderListActivity.this);
					detailView.setText(detail);
//					detailView.setTag(folderID);
//					// folder detail will be loaded asynchronously
//					mBackgroundTaskQueue.addTask(new Runnable() {
//						@Override
//						public void run() {
//							final String detail = mSudokuDB.getFolderInfo(folderID).getDetail(FolderListActivity.this);
//							
//							guiHandler.post(new Runnable() {
//								@Override
//								public void run() {
//									synchronized (detailView) {
//										// check that view still contains same data
//										if (detailView.getTag() != null && (Long)detailView.getTag() == folderID) {
//											detailView.setText(detail);
//										}
//									}
//								}
//							});
//						}
//					});
				}
				
				return true;
			}
		});
		
		// TODO: pravdepodobne se nebude aktualizovat se zmenou stavu
        
        
        setListAdapter(adapter);
	}
    
    @Override
    protected void onStart() {
    	// TODO Auto-generated method stub
    	super.onStart();
    	
    	update();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	//mBackgroundTaskQueue.start();
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	//mBackgroundTaskQueue.stop();
    }
	
	private void update() {
		mCursor.requery();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
        // This is our one standard application action -- inserting a
        // new note into the list.
		menu.add(0, MENU_ITEM_ADD, 0, R.string.add_folder)
                .setShortcut('3', 'a')
                .setIcon(android.R.drawable.ic_menu_add);
        menu.add(0, MENU_ITEM_ABOUT, 1, R.string.about)
        .setShortcut('1', 'h')
        .setIcon(android.R.drawable.ic_menu_info_details);

        // TODO: visible only in debug mode
        menu.add(0, MENU_ITEM_DEBUG, 2, "debug");
        
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
        // TODO: don't assume that second column is name
        menu.setHeaderTitle(cursor.getString(2));

        // Add a menu item to delete the note
        menu.add(0, MENU_ITEM_RENAME, 0, R.string.rename_folder);
        menu.add(0, MENU_ITEM_DELETE, 1, R.string.delete_folder);
    }
    

    private void showAboutDialog() {
		LayoutInflater factory = LayoutInflater.from(this);
        final View aboutView = factory.inflate(R.layout.about, null);
        TextView version_label = (TextView)aboutView.findViewById(R.id.version_label);
        String versionName = null;
		try {
			versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			versionName = "unable to retreive version";
		}
        version_label.setText(getString(R.string.version, versionName));
        final Dialog dialog = new AlertDialog.Builder(this)
            .setIcon(R.drawable.opensudoku)
            .setTitle(R.string.app_name)
            .setView(aboutView)
            .setPositiveButton("OK", null)
            .create();
        dialog.show();
    }
    
    /**
	 * Shows "Add folder" dialog. 
	 * 
	 * I don't use onCreateDialog for this, because I don't want to reuse
	 * single instance of this dialog on every show. (TODO: maybe I'm missing something here)
	 * 
	 */
    private void showAddFolderDialog() {
		// TODO: if I change orientation with dialog visible, WindowLeaked exception is logged
    	LayoutInflater factory = LayoutInflater.from(this);
        final View nameView = factory.inflate(R.layout.folder_name, null);
        final TextView nameInput = (TextView)nameView.findViewById(R.id.name);
        final Dialog dialog = new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_menu_add)
            .setTitle(R.string.add_folder)
            .setView(nameView)
            .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	SudokuDatabase db = new SudokuDatabase(FolderListActivity.this);
                	db.insertFolder(nameInput.getText().toString().trim());
                	update();
                }
            })
            .setNegativeButton(android.R.string.cancel, null)
            .create();
        dialog.show();
    }
    
    /**
	 * Shows "Rename folder" dialog. 
	 * 
	 * I don't use onCreateDialog for this, because I need to change some
	 * dialog attributes on each show.
	 * 
	 * @param folderID
	 */
    private void showRenameFolderDialog(long folderID) {
    	FolderInfo folder = mSudokuDB.getFolderInfo(folderID);
        LayoutInflater factory = LayoutInflater.from(this);
        final View nameView = factory.inflate(R.layout.folder_name, null);
        final TextView nameInput = (TextView)nameView.findViewById(R.id.name);
        nameInput.setText(folder.name);

        final long folderToEditId = folder.id;
        final Dialog dialog = new AlertDialog.Builder(this)
        .setIcon(android.R.drawable.ic_menu_edit)
        .setTitle(getString(R.string.rename_folder_title, folder.name))
        .setView(nameView)
        .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	SudokuDatabase db = new SudokuDatabase(FolderListActivity.this);
            	db.updateFolder(folderToEditId, nameInput.getText().toString().trim());
            	update();
            	//dialog.dismiss();
            }
        })
        .setNegativeButton(android.R.string.cancel, null)
        .create();
        
        dialog.show();
	}
    
	/**
	 * Shows "Do you really want to do this?" dialog. 
	 * 
	 * I don't use onCreateDialog for this, because I need to change some
	 * dialog attributes on each show.
	 * 
	 * @param folder
	 */
    private void showDeleteFolderDialog(long folderID) {
    	FolderInfo folder = mSudokuDB.getFolderInfo(folderID);
        final long folderToDeleteId = folder.id;
    	final Dialog dialog = new AlertDialog.Builder(this)
        .setIcon(android.R.drawable.ic_delete)
        .setTitle(getString(R.string.delete_folder_title, folder.name))
        .setMessage(R.string.delete_folder_confirm)
        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	// TODO: this could take a while, I should show progress dialog
            	SudokuDatabase db = new SudokuDatabase(FolderListActivity.this);
            	db.deleteFolder(folderToDeleteId);
            	update();
            }
        })
        .setNegativeButton(android.R.string.no, null)
        .create();
        
        dialog.show();
    	
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
        case MENU_ITEM_RENAME:
        	showRenameFolderDialog(info.id);
        	return true;
        case MENU_ITEM_DELETE:
        	showDeleteFolderDialog(info.id);
        	return true;
        }
        return false;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
        case MENU_ITEM_ADD:
        	showAddFolderDialog();
            return true;
        case MENU_ITEM_ABOUT:
        	showAboutDialog();
        	return true;
        case MENU_ITEM_DEBUG:
        	Intent i = new Intent();
        	i.setClass(this, DebugActivity.class);
        	startActivity(i);
        	return true;
        }
        return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent i = new Intent(this, SudokuListActivity.class);
		i.putExtra(SudokuListActivity.EXTRAS_FOLDER_ID, id);
		startActivity(i);
	}
}
