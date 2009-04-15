package cz.romario.opensudoku.gui;

import java.text.DateFormat;
import java.util.Date;
import java.util.Formatter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
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
import cz.romario.opensudoku.db.SudokuColumns;
import cz.romario.opensudoku.db.SudokuDatabase;
import cz.romario.opensudoku.game.FolderInfo;
import cz.romario.opensudoku.game.SudokuCellCollection;
import cz.romario.opensudoku.game.SudokuGame;

public class SudokuListActivity extends ListActivity {

	public static final int MENU_ITEM_INSERT = Menu.FIRST;
	public static final int MENU_ITEM_EDIT = Menu.FIRST + 1;
	public static final int MENU_ITEM_DELETE = Menu.FIRST + 2;
	public static final int MENU_ITEM_PLAY = Menu.FIRST + 3;
	public static final int MENU_ITEM_RESET = Menu.FIRST + 4;
	public static final int MENU_ITEM_EDIT_NOTE = Menu.FIRST + 5;

	public static final String EXTRAS_FOLDER_ID = "folder_id";
	private static final String TAG = "SudokuListActivity";

	// TODO: duplicated code
	private StringBuilder timeText;
	private Formatter gameTimeFormatter;
	private DateFormat dateTimeFormatter = DateFormat.getDateTimeInstance(
			DateFormat.SHORT, DateFormat.SHORT);
	private DateFormat timeFormatter = DateFormat
			.getTimeInstance(DateFormat.SHORT);

	private SimpleCursorAdapter adapter;
	private Cursor cursor;

	private long folderID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
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

		timeText = new StringBuilder();
		gameTimeFormatter = new Formatter(timeText);

		cursor = sudokuDB.getSudokuList(folderID);
		startManagingCursor(cursor);
		adapter = new SimpleCursorAdapter(this, R.layout.sudoku_list_item,
				cursor, new String[] { SudokuColumns.DATA, SudokuColumns.STATE,
						SudokuColumns.TIME, SudokuColumns.LAST_PLAYED,
						SudokuColumns.CREATED, SudokuColumns.PUZZLE_NOTE },
				new int[] { R.id.sudoku_board, R.id.state, R.id.time,
						R.id.last_played, R.id.created, R.id.note });

		adapter.setViewBinder(new ViewBinder() {
			@Override
			public boolean setViewValue(View view, Cursor c, int columnIndex) {

				int state = c.getInt(c.getColumnIndex(SudokuColumns.STATE));
				
				TextView label = null;

				switch (view.getId()) {
				case R.id.sudoku_board:
					String data = c.getString(columnIndex);
					// TODO: porad by slo jeste zrychlit, nemusim volat
					// initCollection, cist poznamky
					SudokuCellCollection cells = SudokuCellCollection
							.deserialize(data);
					SudokuBoardView board = (SudokuBoardView) view;
					board.setReadOnly(true);
					board.setFocusable(false);
					((SudokuBoardView) view).setCells(cells);
					break;
				case R.id.state:
					label = ((TextView) view);
					String stateString = null;
					switch (state) {
					case SudokuGame.GAME_STATE_COMPLETED:
						stateString = getString(R.string.solved);
						break;
					case SudokuGame.GAME_STATE_PLAYING:
						stateString = getString(R.string.playing);
						break;
					}
					label.setVisibility(stateString == null ? View.GONE
							: View.VISIBLE);
					label.setText(stateString);
					if (state == SudokuGame.GAME_STATE_COMPLETED) {
						// TODO: read colors from android resources
						label.setTextColor(Color.rgb(187, 187, 187));
					} else {
						//label.setTextColor(SudokuListActivity.this.getResources().getColor(R.));
					}
					break;
				case R.id.time:
					long time = c.getLong(columnIndex);
					label = ((TextView) view);
					String timeString = null;
					if (time != 0) {
						timeString = getTime(time);
					}
					label.setVisibility(timeString == null ? View.GONE
							: View.VISIBLE);
					label.setText(timeString);
					if (state == SudokuGame.GAME_STATE_COMPLETED) {
						// TODO: read colors from android resources
						label.setTextColor(Color.rgb(187, 187, 187));
					} else {
						label.setTextColor(Color.rgb(255, 255, 255));
					}
					break;
				case R.id.last_played:
					long lastPlayed = c.getLong(columnIndex);
					label = ((TextView) view);
					String lastPlayedString = null;
					if (lastPlayed != 0) {
						lastPlayedString = getString(R.string.last_played_at,
								getDateAndTimeForHumans(lastPlayed));
					}
					label.setVisibility(lastPlayedString == null ? View.GONE
							: View.VISIBLE);
					label.setText(lastPlayedString);
					break;
				case R.id.created:
					long created = c.getLong(columnIndex);
					label = ((TextView) view);
					String createdString = null;
					if (created != 0) {
						createdString = getString(R.string.created_at,
								getDateAndTimeForHumans(created));
					}
					label.setVisibility(createdString == null ? View.GONE
							: View.VISIBLE);
					label.setText(createdString);
					break;
				case R.id.note:
					String note = c.getString(columnIndex);
					label = ((TextView) view);
					if (note == null || note.trim() == "") {
						((TextView) view).setVisibility(View.GONE);
					} else {
						((TextView) view).setText(note);
					}
					label
							.setVisibility((note == null || note.trim() == "") ? View.GONE
									: View.VISIBLE);
					label.setText(note);
					break;
				}
				
				return true;
			}
		});

