package kimle.michal.android.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import java.text.DecimalFormat;
import kimle.michal.android.activity.R;

public class PreferenceHelper {

    public static DecimalFormat loadFormat(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return new DecimalFormat(pref.getString(context.getResources().getString(R.string.currency_key), ""));
    }
}
