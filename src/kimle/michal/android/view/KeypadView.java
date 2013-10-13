package kimle.michal.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import kimle.michal.android.activity.R;

public class KeypadView extends LinearLayout {

    public KeypadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_keypad, this, true);

    }

    public KeypadView(Context context) {
        this(context, null);
    }
}
