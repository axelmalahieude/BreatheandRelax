package com.axel.breatheandrelax.view

import android.content.Context
import android.content.SharedPreferences
import android.content.res.TypedArray
import android.preference.PreferenceManager
import android.util.AttributeSet
import android.util.Log

import com.axel.breatheandrelax.R

import java.util.ArrayList
import java.util.Arrays

import androidx.preference.DialogPreference

class CustomListPreference : DialogPreference {

    private var mEntryKeys: Array<CharSequence>? = null
    private var mEntryValues: Array<CharSequence>? = null
    private var mDefaultEntry: String? = null
    private var mKey: String? = null // key for the preference, so we know which SharedPreference to update

    val entryValues: List<String>
        get() {
            val entries = mutableListOf<String>()
            for (i in mEntryKeys!!.indices)
                entries.add(mEntryKeys!![i].toString())
            return entries
        }

    val entryKeys: List<String>
        get() {
            val entryValues = mutableListOf<String>()
            for (i in mEntryValues!!.indices)
                entryValues.add(mEntryValues!![i].toString())
            return entryValues
        }

    /**
     * Get the entry in the ListPreference that is currently selected, or the default if none is selected
     * @return the selected entry
     */
    var value: String?
        get() {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            return sharedPreferences.getString(mKey, mDefaultEntry)
        }
        set(newValue) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            sharedPreferences.edit().putString(mKey, newValue).apply()

        }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        constructor(attrs)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        constructor(attrs)
    }

    constructor(context: Context) : super(context) {
        constructor(null)
    }

    /**
     * Common constructor called by each overloaded constructor
     * @param attrs XML attributes, which may not be present
     */
    private fun constructor(attrs: AttributeSet?) {
        val stylizedAttributes = context.resources.obtainAttributes(attrs, R.styleable.CustomListPreference)
        try {
            mDefaultEntry = stylizedAttributes.getText(R.styleable.CustomListPreference_android_defaultValue).toString()
            mEntryKeys = stylizedAttributes.getTextArray(R.styleable.CustomListPreference_android_entries)
            mEntryValues = stylizedAttributes.getTextArray(R.styleable.CustomListPreference_android_entryValues)
            mKey = stylizedAttributes.getText(R.styleable.CustomListPreference_android_key).toString()
        } finally {
            stylizedAttributes.recycle()
        }
    }

    fun setSummary() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val selection = sharedPreferences.getString(mKey, mDefaultEntry)!!
        summary = entryValues[entryKeys.indexOf(selection)]
    }
}
