package kimle.michal.android.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import kimle.michal.android.activity.R;

public class FigureInputView extends LinearLayout {

    private static final String LOG = "FigureInputView";

    private static final int TOP = 0;
    private static final int BOTTOM = 1;

    private FigureDisplayView fdv;
    private FigureKeypadView fkv;

    public FigureInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(LinearLayout.VERTICAL);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.FigureInputView,
                0, 0);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        try {
            int displayPosition = a.getInt(R.styleable.FigureInputView_displayPosition, 0);
            switch (displayPosition) {
                case (BOTTOM):
                    inflater.inflate(R.layout.figure_input_bottom, this, true);
                    break;
                case (TOP):
                default:
                    inflater.inflate(R.layout.figure_input_top, this, true);
                    break;
            }
        } finally {
            a.recycle();
        }

        fdv = (FigureDisplayView) findViewById(R.id.figure_display);
        fkv = (FigureKeypadView) findViewById(R.id.figure_keypad);

        fdv.setKeypad(fkv);
        fkv.setDisplay(fdv);
    }

    public FigureInputView(Context context) {
        this(context, null);
    }

    public double getValue() {
        return fdv.getFigure();
    }

    public void setValue(double value) {
        fdv.setFigure(value);
        fkv.setFigure(value);
    }

    public CharSequence getFormatedValue() {
        return fdv.getFormatedFigure();
    }

    public void updateSettings() {
        fdv.loadFormat();
    }

    public void reset() {
        fkv.reset();
    }
}
