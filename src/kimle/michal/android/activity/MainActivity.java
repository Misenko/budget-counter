package kimle.michal.android.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import kimle.michal.android.contentprovider.BudgetContentProvider;
import kimle.michal.android.db.BudgetDbContract;
import kimle.michal.android.view.FigureInputView;

public class MainActivity extends Activity implements DialogInterface.OnClickListener {

    private static final String LOG = "MainActivity";
    private static final String STATE_CUT = "stateCut";
    private FigureInputView fiv;
    private TextView weekRemaining;
    private double remainingValue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, true);

        fiv = (FigureInputView) findViewById(R.id.figure_input);
        weekRemaining = (TextView) findViewById(R.id.textview_current_week_remaining);

        updateRemainingValue(BudgetDbContract.getCurrentWeek(this));
    }

    private void updateRemainingValue(int weekId) {
        Uri uri = Uri.parse(BudgetContentProvider.WEEKS_URI + "/" + weekId);
        String[] projection = {
            BudgetDbContract.BudgetDbEntry.WEEK_OVERALL_COLUMN
        };
        uri = uri.buildUpon().appendQueryParameter(BudgetDbContract.BudgetDbEntry.GROUP_BY,
                BudgetDbContract.BudgetDbEntry.WEEK_ID_COLUMN).build();

        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();

            remainingValue = cursor.getDouble(cursor.getColumnIndexOrThrow(BudgetDbContract.BudgetDbEntry.WEEK_OVERALL_COLUMN));
            cursor.close();
            formatRemainingValue();
        }
    }

    private void formatRemainingValue() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        DecimalFormat format = new DecimalFormat(pref.getString(getResources().getString(R.string.currency_key), ""));
        weekRemaining.setText(format.format(remainingValue));
    }

    public void addCut(View v) {
        double cutValue = fiv.getValue();
        if (cutValue == 0) {
            return;
        }

        int weekId = BudgetDbContract.getCurrentWeek(this);
        SimpleDateFormat datetimeFormat = new SimpleDateFormat(getResources().getString(R.string.datetime_format));
        Date timestamp = new Date(System.currentTimeMillis());

        ContentValues values = new ContentValues();
        values.put(BudgetDbContract.BudgetDbEntry.CUT_TIMESTAMP_COLUMN, datetimeFormat.format(timestamp));
        values.put(BudgetDbContract.BudgetDbEntry.CUT_VALUE_COLUMN, cutValue);
        values.put(BudgetDbContract.BudgetDbEntry.CUT_WEEK_ID_COLUMN, weekId);

        getContentResolver().insert(BudgetContentProvider.CUTS_URI, values);

        updateRemainingValue(weekId);
        fiv.reset();
    }

    @Override
    public void onResume() {
        super.onResume();

        updateRemainingValue(BudgetDbContract.getCurrentWeek(this));
        fiv.updateSettings();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putDouble(STATE_CUT, fiv.getValue());

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        fiv.setValue(savedInstanceState.getDouble(STATE_CUT));
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
            case R.id.weeks_overview:
                showWeekOverview();
                return true;
            case R.id.remove_data:
                showRemoveDataDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showWeekOverview() {
        Intent intent = new Intent(this, WeeksOverviewActivity.class);
        startActivity(intent);
    }

    private void showSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void showRemoveDataDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.rdd_title);
        builder.setMessage(R.string.rdd_message);
        builder.setPositiveButton(android.R.string.ok, this);
        builder.setNegativeButton(android.R.string.cancel, this);

        Dialog dialog = builder.create();
        dialog.show();
    }

    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                getContentResolver().delete(BudgetContentProvider.WEEKS_URI, null, null);
                updateRemainingValue(BudgetDbContract.getCurrentWeek(this));
                break;
            case DialogInterface.BUTTON_NEGATIVE:
            default:
                break;
        }
    }
}
