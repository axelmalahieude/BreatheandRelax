package com.axel.breatheandrelax.view

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.RadioButton
import android.widget.TextView

import com.axel.breatheandrelax.R

import java.util.ArrayList
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceDialogFragmentCompat

class CustomPreferenceDialog : PreferenceDialogFragmentCompat() {

    companion object {

        const val ARG_TITLE = "title"
        const val ARG_ENTRYVALUES = "entries"
        const val ARG_ENTRYKEYS = "entryvalues"
        const val ARG_DEFAULT = "def"

        fun createInstance(preference: CustomListPreference): CustomPreferenceDialog {
            val fragment = CustomPreferenceDialog()
            val args = Bundle()
            args.putString(ARG_KEY, preference.key)
            args.putString(ARG_TITLE, preference.title.toString())
            args.putStringArray(ARG_ENTRYVALUES, preference.entryValues.toTypedArray())
            args.putStringArray(ARG_ENTRYKEYS, preference.entryKeys.toTypedArray())
            args.putString(ARG_DEFAULT, preference.value)
            fragment.arguments = args
            return fragment
        }
    }

    // Interface to pass data back to the Custom Preference that uses this PreferenceDialog
    private var mDialogClosedListener: DialogClosedListener? = null

    interface DialogClosedListener {
        fun onSelection(key: String, newValue: String)
    }

    fun setDialogClosedListener(dcl: DialogClosedListener) {
        mDialogClosedListener = dcl
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        dismiss() // close dialog
    }

    override fun onCreateDialogView(context: Context): View {
        val view = View.inflate(context, R.layout.pref_list_dialog, null)

        // Fetch arguments and initialize values
        val args = arguments
        val title: String
        val defaultEntry: String
        val key: String
        val entryValuesArray: Array<String>
        val entryKeysArray: Array<String>
        if (args != null) {
            key = args.getString(ARG_KEY) ?: ""
            title = args.getString(ARG_TITLE) ?: ""
            defaultEntry = args.getString(ARG_DEFAULT) ?: ""
            entryValuesArray = args.getStringArray(ARG_ENTRYVALUES) ?: arrayOf()
            entryKeysArray = args.getStringArray(ARG_ENTRYKEYS) ?: arrayOf()
        } else {
            return super.onCreateDialogView(context)
        }

        if (entryKeysArray.isNullOrEmpty() || entryValuesArray.isNullOrEmpty())
            return super.onCreateDialogView(context)

        val entries = ArrayList(entryValuesArray.asList())
        val entryKeys = ArrayList(entryKeysArray.asList())

        // Set the title
        (view.findViewById<View>(R.id.pref_dialog_title) as TextView).text = title

        // Fetch the ListView and remove styling of elements
        val lv = view.findViewById<ListView>(R.id.pref_dialog_listview)
        lv.divider = null
        lv.dividerHeight = 0

        val defaultSelection = entryKeys.indexOf(defaultEntry)

        // Set ListView adapter and click listener
        val adapter = Adapter(context, entries, defaultSelection)
        lv.adapter = adapter
        lv.onItemClickListener = AdapterView.OnItemClickListener { _, viewClicked, position, _ ->
            (viewClicked.findViewById<View>(R.id.pref_dialog_listview_button) as RadioButton).isChecked = true
            val keySelected = entryKeys[position]

            mDialogClosedListener!!.onSelection(key, keySelected) // notify a selection was made
            dismiss() // close the dialog
        }

        view.findViewById<View>(R.id.pref_dialog_cancel).setOnClickListener { dismiss() }

        return view
    }

    /**
     * Override required to remove default buttons and title. This allows for custom theming using
     * an XML layout.
     * @param builder dialog builder to manipulate
     */
    override fun onPrepareDialogBuilder(builder: AlertDialog.Builder) {
        builder.setTitle(null)
        builder.setPositiveButton(null, null)
        builder.setNegativeButton(null, null)
    }

    /**
     * Extend the ArrayAdapter to have our custom view in the ListView
     */
    private inner class Adapter constructor(context: Context, entries: ArrayList<String>, defaultSelection: Int) : ArrayAdapter<String>(context, R.layout.pref_list_dialog, entries) {

        private val mCheckedItem = if (defaultSelection >= 0 && defaultSelection < entries.size) defaultSelection else 0

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: layoutInflater.inflate(R.layout.listview_item, parent, false)

            // Set the text for the ListView item
            (view!!.findViewById<View>(R.id.pref_dialog_listview_text) as TextView).text = getItem(position)

            // Determine whether the ListView item should be checked
            var shouldBeChecked = false
            if (mCheckedItem == position) {
                shouldBeChecked = true
            }

            (view.findViewById<View>(R.id.pref_dialog_listview_button) as RadioButton).isChecked = shouldBeChecked
            return view
        }
    }
}
