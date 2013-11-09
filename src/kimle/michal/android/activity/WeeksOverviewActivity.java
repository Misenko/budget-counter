package kimle.michal.android.activity;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import java.text.DecimalFormat;
import kimle.michal.android.contentprovider.BudgetContentProvider;
import kimle.michal.android.db.BudgetDbContract;

public class WeeksOverviewActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG = "WeeksOverviewActivity";
    private SimpleCursorAdapter adapter;
    private DecimalFormat format;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.weeks_overview);
        loadFormat();
        setupAdapter();
        loadWeeksTotalOverview();

        registerForContextMenu(findViewById(android.R.id.list));
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.weeks_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.weeks_delete:
                deleteWeek(info.id);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void deleteWeek(long weekId) {
        if (weekId == BudgetDbContract.getCurrentWeek(this)) {
            String selection = BudgetDbContract.BudgetDbEntry.CUT_WEEK_ID_COLUMN + " = ?";
            String[] arguments = {
                String.valueOf(weekId)
            };
            getContentResolver().delete(BudgetContentProvider.CUTS_URI, selection, arguments);
        } else {
            Uri uri = Uri.parse(BudgetContentProvider.WEEKS_URI + "/" + weekId);
            getContentResolver().delete(uri, null, null);
        }

        updateOverview();
    }

    private void updateOverview() {
        getLoaderManager().restartLoader(0, null, this);
        adapter.notifyDataSetChanged();

        loadWeeksTotalOverview();
    }

    private void loadFormat() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        format = new DecimalFormat(pref.getString(getResources().getString(R.string.currency_key), ""));
    }

    private void setupAdapter() {
        String[] columns = new String[]{
            BudgetDbContract.BudgetDbEntry.WEEK_OVERALL_COLUMN,
            BudgetDbContract.BudgetDbEntry.WEEK_START_COLUMN,
            BudgetDbContract.BudgetDbEntry.WEEK_END_COLUMN};

        int[] to = new int[]{
            R.id.textview_week_remaining,
            R.id.textview_week_start,
            R.id.textview_week_end};

        getLoaderManager().initLoader(0, null, this);

        adapter = new SimpleCursorAdapter(
                this, R.layout.week,
                null,
                columns,
                to,
                0);

        adapter.setViewBinder(new ViewBinder() {

            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {

                if (columnIndex == cursor.getColumnIndexOrThrow(BudgetDbContract.BudgetDbEntry.WEEK_OVERALL_COLUMN)) {
                    double value = cursor.getDouble(cursor.getColumnIndexOrThrow(BudgetDbContract.BudgetDbEntry.WEEK_OVERALL_COLUMN));
                    TextView textView = (TextView) view;
                    textView.setText(format.format(value));
                    return true;
                }

                return false;
            }
        });

        setListAdapter(adapter);
    }

    private void loadWeeksTotalOverview() {
        String[] projection = {
            BudgetDbContract.BudgetDbEntry.WEEK_TOTAL_OVERALL_COLUMN
        };
        Cursor cursor = getContentResolver().query(BudgetContentProvider.TOTAL_URI, projection, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();

            TextView textView = (TextView) findViewById(R.id.textview_total_remaining_content);
            textView.setText(format.format(cursor.getDouble(cursor.getColumnIndexOrThrow(BudgetDbContract.BudgetDbEntry.WEEK_TOTAL_OVERALL_COLUMN))));
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent i = new Intent(this, CutsOverviewActivity.class);
        Uri weekUri = Uri.parse(BudgetContentProvider.WEEKS_URI + "/" + id);
        i.putExtra(BudgetContentProvider.WEEK_CONTENT_ITEM_TYPE, weekUri);

        startActivity(i);
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
            BudgetDbContract.BudgetDbEntry.WEEK_ID_COLUMN,
            BudgetDbContract.BudgetDbEntry.WEEK_OVERALL_COLUMN,
            BudgetDbContract.BudgetDbEntry.WEEK_START_COLUMN,
            BudgetDbContract.BudgetDbEntry.WEEK_END_COLUMN};

        Uri uri = BudgetContentProvider.WEEKS_URI;
        uri = uri.buildUpon().appendQueryParameter(BudgetDbContract.BudgetDbEntry.GROUP_BY,
                BudgetDbContract.BudgetDbEntry.WEEK_ID_COLUMN).build();

        CursorLoader cursorLoader = new CursorLoader(this, uri, projection, null, null, BudgetDbContract.BudgetDbEntry.WEEK_START_COLUMN + " DESC");
        return cursorLoader;
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}