		setListAdapter(adapter);
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		updateTitle();
	}
	
	/**
	 * Updates whole list.
	 */
	private void update() {
		// update title
		updateTitle();
		
		// update data bound to the list
		cursor.requery();
	}
	
	private void updateTitle() {
		SudokuDatabase sudokuDB = new SudokuDatabase(this);
		FolderInfo folder = sudokuDB.getFolder(folderID);
		setTitle(folder.name + " - " + sudokuDB.getFolder(folderID).getDetail(this));
	}

	private String getTime(long time) {
		timeText.setLength(0);
		gameTimeFormatter.format("%02d:%02d", time / 60000, time / 1000 % 60);
		return timeText.toString();
	}

	private String getDateAndTimeForHumans(long datetime) {
		Date date = new Date(datetime);

		// TODO: temporary version, find clearer way and perhaps optimize
		Date now = new Date(System.currentTimeMillis());
		Date today = new Date(now.getYear(), now.getMonth(), now.getDate());
		Date yesterday = new Date(System.currentTimeMillis()
				- (1000 * 60 * 60 * 24));

		if (date.after(today)) {
			return getString(R.string.at_time, timeFormatter.format(date));
		} else if (date.after(yesterday)) {
			return getString(R.string.yesterday_at_time, timeFormatter.format(date));
		} else {
			return getString(R.string.on_date, dateTimeFormatter.format(date));
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		// This is our one standard application action -- inserting a
		// new note into the list.
		menu.add(0, MENU_ITEM_INSERT, 0, R.string.add_sudoku).setShortcut('3', 'a')
				.setIcon(android.R.drawable.ic_menu_add);

		// Generate any additional actions that can be performed on the
		// overall list. In a normal install, there are no additional
		// actions found here, but this allows other applications to extend
		// our menu with their own actions.
		Intent intent = new Intent(null, getIntent().getData());
		intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
		menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
				new ComponentName(this, FolderListActivity.class), null,
				intent, 0, null);

		return true;

	}

	/**
	 * Shows dialog confirming deletion of selected puzzle.
	 * 
	 * @param sudokuId
	 *            Id of selected puzzle.
	 */
	private void showDeleteDialog(long sudokuId) {
		final long sudokuToDeleteId = sudokuId;
		Dialog dialog = new AlertDialog.Builder(this).setIcon(
				android.R.drawable.ic_delete).setTitle("Puzzle").setMessage(
				R.string.delete_puzzle_confirm)
				.setPositiveButton(android.R.string.yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								SudokuDatabase db = new SudokuDatabase(
										SudokuListActivity.this);
								db.deleteSudoku(sudokuToDeleteId);
								update();
							}
						}).setNegativeButton(android.R.string.no, null).create();

		dialog.show();
	}

	/**
	 * Shows dialog confirming resetting of selected puzzle.
	 * 
	 * @param sudokuId
	 *            Id of selected puzzle.
	 */
	private void showResetDialog(long sudokuId) {
		final long sudokuToResetId = sudokuId;
		Dialog dialog = new AlertDialog.Builder(this).setIcon(
				android.R.drawable.ic_menu_rotate).setTitle("Puzzle")
				.setMessage(R.string.reset_puzzle_confirm)
				.setPositiveButton(android.R.string.yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								SudokuDatabase db = new SudokuDatabase(
										SudokuListActivity.this);
								SudokuGame game = db.getSudoku(sudokuToResetId);
								if (game != null) {
									game.reset();
									db.updateSudoku(game);
								}
								update();
							}
						}).setNegativeButton(android.R.string.no, null).create();

		dialog.show();
	}

	/**
	 * Shows dialog which allows user to edit note attached to selected puzzle.
	 * 
	 * @param sudokuId
	 *            Id of selected puzzle.
	 */
	private void showEditNoteDialog(long sudokuId) {
		SudokuDatabase db = new SudokuDatabase(this);
		final SudokuGame gameToUpdate = db.getSudoku(sudokuId);

		LayoutInflater factory = LayoutInflater.from(this);
		final View noteView = factory.inflate(R.layout.sudoku_list_item_note,
				null);
		final TextView noteInput = (TextView) noteView.findViewById(R.id.note);

		noteInput.setText(gameToUpdate.getNote());

		Dialog dialog = new AlertDialog.Builder(this).setIcon(
				android.R.drawable.ic_menu_add).setTitle(R.string.edit_note)
				.setView(noteView).setPositiveButton(R.string.save,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								SudokuDatabase db = new SudokuDatabase(
										SudokuListActivity.this);
								gameToUpdate.setNote(noteInput.getText()
										.toString());
								db.updateSudoku(gameToUpdate);
								update();
							}
						}).setNegativeButton(android.R.string.cancel, null).create();

		dialog.show();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view,
			ContextMenuInfo menuInfo) {
		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		} catch (ClassCastException e) {
			Log.e(TAG, "bad menuInfo", e);
			return;
		}

		// TODO:
		// Cursor cursor = (Cursor)
		// sudokuList.getAdapter().getItem(info.position);
		Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
		if (cursor == null) {
			// For some reason the requested item isn't available, do nothing
			return;
		}

		menu.setHeaderTitle("Puzzle");

		// Add a menu item to delete the note
		menu.add(0, MENU_ITEM_PLAY, 0, R.string.play_puzzle);
		menu.add(0, MENU_ITEM_EDIT_NOTE, 1, R.string.edit_note);
		menu.add(0, MENU_ITEM_RESET, 2, R.string.reset_puzzle);
		menu.add(0, MENU_ITEM_EDIT, 3, R.string.edit_puzzle);
		menu.add(0, MENU_ITEM_DELETE, 4, R.string.delete_puzzle);
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
		case MENU_ITEM_PLAY:
			playSudoku(info.id);
			return true;
		case MENU_ITEM_EDIT:
			Intent i = new Intent(this, SudokuEditActivity.class);
			i.setAction(Intent.ACTION_EDIT);
			i.putExtra(SudokuEditActivity.EXTRAS_SUDOKU_ID, info.id);
			startActivity(i);
			return true;
		case MENU_ITEM_DELETE:
			showDeleteDialog(info.id);
			return true;
		case MENU_ITEM_EDIT_NOTE:
			showEditNoteDialog(info.id);
			return true;
		case MENU_ITEM_RESET:
			showResetDialog(info.id);
			return true;

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

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		playSudoku(id);
	}

	// private OnItemClickListener sudokuListItemListener = new
	// OnItemClickListener() {
	//
	// @Override
	// public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
	// long id) {
	// playSudoku(id);
	// }
	// };

	private void playSudoku(long sudokuID) {
		Intent i = new Intent(SudokuListActivity.this, SudokuPlayActivity.class);
		i.putExtra(SudokuPlayActivity.EXTRAS_SUDOKU_ID, sudokuID);
		startActivity(i);
	}

}
