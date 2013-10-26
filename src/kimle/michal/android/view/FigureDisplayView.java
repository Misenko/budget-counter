package kimle.michal.android.view;

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

    private FigureKeypadView fkv;
    private float figure;
    private CharSequence formatedFigure;

    public FigureDisplayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.figure_display, this, true);
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
        DecimalFormat df = new DecimalFormat(pref.getString(getResources().getString(R.string.currency_key), ""));

        formatedFigure = df.format(figure);
        this.figure = figure;

        TextView displayView = (TextView) findViewById(R.id.textview_display);
        displayView.setText(formatedFigure);
    }

    public void onClick(View v) {
        if (fkv != null) {
            fkv.deleteLast();
        }
    }

    public boolean onLongClick(View v) {
        if (fkv != null) {
            fkv.reset();
            return true;
        }

        return false;
    }

    public void setKeypad(FigureKeypadView fkv) {
        this.fkv = fkv;
    }

    public float getFigure() {
        return figure;
    }

    public void setFigure(float figure) {
        this.figure = figure;
    }

    public CharSequence getFormatedFigure() {
        return formatedFigure;
    }
}
