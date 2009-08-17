/* 
 * Copyright (C) 2009 Roman Masek
 * 
 * This file is part of OpenSudoku.
 * 
 * OpenSudoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * OpenSudoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with OpenSudoku.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package cz.romario.opensudoku.gui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
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
    
    private static final int DIALOG_ABOUT = 0;
    private static final int DIALOG_ADD_FOLDER = 1;
    private static final int DIALOG_RENAME_FOLDER = 2;
    private static final int DIALOG_DELETE_FOLDER = 3;
    
    private static final String TAG = "FolderListActivity";
    
    //private Handler mGuiHandler;
    //private TaskQueue mBackgroundTaskQueue;
    private Cursor mCursor;
    private SudokuDatabase mSudokuDB;
    
    private SudokuDatabase mCursorDB;
    
    // input parameters for dialogs
    private TextView mAddFolderNameInput;
    private TextView mRenameFolderNameInput;
    private long mRenameFolderID; 
    private long mDeleteFolderID;
    
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.folder_list);
		
		setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);
		
		View getMorePuzzles = (View)findViewById(R.id.get_more_puzzles);
		getMorePuzzles.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://code.google.com/p/opensudoku-android/wiki/Puzzles"));
				//intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				startActivity(intent);
			}
		});
		
		//mGuiHandler = new Handler();
		//mBackgroundTaskQueue = new TaskQueue();
		
		mSudokuDB = new SudokuDatabase(getApplicationContext());
		
		// Inform the list we provide context menus for items
        getListView().setOnCreateContextMenuListener(this);

        // TODO: it is important that only getFolderList is called on this instance of SudokuDatabase.
        mCursorDB = new SudokuDatabase(getApplicationContext());
        mCursor = mCursorDB.getFolderList();
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
					String detail = mSudokuDB.getFolderInfo(folderID).getDetail(getApplicationContext());
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
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	
    	mCursorDB.close();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	
    	outState.putLong("mRenameFolderID", mRenameFolderID);
    	outState.putLong("mDeleteFolderID", mDeleteFolderID);
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle state) {
    	super.onRestoreInstanceState(state);
    	
    	mRenameFolderID = state.getLong("mRenameFolderID");
    	mDeleteFolderID = state.getLong("mDeleteFolderID");
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
        //menu.add(0, MENU_ITEM_DEBUG, 2, "debug");
        
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

    @Override
    protected Dialog onCreateDialog(int id) {
    	LayoutInflater factory = LayoutInflater.from(this);
    	
    	switch (id) {
    	case DIALOG_ABOUT:
            final View aboutView = factory.inflate(R.layout.about, null);
            TextView versionLabel = (TextView)aboutView.findViewById(R.id.version_label);
            String versionName = null;
    		try {
    			versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
    		} catch (NameNotFoundException e) {
    			versionName = "unable to retreive version";
    		}
            versionLabel.setText(getString(R.string.version, versionName));
            return new AlertDialog.Builder(this)
                .setIcon(R.drawable.opensudoku)
                .setTitle(R.string.app_name)
                .setView(aboutView)
                .setPositiveButton("OK", null)
                .create();
    	case DIALOG_ADD_FOLDER:
    		View addFolderView = factory.inflate(R.layout.folder_name, null);
            mAddFolderNameInput = (TextView)addFolderView.findViewById(R.id.name);
            return new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_menu_add)
                .setTitle(R.string.add_folder)
                .setView(addFolderView)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	SudokuDatabase db = new SudokuDatabase(getApplicationContext());
                    	db.insertFolder(mAddFolderNameInput.getText().toString().trim());
                    	update();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    	case DIALOG_RENAME_FOLDER:
            final View renameFolderView = factory.inflate(R.layout.folder_name, null);
            mRenameFolderNameInput = (TextView)renameFolderView.findViewById(R.id.name);

            return new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_menu_edit)
            .setTitle(R.string.rename_folder_title)
            .setView(renameFolderView)
            .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	SudokuDatabase db = new SudokuDatabase(getApplicationContext());
                	db.updateFolder(mRenameFolderID, mRenameFolderNameInput.getText().toString().trim());
                	update();
                }
            })
            .setNegativeButton(android.R.string.cancel, null)
            .create();
    	case DIALOG_DELETE_FOLDER:
        	return new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_delete)
            .setTitle(R.string.delete_folder_title)
            .setMessage(R.string.delete_folder_confirm)
            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	// TODO: this could take a while, I should show progress dialog
                	SudokuDatabase db = new SudokuDatabase(getApplicationContext());
                	db.deleteFolder(mDeleteFolderID);
                	update();
                }
            })
            .setNegativeButton(android.R.string.no, null)
            .create();
    		
    		
    	}
    	
    	return null;
    }
    
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
    	super.onPrepareDialog(id, dialog);
    	
    	// TODO: when changing orientation, it seems that only onCreateDialog is called, delete folder than for
    	// example does not have proper title
    	
    	switch (id) {
    	case DIALOG_ADD_FOLDER:
    		break;
    	case DIALOG_RENAME_FOLDER:
    	{
    		FolderInfo folder = mSudokuDB.getFolderInfo(mRenameFolderID);
    		dialog.setTitle(getString(R.string.rename_folder_title, folder.name));
    		mRenameFolderNameInput.setText(folder.name);
    		break;
    	}
    	case DIALOG_DELETE_FOLDER:
    	{
    		FolderInfo folder = mSudokuDB.getFolderInfo(mDeleteFolderID);
    		dialog.setTitle(getString(R.string.delete_folder_title, folder.name));
    		break;
    	}
    	}
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
        	mRenameFolderID = info.id;
        	showDialog(DIALOG_RENAME_FOLDER);
        	return true;
        case MENU_ITEM_DELETE:
        	mDeleteFolderID = info.id;
        	showDialog(DIALOG_DELETE_FOLDER);
        	return true;
        }
        return false;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
        case MENU_ITEM_ADD:
        	showDialog(DIALOG_ADD_FOLDER);
            return true;
        case MENU_ITEM_ABOUT:
        	showDialog(DIALOG_ABOUT);
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
