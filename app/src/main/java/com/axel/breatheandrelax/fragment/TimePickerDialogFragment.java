package com.axel.breatheandrelax.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import com.axel.breatheandrelax.R;
import com.axel.breatheandrelax.view.CustomNumberPicker;

public class TimePickerDialogFragment extends DialogFragment {

    // Data members
    private Context mContext;
    private TimePickerDialogListener mListener;

    // View references
    private CustomNumberPicker mMinutePicker;
    private CustomNumberPicker mSecondPicker;

    // Bundle variable codes
    public static final String START_TIME_CODE = "starting_time";

    public interface TimePickerDialogListener {
        void setStopWatchTime(int minutes, int seconds);
        void onDialogCancelled();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;

        try {
            mListener = (TimePickerDialogListener) context;
        } catch(ClassCastException cce) {
            throw new ClassCastException(context.toString() + " must implement TimePickerDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        // Inflate the layout and attach the view references
        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_time_picker, null);
        mMinutePicker = dialogView.findViewById(R.id.minutePicker);
        mSecondPicker = dialogView.findViewById(R.id.secondPicker);

        // Fetch arguments and set default values
        Bundle args = getArguments();
        if (args != null && args.containsKey(START_TIME_CODE)) {
            long milliseconds = args.getLong(START_TIME_CODE);
            if (milliseconds < 1000) milliseconds = 60 * 1000; // at least one minute if time is under a second
            int minutes = (int) milliseconds / 1000 / 60;
            int seconds = (int) ((minutes > 0) ? (milliseconds / 1000) % (60 * minutes) : milliseconds / 1000);
            mMinutePicker.setValue(minutes);
            mSecondPicker.setValue(seconds);
        }

        // Build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setView(dialogView);

        // Set confirm and cancel buttons
        dialogView.findViewById(R.id.bu_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.setStopWatchTime(mMinutePicker.getValue(), mSecondPicker.getValue());
                dismiss();
            }
        });
        dialogView.findViewById(R.id.bu_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onDialogCancelled();
                dismiss();
            }
        });

        // Create the dialog
        return builder.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        mListener.onDialogCancelled();
    }
}
