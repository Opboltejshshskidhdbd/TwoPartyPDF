package com.twoparty.pdfreader;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "StudyOrganizer.db";
    public static final String TABLE_NAME = "items";
    public static final String TABLE_HISTORY = "history";

    public DBHelper(Context context) { 
        super(context, DB_NAME, null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, NAME TEXT, PATH TEXT, PARENT_ID INTEGER)");
        db.execSQL("CREATE TABLE " + TABLE_HISTORY + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, ITEM_ID INTEGER UNIQUE, LAST_PAGE INTEGER, TIMESTAMP LONG)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_HISTORY + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, ITEM_ID INTEGER UNIQUE, LAST_PAGE INTEGER, TIMESTAMP LONG)");
        }
    }

    public long insertItem(String name, String path, int parentId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("NAME", name);
        cv.put("PATH", path);
        cv.put("PARENT_ID", parentId);
        return db.insert(TABLE_NAME, null, cv);
    }

    public void updateItemName(int id, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("NAME", newName);
        db.update(TABLE_NAME, cv, "ID = ?", new String[]{String.valueOf(id)});
    }

    public List<StudyItem> getItemsInFolder(int parentId) {
        List<StudyItem> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM items WHERE PARENT_ID = ?", new String[]{String.valueOf(parentId)});
        if (c.moveToFirst()) {
            do { 
                list.add(new StudyItem(c.getInt(0), c.getString(1), c.getString(2), c.getInt(3)));
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    public void deleteItem(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, "ID = ?", new String[]{String.valueOf(id)});
        db.delete(TABLE_HISTORY, "ITEM_ID = ?", new String[]{String.valueOf(id)});
    }

    public void updateHistory(int itemId, int page) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("ITEM_ID", itemId);
        cv.put("LAST_PAGE", page);
        cv.put("TIMESTAMP", System.currentTimeMillis());
        db.insertWithOnConflict(TABLE_HISTORY, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public int getLastPage(int itemId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT LAST_PAGE FROM history WHERE ITEM_ID = ?", new String[]{String.valueOf(itemId)});
        int page = 0;
        if (c.moveToFirst()) {
            page = c.getInt(0);
        }
        c.close();
        return page;
    }

    public List<StudyItem> getRecentItems() {
        List<StudyItem> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT items.ID, items.NAME, items.PATH, items.PARENT_ID FROM history " +
                "JOIN items ON history.ITEM_ID = items.ID ORDER BY history.TIMESTAMP DESC LIMIT 5", null);
        if (c.moveToFirst()) {
            do {
                list.add(new StudyItem(c.getInt(0), c.getString(1), c.getString(2), c.getInt(3)));
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }
}