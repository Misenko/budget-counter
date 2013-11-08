package kimle.michal.android.activity;

import android.app.Activity;
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
import kimle.michal.android.contentprovider.BudgetContentProvider;
import kimle.michal.android.db.BudgetDbContract;
import kimle.michal.android.view.FigureInputView;

public class MainActivity extends Activity {

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
        weekRemaining = (TextView) findViewById(R.id.textview_week_remaining);

        loadRemainingValue(BudgetDbContract.getCurrentWeek(this));
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

            remainingValue = cursor.getDouble(cursor.getColumnIndexOrThrow(BudgetDbContract.BudgetDbEntry.WEEK_OVERALL_COLUMN));
            updateRemainingValue();
        }
    }

    private void updateRemainingValue() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        DecimalFormat format = new DecimalFormat(pref.getString(getResources().getString(R.string.currency_key), ""));
        weekRemaining.setText(format.format(remainingValue));
    }

    @Override
    public void onResume() {
        super.onResume();

        loadRemainingValue(BudgetDbContract.getCurrentWeek(this));
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}
