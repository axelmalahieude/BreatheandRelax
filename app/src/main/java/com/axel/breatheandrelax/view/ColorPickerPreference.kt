package com.axel.breatheandrelax.view

import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.content.res.Resources
import android.content.res.TypedArray
import android.util.AttributeSet
import android.util.Log
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import androidx.core.content.ContextCompat

import com.axel.breatheandrelax.R
import com.axel.breatheandrelax.util.Tools

import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder

class ColorPickerPreference : Preference {

    // Views
    private var mSummary: TextView? = null
    private var mCb1: CheckBox? = null
    private var mCb2: CheckBox? = null
    private var mCb3: CheckBox? = null
    private var mCb4: CheckBox? = null

    // Data received from XML attributes
    private var mTitle: String? = null
    private var mKey: String? = null
    private var mDefaultColor: Int = 0
    private var mColors: Array<CharSequence>? = null

    // Debugging
    private val TAG = ColorPickerPreference::class.java.simpleName

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        constructor(context, attrs, defStyleAttr)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        constructor(context, attrs, 0)
    }

    constructor(context: Context) : super(context) {
        constructor(context, null, 0)
    }

    private fun constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        this.layoutResource = R.layout.pref_color_picker
        if (attrs != null)
            getAttributes(attrs)
    }

    private fun getAttributes(attrs: AttributeSet) {
        // Get custom attributes
        val stylizedAttributes = context.obtainStyledAttributes(attrs, R.styleable.ColorPickerPreference)
        try {
            mTitle = stylizedAttributes.getString(R.styleable.ColorPickerPreference_android_title)
            mKey = stylizedAttributes.getString(R.styleable.ColorPickerPreference_android_key)
            mDefaultColor = stylizedAttributes.getColor(R.styleable.ColorPickerPreference_android_defaultValue, getDefaultValue())
            mColors = stylizedAttributes.getTextArray(R.styleable.ColorPickerPreference_android_entries)
        } finally {
            stylizedAttributes.recycle()
        }
    }

    override fun setSummary(currentColor: Int) {
        val summary = Tools.intColorToString(context.resources, currentColor)
        if (mSummary != null)
            mSummary!!.text = summary
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        // Fetch references for the views in this preference
        val title = holder.findViewById(R.id.pref_cb_title) as TextView
        mSummary = holder.findViewById(R.id.pref_color_summary) as TextView
        mCb1 = holder.findViewById(R.id.pref_cb_1) as CheckBox
        mCb2 = holder.findViewById(R.id.pref_cb_2) as CheckBox
        mCb3 = holder.findViewById(R.id.pref_cb_3) as CheckBox
        mCb4 = holder.findViewById(R.id.pref_cb_4) as CheckBox

        val checkBoxes = arrayOf(mCb1!!, mCb2!!, mCb3!!, mCb4!!)

        // Check the default color
        //TODO: Properly check for defaults if first time use. Otherwise no color is chosen
        val sharedPreferences = this.sharedPreferences
        val currentColor = sharedPreferences.getString(mKey, Tools.intColorToKey(context.resources, mDefaultColor))

        // Set the checkboxes to be colored according to which color they select
        val states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf(-android.R.attr.state_checked))
        for (i in checkBoxes.indices) {
            if (mColors!![i].toString() == currentColor)
                checkBoxes[i].isChecked = true
            val color = Tools.colorKeyToInt(context.resources, mColors!![i].toString())
            val colorArray = intArrayOf(color, color)
            checkBoxes[i].buttonTintList = ColorStateList(states, colorArray)
        }

        title.text = mTitle
        setSummary(Tools.colorKeyToInt(context.resources, currentColor))

        // Set the listeners for when a checkbox is clicked
        for (checkBox in checkBoxes) {
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                // Make sure only this checkbox is checked by unchecking all other checkboxes
                if (isChecked) {
                    for (c in checkBoxes) {
                        if (c !== checkBox)
                            c.isChecked = false
                    }

                    // Apply the new color to SharedPreferences
                    val color = try {
                        checkBox.buttonTintList!!.getColorForState(intArrayOf(android.R.attr.state_checked), 0)
                    } catch (e: NullPointerException) {
                        0
                    }

                    setSummary(color)
                    sharedPreferences.edit().putString(mKey, Tools.intColorToKey(context.resources, color)).apply()
                } else if (allNotChecked()) {
                    // Make sure this checkbox stays checked if it was already checked
                    checkBox.isChecked = true
                }
            }
        }

    }

    /**
     * Determine whether no checkboxes are checked
     * @return true if no checkboxes are checked, false if 1 or more are checked
     */
    private fun allNotChecked(): Boolean {
        return !mCb1!!.isChecked && !mCb2!!.isChecked && !mCb3!!.isChecked && !mCb4!!.isChecked
    }

    private fun getDefaultValue() : Int {
        return when(mKey) {
            context.resources.getString(R.string.pref_colors_inhale_key) -> ContextCompat.getColor(context, R.color.inhale_default_color)
            context.resources.getString(R.string.pref_colors_exhale_key) -> ContextCompat.getColor(context, R.color.exhale_default_color)
            else -> ContextCompat.getColor(context, R.color.inhale_default_color)
        }
    }
}
