package cz.romario.opensudoku.db;

import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import cz.romario.opensudoku.game.FolderInfo;
import cz.romario.opensudoku.game.SudokuCellCollection;
import cz.romario.opensudoku.game.SudokuGame;

/**
 * 
 * @author romario
 *
 */
public class SudokuDatabase {
	public static final String DATABASE_NAME = "opensudoku";
    public static final int DATABASE_VERSION = 7;
    
    public static final String SUDOKU_TABLE_NAME = "sudoku";
    public static final String FOLDER_TABLE_NAME = "folder";
    
    private static final String[] sudokuListProjection;
    //private static final String TAG = "SudokuDatabase";

    private DatabaseHelper mOpenHelper;
    
    public SudokuDatabase(Context context) {
    	mOpenHelper = new DatabaseHelper(context);
    }

    /**
     * Returns list of puzzle folders.
     * TODO: Other methods are closing database attached to cursor, which this method returns.
     * 
     * @return
     */
    public Cursor getFolderList() {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        qb.setTables(FOLDER_TABLE_NAME);
        
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        return qb.query(db, null, null, null, null, null, "created ASC");
    }
    
    /**
     * Returns the folder info.
     * 
     * @param folderID Primary key of folder.
     * @return
     */
    public FolderInfo getFolderInfo(long folderID) {
    	FolderInfo folder = null;
    	
    	SQLiteDatabase db = null;
    	Cursor c = null;
    	try
        {
    		
    		
	    	db = mOpenHelper.getReadableDatabase();
	        
	        // selectionArgs: You may include ?s in where clause in the query, which will be replaced by the values from selectionArgs. The values will be bound as Strings.
	    	String q = "select folder._id as _id, folder.name as name, sudoku.state as state, count(sudoku.state) as count from folder left join sudoku on folder._id = sudoku.folder_id where folder._id = " + folderID + " group by sudoku.state";
	        c = db.rawQuery(q, null);
	        
	        while (c.moveToNext()) {
	        	long id = c.getLong(c.getColumnIndex(FolderColumns._ID));
	        	String name = c.getString(c.getColumnIndex(FolderColumns.NAME));
	        	int state = c.getInt(c.getColumnIndex(SudokuColumns.STATE));
	        	int count = c.getInt(c.getColumnIndex("count"));
	        	
	        	if (folder == null) {
	        		folder = new FolderInfo(id, name);
	        	}
	        	
	        	folder.puzzleCount += count;
	        	if (state == SudokuGame.GAME_STATE_COMPLETED) {
	        		folder.solvedCount += count;
	        	}
	        	if (state == SudokuGame.GAME_STATE_PLAYING) {
	        		folder.playingCount += count;
	        	}
	        }
        }
        finally {
        	if (c != null) {
        		c.close();
        	}
        	// TODO: I think that db should be closed explicitly by client
        	if (db != null) {
        		db.close();
        	}
        }
        
        return folder;
    }
    
    /**
     * Inserts new puzzle folder into the database. 
     * @param name Name of the folder.
     * @return
     */
    public long insertFolder(String name) {
        Long created = Long.valueOf(System.currentTimeMillis());

        ContentValues values = new ContentValues();
        values.put(FolderColumns.CREATED, created);
        values.put(FolderColumns.NAME, name);

        SQLiteDatabase db = null;
        long rowId;
        try {
	        db = mOpenHelper.getWritableDatabase();
	        rowId = db.insert(FOLDER_TABLE_NAME, FolderColumns._ID, values);
        } finally {
        	if (db != null) db.close();
        }

        if (rowId > 0) {
            return rowId;
        }

        throw new SQLException(String.format("Failed to insert folder '%s'.", name));
    }
    
    /**
     * Updates folder's information.
     * 
     * @param folderID Primary key of folder.
     * @param name New name for the folder.
     */
    public void updateFolder(long folderID, String name) {
        ContentValues values = new ContentValues();
        values.put(FolderColumns.NAME, name);

        SQLiteDatabase db = null;
        try {
	        db = mOpenHelper.getWritableDatabase();
	        db.update(FOLDER_TABLE_NAME, values, FolderColumns._ID + "=" + folderID, null);
        } finally {
        	if (db != null) db.close();
        }
    }
    
    /**
     * Deletes given folder.
     * 
     * @param folderID Primary key of folder.
     */
    public void deleteFolder(long folderID) {
    	SQLiteDatabase db = null;
    	try {
	    	db = mOpenHelper.getWritableDatabase();
	        // delete all puzzles in folder we are going to delete
	    	db.delete(SUDOKU_TABLE_NAME, SudokuColumns.FOLDER_ID + "=" + folderID, null);
	    	// delete the folder
	    	db.delete(FOLDER_TABLE_NAME, FolderColumns._ID + "=" + folderID, null);
	    } finally {
	    	if (db != null) db.close();
	    }

    }
    
