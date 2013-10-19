package kimle.michal.android.view;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.text.DecimalFormat;
import kimle.michal.android.activity.R;

public class FigureDisplayView extends LinearLayout implements Button.OnClickListener, Button.OnLongClickListener {

    private static final String LOG = "FigureDisplayView";
    private static final int EURO = 1;
    private static final int POUND = 2;
    private static final String EURO_SIGN = "€";
    private static final String POUND_SIGN = "£";

    public FigureDisplayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.display, this, true);
    }

    public FigureDisplayView(Context context) {
        this(context, null);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        ImageButton ib = (ImageButton) findViewById(R.id.button_backspace);
        ib.setOnClickListener(this);
        ib.setOnLongClickListener(this);
    }

    public void setDisplayContent(float figure) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        int currency = Integer.parseInt(pref.getString(getResources().getString(R.string.currency_key), ""));
        String currencySign;
        DecimalFormat df;

        switch (currency) {
            case (POUND):
                currencySign = POUND_SIGN;
                df = new DecimalFormat(currencySign + "00.00");
                break;
            case (EURO):
            default:
                currencySign = EURO_SIGN;
                df = new DecimalFormat("00.00" + currencySign);
                break;
        }

        String displayString = df.format(figure);

        TextView displayView = (TextView) findViewById(R.id.textview_display);
        displayView.setText(displayString);
    }

    public void onClick(View v) {
        Activity activity = (Activity) getContext();
        if (activity instanceof FigureDisplayView.FigureDisplayViewHandler) {
            FigureDisplayView.FigureDisplayViewHandler fdvh = (FigureDisplayView.FigureDisplayViewHandler) activity;
            fdvh.onBackspaceClick();
        }
    }

    public boolean onLongClick(View v) {
        Activity activity = (Activity) getContext();
        if (activity instanceof FigureDisplayView.FigureDisplayViewHandler) {
            FigureDisplayView.FigureDisplayViewHandler fdvh = (FigureDisplayView.FigureDisplayViewHandler) activity;
            fdvh.onBackspaceLongClick();
            return true;
        }

        return false;
    }

    public interface FigureDisplayViewHandler {

        public void onBackspaceClick();

        public void onBackspaceLongClick();
    }

}
