package kimle.michal.android.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import kimle.michal.android.activity.R;

public class FigureKeypadView extends LinearLayout implements Button.OnClickListener {

    private static final int FIGURE_START = 0;
    private static final int COEF = 10;
    private static final int TOP = 1000;
    private static final int DECIMAL_TOP = 10;
    private static final int FIFTY = 50;

    private final Button numbers[] = new Button[10];
    private Button dot;
    private Button fifty;
    private int figure = FIGURE_START;
    private int decimalFigure = FIGURE_START;
    private boolean onDecimal = false;

    public FigureKeypadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.keypad, this, true);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        numbers[0] = (Button) findViewById(R.id.button_0);
        numbers[1] = (Button) findViewById(R.id.button_1);
        numbers[2] = (Button) findViewById(R.id.button_2);
        numbers[3] = (Button) findViewById(R.id.button_3);
        numbers[4] = (Button) findViewById(R.id.button_4);
        numbers[5] = (Button) findViewById(R.id.button_5);
        numbers[6] = (Button) findViewById(R.id.button_6);
        numbers[7] = (Button) findViewById(R.id.button_7);
        numbers[8] = (Button) findViewById(R.id.button_8);
        numbers[9] = (Button) findViewById(R.id.button_9);

        dot = (Button) findViewById(R.id.button_dot);
        fifty = (Button) findViewById(R.id.button_50);

        dot.setOnClickListener(this);
        fifty.setOnClickListener(this);

        for (int i = 0; i < 10; i++) {
            numbers[i].setOnClickListener(this);
        }
    }

    public FigureKeypadView(Context context) {
        this(context, null);
    }

    public void onClick(View v) {
        Button b = (Button) v;
        int id = b.getId();
        switch (id) {
            case (R.id.button_0):
            case (R.id.button_1):
            case (R.id.button_2):
            case (R.id.button_3):
            case (R.id.button_4):
            case (R.id.button_5):
            case (R.id.button_6):
            case (R.id.button_7):
            case (R.id.button_8):
            case (R.id.button_9):
                int value = Integer.parseInt((b.getText().toString()));
                if (onDecimal) {
                    decimalFigure = (decimalFigure * COEF) + value;
                    if (decimalFigure >= DECIMAL_TOP) {
                        setNumbers(false);
                    }
                } else {
                    figure = (figure * COEF) + value;
                    if (figure >= TOP) {
                        setNumbers(false);
                    }
                }
                break;
            case (R.id.button_50):
                decimalFigure = FIFTY;
                setNumbers(false);
                setDecimal(false);
                break;
            case (R.id.button_dot):
                setDecimal(false);
                setNumbers(true);
                break;
        }
        updateDisplay();
    }

    public void updateDisplay() {
        Activity activity = (Activity) getContext();
        if (activity instanceof FigureKeypadViewHandler) {
            FigureKeypadViewHandler fkvh = (FigureKeypadViewHandler) activity;
            fkvh.onFigureChange(figure + ((float) decimalFigure / (DECIMAL_TOP * COEF)));
        }
    }

    @Override
    protected void onAttachedToWindow() {
        updateDisplay();
    }

    public void reset() {
        figure = FIGURE_START;
        decimalFigure = FIGURE_START;
        setDecimal(true);
        setNumbers(true);
        updateDisplay();
    }

    public void deleteLast() {
        if (onDecimal) {
            if (decimalFigure == FIGURE_START) {
                setDecimal(true);
                deleteLast();
                return;
            }

            decimalFigure = (int) Math.floor(decimalFigure / 10);
            if (decimalFigure == FIGURE_START) {
                setDecimal(true);
                if (figure >= TOP) {
                    setNumbers(false);
                } else {
                    setNumbers(true);
                }
            }
        } else {
            if (figure == FIGURE_START) {
                return;
            }

            figure = (int) Math.floor(figure / 10);
            setNumbers(true);
        }

        updateDisplay();
    }

    private void setDecimal(boolean status) {
        dot.setEnabled(status);
        fifty.setEnabled(status);
        onDecimal = !status;
    }

    private void setNumbers(boolean status) {
        for (int i = 0; i < 10; i++) {
            numbers[i].setEnabled(status);
        }
    }

    public interface FigureKeypadViewHandler {

        public void onFigureChange(float figure);
    }
}
