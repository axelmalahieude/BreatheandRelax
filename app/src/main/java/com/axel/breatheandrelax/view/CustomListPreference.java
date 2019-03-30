package com.axel.breatheandrelax.view;
import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.axel.breatheandrelax.R;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.DialogPreference;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.preference.PreferenceViewHolder;

public class CustomListPreference extends DialogPreference {

    private static String TAG = CustomListPreference.class.getSimpleName();

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
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
    }

    public static class CustomPreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat {
        public static CustomPreferenceDialogFragmentCompat createFragment(String key) {
            CustomPreferenceDialogFragmentCompat fragment = new CustomPreferenceDialogFragmentCompat();
            Bundle args = new Bundle(1);
            args.putString(ARG_KEY, key);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onDialogClosed(boolean positiveResult) {
            Log.d(TAG, "Success");
        }

        @Override
        protected View onCreateDialogView(Context context) {
            View view = View.inflate(context, R.layout.dialog_single_message, null);
            return view;
        }

        @Override
        protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
            builder.setTitle(null);
            builder.setPositiveButton(null, null);
            builder.setNegativeButton(null, null);
        }
    }
}
