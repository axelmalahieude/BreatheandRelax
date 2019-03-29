package com.axel.breatheandrelax.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.axel.breatheandrelax.R;

public class SingleMessageDialogFragment extends DialogFragment {

    // Data members
    private Context mContext;
    private DialogFinishedListener mListener;
    private String mMessage;
    private String mButtonLabel;
    private String mTitle;

    // Constants
    public static final String MESSAGE_KEY = "message";
    public static final String BUTTON_LABEL_KEY = "button";
    public static final String TITLE_KEY = "title";

    public interface DialogFinishedListener {
        void onDialogDismissed();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;

        try {
            mListener = (DialogFinishedListener) context;
        } catch(ClassCastException cce) {
            throw new ClassCastException(context.toString() + " must implement DialogFinishedListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setStyle(STYLE_NO_TITLE, R.style.dialog);
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mMessage = getArguments().getString(MESSAGE_KEY);
            mTitle = getArguments().getString(TITLE_KEY);
            mButtonLabel = getArguments().getString(BUTTON_LABEL_KEY);
        } else {
            mMessage = mTitle = mButtonLabel = "";
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        // Build the dialog
        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_single_message, null);
        if (mMessage.length() < 70) // greater than 70 characters
            (dialogView.findViewById(R.id.tv_dialog_message)).setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        ((TextView) dialogView.findViewById(R.id.tv_dialog_message)).setText(mMessage);
        ((TextView) dialogView.findViewById(R.id.tv_dialog_title)).setText(mTitle);

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setView(dialogView);

        Button button = dialogView.findViewById(R.id.bu_dismiss_dialog);
        button.setText(mButtonLabel);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onDialogDismissed();
                dismiss();
            }
        });

        // Create the dialog
        return builder.create();
    }
}
