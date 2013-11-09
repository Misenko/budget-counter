package kimle.michal.android.fragment;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import java.text.DecimalFormat;
import kimle.michal.android.activity.R;
import kimle.michal.android.contentprovider.BudgetContentProvider;
import kimle.michal.android.db.BudgetDbContract;
import kimle.michal.android.preference.FigurePickerPreference;
import kimle.michal.android.preference.PreferenceHelper;

public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

    private static final String LOG = "SettingsFragment";
    private DecimalFormat format;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.preferences);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.registerOnSharedPreferenceChangeListener(this);
        format = PreferenceHelper.loadFormat(getActivity());
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getResources().getString(R.string.budget_key))) {
            FigurePickerPreference fpp = (FigurePickerPreference) findPreference(key);
            fpp.setSummary(format.format(sharedPreferences.getFloat(key, 0)));

            ContentValues values = new ContentValues();
            Log.d(LOG, "" + BudgetDbContract.roundTwoDecimals(fpp.getFigure()));
            values.put(BudgetDbContract.BudgetDbEntry.WEEK_AMOUNT_COLUMN, BudgetDbContract.roundTwoDecimals(fpp.getFigure()));
            Uri uri = Uri.parse(BudgetContentProvider.WEEKS_URI + "/" + BudgetDbContract.getCurrentWeek(getActivity()));
            getActivity().getContentResolver().update(uri, values, null, null);
        }

        if (key.equals(getResources().getString(R.string.currency_key))) {
            format = new DecimalFormat(sharedPreferences.getString(key, ""));

            FigurePickerPreference fpp = (FigurePickerPreference) findPreference(getResources().getString(R.string.budget_key));
            fpp.setSummary(format.format(fpp.getFigure()));
        }

        if (key.equals(getResources().getString(R.string.week_start_key))) {
            ContentValues values = BudgetDbContract.getCurrentWeekDates(getActivity());
            Uri uri = Uri.parse(BudgetContentProvider.WEEKS_URI + "/" + BudgetDbContract.getCurrentWeek(getActivity()));
            getActivity().getContentResolver().update(uri, values, null, null);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }
}
