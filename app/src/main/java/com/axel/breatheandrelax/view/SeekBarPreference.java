package com.axel.breatheandrelax.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.axel.breatheandrelax.R;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

public class SeekBarPreference extends Preference {

    private TextView mTextViewValue;
    private String TAG = SeekBarPreference.class.getSimpleName();

    public SeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setLayoutResource(R.layout.pref_seekbar);
    }

    public SeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setLayoutResource(R.layout.pref_seekbar);
    }

    public SeekBarPreference(Context context) {
        super(context);
        this.setLayoutResource(R.layout.pref_seekbar);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        SeekBar mSeekBar = (SeekBar) holder.findViewById(R.id.seekbar);
        mTextViewValue = (TextView) holder.findViewById(R.id.tv_seekbar_value);

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

            }
        });
    }
}