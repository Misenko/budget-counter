package kimle.michal.android.fragment;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import kimle.michal.android.activity.R;
import kimle.michal.android.preference.FigurePickerPreference;

public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

    private static final String LOG = "SettingsFragment";
    private String format;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.preferences);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.registerOnSharedPreferenceChangeListener(this);
        format = sp.getString(getResources().getString(R.string.currency_key), "");
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getResources().getString(R.string.budget_key))) {
            FigurePickerPreference fpp = (FigurePickerPreference) findPreference(key);
            fpp.setSummary(FigurePickerPreference.getFormatedFigure(format, sharedPreferences.getFloat(key, 0)));
        }

        if (key.equals(getResources().getString(R.string.currency_key))) {
            format = sharedPreferences.getString(key, "");

            FigurePickerPreference fpp = (FigurePickerPreference) findPreference(getResources().getString(R.string.budget_key));
            fpp.setSummary(FigurePickerPreference.getFormatedFigure(format, fpp.getFigure()));
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
