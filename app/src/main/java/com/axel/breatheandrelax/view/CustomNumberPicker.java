package com.axel.breatheandrelax.view;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;

/**
 * Extends the NumberPicker to include useful attributes, such as min and max values.
 */
public class CustomNumberPicker extends NumberPicker {

    /**
     * Necessary constructor to override NumberPicker
     * @param context the parent activity's context
     */
    public CustomNumberPicker(Context context) {
        super(context);
    }

    /**
     * Necessary constructor to override NumberPicker
     * @param context the parent activity's context
     * @param attrs the XML attributes defining the view
     */
    public CustomNumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        setAttributes(attrs);
    }

    /**
     * Necessary constructor to override NumberPicker
     * @param context the parent activity's context
     * @param attrs the XML attributes defining the view
     * @param defStyleAttr the style for the XML attributes
     */
    public CustomNumberPicker (Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setAttributes(attrs);
    }

    /**
     * Sets custom attributes to properly set up the NumberPicker
     * @param attrs is the AttributeSet containing the attribute we want to set up
     */
    public void setAttributes(AttributeSet attrs) {
        // Fetch the attributes we set in the XML layout file
        String namespace = "http://schemas.android.com/apk/res-auto";
        int maxValue = attrs.getAttributeIntValue(namespace, "maxValue", 0);
        int minValue = attrs.getAttributeIntValue(namespace, "minValue", 0);
        boolean twoDigitNumbers = attrs.getAttributeBooleanValue(namespace, "twoDigitNumbers", false);

        // Quick error checking for valid attributes
        if (maxValue == minValue) return;
        if (minValue > maxValue) {
            int temp = minValue;
            minValue = maxValue;
            maxValue = temp;
        }

        // Apply attributes
        setMinValue(minValue);
        setMaxValue(maxValue);

        // Populate NumberPicker with numbers between minValue and maxValue
        String[] minutes = new String[maxValue - minValue + 1];
        for (int i = minValue, j = 0; i <= maxValue; i++, j++) {
            String number = String.valueOf(i);
            if (twoDigitNumbers && i < 10) // prepend 0 if attribute is set
                number = "0" + String.valueOf(i);
            minutes[j] = number;
        }
        setDisplayedValues(minutes);

        // Set the keyboard to the proper style (numbers only)
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child instanceof EditText) {
                ((EditText) child).setInputType(InputType.TYPE_CLASS_NUMBER);
                ((EditText) child).setCursorVisible(false);
                child.setBackgroundColor(0);
            }
        }
    }

    /**
     * Required to remove the gradient fade on the NumberPicker. Transparent removes it completely;
     * selecting a different color would change the fade to that color, which only works if the
     * background color never changes.
     * @return the color to fade the NumberPicker edges to
     */
    @Override
    public int getSolidColor() {
        return getResources().getColor(android.R.color.transparent);
    }
}
