package kimle.michal.android.fragment;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import kimle.michal.android.activity.R;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.preferences);
    }
}
