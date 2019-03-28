package com.axel.breatheandrelax.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

import com.axel.breatheandrelax.R;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

public class CustomSeekBarPreference extends Preference {

    private TextView mTextViewValue;
    private SeekBar mSeekBar;
    private SharedPreferences mSharedPreferences;
    private String TAG = CustomSeekBarPreference.class.getSimpleName();

    public CustomSeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        constructor(context, attrs, defStyleAttr);
    }

    public CustomSeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        constructor(context, attrs, 0);
    }

    public CustomSeekBarPreference(Context context) {
        super(context);
        constructor(context, null, 0);
    }

    /**
     * Collective constructor handling
     * @param context context for view
     * @param attrs attributes defined in XML for view
     * @param defStyleAttr theme reference
     */
    private void constructor(Context context, AttributeSet attrs, int defStyleAttr) {
        this.setLayoutResource(R.layout.pref_seekbar);
        setAttributes(attrs);
    }

    /**
     * Sets attributes for this custom view based on XML definitions
     * @param attrs set of attributes to handle
     */
    private void setAttributes(AttributeSet attrs) {
        String namespace = "http://schemas.android.com/apk/res-auto";
        int defaultValue = attrs.getAttributeIntValue(namespace, "startAt", 0);
        int maxValue = attrs.getAttributeIntValue(namespace, "max", 0);
        String title = attrs.getAttributeValue(namespace, "title");
        //Log.d(TAG, title);
        Log.d(TAG, String.valueOf(defaultValue));

    }

    /**
     * Binds elements to view. Used to control XML elements of this view
     * @param holder viewholder
     */
    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        mSeekBar = (SeekBar) holder.findViewById(R.id.seekbar);
        mTextViewValue = (TextView) holder.findViewById(R.id.tv_seekbar_value);

        mSharedPreferences = this.getSharedPreferences();

        // Dynamically updates the tooltip to reflect the new value of the SeekBar
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTextViewValue.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mSharedPreferences.edit().putInt(
                        getContext().getResources().getString(R.string.pref_seekbar_inhale_key), seekBar.getProgress())
                        .apply();
            }
        });
    }

    public int getValue() {
        if (mSeekBar == null)
            return getContext().getResources().getInteger(R.integer.inhale_default);
        else
            return mSeekBar.getProgress();
    }

    public void setValue(int value) {
        if (mSeekBar == null || value < 0 || value > 10) return;
        mSeekBar.setProgress(value);
    }
}