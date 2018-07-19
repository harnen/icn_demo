package uk.ac.ucl.ndnocr.data;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import uk.ac.ucl.ndnocr.utils.G;

/**
 * Created by srenevic on 24/08/17.
 */

public class DatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "contentManager";

    // Contacts table name
    private static final String TABLE_CONTENT = "content";

    // Contacts Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_DESC = "desc";
    private static final String KEY_URL = "url";
    private static final String KEY_DOWN = "downloaded";
    private static final String KEY_URI = "uri";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTENT + "("
                + KEY_URI + " TEXT PRIMARY KEY NOT NULL,"
                + KEY_NAME + " TEXT ,"
                + KEY_DESC + " TEXT," + KEY_URL + " TEXT, " + KEY_DOWN + " INT)";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTENT);

        // Create tables again
        onCreate(db);
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    // Adding new contact
    public boolean addContent(Content content) {
        SQLiteDatabase db = this.getWritableDatabase();

        String selectQuery = "SELECT COUNT(*) FROM " + TABLE_CONTENT + " WHERE name='" +content.getName()+"'";
        Cursor cursor = db.rawQuery(selectQuery, null);
        int val=0;
        if (cursor.moveToFirst()) {
            do {
                G.Log("Data "+cursor.getInt(0));
                val=cursor.getInt(0);

            } while (cursor.moveToNext());
        }
        if(val>0) return false;
        ContentValues values = new ContentValues();
        values.put(KEY_URI, content.getUri());
        values.put(KEY_NAME, content.getName()); // Contact Name
        values.put(KEY_DESC, content.getText()); // Contact Phone
        values.put(KEY_URL, content.getUrl()); // Contact Phone
        values.put(KEY_DOWN, 0);
        // Inserting Row
        db.insert(TABLE_CONTENT, null, values);
        db.close(); // Closing database connection

        return true;
    }

    public void rmContent(String uri) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CONTENT, KEY_URI + " = ?",
                new String[] { uri });
        db.close();
    }


    public int setContentText(String name,String text)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_DESC, text);
        G.Log("Update  "+name+" "+text);
        // updating row
        return db.update(TABLE_CONTENT, values, KEY_NAME + " = ?",
                new String[] {String.valueOf(name)});
    }
    public int setContentDownloaded(String uri)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_DOWN, 1);

        // updating row
        return db.update(TABLE_CONTENT, values, KEY_URI + " = ?",
                new String[] {String.valueOf(uri)});
    }

    public List<String> getPendingContent()
    {
        List<String> contentList = new ArrayList<String>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CONTENT + " WHERE "+KEY_DOWN+"=0";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                contentList.add(cursor.getString(1));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close(); // Closing database connection
        // return contact list
        return contentList;
    }

    public List<Content> getContentDownloaded()
    {
        List<Content> contentList = new ArrayList<Content>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CONTENT + " WHERE "+KEY_DOWN+"=1";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Content content = new Content();
                // content.setID(Integer.parseInt(cursor.getString(0)));
                content.setUri(cursor.getString(0));
                content.setName(cursor.getString(1));
                content.setText(cursor.getString(2));
                content.setUrl(cursor.getString(3));
                contentList.add(content);

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close(); // Closing database connection
        // return contact list
        return contentList;
    }


    public int getPendingCount()
    {
        // Select All Query
        String countQuery = "SELECT  * FROM " + TABLE_CONTENT + " WHERE "+KEY_DOWN+"=0";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int value = cursor.getCount();
        cursor.close();
        db.close(); // Closing database connection

        // return count
        return value;
    }

    // Getting All Contacts
    public List<Content> getContent() {
        List<Content> contentList = new ArrayList<Content>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CONTENT;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Content content = new Content();
                // content.setID(Integer.parseInt(cursor.getString(0)));
                content.setUri(cursor.getString(0));
                content.setName(cursor.getString(1));
                content.setText(cursor.getString(2));
                content.setUrl(cursor.getString(3));
                // Adding contact to list
                contentList.add(content);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close(); // Closing database connection
        // return contact list
        return contentList;
    }

    /*// Updating single contact
    public int updateContact(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, contact.getName());
        values.put(KEY_PH_NO, contact.getPhoneNumber());

        // updating row
        return db.update(TABLE_CONTENT, values, KEY_ID + " = ?",
                new String[] { String.valueOf(contact.getID()) });
    }*/



    // Getting contacts Count
    public int getContentCount() {
        String countQuery = "SELECT  * FROM " + TABLE_CONTENT;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int value = cursor.getCount();
        cursor.close();
        db.close(); // Closing database connection

        // return count
        return value;
    }

}
