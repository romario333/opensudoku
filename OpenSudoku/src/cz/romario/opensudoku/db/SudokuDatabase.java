package cz.romario.opensudoku.db;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import cz.romario.opensudoku.game.FolderInfo;
import cz.romario.opensudoku.game.SudokuCellCollection;
import cz.romario.opensudoku.game.SudokuGame;

public class SudokuDatabase {
	public static final String DATABASE_NAME = "sudoku"; // TODO: debug
    public static final int DATABASE_VERSION = 9;
    
    public static final String SUDOKU_TABLE_NAME = "sudoku";
    public static final String FOLDER_TABLE_NAME = "folder";
    
    private static final String[] sudokuListProjection;
    
    private static final String TAG = "SudokuDatabase";
    

    private DatabaseHelper mOpenHelper;
    
    public SudokuDatabase(Context context) {
    	mOpenHelper = new DatabaseHelper(context);
    }
    

    public FolderInfo[] getFolderList() {
    	Map<Long, FolderInfo> folders = new HashMap<Long, FolderInfo>();
    	
    	SQLiteDatabase db = null;
    	Cursor c = null;
    	try
        {
	    	db = mOpenHelper.getReadableDatabase();
	        
	        // selectionArgs: You may include ?s in where clause in the query, which will be replaced by the values from selectionArgs. The values will be bound as Strings.
	        c = db.rawQuery("select folder._id as _id, folder.name as name, sudoku.state as state, count(sudoku.state) as count from folder left join sudoku on folder._id = sudoku.folder_id group by folder._id, sudoku.state;", null);
	        
	        while (c.moveToNext()) {
	        	long id = c.getLong(c.getColumnIndex(FolderColumns._ID));
	        	String name = c.getString(c.getColumnIndex(SudokuColumns.NAME));
	        	int state = c.getInt(c.getColumnIndex(SudokuColumns.STATE));
	        	int count = c.getInt(c.getColumnIndex("count"));
	        	
	        	if (!folders.containsKey(id)) {
	        		folders.put(id, new FolderInfo(id, name));
	        	}
	        	FolderInfo folder = folders.get(id);
	        	folder.puzzleCount += count;
	        	if (state == SudokuGame.GAME_STATE_COMPLETED) {
	        		folder.solvedCount += count;
	        	}
	        }
        }
        finally {
        	if (c != null) {
        		c.close();
        	}
        	if (db != null) {
        		db.close();
        	}
        }
        
        FolderInfo[] foldersArray = new FolderInfo[folders.size()];
        folders.values().toArray(foldersArray);
        
        return foldersArray;
    }
    
    public String getFolderName(long folderID) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        qb.setTables(FOLDER_TABLE_NAME);
        //qb.setProjectionMap(sPlacesProjectionMap);
        qb.appendWhere(FolderColumns._ID + "=" + folderID);
        
        SQLiteDatabase db = null;
        Cursor c = null;
        try {
	        db = mOpenHelper.getReadableDatabase();
	        c = qb.query(db, null, null, null, null, null, null);
	        c.moveToNext();
	        String name = c.getString(c.getColumnIndex(SudokuColumns.NAME));
	        return name;
        } finally {
        	if (c != null) {
        		c.close();
        	}
        	if (db != null) {
        		db.close();
        	}
        }
    }
    
    // TODO: Folder object
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
        	// TODO: mozna nemusim delat
        	if (db != null) db.close();
        }

        if (rowId > 0) {
            return rowId;
        }
        

        throw new SQLException(String.format("Failed to insert folder '%s'.", name));
    }
    
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
    
    public void deleteFolder(long folderID) {
    	// TODO: kontrola, ze v nem nejsou zadna sudoku
    	SQLiteDatabase db = null;
    	try {
	    	db = mOpenHelper.getWritableDatabase();
	        db.delete(FOLDER_TABLE_NAME, FolderColumns._ID + "=" + folderID, null);
	    } finally {
	    	if (db != null) db.close();
	    }

    }
    
    public Cursor getSudokuList(long folderID) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        qb.setTables(SUDOKU_TABLE_NAME);
        //qb.setProjectionMap(sPlacesProjectionMap);
        qb.appendWhere(SudokuColumns.FOLDER_ID + "=" + folderID);
        
        // Get the database and run the query
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        return qb.query(db, sudokuListProjection, null, null, null, null, "created DESC");
    }
    
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
            	String name = c.getString(c.getColumnIndex(SudokuColumns.NAME));
            	int state = c.getInt(c.getColumnIndex(SudokuColumns.STATE));
            	long time = c.getLong(c.getColumnIndex(SudokuColumns.TIME));
            	
            	s = new SudokuGame();
            	s.setId(id);
            	s.setCreated(created);
            	s.setCells(SudokuCellCollection.deserialize(data));
            	s.setLastPlayed(lastPlayed);
            	s.setName(name);
            	s.setState(state);
            	s.setTime(time);
        	}
        } finally {
        	if (c != null) {
	        	c.close();
        	}
        	
        	if (db != null) {
        		db.close();
        	}
        }
        
        return s;
        
    }
    
    public long insertSudoku(long folderID, String name, SudokuCellCollection sudoku) {
        Long created = Long.valueOf(System.currentTimeMillis());

        ContentValues values = new ContentValues();
        values.put(SudokuColumns.CREATED, created);
        // TODO: auto-generate name if not set
        values.put(SudokuColumns.NAME, name);
        values.put(SudokuColumns.TIME, 0);
        values.put(SudokuColumns.STATE, SudokuGame.GAME_STATE_NOT_STARTED); // TODO: enum
        values.put(SudokuColumns.FOLDER_ID, folderID);
        values.put(SudokuColumns.DATA, sudoku.serialize());

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert(SUDOKU_TABLE_NAME, SudokuColumns.NAME, values);
        if (rowId > 0) {
            return rowId;
        }

        throw new SQLException(String.format("Failed to insert sudoku '%s'.", name));
    }
    
    public void updateSudoku(SudokuGame sudoku) {
        ContentValues values = new ContentValues();
        values.put(SudokuColumns.NAME, sudoku.getName());
        values.put(SudokuColumns.DATA, sudoku.getCells().serialize());
        values.put(SudokuColumns.LAST_PLAYED, sudoku.getLastPlayed().getTime());
        values.put(SudokuColumns.STATE, sudoku.getState());
        values.put(SudokuColumns.TIME, sudoku.getTime());
        
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.update(SUDOKU_TABLE_NAME, values, SudokuColumns._ID + "=" + sudoku.getId(), null);
    }
    
    public void deleteSudoku(SudokuCellCollection sudoku) {
    	
    }
    
    static {
    	sudokuListProjection = new String[] {
    		SudokuColumns._ID,
    		SudokuColumns.NAME,
    		SudokuColumns.CREATED,
    		SudokuColumns.STATE,
    		SudokuColumns.TIME,
    		SudokuColumns.DATA,
    		SudokuColumns.LAST_PLAYED
    	};
    }

}
