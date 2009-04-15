package cz.romario.opensudoku.gui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
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
    public static final int MENU_ITEM_ABOUT = Menu.FIRST + 3;
    
    private static final String TAG = "FolderListActivity";
    
    private FolderListAdapter listAdapter;
    
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.folder_list);
		
		setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);
		
        // Inform the list we provide context menus for items
        getListView().setOnCreateContextMenuListener(this);

        
        listAdapter = new FolderListAdapter(this);
        setListAdapter(listAdapter);
	}
	
	@Override
	protected void onStart() {
		super.onStart();

		// we will load current folders data on every start
		fillData();

	}
	
	private void fillData() {
        SudokuDatabase sudokuDB = new SudokuDatabase(this);
        FolderInfo[] folderList = sudokuDB.getFolderList();
        listAdapter.setFolders(folderList);
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
		// TODO: enter should act as Save button
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
                	fillData();
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
	 * @param folder
	 */
    private void showRenameFolderDialog(FolderInfo folder) {
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
            	fillData();
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
    private void showDeleteFolderDialog(FolderInfo folder) {
        final long folderToDeleteId = folder.id;
    	final Dialog dialog = new AlertDialog.Builder(this)
        .setIcon(android.R.drawable.ic_delete)
        .setTitle(getString(R.string.delete_folder_title, folder.name))
        .setMessage(R.string.delete_folder_confirm)
        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	SudokuDatabase db = new SudokuDatabase(FolderListActivity.this);
            	db.deleteFolder(folderToDeleteId);
            	fillData();
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

        FolderInfo folder;
        switch (item.getItemId()) {
        case MENU_ITEM_RENAME:
        	folder = (FolderInfo)getListAdapter().getItem(info.position);
        	//showDialog(DIALOG_RENAME_FOLDER, folder);
        	showRenameFolderDialog(folder);
        	return true;
        case MENU_ITEM_DELETE:
        	folder = (FolderInfo)getListAdapter().getItem(info.position);
        	showDeleteFolderDialog(folder);
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
        }
        return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent i = new Intent(this, SudokuListActivity.class);
		i.putExtra(SudokuListActivity.EXTRAS_FOLDER_ID, id);
		startActivity(i);
	}
	
	private class FolderListAdapter extends BaseAdapter {

		private LayoutInflater inflater;
		private FolderInfo[] folders = new FolderInfo[0];
		
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
			
			detailLabel.setText(folder.getDetail(FolderListActivity.this));
			
			return itemView;
		}
	}

}
