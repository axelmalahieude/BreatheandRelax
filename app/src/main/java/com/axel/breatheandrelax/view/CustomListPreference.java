package com.axel.breatheandrelax.view;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

import com.axel.breatheandrelax.R;

import androidx.preference.DialogPreference;
import androidx.preference.PreferenceViewHolder;

public class CustomListPreference extends DialogPreference {

    private static String TAG = CustomListPreference.class.getSimpleName();

    private CharSequence[] mEntries;
    private CharSequence[] mEntryValues;
    private CharSequence mDefaultEntry;
    private String mKey; // key for the preference, so we know which SharedPreference to update

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
            mKey = stylizedAttributes.getText(R.styleable.CustomListPreference_android_key).toString();
        } finally {
            stylizedAttributes.recycle();
        }
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

    /**
     * Get the entry in the ListPreference that is currently selected, or the default if none is selected
     * @return the selected entry
     */
    public String getSelectedEntry() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        // TODO: Change default to be more generic
        String defaultSelection = getContext().getResources().getString(R.string.pref_animation_style_default);
        return sharedPreferences.getString(mKey, defaultSelection);
    }
}
