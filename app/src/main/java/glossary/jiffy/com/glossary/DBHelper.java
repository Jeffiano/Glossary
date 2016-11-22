package glossary.jiffy.com.glossary;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2016/11/4.
 */
public class DBHelper extends SQLiteOpenHelper {

    public static String db = "jiffy_glossary.db";
    public static DBHelper instance;
    private static final int VERSION = 2;

    public static DBHelper initSingleton(Context context) {
        if (instance == null) {
            instance = new DBHelper(context, null, null, VERSION);
        }
        return instance;
    }

    public DBHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, db, factory, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS word(_id integer primary key, "
                + "cache_state integer ,in_memory_count integer, pron_url varchar(32), pron_file_path varchar(32),  english varchar(32),chinese varchar(32))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE word ADD COLUMN cache_state integer");
        }
    }

    private static void close(Cursor cursor) {
        if (cursor != null)
            cursor.close();
    }

    private static boolean moveCusror(Cursor cursor) {
        return (cursor != null && cursor.moveToFirst() && cursor.getCount() > 0);
    }

    private SQLiteDatabase getDb() {
        return this.getReadableDatabase();
    }

    public static class WordInfo {
        private static String table = "word";

        public static synchronized void insert(Word word) {
            try {
                SQLiteDatabase sdb = instance.getDb();
                ContentValues cvs = new ContentValues();
                cvs.put("english", word.english);
                cvs.put("chinese", word.chinese);
                cvs.put("in_memory_count", word.inMemoryCount);
                cvs.put("pron_file_path", word.pronFilePath);
                cvs.put("pron_url", word.pronUrl);
                cvs.put("cache_state", word.cacheState);
                word._id = sdb.insert(table, null, cvs);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static void deleteWord(int _id) {
            SQLiteDatabase sdb = instance.getDb();
            sdb.execSQL("delete from word where _id=" + _id);
        }

        public static boolean isWordExist(String english) {
            SQLiteDatabase sdb = instance.getDb();
            String sql = "select count(*) from word where english= '" + english+"'";
            Cursor cursor = sdb.rawQuery(sql, null);
            if (moveCusror(cursor)) {
                int result = cursor.getInt(0);
                close(cursor);
                return result > 0;
            } else {
                close(cursor);
                return false;
            }
        }
        public static void updateInMemoryCount(Word word) {
            SQLiteDatabase sdb = instance.getDb();
            sdb.execSQL("update word set in_memory_count = " + word.inMemoryCount  +" where _id=" + word._id);
        }
        public static void updateCacheState(Word word) {
            SQLiteDatabase sdb = instance.getDb();
            sdb.execSQL("update word set cache_state = " + word.cacheState  +" where _id=" + word._id);
        }

        public static int getWordCount() {
            SQLiteDatabase sdb = instance.getDb();
            String sql = "select count(*) from word ";
            Cursor cursor = sdb.rawQuery(sql, null);
            if (moveCusror(cursor)) {
                int result = cursor.getInt(0);
                close(cursor);
                return result;
            } else {
                close(cursor);
                return 0;
            }
        }

        public static int listWords(List<Word> list, int filter) {
            SQLiteDatabase sdb = instance.getDb();
            int count = 0;
            String sql = "";
            if (list != null) {
                if(filter == Word.MEMORY_COUNT_ALL){
                    sql = "select * from word";
                }else if(filter == Word.MEMORY_COUNT_IN){
                    sql = "select * from word where in_memory_count = " + Word.MEMORY_COUNT_IN;
                }else if(filter == Word.MEMORY_COUNT_OUT){
                    sql = "select * from word where in_memory_count = " + Word.MEMORY_COUNT_OUT;
                }
            }
            Cursor cursor = sdb.rawQuery(sql, null);
            if (moveCusror(cursor)) {
                count = cursor.getCount();
                list.clear();
                for (int i = 0; i < count; i++) {
                    cursor.moveToPosition(i);
                    Word word = new Word();
                    word._id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
                    word.english = cursor.getString(cursor.getColumnIndexOrThrow("english"));
                    word.chinese = cursor.getString(cursor.getColumnIndexOrThrow("chinese"));
                    word.inMemoryCount = cursor.getInt(cursor.getColumnIndexOrThrow("in_memory_count"));
                    word.pronFilePath = cursor.getString(cursor.getColumnIndexOrThrow("pron_file_path"));
                    word.pronUrl = cursor.getString(cursor.getColumnIndexOrThrow("pron_url"));
                    word.cacheState = cursor.getInt(cursor.getColumnIndexOrThrow("cache_state"));
                    list.add(word);
                }
                Collections.shuffle(list);
            }
            close(cursor);
            return count;
        }

    }
}

