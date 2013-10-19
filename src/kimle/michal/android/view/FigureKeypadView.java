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

    private static final float HIGHEST_POSITION = 100;
    private static final float START_POSITION = 1;
    private static final float LOWEST_POSITION = 0.01f;
    private static final float FIGURE_START = 0;

    private Button numbers[] = new Button[10];
    private Button dot;
    private Button fifty;
    private float figure = FIGURE_START;
    private float pos = START_POSITION;
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
                figure = (figure * pos) + Float.parseFloat((b.getText().toString()));
                pos *= 10;
                if (pos > HIGHEST_POSITION) {
                    setNumbers(false);
                }
                break;
            case (R.id.button_50):
                figure += 0.5;
                setNumbers(false);
                setDecimal(false);
                pos = START_POSITION;
                onDecimal = true;
                break;
            case (R.id.button_dot):
                setDecimal(false);
                pos = LOWEST_POSITION;
                onDecimal = true;
                break;
        }

        Activity activity = (Activity) getContext();
        if (activity instanceof FigureKeypadViewHandler) {
            FigureKeypadViewHandler fkvh = (FigureKeypadViewHandler) activity;
            fkvh.onFigureChange(figure);
        }
    }

    public void reset() {
        figure = FIGURE_START;
        onDecimal = false;
        pos = START_POSITION;
        setDecimal(true);
        setNumbers(true);
    }

    public void deleteLast() {

    }

    private void setDecimal(boolean status) {
        dot.setEnabled(status);
        fifty.setEnabled(status);
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
