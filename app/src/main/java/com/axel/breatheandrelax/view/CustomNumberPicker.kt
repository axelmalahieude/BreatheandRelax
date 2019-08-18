package com.axel.breatheandrelax.view

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.NumberPicker

/**
 * Extends the NumberPicker to include useful attributes, such as min and max values.
 */
class CustomNumberPicker : NumberPicker {

    /**
     * Necessary constructor to override NumberPicker
     * @param context the parent activity's context
     */
    constructor(context: Context) : super(context)

    /**
     * Necessary constructor to override NumberPicker
     * @param context the parent activity's context
     * @param attrs the XML attributes defining the view
     */
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setAttributes(attrs)
    }

    /**
     * Necessary constructor to override NumberPicker
     * @param context the parent activity's context
     * @param attrs the XML attributes defining the view
     * @param defStyleAttr the style for the XML attributes
     */
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setAttributes(attrs)
    }

    /**
     * Sets custom attributes to properly set up the NumberPicker
     * @param attrs is the AttributeSet containing the attribute we want to set up
     */
    private fun setAttributes(attrs: AttributeSet) {
        // Fetch the attributes we set in the XML layout file
        val namespace = "http://schemas.android.com/apk/res-auto"
        var maxValue = attrs.getAttributeIntValue(namespace, "maxValue", 0)
        var minValue = attrs.getAttributeIntValue(namespace, "minValue", 0)
        val twoDigitNumbers = attrs.getAttributeBooleanValue(namespace, "twoDigitNumbers", false)

        // Quick error checking for valid attributes
        if (maxValue == minValue) {
            return
        } else if (minValue > maxValue) {
            val temp = minValue
            minValue = maxValue
            maxValue = temp
        }

        // Apply attributes
        setMinValue(minValue)
        setMaxValue(maxValue)

        // Populate NumberPicker with numbers between minValue and maxValue
        val minutes = arrayOfNulls<String>(maxValue - minValue + 1)
        run {
            var i = minValue
            var j = 0
            while (i <= maxValue) {
                val number = if (twoDigitNumbers && i < 10) {
                    "0$i"
                } else {
                    "$i"
                }
                minutes[j] = number
                i++
                j++
            }
        }
        displayedValues = minutes

        // Find and set the keyboard to the proper style (numbers only)
        val count = childCount
        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child is EditText) {
                child.inputType = InputType.TYPE_CLASS_NUMBER
                child.isCursorVisible = false
                child.setBackgroundColor(0)
            }
        }
    }

    /**
     * Required to remove the gradient fade on the NumberPicker. Transparent removes it completely;
     * selecting a different color would change the fade to that color, which only works if the
     * background color never changes.
     * @return the color to fade the NumberPicker edges to
     */
    override fun getSolidColor(): Int {
        return resources.getColor(android.R.color.transparent)
    }
}
