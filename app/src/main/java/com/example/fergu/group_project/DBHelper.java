package com.example.fergu.group_project;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.example.fergu.group_project.listeners.OnChangeListener;

import java.util.Comparator;

/**
 * Created by fergu on 2017-12-28.
 */
public class DBHelper extends SQLiteOpenHelper {


        private Context context;
        private static final String LOG_TAG = "DBHelper";
        private static OnChangeListener databaseChangeListener;
        public static final String DATABASE_NAME = "saved_recordings.db";
        private static final int DATABASE_VERSION = 1;

        public static abstract class DBHelperItem implements BaseColumns {
            public static final String TABLE_NAME = "saved_recordings";
            public static final String COLUMN_NAME_RECORDING_NAME = "recording_name";
            public static final String COLUMN_NAME_RECORDING_FILE_PATH = "file_path";
            public static final String COLUMN_NAME_RECORDING_LENGTH = "length";
            public static final String COLUMN_NAME_TIME_ADDED = "time_added";
        }
        private static final String TEXT_TYPE = " TEXT";
        private static final String COMMA_SEP = ",";
        private static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + DBHelperItem.TABLE_NAME + " (" +
                        DBHelperItem._ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                        DBHelperItem.COLUMN_NAME_RECORDING_NAME + TEXT_TYPE + COMMA_SEP +
                        DBHelperItem.COLUMN_NAME_RECORDING_FILE_PATH + TEXT_TYPE + COMMA_SEP +
                        DBHelperItem.COLUMN_NAME_RECORDING_LENGTH + " INTEGER " + COMMA_SEP +
                        DBHelperItem.COLUMN_NAME_TIME_ADDED + " INTEGER " + ")";
        private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + DBHelperItem.TABLE_NAME;

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }

        public DBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            this.context = context;
        }

        public static void setOnDatabaseChangedListener(OnChangeListener listener) {
            databaseChangeListener = listener;
        }

        public RecordingMemo getItemAt(int position) {
            SQLiteDatabase database = getReadableDatabase();
            String[] projection = {
                    DBHelperItem._ID,
                    DBHelperItem.COLUMN_NAME_RECORDING_NAME,
                    DBHelperItem.COLUMN_NAME_RECORDING_FILE_PATH,
                    DBHelperItem.COLUMN_NAME_RECORDING_LENGTH,
                    DBHelperItem.COLUMN_NAME_TIME_ADDED
            };
            Cursor query = database.query(DBHelperItem.TABLE_NAME, projection, null, null, null, null, null);
            if (query.moveToPosition(position)) {
                RecordingMemo memo = new RecordingMemo();
                memo.setId(query.getInt(query.getColumnIndex(DBHelperItem._ID)));
                memo.setName(query.getString(query.getColumnIndex(DBHelperItem.COLUMN_NAME_RECORDING_NAME)));
                memo.setFilePath(query.getString(query.getColumnIndex(DBHelperItem.COLUMN_NAME_RECORDING_FILE_PATH)));
                memo.setLength(query.getInt(query.getColumnIndex(DBHelperItem.COLUMN_NAME_RECORDING_LENGTH)));
                memo.setTime(query.getLong(query.getColumnIndex(DBHelperItem.COLUMN_NAME_TIME_ADDED)));
                query.close();
                return memo;
            }
            return null;
        }

        public void removeItemWithId(int id) {
            SQLiteDatabase db = getWritableDatabase();
            String[] whereArgs = { String.valueOf(id) };
            db.delete(DBHelperItem.TABLE_NAME, "_ID=?", whereArgs);
        }

        public int getCount() {
            SQLiteDatabase database = getReadableDatabase();
            String[] projection = { DBHelperItem._ID };
            Cursor query = database.query(DBHelperItem.TABLE_NAME, projection, null, null, null, null, null);
            int count = query.getCount();
            query.close();
            return count;
        }

        public long addRecording(String recordingName, String filePath, long length) {

            SQLiteDatabase database = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DBHelperItem.COLUMN_NAME_RECORDING_NAME, recordingName);
            values.put(DBHelperItem.COLUMN_NAME_RECORDING_FILE_PATH, filePath);
            values.put(DBHelperItem.COLUMN_NAME_RECORDING_LENGTH, length);
            values.put(DBHelperItem.COLUMN_NAME_TIME_ADDED, System.currentTimeMillis());
            long rowId = database.insert(DBHelperItem.TABLE_NAME, null, values);

            if (databaseChangeListener != null) {
                databaseChangeListener.onNewDatabaseEntryAdded();
            }

            return rowId;
        }

        public void renameItem(RecordingMemo item, String recordingName, String filePath) {
            SQLiteDatabase database = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DBHelperItem.COLUMN_NAME_RECORDING_NAME, recordingName);
            values.put(DBHelperItem.COLUMN_NAME_RECORDING_FILE_PATH, filePath);
            database.update(DBHelperItem.TABLE_NAME, values,
                    DBHelperItem._ID + "=" + item.getId(), null);

            if (databaseChangeListener != null) {
                databaseChangeListener.onDatabaseEntryRenamed();
            }
        }


}

