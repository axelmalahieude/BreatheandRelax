package com.axel.breatheandrelax.view;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.axel.breatheandrelax.R;

import androidx.preference.DialogPreference;
import androidx.preference.PreferenceViewHolder;

public class CustomListPreference extends DialogPreference {

    private static String TAG = CustomListPreference.class.getSimpleName();

    private CharSequence[] mEntries;
    private CharSequence[] mEntryValues;
    private CharSequence mDefaultEntry;

    public CustomListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        constructor(context, attrs, defStyleAttr);
    }

    public CustomListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        constructor(context, attrs, 0);
    }

    public CustomListPreference(Context context) {
        super(context);
        constructor(context, null, 0);
    }

    private void constructor(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray stylizedAttributes = getContext().getResources().obtainAttributes(attrs, R.styleable.CustomListPreference);
        try {
            mDefaultEntry = stylizedAttributes.getText(R.styleable.CustomListPreference_android_defaultValue);
            mEntries = stylizedAttributes.getTextArray(R.styleable.CustomListPreference_android_entries);
            mEntryValues = stylizedAttributes.getTextArray(R.styleable.CustomListPreference_android_entryValues);
        } finally {
            stylizedAttributes.recycle();
        }
    }

    public void preferenceChanged() {

    }

    public String[] getEntries() {
        String[] entries = new String[mEntries.length];
        for (int i = 0; i < mEntries.length; i++)
            entries[i] = mEntries[i].toString();
        return entries;
    }

    public String[] getEntryValues() {
        String[] entryValues = new String[mEntryValues.length];
        for (int i = 0; i < mEntryValues.length; i++)
            entryValues[i] = mEntryValues[i].toString();
        return entryValues;
    }

    public String getDefaultEntry() {
        return mDefaultEntry.toString();
    }
}
