package kimle.michal.android.contentprovider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import java.util.Arrays;
import java.util.HashSet;
import kimle.michal.android.db.BudgetDbContract;
import kimle.michal.android.db.BudgetDbHelper;

public class BudgetContentProvider extends ContentProvider {

    private static final String LOG = "BudgetContentProvider";
    private BudgetDbHelper dbHelper;
    private SQLiteDatabase db;
    private static final String AUTHORITY = "kimle.michal.android.contentprovider";
    private static final int WEEKS = 10;
    private static final int WEEKS_ID = 11;
    private static final int WEEKS_DATE = 15;
    private static final int TOTAL = 12;
    private static final int CUTS = 13;
    private static final int CUTS_ID = 14;
    private static final String WEEKS_PATH = "weeks";
    private static final String TOTAL_PATH = "total";
    private static final String CUTS_PATH = "cuts";
    public static final Uri WEEKS_URI = Uri.parse("content://" + AUTHORITY + "/" + WEEKS_PATH);
    public static final Uri TOTAL_URI = Uri.parse("content://" + AUTHORITY + "/" + TOTAL_PATH);
    public static final Uri CUTS_URI = Uri.parse("content://" + AUTHORITY + "/" + CUTS_PATH);
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    public static final String WEEK_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/weeks";
    public static final String WEEK_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/week";
    private static final String VND = "vnd.kimle.michal.android.contentprovider";

    static {
        sURIMatcher.addURI(AUTHORITY, WEEKS_PATH, WEEKS);
        sURIMatcher.addURI(AUTHORITY, WEEKS_PATH + "/#", WEEKS_ID);
        sURIMatcher.addURI(AUTHORITY, WEEKS_PATH + "/*", WEEKS_DATE);
        sURIMatcher.addURI(AUTHORITY, CUTS_PATH, CUTS);
        sURIMatcher.addURI(AUTHORITY, CUTS_PATH + "/#", CUTS_ID);
        sURIMatcher.addURI(AUTHORITY, TOTAL_PATH, TOTAL);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new BudgetDbHelper(getContext());

        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        int uriType = sURIMatcher.match(uri);
        db = dbHelper.getReadableDatabase();
        Cursor cursor;
        checkColumns(projection);

        if (uriType == TOTAL) {
            cursor = db.rawQuery("select "
                    + "sum(week_total) as 'overall_total' "
                    + "from ("
                    + "select week._id, amount-coalesce(sum(value),0) as week_total "
                    + "from week left outer join cut "
                    + "on week._id = cut.week_id "
                    + "group by week._id);", null);
        } else {
            SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
            switch (uriType) {
                case WEEKS_ID:
                    queryBuilder.appendWhere(BudgetDbContract.BudgetDbEntry.WEEK_ID_COLUMN + "="
                            + uri.getLastPathSegment());
                case WEEKS_DATE:
                case WEEKS:
                    queryBuilder.setTables(BudgetDbContract.BudgetDbEntry.WEEK_TABLE
                            + " left outer join " + BudgetDbContract.BudgetDbEntry.CUT_TABLE
                            + " on " + BudgetDbContract.BudgetDbEntry.WEEK_ID_COLUMN
                            + " = " + BudgetDbContract.BudgetDbEntry.CUT_WEEK_ID_COLUMN);
                    break;
                case CUTS_ID:
                    queryBuilder.appendWhere(BudgetDbContract.BudgetDbEntry.CUT_ID_COLUMN + "="
                            + uri.getLastPathSegment());
                case CUTS:
                    break;
                default:
                    throw new IllegalArgumentException("Unknown URI: " + uri);
            }

            String limit = uri.getQueryParameter(BudgetDbContract.BudgetDbEntry.LIMIT);
            String groupBy = uri.getQueryParameter(BudgetDbContract.BudgetDbEntry.GROUP_BY);
            cursor = queryBuilder.query(db, projection, selection, selectionArgs, groupBy, null, sortOrder, limit);
        }

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case WEEKS:
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + VND + "." + WEEKS_PATH;
            case WEEKS_ID:
                return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + VND + "." + WEEKS_PATH;
            case CUTS:
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + VND + "." + CUTS_PATH;
            case CUTS_ID:
                return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + VND + "." + CUTS_PATH;
            case TOTAL:
                return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + VND + "." + TOTAL_PATH;
            default:
                return null;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        db = dbHelper.getWritableDatabase();
        long id;
        switch (uriType) {
            case CUTS:
                id = db.insert(BudgetDbContract.BudgetDbEntry.CUT_TABLE, null, values);
                break;
            case WEEKS:
                id = db.insert(BudgetDbContract.BudgetDbEntry.WEEK_TABLE, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        db = dbHelper.getWritableDatabase();
        int rowsDeleted;
        String tableName;

        if (uriType == WEEKS || uriType == WEEKS_ID) {
            tableName = BudgetDbContract.BudgetDbEntry.WEEK_TABLE;
        } else {
            tableName = BudgetDbContract.BudgetDbEntry.CUT_TABLE;
        }

        switch (uriType) {
            case WEEKS:
            case CUTS:
                rowsDeleted = db.delete(tableName, selection,
                        selectionArgs);
                break;
            case WEEKS_ID:
            case CUTS_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = db.delete(tableName,
                            BudgetDbContract.BudgetDbEntry._ID + "=" + id,
                            null);
                } else {
                    rowsDeleted = db.delete(tableName,
                            BudgetDbContract.BudgetDbEntry._ID + "=" + id
                            + " and " + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        db = dbHelper.getWritableDatabase();
        int rowsUpdated;
        String tableName;

        if (uriType == WEEKS || uriType == WEEKS_ID) {
            tableName = BudgetDbContract.BudgetDbEntry.WEEK_TABLE;
        } else {
            tableName = BudgetDbContract.BudgetDbEntry.CUT_TABLE;
        }

        switch (uriType) {
            case WEEKS:
            case CUTS:
                rowsUpdated = db.update(tableName, values, selection,
                        selectionArgs);
                break;
            case WEEKS_ID:
            case CUTS_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = db.update(tableName, values,
                            BudgetDbContract.BudgetDbEntry._ID + "=" + id,
                            null);
                } else {
                    rowsUpdated = db.update(tableName, values,
                            BudgetDbContract.BudgetDbEntry._ID + "=" + id
                            + " and " + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    private void checkColumns(String[] projection) {
        String[] available = {
            BudgetDbContract.BudgetDbEntry._ID,
            BudgetDbContract.BudgetDbEntry.CUT_ID_COLUMN,
            BudgetDbContract.BudgetDbEntry.WEEK_ID_COLUMN,
            BudgetDbContract.BudgetDbEntry.CUT_TIMESTAMP_COLUMN,
            BudgetDbContract.BudgetDbEntry.CUT_VALUE_COLUMN,
            BudgetDbContract.BudgetDbEntry.WEEK_AMOUNT_COLUMN,
            BudgetDbContract.BudgetDbEntry.WEEK_OVERALL_COLUMN,
            BudgetDbContract.BudgetDbEntry.WEEK_END_COLUMN,
            BudgetDbContract.BudgetDbEntry.WEEK_START_COLUMN,
            BudgetDbContract.BudgetDbEntry.WEEK_TOTAL_OVERALL_COLUMN
        };
        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }
}
