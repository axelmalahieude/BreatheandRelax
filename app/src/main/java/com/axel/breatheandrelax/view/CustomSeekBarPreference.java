package com.axel.breatheandrelax.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.SeekBar;
import android.widget.TextView;

import com.axel.breatheandrelax.R;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

public class CustomSeekBarPreference extends Preference {

    private TextView mTextViewValue;
    private SeekBar mSeekBar;
    private SharedPreferences mSharedPreferences;

    private String mTitle;
    private String mKey;
    private int mMaxVal;

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
        if (attrs != null)
            getAttributes(attrs);
    }

    /**
     * Gets attributes for this custom view based on XML definitions
     * @param attrs set of attributes to handle
     */
    private void getAttributes(AttributeSet attrs) {
        String androidNamespace = "http://schemas.android.com/apk/res/android";

        // Get custom attributes
        TypedArray styledAttributes = getContext().obtainStyledAttributes(attrs, R.styleable.CustomSeekBarPreference);
        mMaxVal = styledAttributes.getInt(R.styleable.CustomSeekBarPreference_max, 0);
        styledAttributes.recycle();

        // Get builtin Android attributes
        int titleResourceID = attrs.getAttributeResourceValue(androidNamespace, "title", 0);
        mTitle = getContext().getResources().getString(titleResourceID);
        int keyResourceID = attrs.getAttributeResourceValue(androidNamespace, "key", 0);
        mKey = getContext().getResources().getString(keyResourceID);

    }


    /**
     * Binds elements to view. Used to control XML elements of this view
     * @param holder view holder
     */
    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        mSeekBar = (SeekBar) holder.findViewById(R.id.seekbar);
        mTextViewValue = (TextView) holder.findViewById(R.id.tv_seekbar_value);
        TextView label = (TextView) holder.findViewById(R.id.tv_seekbar_title);

        mSharedPreferences = this.getSharedPreferences();
        int currVal = mSharedPreferences.getInt(mKey, 12341234);

        label.setText(mTitle);
        mSeekBar.setMax(mMaxVal);
        mSeekBar.setProgress(currVal);
        mTextViewValue.setText(String.valueOf(currVal));

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
                mSharedPreferences.edit().putInt(mKey, seekBar.getProgress()).apply();
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
        mTextViewValue.setText(String.valueOf(value));
        mSharedPreferences.edit().putInt(mKey, value).apply();
    }
}