package kimle.michal.android.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import java.text.DecimalFormat;
import kimle.michal.android.activity.R;
import kimle.michal.android.view.FigureInputView;

public class FigurePickerPreference extends DialogPreference {

    private static final String LOG = "FigurePickerPreference";

    private double figure;
    private FigureInputView fiv;

    public FigurePickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDialogLayoutResource(R.layout.figure_input_preference);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);

        setDialogIcon(null);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            Log.d(LOG, "in onDialogClosed");
            figure = fiv.getValue();
            persistFloat(new Float(figure));
        }
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        Log.d(LOG, "in onBindDialogView");

        fiv = (FigureInputView) view.findViewById(R.id.figure_input);
        fiv.setValue(figure);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            // Restore existing state
            TypedValue tempVal = new TypedValue();
            getContext().getResources().getValue(R.figure.default_budget, tempVal, true);
            figure = getPersistedFloat(tempVal.getFloat());
        } else {
            // Set default state from the XML attribute
            figure = (Float) defaultValue;
            persistFloat(new Float(figure));
        }
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        setSummary(getFormatedFigure(sp.getString(getContext().getResources().getString(R.string.currency_key), ""), figure));
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        TypedValue tempVal = new TypedValue();
        getContext().getResources().getValue(R.figure.default_budget, tempVal, true);
        return a.getFloat(index, tempVal.getFloat());
    }

    private static class SavedState extends BaseSavedState {

        // Member that holds the setting's value
        // Change this data type to match the type saved by your Preference
        double value;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public SavedState(Parcel source) {
            super(source);
            // Get the current preference's value
            value = source.readFloat();  // Change this to read the appropriate data type
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            // Write the preference's value
            dest.writeDouble(value);  // Change this to write the appropriate data type
        }

        // Standard creator object using an instance of this class
        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {

                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        // Check whether this Preference is persistent (continually saved)
        if (isPersistent()) {
            // No need to save instance state since it's persistent, use superclass state
            return superState;
        }

        // Create instance of custom BaseSavedState
        final SavedState myState = new SavedState(superState);
        // Set the state's value with the class member that holds current setting value
        myState.value = figure;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        // Check whether we saved the state in onSaveInstanceState
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save the state, so call superclass
            super.onRestoreInstanceState(state);
            return;
        }

        // Cast state to custom BaseSavedState and pass to superclass
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());

        // Set this Preference's widget to reflect the restored state
        fiv.setValue(myState.value);
    }

    public static CharSequence getFormatedFigure(String format, double figure) {
        DecimalFormat df = new DecimalFormat(format);
        return df.format(figure);
    }

    public double getFigure() {
        return figure;
    }
}
