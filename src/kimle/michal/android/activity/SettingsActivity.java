package kimle.michal.android.activity;

import android.app.Activity;
import android.os.Bundle;
import kimle.michal.android.fragment.SettingsFragment;

public class SettingsActivity extends Activity {

    private static final String LOG = "SettingsActivity";

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

    }
}
