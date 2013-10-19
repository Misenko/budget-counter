package kimle.michal.android.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import kimle.michal.android.activity.R;

public class FigureDisplayView extends LinearLayout {

    private static String EURO = "1";
    private static String POUND = "2";

    public FigureDisplayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.display, this, true);
    }

    public FigureDisplayView(Context context) {
        this(context, null);
    }

    public void setDisplayContent(float figure) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        String currency = pref.getString(getResources().getString(R.string.currency_key), "NULL");
        String currency_sign = "";

        if (currency.equals(EURO)) {
            currency_sign = "€";
        } else if (currency.equals(POUND)) {
            currency_sign = "£";
        }

        String displayString = Float.toString(figure) + currency_sign;

        TextView displayView = (TextView) findViewById(R.id.textview_display);
        displayView.setText(displayString);
    }

}
