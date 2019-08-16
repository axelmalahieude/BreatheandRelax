package com.axel.breatheandrelax.fragment

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView

import com.axel.breatheandrelax.R

class SingleMessageDialogFragment : DialogFragment() {

    // Data members
    private var mContext: Context? = null
    private var mListener: DialogFinishedListener? = null
    private var mMessage: String? = null
    private var mButtonLabel: String? = null
    private var mTitle: String? = null

    interface DialogFinishedListener {
        fun onDialogDismissed()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mContext = context

        try {
            mListener = context as DialogFinishedListener?
        } catch (cce: ClassCastException) {
            throw ClassCastException(context!!.toString() + " must implement DialogFinishedListener")
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.dialog)
        super.onCreate(savedInstanceState)

        if (arguments != null) {
            mMessage = arguments!!.getString(MESSAGE_KEY)
            mTitle = arguments!!.getString(TITLE_KEY)
            mButtonLabel = arguments!!.getString(BUTTON_LABEL_KEY)
        } else {
            mButtonLabel = ""
            mTitle = mButtonLabel
            mMessage = mTitle
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        // Build the dialog
        val inflater = (mContext as Activity).layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_single_message, null)
        if (mMessage!!.length < 70)
        // greater than 70 characters
            dialogView.findViewById<View>(R.id.tv_dialog_message).textAlignment = View.TEXT_ALIGNMENT_CENTER
        (dialogView.findViewById<View>(R.id.tv_dialog_message) as TextView).text = mMessage
        (dialogView.findViewById<View>(R.id.tv_dialog_title) as TextView).text = mTitle

        val builder = AlertDialog.Builder(mContext!!)
        builder.setView(dialogView)

        val button = dialogView.findViewById<Button>(R.id.bu_dismiss_dialog)
        button.text = mButtonLabel
        button.setOnClickListener {
            mListener!!.onDialogDismissed()
            dismiss()
        }

        // Create the dialog
        return builder.create()
    }

    companion object {

        // Constants
        val MESSAGE_KEY = "message"
        val BUTTON_LABEL_KEY = "button"
        val TITLE_KEY = "title"
    }
}
