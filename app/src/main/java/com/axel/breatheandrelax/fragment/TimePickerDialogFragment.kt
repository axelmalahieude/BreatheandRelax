package com.axel.breatheandrelax.fragment

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View

import com.axel.breatheandrelax.R
import com.axel.breatheandrelax.view.CustomNumberPicker

class TimePickerDialogFragment : DialogFragment() {

    // Data members
    private var mContext: Context? = null
    private var mListener: TimePickerDialogListener? = null

    // View references
    private var mMinutePicker: CustomNumberPicker? = null
    private var mSecondPicker: CustomNumberPicker? = null

    companion object {
        // Bundle variable codes
        const val START_TIME_CODE = "starting_time"
    }

    interface TimePickerDialogListener {
        fun setStopWatchTime(minutes: Int, seconds: Int)
        fun onDialogCancelled()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mContext = context

        try {
            mListener = context as TimePickerDialogListener?
        } catch (cce: ClassCastException) {
            throw ClassCastException(context!!.toString() + " must implement TimePickerDialogListener")
        }

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        // Inflate the layout and attach the view references
        val inflater = (mContext as Activity).layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_time_picker, null)
        mMinutePicker = dialogView.findViewById(R.id.minutePicker)
        mSecondPicker = dialogView.findViewById(R.id.secondPicker)

        // Fetch arguments and set default values
        val args = arguments
        if (args != null && args.containsKey(START_TIME_CODE)) {
            var milliseconds = args.getLong(START_TIME_CODE)
            if (milliseconds < 1000) milliseconds = (60 * 1000).toLong() // at least one minute if time is under a second
            val minutes = milliseconds.toInt() / 1000 / 60
            val seconds = (if (minutes > 0) milliseconds / 1000 % (60 * minutes) else milliseconds / 1000).toInt()
            mMinutePicker!!.value = minutes
            mSecondPicker!!.value = seconds
        }

        // Build the dialog
        val builder = AlertDialog.Builder(mContext!!)
        builder.setView(dialogView)

        // Set confirm and cancel buttons
        dialogView.findViewById<View>(R.id.bu_confirm).setOnClickListener {
            mListener!!.setStopWatchTime(mMinutePicker!!.value, mSecondPicker!!.value)
            dismiss()
        }
        dialogView.findViewById<View>(R.id.bu_cancel).setOnClickListener {
            mListener!!.onDialogCancelled()
            dismiss()
        }

        // Create the dialog
        return builder.create()
    }

    override fun onCancel(dialog: DialogInterface?) {
        super.onCancel(dialog)
        mListener!!.onDialogCancelled()
    }
}
