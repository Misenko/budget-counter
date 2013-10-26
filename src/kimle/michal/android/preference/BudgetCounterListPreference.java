package kimle.michal.android.preference;

import android.content.Context;
import android.preference.ListPreference;
import android.preference.Preference;
import android.util.AttributeSet;

public class BudgetCounterListPreference extends ListPreference {

    private static final String LOG = "BudgetCounterListPreference";

    public BudgetCounterListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BudgetCounterListPreference(Context context) {
        super(context);
        init();
    }

    @Override
    public CharSequence getSummary() {
        return super.getEntry();
    }

    private void init() {

        setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference arg0, Object arg1) {
                arg0.setSummary(getEntry());
                return true;
            }
        });
    }
}
