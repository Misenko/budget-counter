package kimle.michal.android.activity;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import java.sql.Date;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import kimle.michal.android.contentprovider.BudgetContentProvider;
import kimle.michal.android.db.BudgetDbContract;
import kimle.michal.android.preference.PreferenceHelper;

public class CutsOverviewActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG = "CutsOverviewActivity";
    private SimpleCursorAdapter adapter;
    private DecimalFormat format;
    private long weekId;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        Bundle extras = getIntent().getExtras();
        weekId = (icicle == null) ? 0 : icicle.getLong(WeeksOverviewActivity.WEEK_ID);
        if (extras != null) {
            weekId = extras.getLong(WeeksOverviewActivity.WEEK_ID);
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.cuts_overview);
        format = PreferenceHelper.loadFormat(this);
        setupAdapter();
        loadCutsOverview();

        registerForContextMenu(findViewById(android.R.id.list));
    }

    @Override
    public void onResume() {
        super.onResume();

        updateOverview();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.cuts_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.cuts_delete:
                deleteCut(info.id);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void deleteCut(long cutId) {
        Uri uri = Uri.parse(BudgetContentProvider.CUTS_URI + "/" + cutId);
        getContentResolver().delete(uri, null, null);

        updateOverview();
    }

    private void updateOverview() {
        getLoaderManager().restartLoader(0, null, this);
        adapter.notifyDataSetChanged();

        loadCutsOverview();
    }

    private void loadCutsOverview() {
        String[] projection = {
            BudgetDbContract.BudgetDbEntry.WEEK_AMOUNT_COLUMN,
            BudgetDbContract.BudgetDbEntry.WEEK_START_COLUMN,
            BudgetDbContract.BudgetDbEntry.WEEK_END_COLUMN,
            BudgetDbContract.BudgetDbEntry.WEEK_OVERALL_COLUMN
        };

        Uri uri = Uri.parse(BudgetContentProvider.WEEKS_URI + "/" + weekId);
        uri = uri.buildUpon().appendQueryParameter(BudgetDbContract.BudgetDbEntry.GROUP_BY,
                BudgetDbContract.BudgetDbEntry.WEEK_ID_COLUMN).build();

        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();

            TextView textView = (TextView) findViewById(R.id.textview_budget_content);
            textView.setText(format.format(cursor.getDouble(cursor.getColumnIndexOrThrow(BudgetDbContract.BudgetDbEntry.WEEK_AMOUNT_COLUMN))));
            textView = (TextView) findViewById(R.id.textview_dates_content);
            Date from = Date.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(BudgetDbContract.BudgetDbEntry.WEEK_START_COLUMN)));
            Date to = Date.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(BudgetDbContract.BudgetDbEntry.WEEK_END_COLUMN)));
            DateFormat df = new SimpleDateFormat(getResources().getString(R.string.date_format));
            textView.setText(df.format(from) + " - " + df.format(to));
            textView = (TextView) findViewById(R.id.textview_week_total_content);
            textView.setText(format.format(cursor.getDouble(cursor.getColumnIndexOrThrow(BudgetDbContract.BudgetDbEntry.WEEK_OVERALL_COLUMN))));

            cursor.close();
        }
    }

    private void setupAdapter() {
        String[] columns = new String[]{
            BudgetDbContract.BudgetDbEntry.CUT_VALUE_COLUMN,
            BudgetDbContract.BudgetDbEntry.CUT_DATE_COLUMN,
            BudgetDbContract.BudgetDbEntry.CUT_TIME_COLUMN};

        int[] to = new int[]{
            R.id.textview_cut_value,
            R.id.textview_cut_date,
            R.id.textview_cut_time};

        getLoaderManager().initLoader(0, null, this);

        adapter = new SimpleCursorAdapter(
                this, R.layout.cut,
                null,
                columns,
                to,
                0);

        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {

                if (columnIndex == cursor.getColumnIndexOrThrow(BudgetDbContract.BudgetDbEntry.CUT_VALUE_COLUMN)) {
                    double value = cursor.getDouble(cursor.getColumnIndexOrThrow(BudgetDbContract.BudgetDbEntry.CUT_VALUE_COLUMN));
                    TextView textView = (TextView) view;
                    textView.setText(format.format(value));
                    return true;
                }

                return false;
            }
        });

        setListAdapter(adapter);
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
            BudgetDbContract.BudgetDbEntry.CUT_ID_COLUMN,
            BudgetDbContract.BudgetDbEntry.CUT_VALUE_COLUMN,
            BudgetDbContract.BudgetDbEntry.CUT_DATE_COLUMN,
            BudgetDbContract.BudgetDbEntry.CUT_TIME_COLUMN};

        Uri uri = BudgetContentProvider.CUTS_URI;
        String selection = BudgetDbContract.BudgetDbEntry.CUT_WEEK_ID_COLUMN + " = ?";
        String[] arguments = {
            String.valueOf(weekId)
        };

        String order = BudgetDbContract.BudgetDbEntry.CUT_TIMESTAMP_COLUMN + " DESC";

        CursorLoader cursorLoader = new CursorLoader(this, uri, projection, selection, arguments, order);
        return cursorLoader;
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}
