package com.axel.breatheandrelax.util;

import android.content.res.Resources;

import com.axel.breatheandrelax.R;

public class  Tools {

    /**
     * Converts a resource String to the associated color
     * @param colorString the String to convert (found in strings.xml)
     * @return an integer representing colorString's associated color in hexadecimal
     */
    public static int colorKeyToInt(Resources resources, String colorString) {
        if (colorString.equals(resources.getString(R.string.pref_colors_red_value)))
            return resources.getColor(R.color.breathe_red);
        if (colorString.equals(resources.getString(R.string.pref_colors_green_value)))
            return resources.getColor(R.color.breathe_green);
        if (colorString.equals(resources.getString(R.string.pref_colors_blue_value)))
            return resources.getColor(R.color.breathe_blue);
        if (colorString.equals(resources.getString(R.string.pref_colors_yellow_value)))
            return resources.getColor(R.color.breathe_yellow);
        else return -1;
    }

    public static String intColorToKey(Resources resources, int color) {
        if (color == resources.getColor(R.color.breathe_red))
            return resources.getString(R.string.pref_colors_red_value);
        if (color == resources.getColor(R.color.breathe_green))
            return resources.getString(R.string.pref_colors_green_value);
        if (color == resources.getColor(R.color.breathe_blue))
            return resources.getString(R.string.pref_colors_blue_value);
        if (color == resources.getColor(R.color.breathe_yellow))
            return resources.getString(R.string.pref_colors_yellow_value);
        else return "";
    }

    public static String intColorToString(Resources resources, int color) {
        if (color == resources.getColor(R.color.breathe_red))
            return resources.getString(R.string.pref_colors_red);
        if (color == resources.getColor(R.color.breathe_green))
            return resources.getString(R.string.pref_colors_green);
        if (color == resources.getColor(R.color.breathe_blue))
            return resources.getString(R.string.pref_colors_blue);
        if (color == resources.getColor(R.color.breathe_yellow))
            return resources.getString(R.string.pref_colors_yellow);
        else return "";
    }
}
