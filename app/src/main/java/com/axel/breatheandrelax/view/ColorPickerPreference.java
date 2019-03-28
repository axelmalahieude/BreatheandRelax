package com.axel.breatheandrelax.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.axel.breatheandrelax.R;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

public class ColorPickerPreference extends Preference {

    // Views
    private TextView mTvTitle;
    private CheckBox mCb1;
    private CheckBox mCb2;
    private CheckBox mCb3;
    private CheckBox mCb4;

    // Data received from XML attributes
    private String mTitle;
    private String mKey;
    private int mDefaultColor;
    private CharSequence[] mColors;

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
        String androidNamespace = "http://schemas.android.com/apk/res/android";

        // Get custom attributes
        TypedArray stylizedAttributes = getContext().obtainStyledAttributes(attrs, R.styleable.ColorPickerPreference);
        try {
            mTitle = stylizedAttributes.getString(R.styleable.ColorPickerPreference_android_title);
            mKey = stylizedAttributes.getString(R.styleable.ColorPickerPreference_android_key);
            mDefaultColor = stylizedAttributes.getColor(R.styleable.ColorPickerPreference_android_defaultValue, getContext().getResources().getColor(R.color.white));
            mColors = stylizedAttributes.getTextArray(R.styleable.ColorPickerPreference_android_entries);
            Log.d(TAG, String.valueOf(mColors[0]));
        } finally {
            stylizedAttributes.recycle();
        }


        // Get builtin Android attributes
        int keyResourceID = attrs.getAttributeResourceValue(androidNamespace, "key", 0);
        mKey = getContext().getResources().getString(keyResourceID);


    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        mTvTitle = (TextView) holder.findViewById(R.id.pref_cb_title);
        mCb1 = (CheckBox) holder.findViewById(R.id.pref_cb_1);
        mCb2 = (CheckBox) holder.findViewById(R.id.pref_cb_2);
        mCb3 = (CheckBox) holder.findViewById(R.id.pref_cb_3);
        mCb4 = (CheckBox) holder.findViewById(R.id.pref_cb_4);

        mTvTitle.setText(mTitle);

        mCb1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mCb2.setChecked(false);
                    mCb3.setChecked(false);
                    mCb4.setChecked(false);
                } else if (allNotChecked()){
                    mCb1.setChecked(true);
                }
            }
        });

        mCb2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mCb1.setChecked(false);
                    mCb3.setChecked(false);
                    mCb4.setChecked(false);
                } else if (allNotChecked()){
                    mCb2.setChecked(true);
                }
            }
        });

        mCb3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mCb2.setChecked(false);
                    mCb1.setChecked(false);
                    mCb4.setChecked(false);
                } else if (allNotChecked()){
                    mCb3.setChecked(true);
                }
            }
        });

        mCb4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mCb2.setChecked(false);
                    mCb3.setChecked(false);
                    mCb1.setChecked(false);
                } else if (allNotChecked()){
                    mCb4.setChecked(true);
                }
            }
        });

    }

    /**
     * Determine whether no checkboxes are checked
     * @return true if no checkboxes are checked, false if 1 or more are checked
     */
    public boolean allNotChecked() {
        return !mCb1.isChecked() && !mCb2.isChecked() && !mCb3.isChecked() && !mCb4.isChecked();
    }
}
