package com.axel.breatheandrelax.fragment

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceGroupAdapter
import androidx.preference.PreferenceScreen
import androidx.preference.PreferenceViewHolder
import androidx.recyclerview.widget.RecyclerView

import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter

import com.axel.breatheandrelax.R
import com.axel.breatheandrelax.view.ColorPickerPreference
import com.axel.breatheandrelax.view.CustomListPreference
import com.axel.breatheandrelax.view.CustomPreferenceDialog
import com.axel.breatheandrelax.view.CustomSeekBarPreference

/**
 * SettingsFragment contained within SettingsActivity. Takes care of all preference-related
 * operations.
 */

class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    private var mAnimationPreference: CustomListPreference? = null
    private var mBreathingPresetPreference: CustomListPreference? = null

    /**
     * Populates the fragment with the proper set of preferences
     * @param bundle data arguments
     * @param s pref name
     */
    override fun onCreatePreferences(bundle: Bundle?, s: String?) {
        addPreferencesFromResource(R.xml.pref)
        mAnimationPreference = findPreference(resources.getString(R.string.pref_animation_style_key))
        mBreathingPresetPreference = findPreference(resources.getString(R.string.pref_list_breath_style_key))
        mAnimationPreference!!.setSummary()
        mBreathingPresetPreference!!.setSummary()
    }

    /**
     * Unregister the SharedPreferenceChangeListener when the activity goes into the background.
     */
    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences
                .unregisterOnSharedPreferenceChangeListener(this)
    }

    /**
     * Register the SharedPreferenceChangeListener to properly adapt to changes in settings
     * while the activity is in the foreground.
     */
    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences
                .registerOnSharedPreferenceChangeListener(this)
        // Check for proper sync between ListPreference and SeekBarPreferences
        onSharedPreferenceChanged(preferenceScreen.sharedPreferences,
                resources.getString(R.string.pref_seekbar_inhale_key))
    }

    /**
     * Override to remove strange indentation in left margin of each preference.
     * Created with solution from https://stackoverflow.com/questions/18509369/android-how-to-get-remove-margin-padding-in-preference-screen.
     * @param preferenceScreen is the preference screen to manipulate
     * @return the preference adapter
     */
    @SuppressLint("RestrictedAPI")
    override fun onCreateAdapter(preferenceScreen: PreferenceScreen): RecyclerView.Adapter<*> {
        return object : PreferenceGroupAdapter(preferenceScreen) {
            override fun onBindViewHolder(holder: PreferenceViewHolder, position: Int, payloads: List<Any>) {
                super.onBindViewHolder(holder, position, payloads)
                val preference = getItem(position)
                if (preference is PreferenceCategory)
                    setZeroPaddingToLayoutChildren(holder.itemView)
                else {
                    val iconFrame = holder.itemView.findViewById<View>(R.id.icon_frame)
                    if (iconFrame != null)
                        iconFrame.visibility = if (preference.icon == null) View.GONE else View.VISIBLE
                }

            }
        }
    }

    /**
     * Used as a part of onCreateAdapter to remove strange left margin.
     * @param view the parent view to remove padding from
     */
    private fun setZeroPaddingToLayoutChildren(view: View) {
        if (view !is ViewGroup)
            return
        val childCount = view.childCount

        // Remove padding from each child
        for (i in 0 until childCount) {
            setZeroPaddingToLayoutChildren(view.getChildAt(i))
            view.setPaddingRelative(0, view.paddingTop, view.paddingEnd, view.paddingBottom)
        }
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (preference is CustomListPreference) {
            val df = CustomPreferenceDialog.createInstance(preference)
            df.setTargetFragment(this, 0)
            if (fragmentManager != null) {
                df.setDialogClosedListener( object : CustomPreferenceDialog.DialogClosedListener {
                    override fun onSelection(key: String, newValue: String) {
                        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                        sharedPreferences.edit().putString(key, newValue).apply()
                    }
                })
                df.show(fragmentManager!!, null)
            }
        } else
            super.onDisplayPreferenceDialog(preference)

    }

    /**
     * Listens for changes in preferences to update the display.
     * @param sharedPreferences the set of preferences to read from
     * @param key the preference that was changed
     */
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == resources.getString(R.string.pref_animation_style_key)) {
            mAnimationPreference!!.setSummary()
        } else if (key == resources.getString(R.string.pref_list_breath_style_key)) {
            mBreathingPresetPreference!!.setSummary()
        }

        // This block listens for changes in the ListPreference and changes the SeekBarPreferences
        // according to which preset the user chose
        if (key == resources.getString(R.string.pref_list_breath_style_key)) {
            val listPreference = findPreference<CustomListPreference>(key)
            val currentValue = listPreference.value
            val inhaleSeekBar = preferenceManager.findPreference<CustomSeekBarPreference>(resources.getString(R.string.pref_seekbar_inhale_key))
            val exhaleSeekBar = preferenceManager.findPreference<CustomSeekBarPreference>(resources.getString(R.string.pref_seekbar_exhale_key))
            val holdSeekBar = preferenceManager.findPreference<CustomSeekBarPreference>(resources.getString(R.string.pref_seekbar_hold_key))
            val pauseSeekBar = preferenceManager.findPreference<CustomSeekBarPreference>(resources.getString(R.string.pref_seekbar_pause_key))
            when (currentValue) {
                resources.getString(R.string.pref_list_uplifting_breathing_value) -> {
                    inhaleSeekBar.value = resources.getInteger(R.integer.inhale_uplifting_default)
                    exhaleSeekBar.value = resources.getInteger(R.integer.exhale_uplifting_default)
                    holdSeekBar.value = resources.getInteger(R.integer.hold_uplifting_default)
                    pauseSeekBar.value = resources.getInteger(R.integer.pause_uplifting_default)
                }
                resources.getString(R.string.pref_list_relaxing_breathing_value) -> {
                    inhaleSeekBar.value = resources.getInteger(R.integer.inhale_relaxing_default)
                    exhaleSeekBar.value = resources.getInteger(R.integer.exhale_relaxing_default)
                    holdSeekBar.value = resources.getInteger(R.integer.hold_relaxing_default)
                    pauseSeekBar.value = resources.getInteger(R.integer.pause_relaxing_default)
                }
                resources.getString(R.string.pref_list_meditative_breathing_value) -> {
                    inhaleSeekBar.value = resources.getInteger(R.integer.inhale_meditative_default)
                    exhaleSeekBar.value = resources.getInteger(R.integer.exhale_meditative_default)
                    holdSeekBar.value = resources.getInteger(R.integer.hold_meditative_default)
                    pauseSeekBar.value = resources.getInteger(R.integer.pause_meditative_default)
                }
            }
            // This block listens for changes in any of the SeekBars and changes the ListPreference
        } else if (key == resources.getString(R.string.pref_seekbar_inhale_key) ||
                key == resources.getString(R.string.pref_seekbar_exhale_key) ||
                key == resources.getString(R.string.pref_seekbar_hold_key) ||
                key == resources.getString(R.string.pref_seekbar_pause_key)) {
            val listPreference = preferenceManager
                    .findPreference<CustomListPreference>(resources.getString(R.string.pref_list_breath_style_key))
            val inhaleTime = (preferenceManager.findPreference<Preference>(
                    resources.getString(R.string.pref_seekbar_inhale_key)) as CustomSeekBarPreference).value
            val exhaleTime = (preferenceManager.findPreference<Preference>(
                    resources.getString(R.string.pref_seekbar_exhale_key)) as CustomSeekBarPreference).value
            val holdTime = (preferenceManager.findPreference<Preference>(
                    resources.getString(R.string.pref_seekbar_hold_key)) as CustomSeekBarPreference).value
            val pauseTime = (preferenceManager.findPreference<Preference>(
                    resources.getString(R.string.pref_seekbar_pause_key)) as CustomSeekBarPreference).value
            // This block listens for changes in the preset options and signals an update to
            // the SeekBars appropriately
            listPreference.value = if (inhaleTime == resources.getInteger(R.integer.inhale_uplifting_default) &&
                    exhaleTime == resources.getInteger(R.integer.exhale_uplifting_default) &&
                    holdTime == resources.getInteger(R.integer.hold_uplifting_default) &&
                    pauseTime == resources.getInteger(R.integer.pause_uplifting_default)) {
                resources.getString(R.string.pref_list_uplifting_breathing_value)
            } else if (inhaleTime == resources.getInteger(R.integer.inhale_relaxing_default) &&
                    exhaleTime == resources.getInteger(R.integer.exhale_relaxing_default) &&
                    holdTime == resources.getInteger(R.integer.hold_relaxing_default) &&
                    pauseTime == resources.getInteger(R.integer.pause_relaxing_default)) {
                resources.getString(R.string.pref_list_relaxing_breathing_value)
            } else if (inhaleTime == resources.getInteger(R.integer.inhale_meditative_default) &&
                    exhaleTime == resources.getInteger(R.integer.exhale_meditative_default) &&
                    holdTime == resources.getInteger(R.integer.hold_meditative_default) &&
                    pauseTime == resources.getInteger(R.integer.pause_meditative_default)) {
                resources.getString(R.string.pref_list_meditative_breathing_value)
            } else {
                resources.getString(R.string.pref_list_custom_breathing_value)
            }
        }
    }
}
