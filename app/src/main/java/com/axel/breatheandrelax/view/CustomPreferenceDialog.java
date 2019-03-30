package com.axel.breatheandrelax.view;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.axel.breatheandrelax.R;

import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.PreferenceDialogFragmentCompat;

public class CustomPreferenceDialog extends PreferenceDialogFragmentCompat {

    private static final String TAG = CustomPreferenceDialog.class.getSimpleName();
    private static final String ARG_TITLE = "title";
    private static final String ARG_ENTRIES = "entries";
    private static final String ARG_ENTRYVALUES = "entryvalues";
    private static final String ARG_DEFAULT = "def";

    public static CustomPreferenceDialog createInstance(Preference preference) {
        CustomPreferenceDialog fragment = new CustomPreferenceDialog();
        Bundle args = new Bundle(1);
        args.putString(ARG_KEY, preference.getKey());
        args.putString(ARG_TITLE, preference.getTitle().toString());
        args.putStringArray(ARG_ENTRIES, ((CustomListPreference) preference).getEntries());
        args.putStringArray(ARG_ENTRYVALUES, ((CustomListPreference) preference).getEntryValues());
        args.putString(ARG_DEFAULT, ((CustomListPreference) preference).getDefaultEntry());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        Log.d(TAG, "Success");
    }

    @Override
    protected View onCreateDialogView(Context context) {
        View view = View.inflate(context, R.layout.pref_list_dialog, null);

        Bundle args = getArguments();
        String title, defaultEntry;
        String[] entries, entryValues;
        if (args != null) {
            title = args.getString(ARG_TITLE);
            defaultEntry = args.getString(ARG_DEFAULT);
            entries = args.getStringArray(ARG_ENTRIES);
            entryValues = args.getStringArray(ARG_ENTRYVALUES);
        } else {
            return super.onCreateDialogView(context);
        }

        ((TextView) view.findViewById(R.id.pref_dialog_title)).setText(title);
        ListView lv = (ListView) view.findViewById(R.id.pref_dialog_listview);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.pref_list_dialog, entries);
        lv.setAdapter(adapter);

        return view;
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        builder.setTitle(null);
        builder.setPositiveButton(null, null);
        builder.setNegativeButton(null, null);
    }
}
