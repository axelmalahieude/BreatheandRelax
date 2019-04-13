package com.axel.breatheandrelax.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.axel.breatheandrelax.R;
import com.axel.breatheandrelax.util.Tools;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

public class ColorPickerPreference extends Preference {

    // Views
    private TextView mSummary;
    private CheckBox mCb1;
    private CheckBox mCb2;
    private CheckBox mCb3;
    private CheckBox mCb4;

    // Data received from XML attributes
    private String mTitle;
    private String mKey;
    private int mDefaultColor;
    private CharSequence[] mColors;

    // Other data members
    private int mCurrentColor;

    // Debugging
    private String TAG = ColorPickerPreference.class.getSimpleName();

    public ColorPickerPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        constructor(context, attrs, defStyleAttr);
    }

    public ColorPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        constructor(context, attrs, 0);
    }

    public ColorPickerPreference(Context context) {
        super(context);
        constructor(context, null, 0);
    }

    private void constructor(Context context, AttributeSet attrs, int defStyleAttr) {
        this.setLayoutResource(R.layout.pref_color_picker);
        if (attrs != null)
            getAttributes(attrs);
    }

    private void getAttributes(AttributeSet attrs) {
        // Get custom attributes
        TypedArray stylizedAttributes = getContext().obtainStyledAttributes(attrs, R.styleable.ColorPickerPreference);
        try {
            mTitle = stylizedAttributes.getString(R.styleable.ColorPickerPreference_android_title);
            mKey = stylizedAttributes.getString(R.styleable.ColorPickerPreference_android_key);
            mDefaultColor = stylizedAttributes.getColor(R.styleable.ColorPickerPreference_android_defaultValue, getContext().getResources().getColor(R.color.white));
            mColors = stylizedAttributes.getTextArray(R.styleable.ColorPickerPreference_android_entries);
        } finally {
            stylizedAttributes.recycle();
        }
    }

    public void setSummary(int currentColor) {
        String summary = Tools.intColorToString(getContext().getResources(), currentColor);
        if (mSummary != null)
            mSummary.setText(summary);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        // Fetch references for the views in this preference
        TextView title = (TextView) holder.findViewById(R.id.pref_cb_title);
        mSummary = (TextView) holder.findViewById(R.id.pref_color_summary);
        mCb1 = (CheckBox) holder.findViewById(R.id.pref_cb_1);
        mCb2 = (CheckBox) holder.findViewById(R.id.pref_cb_2);
        mCb3 = (CheckBox) holder.findViewById(R.id.pref_cb_3);
        mCb4 = (CheckBox) holder.findViewById(R.id.pref_cb_4);

        final CheckBox[] checkBoxes = new CheckBox[] { mCb1, mCb2, mCb3, mCb4 };

        Resources resources = getContext().getResources();

        // Check the default color
        //TODO: Properly check for defaults if first time use. Otherwise no color is chosen
        final SharedPreferences sharedPreferences = this.getSharedPreferences();
        String currentColor = sharedPreferences.getString(mKey, null);
        if (currentColor == null)
            currentColor = Tools.intColorToKey(resources, mDefaultColor);

        // Set the checkboxes to be colored according to which color they select
        int[][] states = new int[][]{
                new int[] { android.R.attr.state_checked },
                new int[] { -android.R.attr.state_checked }
        };
        for (int i = 0; i < checkBoxes.length; i++) {
            if (mColors[i].toString().equals(currentColor))
                checkBoxes[i].setChecked(true);
            int color = Tools.colorKeyToInt(getContext().getResources(), mColors[i].toString());
            int[] colorArray = new int[] { color, color };
            checkBoxes[i].setButtonTintList(new ColorStateList(states, colorArray));
        }

        title.setText(mTitle);
        setSummary(Tools.colorKeyToInt(resources, currentColor));

        // Set the listeners for when a checkbox is clicked
        for (final CheckBox checkBox : checkBoxes) {
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    // Make sure only this checkbox is checked by unchecking all other checkboxes
                    if (isChecked) {
                        for (CheckBox c : checkBoxes) {
                            if (c != checkBox)
                                c.setChecked(false);
                        }

                        // Apply the new color to SharedPreferences
                        ColorStateList colorStateList = checkBox.getButtonTintList();
                        int color;
                        try {
                            color = colorStateList.getColorForState(new int[] { android.R.attr.state_checked }, 0);
                        } catch (NullPointerException e) {
                            Log.d(TAG, "NPE");
                            color = 0;
                        }

                        setSummary(color);
                        sharedPreferences.edit().putString(mKey, Tools.intColorToKey(getContext().getResources(), color)).apply();
                    }
                    // Make sure this checkbox stays checked if it was already checked
                    else if (allNotChecked()) {
                        checkBox.setChecked(true);
                    }
                }
            });
        }

    }

    /**
     * Determine whether no checkboxes are checked
     * @return true if no checkboxes are checked, false if 1 or more are checked
     */
    private boolean allNotChecked() {
        return !mCb1.isChecked() && !mCb2.isChecked() && !mCb3.isChecked() && !mCb4.isChecked();
    }
}
