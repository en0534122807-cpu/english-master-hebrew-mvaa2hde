package com.generated.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "english_mastery.db";
    private static final int DATABASE_VERSION = 1;

    // Table Words
    public static final String TABLE_WORDS = "words";
    public static final String COL_ID = "_id";
    public static final String COL_EN = "english";
    public static final String COL_HE = "hebrew";
    public static final String COL_LEVEL = "level";
    public static final String COL_CAT = "category";
    public static final String COL_IS_CUSTOM = "is_custom";
    public static final String COL_IS_REVIEW = "is_review";
    public static final String COL_CORRECT_STREAK = "correct_streak";

    // Table Stats
    public static final String TABLE_STATS = "stats";
    public static final String COL_KEY = "stat_key";
    public static final String COL_VAL = "stat_val";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_WORDS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_EN + " TEXT UNIQUE NOT NULL, "
                + COL_HE + " TEXT NOT NULL, "
                + COL_LEVEL + " TEXT NOT NULL, "
                + COL_CAT + " TEXT NOT NULL, "
                + COL_IS_CUSTOM + " INTEGER DEFAULT 0, "
                + COL_IS_REVIEW + " INTEGER DEFAULT 0, "
                + COL_CORRECT_STREAK + " INTEGER DEFAULT 0);");

        db.execSQL("CREATE TABLE " + TABLE_STATS + " ("
                + COL_KEY + " TEXT PRIMARY KEY, "
                + COL_VAL + " TEXT);");

        // Insert Default Vocabulary
        insertSeedWords(db);

        // Initialize Stats
        initStat(db, "points", "0");
        initStat(db, "current_streak", "0");
        initStat(db, "max_streak", "0");
        initStat(db, "last_date", "");
        initStat(db, "total_practiced", "0");
        initStat(db, "review_cleared", "0");
    }

    private void initStat(SQLiteDatabase db, String key, String val) {
        ContentValues cv = new ContentValues();
        cv.put(COL_KEY, key);
        cv.put(COL_VAL, val);
        db.insert(TABLE_STATS, null, cv);
    }

    private void insertWord(SQLiteDatabase db, String en, String he, String level, String cat) {
        ContentValues cv = new ContentValues();
        cv.put(COL_EN, en);
        cv.put(COL_HE, he);
        cv.put(COL_LEVEL, level);
        cv.put(COL_CAT, cat);
        cv.put(COL_IS_CUSTOM, 0);
        cv.put(COL_IS_REVIEW, 0);
        cv.put(COL_CORRECT_STREAK, 0);
        db.insertWithOnConflict(TABLE_WORDS, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
    }

    private void insertSeedWords(SQLiteDatabase db) {
        // Beginner - Daily / Basics
        insertWord(db, "Hello", "שלום, היי", "מתחילים", "בסיסי וחיוני");
        insertWord(db, "Water", "מים", "מתחילים", "בסיסי וחיוני");
        insertWord(db, "Food", "אוכל, מזון", "מתחילים", "אוכל ומסעדות");
        insertWord(db, "House", "בית", "מתחילים", "חיי יום-יום");
        insertWord(db, "Family", "משפחה", "מתחילים", "חיי יום-יום");
        insertWord(db, "Friend", "חבר, ידיד", "מתחילים", "חיי יום-יום");
        insertWord(db, "Book", "ספר", "מתחילים", "בסיסי וחיוני");
        insertWord(db, "To work", "לעבוד, עבודה", "מתחילים", "עסקים ועבודה");
        insertWord(db, "To sleep", "לישון", "מתחילים", "חיי יום-יום");
        insertWord(db, "Morning", "בוקר", "מתחילים", "בסיסי וחיוני");
        insertWord(db, "Night", "לילה", "מתחילים", "בסיסי וחיוני");
        insertWord(db, "Money", "כסף", "מתחילים", "עסקים ועבודה");
        insertWord(db, "Car", "מכונית, רכב, אוטו", "מתחילים", "טיולים ומלונאות");
        insertWord(db, "Time", "זמן, שעה", "מתחילים", "בסיסי וחיוני");
        insertWord(db, "To run", "לרוץ, לנהל, ריצה", "מתחילים", "חיי יום-יום");

        // Intermediate - Travel, Food, Work, Tech
        insertWord(db, "Airport", "שדה תעופה, נמל תעופה", "בינוני", "טיולים ומלונאות");
        insertWord(db, "Luggage", "מטען, מזוודות, כבודה", "בינוני", "טיולים ומלונאות");
        insertWord(db, "Reservation", "הזמנה, שמירת מקום", "בינוני", "טיולים ומלונאות");
        insertWord(db, "Delicious", "טעים, ערב לחך", "בינוני", "אוכל ומסעדות");
        insertWord(db, "Recipe", "מתכון", "בינוני", "אוכל ומסעדות");
        insertWord(db, "Interview", "ראיון, לראיין", "בינוני", "עסקים ועבודה");
        insertWord(db, "Colleague", "עמית, קולגה, חבר לעבודה", "בינוני", "עסקים ועבודה");
        insertWord(db, "Deadline", "תאריך יעד, מועד אחרון", "בינוני", "עסקים ועבודה");
        insertWord(db, "Software", "תוכנה", "בינוני", "טכנולוגיה ומחשבים");
        insertWord(db, "Device", "מכשיר, התקן", "בינוני", "טכנולוגיה ומחשבים");
        insertWord(db, "Opportunity", "הזדמנות", "בינוני", "עסקים ועבודה");
        insertWord(db, "Experience", "ניסיון, חוויה", "בינוני", "חיי יום-יום");
        insertWord(db, "Decision", "החלטה", "בינוני", "חיי יום-יום");
        insertWord(db, "Goal", "מטרה, יעד, שער", "בינוני", "בסיסי וחיוני");
        insertWord(db, "Improvement", "שיפור, התקדמות", "בינוני", "בסיסי וחיוני");

        // Advanced - Business, Tech, Nuanced
        insertWord(db, "Breakthrough", "פריצת דרך", "מתקדמים", "טכנולוגיה ומחשבים");
        insertWord(db, "Negotiation", "משא ומתן, מו\"מ", "מתקדמים", "עסקים ועבודה");
        insertWord(db, "Entrepreneur", "יזם", "מתקדמים", "עסקים ועבודה");
        insertWord(db, "Implementation", "יישום, הטמעה", "מתקדמים", "טכנולוגיה ומחשבים");
        insertWord(db, "Sustainable", "בר קיימא, מקיים", "מתקדמים", "בסיסי וחיוני");
        insertWord(db, "Resilience", "חוסן, כושר התאוששות", "מתקדמים", "חיי יום-יום");
        insertWord(db, "Ambiguity", "עמימות, דו-משמעות", "מתקדמים", "חיי יום-יום");
        insertWord(db, "Vulnerable", "פגיע, רגיש", "מתקדמים", "חיי יום-יום");
        insertWord(db, "Perspective", "נקודת מבט, פרספקטיבה", "מתקדמים", "חיי יום-יום");
        insertWord(db, "Compromise", "פשרה, להתפשר", "מתקדמים", "עסקים ועבודה");
        insertWord(db, "Acquisition", "רכישה, השגה", "מתקדמים", "עסקים ועבודה");
        insertWord(db, "Artificial", "מלאכותי", "מתקדמים", "טכנולוגיה ומחשבים");
        insertWord(db, "Destination", "יעד, מחוז חפץ", "מתקדמים", "טיולים ומלונאות");
        insertWord(db, "Exquisite", "מעודן, יוצא מן הכלל, משובח", "מתקדמים", "אוכל ומסעדות");
        insertWord(db, "Fluency", "שטף, רהיטות", "מתקדמים", "בסיסי וחיוני");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STATS);
        onCreate(db);
    }

    // Methods for Word Operations
    public long addCustomWord(String en, String he, String level, String cat) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_EN, en.trim());
        cv.put(COL_HE, he.trim());
        cv.put(COL_LEVEL, (level == null || level.isEmpty()) ? "מותאם אישית" : level);
        cv.put(COL_CAT, (cat == null || cat.isEmpty()) ? "מותאם אישית" : cat);
        cv.put(COL_IS_CUSTOM, 1);
        cv.put(COL_IS_REVIEW, 0);
        cv.put(COL_CORRECT_STREAK, 0);
        return db.insertWithOnConflict(TABLE_WORDS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public List<Word> getWordsForPractice(String level, String category, int limit) {
        List<Word> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        StringBuilder query = new StringBuilder("SELECT * FROM " + TABLE_WORDS + " WHERE 1=1 ");
        List<String> args = new ArrayList<>();

        if (level != null && !level.equals("הכל") && !level.contains("כל הרמות")) {
            query.append(" AND ").append(COL_LEVEL).append(" = ? ");
            args.add(level);
        }
        if (category != null && !category.equals("הכל") && !category.contains("כל הנושאים")) {
            query.append(" AND ").append(COL_CAT).append(" = ? ");
            args.add(category);
        }

        query.append(" ORDER BY RANDOM() LIMIT ?");
        args.add(String.valueOf(limit));

        Cursor cursor = db.rawQuery(query.toString(), args.toArray(new String[0]));
        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToWord(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public List<Word> getReviewWords() {
        List<Word> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_WORDS + " WHERE " + COL_IS_REVIEW + " = 1 ORDER BY " + COL_ID + " DESC", null);
        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToWord(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public List<Word> getCustomWords() {
        List<Word> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_WORDS + " WHERE " + COL_IS_CUSTOM + " = 1 ORDER BY " + COL_ID + " DESC", null);
        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToWord(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public int getReviewCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_WORDS + " WHERE " + COL_IS_REVIEW + " = 1", null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public void setWordReviewStatus(long wordId, boolean isReview) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_IS_REVIEW, isReview ? 1 : 0);
        if (isReview) {
            cv.put(COL_CORRECT_STREAK, 0);
        }
        db.update(TABLE_WORDS, cv, COL_ID + " = ?", new String[]{String.valueOf(wordId)});
    }

    public void incrementWordStreak(long wordId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_WORDS + " SET " + COL_CORRECT_STREAK + " = " + COL_CORRECT_STREAK + " + 1 WHERE " + COL_ID + " = " + wordId);
    }

    private Word cursorToWord(Cursor c) {
        return new Word(
                c.getLong(c.getColumnIndexOrThrow(COL_ID)),
                c.getString(c.getColumnIndexOrThrow(COL_EN)),
                c.getString(c.getColumnIndexOrThrow(COL_HE)),
                c.getString(c.getColumnIndexOrThrow(COL_LEVEL)),
                c.getString(c.getColumnIndexOrThrow(COL_CAT)),
                c.getInt(c.getColumnIndexOrThrow(COL_IS_CUSTOM)),
                c.getInt(c.getColumnIndexOrThrow(COL_IS_REVIEW)),
                c.getInt(c.getColumnIndexOrThrow(COL_CORRECT_STREAK))
        );
    }

    // Stats Management
    public String getStat(String key, String def) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL_VAL + " FROM " + TABLE_STATS + " WHERE " + COL_KEY + " = ?", new String[]{key});
        String res = def;
        if (cursor.moveToFirst()) {
            res = cursor.getString(0);
        }
        cursor.close();
        return res;
    }

    public void setStat(String key, String val) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_KEY, key);
        cv.put(COL_VAL, val);
        db.insertWithOnConflict(TABLE_STATS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public int getIntStat(String key, int def) {
        try {
            return Integer.parseInt(getStat(key, String.valueOf(def)));
        } catch (Exception e) {
            return def;
        }
    }

    public void addIntStat(String key, int delta) {
        int cur = getIntStat(key, 0);
        setStat(key, String.valueOf(cur + delta));
    }
}
