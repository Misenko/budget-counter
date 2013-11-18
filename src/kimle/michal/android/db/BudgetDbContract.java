package kimle.michal.android.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import kimle.michal.android.activity.R;
import kimle.michal.android.contentprovider.BudgetContentProvider;

public class BudgetDbContract {

    private BudgetDbContract() {
    }

    private static final int DAYS_OFFSET = 6;

    public static abstract class BudgetDbEntry implements BaseColumns {

        public static final String WEEK_TABLE = "week";
        public static final String CUT_TABLE = "cut";
        //cut table
        public static final String CUT_ID_COLUMN = CUT_TABLE + "." + _ID;
        public static final String CUT_VALUE_COLUMN = "value";
        public static final String CUT_TIMESTAMP_COLUMN = "timestamp";
        public static final String CUT_TIME_COLUMN = "time(timestamp)";
        public static final String CUT_DATE_COLUMN = "date(timestamp)";
        public static final String CUT_WEEK_ID_COLUMN = "week_id";
        //week table
        public static final String WEEK_ID_COLUMN = WEEK_TABLE + "." + _ID;
        public static final String WEEK_AMOUNT_COLUMN = "amount";
        public static final String WEEK_START_COLUMN = "start";
        public static final String WEEK_END_COLUMN = "end";
        public static final String WEEK_OVERALL_COLUMN = WEEK_AMOUNT_COLUMN + "-coalesce(sum(" + CUT_VALUE_COLUMN + "),0)";
        public static final String WEEK_TOTAL_OVERALL_COLUMN = "overall_total";
        public static final String LIMIT = "limit";
        public static final String GROUP_BY = "group by";
    }

    public static Integer getCurrentWeek(Context context) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(context.getResources().getString(R.string.db_date_format));
        Calendar cal = new GregorianCalendar();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        Uri uri = Uri.parse(BudgetContentProvider.WEEKS_URI + "/" + dateFormat.format(cal.getTime()));
        uri = uri.buildUpon().appendQueryParameter(BudgetDbContract.BudgetDbEntry.GROUP_BY,
                BudgetDbContract.BudgetDbEntry.WEEK_ID_COLUMN).build();
        String[] projection = {
            BudgetDbContract.BudgetDbEntry.WEEK_ID_COLUMN
        };

        String selection = "? between " + BudgetDbContract.BudgetDbEntry.WEEK_START_COLUMN
                + " and " + BudgetDbContract.BudgetDbEntry.WEEK_END_COLUMN;
        String selectionArgs[] = {
            dateFormat.format(cal.getTime())
        };

        Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);

        if (cursor == null || cursor.getCount() == 0) {
            addCurrentWeek(context);
            if (cursor != null) {
                cursor.close();
            }
            return getCurrentWeek(context);
        }

        cursor.moveToFirst();
        int weekId = cursor.getInt(cursor.getColumnIndexOrThrow(BudgetDbContract.BudgetDbEntry._ID));
        cursor.close();
        return weekId;
    }

    public static void addCurrentWeek(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        ContentValues values = getCurrentWeekDates(context);
        values.put(BudgetDbContract.BudgetDbEntry.WEEK_AMOUNT_COLUMN, roundTwoDecimals(pref.getFloat(context.getResources().getString(R.string.budget_key), 0)));

        context.getContentResolver().insert(BudgetContentProvider.WEEKS_URI, values);
    }

    public static ContentValues getCurrentWeekDates(Context context) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(context.getResources().getString(R.string.db_date_format));
        ContentValues values = new ContentValues();
        Calendar cal = new GregorianCalendar();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        int weekStartDay = Integer.parseInt(pref.getString(context.getResources().getString(R.string.week_start_key), ""));

        cal.set(Calendar.DAY_OF_WEEK, weekStartDay);
        Date weekStart = cal.getTime();
        cal.add(Calendar.DAY_OF_WEEK, DAYS_OFFSET);
        Date weekEnd = cal.getTime();

        values.put(BudgetDbContract.BudgetDbEntry.WEEK_START_COLUMN, dateFormat.format(weekStart));
        values.put(BudgetDbContract.BudgetDbEntry.WEEK_END_COLUMN, dateFormat.format(weekEnd));

        return values;
    }

    public static double roundTwoDecimals(double d) {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        return Double.valueOf(twoDForm.format(d));
    }

}
