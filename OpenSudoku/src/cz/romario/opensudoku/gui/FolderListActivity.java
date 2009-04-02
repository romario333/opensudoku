package cz.romario.opensudoku.gui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import cz.romario.opensudoku.R;
import cz.romario.opensudoku.db.SudokuDatabase;
import cz.romario.opensudoku.game.FolderInfo;

public class FolderListActivity extends ListActivity {
    
	public static final int MENU_ITEM_ADD = Menu.FIRST;
    public static final int MENU_ITEM_RENAME = Menu.FIRST + 1;
    public static final int MENU_ITEM_DELETE = Menu.FIRST + 2;
    
    private static final int DIALOG_ADD_FOLDER = 1;
    private static final int DIALOG_RENAME_FOLDER = 2;
    private static final int DIALOG_DELETE_FOLDER = 3;
    
    private static final String TAG = "FolderListActivity";
    
    private FolderListAdapter listAdapter;
    
    // which folder shoud dialog operate on
    private FolderInfo dialogFolderInfo;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);
		
        // Inform the list we provide context menus for items
        getListView().setOnCreateContextMenuListener(this);

        
        listAdapter = new FolderListAdapter(this);
        loadAdapterData();
        setListAdapter(listAdapter);
	}
	
	private void loadAdapterData() {
        SudokuDatabase sudokuDB = new SudokuDatabase(this);
        FolderInfo[] folderList = sudokuDB.getFolderList();
        listAdapter.setFolders(folderList);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
        // This is our one standard application action -- inserting a
        // new note into the list.
        menu.add(0, MENU_ITEM_ADD, 0, "Add folder")
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

        FolderInfo f = (FolderInfo)getListAdapter().getItem(info.position);
        menu.setHeaderTitle(f.name);

        // Add a menu item to delete the note
        menu.add(0, MENU_ITEM_RENAME, 0, "Rename folder");
        menu.add(0, MENU_ITEM_DELETE, 1, "Delete folder");
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
    	switch (id) {
    	case DIALOG_ADD_FOLDER:
    	{
            LayoutInflater factory = LayoutInflater.from(this);
            final View nameView = factory.inflate(R.layout.folder_name, null);
            final TextView nameInput = (TextView)nameView.findViewById(R.id.name);
            return new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_menu_add)
                .setTitle("Add folder")
                .setView(nameView)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	SudokuDatabase db = new SudokuDatabase(FolderListActivity.this);
                    	db.insertFolder(nameInput.getText().toString().trim());
                    	loadAdapterData();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
    	}
    	case DIALOG_RENAME_FOLDER:
    	{
            LayoutInflater factory = LayoutInflater.from(this);
            final View nameView = factory.inflate(R.layout.folder_name, null);
            final TextView nameInput = (TextView)nameView.findViewById(R.id.name);
            
            nameInput.setText(dialogFolderInfo.name);
            
            return new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_menu_add)
                .setTitle(dialogFolderInfo.name)
                .setView(nameView)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	SudokuDatabase db = new SudokuDatabase(FolderListActivity.this);
                    	db.updateFolder(dialogFolderInfo.id, nameInput.getText().toString().trim());
                    	loadAdapterData();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
    	}
    	case DIALOG_DELETE_FOLDER:
            return new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_delete)
            .setTitle(dialogFolderInfo.name)
            .setMessage("Are you sure you want to delete this folder?")
            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	SudokuDatabase db = new SudokuDatabase(FolderListActivity.this);
                	db.deleteFolder(dialogFolderInfo.id);
                	loadAdapterData();
                }
            })
            .setNegativeButton("No", null)
            .create();
    	}
    	return null;
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

        FolderInfo folder;
        switch (item.getItemId()) {
        case MENU_ITEM_RENAME:
        	folder = (FolderInfo)getListAdapter().getItem(info.position);
        	showDialog(DIALOG_RENAME_FOLDER, folder);
        	break;
        case MENU_ITEM_DELETE:
        	folder = (FolderInfo)getListAdapter().getItem(info.position);
        	showDialog(DIALOG_DELETE_FOLDER, folder);
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
        }
        return super.onOptionsItemSelected(item);
	}
	
	// TODO: comment
	private void showDialog(int dialogID, FolderInfo fi) {
		dialogFolderInfo = fi;
		showDialog(dialogID);
	}
	
    // TODO: onPrepareOptionsMenu - menit polozky v menu podle vybraneho itemu
	
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent i = new Intent(this, SudokuListActivity.class);
		i.putExtra(SudokuListActivity.EXTRAS_FOLDER_ID, id);
		startActivity(i);
	}
	
	private class FolderListAdapter extends BaseAdapter {

		private LayoutInflater inflater;
		private FolderInfo[] folders;
		
		public FolderListAdapter(Context context) {
			this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		public FolderInfo[] getFolders() {
			return folders;			
		}
		
		public void setFolders(FolderInfo[] folders) {
			this.folders = folders;
			notifyDataSetChanged();
		}
		
		@Override
		public int getCount() {
			return folders.length;
		}

		@Override
		public Object getItem(int position) {
			return folders[position];
		}

		@Override
		public long getItemId(int position) {
			return folders[position].id;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View itemView = inflater.inflate(R.layout.folder_list_item, parent, false);
			TextView nameLabel = (TextView) itemView.findViewById(R.id.name);
			TextView detailLabel = (TextView) itemView.findViewById(R.id.detail);
			
			FolderInfo folder = folders[position];
			nameLabel.setText(folder.name);
			
			String folderDetail;
			if (folder.puzzleCount == 0) {
				folderDetail = "No puzzles";
			} else if (folder.puzzleCount == 1) {
				folderDetail = "1 puzzle";
			} else {
				folderDetail = String.format("%s puzzles", folder.puzzleCount);
			}
			if (folder.solvedCount != 0) {
				if (folder.solvedCount == folder.puzzleCount) {
					folderDetail += " (all solved)";
				} else {
					folderDetail += String.format(" (%s solved)", folder.solvedCount);
				}
			}
			detailLabel.setText(folderDetail);
			
			return itemView;
		}
	}

}
