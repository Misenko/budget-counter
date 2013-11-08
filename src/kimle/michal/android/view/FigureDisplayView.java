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
    private double figure = 0;
    private CharSequence formatedFigure;
    private DecimalFormat format;

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
        loadFormat();
    }

    public void loadFormat() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        format = new DecimalFormat(pref.getString(getResources().getString(R.string.currency_key), ""));
        setDisplayContent(figure);
    }

    public void setDisplayContent(double figure) {
        formatedFigure = format.format(figure);
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

    public double getFigure() {
        return figure;
    }

    public void setFigure(double figure) {
        this.figure = figure;
    }

    public CharSequence getFormatedFigure() {
        return formatedFigure;
    }
}
