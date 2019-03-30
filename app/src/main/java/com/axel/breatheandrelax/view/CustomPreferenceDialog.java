package com.axel.breatheandrelax.view;

import android.content.Context;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.axel.breatheandrelax.R;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
        ListView lv = view.findViewById(R.id.pref_dialog_listview);
        lv.setDivider(null);
        lv.setDividerHeight(0);

        Adapter adapter = new Adapter(context, entries);
        lv.setAdapter(adapter);
        Log.d(TAG, "Setting click listener");
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((RadioButton) view.findViewById(R.id.pref_dialog_listview_button)).setChecked(true);
                Log.d(TAG, "Clicked element");
            }
        });

        return view;
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        builder.setTitle(null);
        builder.setPositiveButton(null, null);
        builder.setNegativeButton(null, null);
    }

    /**
     * Extend the ArrayAdapter to have our custom view in the ListView
     */
    private class Adapter extends ArrayAdapter<String> {

        private Adapter(Context context, String[] entries) {
            super(context, R.layout.pref_list_dialog, entries);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.listview_item, parent, false);
            }

            ((TextView) convertView.findViewById(R.id.pref_dialog_listview_text)).setText(getItem(position));

            return convertView;
        }
    }
}
