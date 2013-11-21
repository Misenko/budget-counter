package kimle.michal.android.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import java.text.DecimalFormat;
import kimle.michal.android.activity.R;

public class PreferenceHelper {

    private static final int HUNDRED = 100;
    private static final int LOW_PCT = 5;
    private static final int MIDDLE_PCT = 20;

    public static DecimalFormat loadFormat(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return new DecimalFormat(pref.getString(context.getResources().getString(R.string.currency_key), ""));
    }

    //returns color id not actual color
    public static int calculateColor(double remainder, double budget) {
        double percentage = remainder / (budget / HUNDRED);
        if (percentage < LOW_PCT) {
            return R.color.red_rem;
        }
        if (percentage >= LOW_PCT && percentage < MIDDLE_PCT) {
            return R.color.orange_rem;
        }

        return R.color.green_rem;
    }
}