    /**
     * Returns list of puzzles in the given folder.
     * 
     * TODO: Other methods are closing database attached to cursor, which this method returns.
     * 
     * @param folderID Primary key of folder.
     * @return
     */
    public Cursor getSudokuList(long folderID) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        qb.setTables(SUDOKU_TABLE_NAME);
        //qb.setProjectionMap(sPlacesProjectionMap);
        qb.appendWhere(SudokuColumns.FOLDER_ID + "=" + folderID);
        
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        return qb.query(db, sudokuListProjection, null, null, null, null, "created DESC");
    }
    
    /**
     * Returns sudoku game object.
     * 
     * @param sudokuID Primary key of folder.
     * @return
     */
    public SudokuGame getSudoku(long sudokuID) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        qb.setTables(SUDOKU_TABLE_NAME);
        qb.appendWhere(SudokuColumns._ID + "=" + sudokuID);
        
        // Get the database and run the query
        
        SQLiteDatabase db = null;
        Cursor c = null;
        SudokuGame s = null;
        try {
            db = mOpenHelper.getReadableDatabase();
            c = qb.query(db, null, null, null, null, null, null);
        	
        	if (c.moveToFirst()) {
            	int id = c.getInt(c.getColumnIndex(SudokuColumns._ID));
            	Date created = new Date(c.getLong(c.getColumnIndex(SudokuColumns.CREATED)));
            	String data = c.getString(c.getColumnIndex(SudokuColumns.DATA));
            	Date lastPlayed = new Date(c.getLong(c.getColumnIndex(SudokuColumns.LAST_PLAYED)));
            	int state = c.getInt(c.getColumnIndex(SudokuColumns.STATE));
            	long time = c.getLong(c.getColumnIndex(SudokuColumns.TIME));
            	String note = c.getString(c.getColumnIndex(SudokuColumns.PUZZLE_NOTE));
            	
            	s = new SudokuGame();
            	s.setId(id);
            	s.setCreated(created);
            	s.setCells(SudokuCellCollection.deserialize(data));
            	s.setLastPlayed(lastPlayed);
            	s.setState(state);
            	s.setTime(time);
            	s.setNote(note);
        	}
        } finally {
        	if (c != null) c.close();
        	
        	if (db != null) db.close();
        }
        
        return s;
        
    }
    
    /**
     * Inserts new puzzle into the database.
     * 
     * @param folderID Primary key of the folder in which puzzle should be saved.
     * @param sudoku 
     * @return
     */
    public long insertSudoku(long folderID, SudokuGame sudoku) {
        Long created = Long.valueOf(System.currentTimeMillis());

        ContentValues values = new ContentValues();
        values.put(SudokuColumns.DATA, sudoku.getCells().serialize());
        values.put(SudokuColumns.CREATED, sudoku.getCreated().getTime());
        values.put(SudokuColumns.LAST_PLAYED, sudoku.getLastPlayed().getTime());
        values.put(SudokuColumns.STATE, sudoku.getState());
        values.put(SudokuColumns.TIME, sudoku.getTime());
        values.put(SudokuColumns.PUZZLE_NOTE, sudoku.getNote());
        values.put(SudokuColumns.FOLDER_ID, folderID);
        
        SQLiteDatabase db = null;
        try {
	        db = mOpenHelper.getWritableDatabase();
	        long rowId = db.insert(SUDOKU_TABLE_NAME, FolderColumns.NAME, values);
	        if (rowId > 0) {
	            return rowId;
	        }
        } finally {
        	if (db != null) db.close();
        }

        throw new SQLException("Failed to insert sudoku.");
    }
    
    /**
     * Updates sudoku game in the database.
     * 
     * @param sudoku 
     */
    public void updateSudoku(SudokuGame sudoku) {
        ContentValues values = new ContentValues();
        values.put(SudokuColumns.DATA, sudoku.getCells().serialize());
        values.put(SudokuColumns.LAST_PLAYED, sudoku.getLastPlayed().getTime());
        values.put(SudokuColumns.STATE, sudoku.getState());
        values.put(SudokuColumns.TIME, sudoku.getTime());
        values.put(SudokuColumns.PUZZLE_NOTE, sudoku.getNote());
        
        SQLiteDatabase db = null;
        try {
	        db = mOpenHelper.getWritableDatabase();
	        db.update(SUDOKU_TABLE_NAME, values, SudokuColumns._ID + "=" + sudoku.getId(), null);
        } finally {
        	if (db != null) db.close();
        }
    }
    

    /**
     * Deletes given sudoku from the database.
     * 
     * @param sudokuID
     */
    public void deleteSudoku(long sudokuID) {
    	SQLiteDatabase db = null;
    	try {
	    	db = mOpenHelper.getWritableDatabase();
	        db.delete(SUDOKU_TABLE_NAME, SudokuColumns._ID + "=" + sudokuID, null);
	    } finally {
	    	if (db != null) db.close();
	    }
    }
    
    public void generateDebugPuzzles(int numOfFolders, int puzzlesPerFolder) {
    	for (int f=0; f<numOfFolders; f++) {
    		long folderID = insertFolder("debug" + f);
    		for (int p=0; p<puzzlesPerFolder; p++) {
    			SudokuGame game = new SudokuGame();
    			game.setCells(SudokuCellCollection.createDebugGame());
    			insertSudoku(folderID, game);
    		}
    	}
    }
    
    /**
     * TODO: You need to call this in activity's onDestroy when using getFolderList
     * or getSudokuList and let activity to manage the cursor.
     * 
     */
    public void close() {
    	mOpenHelper.close();
    }
    
    static {
    	sudokuListProjection = new String[] {
    		SudokuColumns._ID,
    		SudokuColumns.CREATED,
    		SudokuColumns.STATE,
    		SudokuColumns.TIME,
    		SudokuColumns.DATA,
    		SudokuColumns.LAST_PLAYED,
    		SudokuColumns.PUZZLE_NOTE
    	};
    }

}
