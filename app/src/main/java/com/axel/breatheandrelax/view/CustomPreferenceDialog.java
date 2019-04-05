package com.axel.breatheandrelax.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.axel.breatheandrelax.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.DialogPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceDialogFragmentCompat;

public class CustomPreferenceDialog extends PreferenceDialogFragmentCompat {

    private static final String TAG = CustomPreferenceDialog.class.getSimpleName();
    private static final String ARG_TITLE = "title";
    private static final String ARG_ENTRIES = "entries";
    private static final String ARG_ENTRYKEYS = "entryvalues";
    private static final String ARG_DEFAULT = "def";

    private DialogClosedListener mDialogClosedListener;

    public interface DialogClosedListener {
        void onSelection(String key, String newValue);
    }

    public void setDialogClosedListener(DialogClosedListener dcl) {
        mDialogClosedListener = dcl;
    }

    public static CustomPreferenceDialog createInstance(Preference preference) {
        CustomPreferenceDialog fragment = new CustomPreferenceDialog();
        Bundle args = new Bundle(1);
        args.putString(ARG_KEY, preference.getKey());
        args.putString(ARG_TITLE, preference.getTitle().toString());
        args.putStringArray(ARG_ENTRIES, ((CustomListPreference) preference).getEntries());
        args.putStringArray(ARG_ENTRYKEYS, ((CustomListPreference) preference).getEntryValues());
        args.putString(ARG_DEFAULT, ((CustomListPreference) preference).getSelection());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        dismiss(); // close dialog
    }

    @Override
    protected View onCreateDialogView(Context context) {
        View view = View.inflate(context, R.layout.pref_list_dialog, null);

        Bundle args = getArguments();
        String title, defaultEntry;
        final String key;
        final String[] entries, entryKeys;
        if (args != null) {
            key = args.getString(ARG_KEY);
            title = args.getString(ARG_TITLE);
            defaultEntry = args.getString(ARG_DEFAULT);
            entries = args.getStringArray(ARG_ENTRIES);
            entryKeys = args.getStringArray(ARG_ENTRYKEYS);
        } else {
            return super.onCreateDialogView(context);
        }

        if (entryKeys == null || entries == null)
            return super.onCreateDialogView(context);

        // Set the title
        ((TextView) view.findViewById(R.id.pref_dialog_title)).setText(title);

        // Fetch the ListView and remove styling of elements
        ListView lv = view.findViewById(R.id.pref_dialog_listview);
        lv.setDivider(null);
        lv.setDividerHeight(0);

        int defaultSelection = 0;
        for (int i = 0; i < entryKeys.length; i++) {
            if (entryKeys[i].equals(defaultEntry)) {
                defaultSelection = i;
            }
        }

        Log.d(TAG, "Selection: " + String.valueOf(defaultSelection));

        // Set ListView adapter and click listener
        Adapter adapter = new Adapter(context, entries, defaultSelection);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((RadioButton) view.findViewById(R.id.pref_dialog_listview_button)).setChecked(true);
                String keySelected = entryKeys[position];

                mDialogClosedListener.onSelection(key, keySelected); // notify a selection was made
                Log.d(TAG, keySelected);
                dismiss(); // close the dialog
            }
        });

        return view;
    }

    /**
     * Override required to remove default buttons and title. This allows for custom theming using
     * an XML layout.
     * @param builder dialog builder to manipulate
     */
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

        private int mCheckedItem;

        private Adapter(Context context, String[] entries, int defaultSelection) {
            super(context, R.layout.pref_list_dialog, entries);
            mCheckedItem = defaultSelection;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.listview_item, parent, false);
            }

            ((TextView) convertView.findViewById(R.id.pref_dialog_listview_text)).setText(getItem(position));

            Log.d(TAG, "Building view #" + String.valueOf(position));

            if (mCheckedItem == position) {
                Log.d(TAG, "Selecting button #" + position + "\t" + mCheckedItem);
                ((RadioButton) convertView.findViewById(R.id.pref_dialog_listview_button)).setChecked(true);
            } else {
                ((RadioButton) convertView.findViewById(R.id.pref_dialog_listview_button)).setChecked(false);
            }

            return convertView;
        }
    }
}
