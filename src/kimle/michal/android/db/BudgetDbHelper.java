package kimle.michal.android.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BudgetDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 5;
    public static final String DATABASE = "budgetcounter.db";
    //week table
    private static final String WEEK_TABLE_CREATE
            = "CREATE TABLE " + BudgetDbContract.BudgetDbEntry.WEEK_TABLE + " ("
            + BudgetDbContract.BudgetDbEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + BudgetDbContract.BudgetDbEntry.WEEK_AMOUNT_COLUMN + " REAL NOT NULL, "
            + BudgetDbContract.BudgetDbEntry.WEEK_START_COLUMN + " DATE NOT NULL, "
            + BudgetDbContract.BudgetDbEntry.WEEK_END_COLUMN + " DATE NOT NULL);";
    private static final String WEEK_TABLE_DROP
            = "DROP TABLE IF EXISTS " + BudgetDbContract.BudgetDbEntry.WEEK_TABLE;
    //cut table
    private static final String CUT_TABLE_CREATE
            = "CREATE TABLE " + BudgetDbContract.BudgetDbEntry.CUT_TABLE + " ("
            + BudgetDbContract.BudgetDbEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + BudgetDbContract.BudgetDbEntry.CUT_VALUE_COLUMN + " REAL NOT NULL, "
            + BudgetDbContract.BudgetDbEntry.CUT_TIMESTAMP_COLUMN + " DATETIME NOT NULL, "
            + BudgetDbContract.BudgetDbEntry.CUT_WEEK_ID_COLUMN + " INTEGER NOT NULL REFERENCES "
            + BudgetDbContract.BudgetDbEntry.WEEK_TABLE + "("
            + BudgetDbContract.BudgetDbEntry._ID + ") ON DELETE CASCADE);";
    private static final String CUT_TABLE_DROP
            = "DROP TABLE IF EXISTS " + BudgetDbContract.BudgetDbEntry.CUT_TABLE;

    public BudgetDbHelper(Context context) {
        super(context, DATABASE, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(WEEK_TABLE_CREATE);
        db.execSQL(CUT_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(CUT_TABLE_DROP);
        db.execSQL(WEEK_TABLE_DROP);
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }
}
