package com.axel.breatheandrelax.view;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.axel.breatheandrelax.R;

import java.util.ArrayList;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.PreferenceDialogFragmentCompat;

public class CustomPreferenceDialog extends PreferenceDialogFragmentCompat {

    private static final String TAG = CustomPreferenceDialog.class.getSimpleName();
    private static final String ARG_TITLE = "title";
    private static final String ARG_ENTRYVALUES = "entries";
    private static final String ARG_ENTRYKEYS = "entryvalues";
    private static final String ARG_DEFAULT = "def";

    // Interface to pass data back to the Custom Preference that uses this PreferenceDialog
    private DialogClosedListener mDialogClosedListener;
    public interface DialogClosedListener {
        void onSelection(String key, String newValue);
    }

    public void setDialogClosedListener(DialogClosedListener dcl) {
        mDialogClosedListener = dcl;
    }

    public static CustomPreferenceDialog createInstance(CustomListPreference preference) {
        CustomPreferenceDialog fragment = new CustomPreferenceDialog();
        Bundle args = new Bundle();
        args.putString(ARG_KEY, preference.getKey());
        args.putString(ARG_TITLE, preference.getTitle().toString());
        args.putStringArray(ARG_ENTRYVALUES, preference.getEntryValues());
        args.putStringArray(ARG_ENTRYKEYS, preference.getEntryKeys());
        args.putString(ARG_DEFAULT, preference.getValue());
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

        // Fetch arguments and initialize values
        Bundle args = getArguments();
        String title, defaultEntry;
        final String key;
        String[] entryValuesArray, entryKeysArray;
        if (args != null) {
            key = args.getString(ARG_KEY);
            title = args.getString(ARG_TITLE);
            defaultEntry = args.getString(ARG_DEFAULT);
            entryValuesArray = args.getStringArray(ARG_ENTRYVALUES);
            entryKeysArray = args.getStringArray(ARG_ENTRYKEYS);
        } else {
            return super.onCreateDialogView(context);
        }

        if (entryKeysArray == null || entryValuesArray == null)
            return super.onCreateDialogView(context);

        final ArrayList<String> entries = new ArrayList<>(Arrays.asList(entryValuesArray));
        final ArrayList<String> entryKeys = new ArrayList<>(Arrays.asList(entryKeysArray));

        // Set the title
        ((TextView) view.findViewById(R.id.pref_dialog_title)).setText(title);

        // Fetch the ListView and remove styling of elements
        ListView lv = view.findViewById(R.id.pref_dialog_listview);
        lv.setDivider(null);
        lv.setDividerHeight(0);

        int defaultSelection = entryKeys.indexOf(defaultEntry);

        // Set ListView adapter and click listener
        Adapter adapter = new Adapter(context, entries, defaultSelection);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((RadioButton) view.findViewById(R.id.pref_dialog_listview_button)).setChecked(true);
                String keySelected = entryKeys.get(position);

                mDialogClosedListener.onSelection(key, keySelected); // notify a selection was made
                dismiss(); // close the dialog
            }
        });

        (view.findViewById(R.id.pref_dialog_cancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
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

        private Adapter(Context context, ArrayList<String> entries, int defaultSelection) {
            super(context, R.layout.pref_list_dialog, entries);
            mCheckedItem = (defaultSelection >= 0 && defaultSelection < entries.size()) ? defaultSelection : 0;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.listview_item, parent, false);
            }

            // Set the text for the ListView item
            ((TextView) convertView.findViewById(R.id.pref_dialog_listview_text)).setText(getItem(position));

            // Determine whether the ListView item should be checked
            boolean shouldBeChecked = false;
            if (mCheckedItem == position) {
                shouldBeChecked = true;
            }

            ((RadioButton) convertView.findViewById(R.id.pref_dialog_listview_button)).setChecked(shouldBeChecked);
            return convertView;
        }
    }
}
