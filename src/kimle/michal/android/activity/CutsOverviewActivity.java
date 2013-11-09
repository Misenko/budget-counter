package kimle.michal.android.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

public class CutsOverviewActivity extends Activity {

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

}
