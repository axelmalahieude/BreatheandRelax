package com.axel.breatheandrelax.view

import android.content.Context
import android.content.SharedPreferences
import android.content.res.TypedArray
import android.util.AttributeSet
import android.util.Log
import android.widget.SeekBar
import android.widget.TextView

import com.axel.breatheandrelax.R

import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder

class CustomSeekBarPreference : Preference {

    private var mTextViewValue: TextView? = null
    private var mSeekBar: SeekBar? = null
    private var mSharedPreferences: SharedPreferences? = null

    private var mTitle: String? = null
    private var mKey: String? = null
    private var mMaxVal: Int = 0

    private val TAG = CustomSeekBarPreference::class.java.simpleName

    var value: Int
        get() = if (mSeekBar == null)
            getDefaultValue()
        else
            mSeekBar!!.progress
        set(value) {
            if (mSeekBar == null || value < 0 || value > 10) return
            mSeekBar!!.progress = value
            mTextViewValue!!.text = value.toString()
            mSharedPreferences!!.edit().putInt(mKey, value).apply()
        }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        constructor(context, attrs, defStyleAttr)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        constructor(context, attrs, 0)
    }

    constructor(context: Context) : super(context) {
        constructor(context, null, 0)
    }

    /**
     * Collective constructor handling
     * @param context context for view
     * @param attrs attributes defined in XML for view
     * @param defStyleAttr theme reference
     */
    private fun constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        this.layoutResource = R.layout.pref_seekbar
        if (attrs != null)
            getAttributes(attrs)
    }

    /**
     * Gets attributes for this custom view based on XML definitions
     * @param attrs set of attributes to handle
     */
    private fun getAttributes(attrs: AttributeSet) {
        val androidNamespace = "http://schemas.android.com/apk/res/android"

        // Get custom attributes
        val styledAttributes = context.obtainStyledAttributes(attrs, R.styleable.CustomSeekBarPreference)
        mMaxVal = styledAttributes.getInt(R.styleable.CustomSeekBarPreference_max, 0)
        styledAttributes.recycle()

        // Get builtin Android attributes
        val titleResourceID = attrs.getAttributeResourceValue(androidNamespace, "title", 0)
        mTitle = context.resources.getString(titleResourceID)
        val keyResourceID = attrs.getAttributeResourceValue(androidNamespace, "key", 0)
        mKey = context.resources.getString(keyResourceID)

    }


    /**
     * Binds elements to view. Used to control XML elements of this view
     * @param holder view holder
     */
    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        mSeekBar = holder.findViewById(R.id.seekbar) as SeekBar
        mTextViewValue = holder.findViewById(R.id.tv_seekbar_value) as TextView
        val label = holder.findViewById(R.id.tv_seekbar_title) as TextView

        mSharedPreferences = this.sharedPreferences
        val currVal = mSharedPreferences!!.getInt(mKey, getDefaultValue())

        label.text = mTitle
        mSeekBar!!.max = mMaxVal
        mSeekBar!!.progress = currVal
        mTextViewValue!!.text = currVal.toString()

        // Dynamically updates the tooltip to reflect the new value of the SeekBar
        mSeekBar!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                mTextViewValue!!.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                mSharedPreferences!!.edit().putInt(mKey, seekBar.progress).apply()
            }
        })
    }

    private fun getDefaultValue() : Int {
        return when (mKey) {
            context.resources.getString(R.string.pref_seekbar_inhale_key) -> context.resources.getInteger(R.integer.inhale_default)
            context.resources.getString(R.string.pref_seekbar_exhale_key) -> context.resources.getInteger(R.integer.exhale_default)
            context.resources.getString(R.string.pref_seekbar_hold_key)   -> context.resources.getInteger(R.integer.hold_default)
            context.resources.getString(R.string.pref_seekbar_pause_key)  -> context.resources.getInteger(R.integer.pause_default)
            else -> 1
        }
    }
}