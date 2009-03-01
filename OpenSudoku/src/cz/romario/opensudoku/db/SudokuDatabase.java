package cz.romario.opensudoku.db;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import cz.romario.opensudoku.Const;
import cz.romario.opensudoku.Sudoku;

public class SudokuDatabase {
	private static final String DATABASE_NAME = "sudoku"; // TODO: debug
    public static final int DATABASE_VERSION = 1;
    
    private static final String SUDOKU_TABLE_NAME = "sudoku";
    private static final String FOLDER_TABLE_NAME = "folder";
    
	/**
     * This class helps open, create, and upgrade the database file.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + SUDOKU_TABLE_NAME + " ("
                    + SudokuColumns._ID + " INTEGER PRIMARY KEY,"
                    + SudokuColumns.NAME + " TEXT,"
                    + SudokuColumns.CREATED + " INTEGER,"
                    + SudokuColumns.STATE + " INTEGER,"
                    + SudokuColumns.TIME + " INTEGER,"
                    + SudokuColumns.LAST_PLAYED + " INTEGER,"
                    + SudokuColumns.DATA + " Text"
                    + ");");
            
            db.execSQL("CREATE TABLE " + FOLDER_TABLE_NAME + " ("
                    + FolderColumns._ID + " INTEGER PRIMARY KEY,"
                    + SudokuColumns.CREATED + " INTEGER,"
                    + FolderColumns.NAME + " TEXT,"
                    + FolderColumns.FOLDER_ID + " INTEGER"
                    + ");");
            
            // TODO: insert root folder
            
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(Const.TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + SUDOKU_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + FOLDER_TABLE_NAME);
            onCreate(db);
        }
    }

    private DatabaseHelper mOpenHelper;
    
    public SudokuDatabase(Context context) {
    	mOpenHelper = new DatabaseHelper(context);
    }
    

    // TODO: nested-folders suport
    public Cursor getFolderList() {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        qb.setTables(FOLDER_TABLE_NAME);
        //qb.setProjectionMap(sPlacesProjectionMap);
        //qb.appendWhere(PlacesColumns._ID + "=" + itemId);
        
        // Get the database and run the query
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        return qb.query(db, null, null, null, null, null, "created DESC");
    }
    
    // TODO: Folder object
    public long insertFolder(String name) {
        Long created = Long.valueOf(System.currentTimeMillis());

        ContentValues values = new ContentValues();
        values.put(FolderColumns.CREATED, created);
        values.put(FolderColumns.NAME, name);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert(FOLDER_TABLE_NAME, FolderColumns.FOLDER_ID, values);
        if (rowId > 0) {
            return rowId;
        }

        throw new SQLException(String.format("Failed to insert folder '%s'.", name));
    }
    
    public void updateFolder(long folderID, String name) {
        ContentValues values = new ContentValues();
        values.put(FolderColumns.NAME, name);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.update(FOLDER_TABLE_NAME, values, FolderColumns._ID + "=" + folderID, null);
    }
    
    public void deleteFolder(long folderID) {
    	// TODO: kontrola, ze v nem nejsou zadna sudoku
    	SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.delete(FOLDER_TABLE_NAME, FolderColumns._ID + "=" + folderID, null);
    }
    
    public Cursor getSudokuList(long folderID) {
    	// setTables("foo LEFT OUTER JOIN bar ON (foo.id = bar.foo_id)")
    	
    	// TODO: neselectit vsechno, DATA by byla zbytecne brut
    	return null;
    }
    
    public Sudoku getSudoku(long sudokuID) {
    	return null;
    }
    
    public void insertSudoku(long folderID, Sudoku sudoku) {
    	
    }
    
    public void updateSudoku(Sudoku sudoku) {
    	
    }
    
    public void deleteSudoku(Sudoku sudoku) {
    	
    }

}
