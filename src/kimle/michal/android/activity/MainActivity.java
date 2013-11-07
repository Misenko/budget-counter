package kimle.michal.android.activity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import kimle.michal.android.contentprovider.BudgetContentProvider;
import kimle.michal.android.db.BudgetDbContract;
import kimle.michal.android.view.FigureInputView;

public class MainActivity extends Activity {

    private static final String LOG = "MainActivity";
    private static final String STATE_CUT = "stateCut";
    private static final int DAYS_OF_WEEK = 7;
    private FigureInputView fiv;
    private TextView weekRemaining;
    private float remainingValue;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, true);

        fiv = (FigureInputView) findViewById(R.id.figure_input);
        weekRemaining = (TextView) findViewById(R.id.textview_week_remaining);

        Integer currentWeekId = getThisWeek();
        if (currentWeekId == null) {
            addCurrentWeek();
            currentWeekId = getThisWeek();
        }

        loadRemainingValue(currentWeekId);
    }

    private void loadRemainingValue(int weekId) {
        Uri uri = Uri.parse(BudgetContentProvider.WEEKS_URI + "/" + weekId);
        String[] projection = {
            BudgetDbContract.BudgetDbEntry.WEEK_OVERALL_COLUMN
        };
        uri = uri.buildUpon().appendQueryParameter(BudgetDbContract.BudgetDbEntry.GROUP_BY,
                BudgetDbContract.BudgetDbEntry.WEEK_ID_COLUMN).build();

        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();

            remainingValue = cursor.getFloat(cursor.getColumnIndexOrThrow(BudgetDbContract.BudgetDbEntry.WEEK_OVERALL_COLUMN));
            updateRemainingValue();
        }
    }

    private Integer getThisWeek() {
        Calendar cal = new GregorianCalendar();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        int weekStartDay = Integer.parseInt(pref.getString(getResources().getString(R.string.week_start_key), ""));
        cal.set(Calendar.DAY_OF_WEEK, weekStartDay);

        Uri uri = Uri.parse(BudgetContentProvider.WEEKS_URI + "/" + dateFormat.format(cal.getTime()));
        uri = uri.buildUpon().appendQueryParameter(BudgetDbContract.BudgetDbEntry.GROUP_BY,
                BudgetDbContract.BudgetDbEntry.WEEK_ID_COLUMN).build();
        String[] projection = {
            BudgetDbContract.BudgetDbEntry.WEEK_ID_COLUMN
        };

        String selection = BudgetDbContract.BudgetDbEntry.WEEK_START_COLUMN + " = ?";
        String selectionArgs[] = {
            dateFormat.format(cal.getTime())
        };

        Cursor cursor = getContentResolver().query(uri, projection, selection, selectionArgs, null);

        if (cursor == null || cursor.getCount() == 0) {
            return null;
        }

        cursor.moveToFirst();
        return cursor.getInt(cursor.getColumnIndexOrThrow(BudgetDbContract.BudgetDbEntry._ID));
    }

    private void addCurrentWeek() {
        ContentValues values = new ContentValues();
        Calendar cal = new GregorianCalendar();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        int weekStartDay = Integer.parseInt(pref.getString(getResources().getString(R.string.week_start_key), ""));

        cal.set(Calendar.DAY_OF_WEEK, weekStartDay);
        Date weekStart = cal.getTime();
        cal.add(Calendar.DAY_OF_WEEK, DAYS_OF_WEEK);
        Date weekEnd = cal.getTime();

        values.put(BudgetDbContract.BudgetDbEntry.WEEK_AMOUNT_COLUMN, pref.getFloat(getResources().getString(R.string.budget_key), 0));
        values.put(BudgetDbContract.BudgetDbEntry.WEEK_START_COLUMN, dateFormat.format(weekStart));
        values.put(BudgetDbContract.BudgetDbEntry.WEEK_END_COLUMN, dateFormat.format(weekEnd));

        getContentResolver().insert(BudgetContentProvider.WEEKS_URI, values);
    }

    private void updateRemainingValue() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        DecimalFormat format = new DecimalFormat(pref.getString(getResources().getString(R.string.currency_key), ""));
        weekRemaining.setText(format.format(remainingValue));
    }

    @Override
    public void onResume() {
        super.onResume();

        updateRemainingValue();
        fiv.updateSettings();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putFloat(STATE_CUT, fiv.getValue());

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        fiv.setValue(savedInstanceState.getFloat(STATE_CUT));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                showSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}
