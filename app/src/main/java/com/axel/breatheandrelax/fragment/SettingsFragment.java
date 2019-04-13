package com.axel.breatheandrelax.fragment;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroupAdapter;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceViewHolder;
import androidx.recyclerview.widget.RecyclerView;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.axel.breatheandrelax.R;
import com.axel.breatheandrelax.view.ColorPickerPreference;
import com.axel.breatheandrelax.view.CustomListPreference;
import com.axel.breatheandrelax.view.CustomPreferenceDialog;
import com.axel.breatheandrelax.view.CustomSeekBarPreference;

import java.util.List;

/**
 * SettingsFragment contained within SettingsActivity. Takes care of all preference-related
 * operations.
 */

public class SettingsFragment extends PreferenceFragmentCompat
    implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final static String TAG = SettingsFragment.class.getSimpleName();

    private CustomListPreference mAnimationPreference;

    /**
     * Populates the fragment with the proper set of preferences
     * @param bundle data arguments
     * @param s pref name
     */
    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.pref);
        mAnimationPreference = findPreference(getResources().getString(R.string.pref_animation_style_key));
        mAnimationPreference.setSummary();
    }

    /**
     * Unregister the SharedPreferenceChangeListener when the activity goes into the background.
     */
    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * Register the SharedPreferenceChangeListener to properly adapt to changes in settings
     * while the activity is in the foreground.
     */
    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
        // Check for proper sync between ListPreference and SeekBarPreferences
        onSharedPreferenceChanged(getPreferenceScreen().getSharedPreferences(),
                getResources().getString(R.string.pref_seekbar_inhale_key));
    }

    /**
     * Override to remove strange indentation in left margin of each preference.
     * Created with solution from https://stackoverflow.com/questions/18509369/android-how-to-get-remove-margin-padding-in-preference-screen.
     * @param preferenceScreen is the preference screen to manipulate
     * @return the preference adapter
     */
    @Override
    protected RecyclerView.Adapter onCreateAdapter(PreferenceScreen preferenceScreen) {
        return new PreferenceGroupAdapter(preferenceScreen) {
            @SuppressLint("RestrictedAPI")
            @Override
            public void onBindViewHolder(@NonNull PreferenceViewHolder holder, int position, @NonNull List<Object> payloads) {
                super.onBindViewHolder(holder, position, payloads);
                Preference preference = getItem(position);
                if (preference instanceof PreferenceCategory)
                    setZeroPaddingToLayoutChildren(holder.itemView);
                else {
                    View iconFrame = holder.itemView.findViewById(R.id.icon_frame);
                    if (iconFrame != null)
                        iconFrame.setVisibility(preference.getIcon() == null ? View.GONE : View.VISIBLE);
                }

            }
        };
    }

    /**
     * Used as a part of onCreateAdapter to remove strange left margin.
     * @param view the parent view to remove padding from
     */
    private void setZeroPaddingToLayoutChildren(View view) {
        if (!(view instanceof ViewGroup))
            return;
        ViewGroup viewGroup = (ViewGroup) view;
        int childCount = viewGroup.getChildCount();

        // Remove padding from each child
        for (int i = 0; i < childCount; i++) {
            setZeroPaddingToLayoutChildren(viewGroup.getChildAt(i));
            viewGroup.setPaddingRelative(0, viewGroup.getPaddingTop(), viewGroup.getPaddingEnd(), viewGroup.getPaddingBottom());
        }
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        if (preference instanceof CustomListPreference) {
            final CustomListPreference CustomListPreference = (CustomListPreference) preference;
            CustomPreferenceDialog df = CustomPreferenceDialog.createInstance(CustomListPreference);
            df.setTargetFragment(this, 0);
            if (getFragmentManager() != null) {
                df.setDialogClosedListener(new CustomPreferenceDialog.DialogClosedListener() {
                    @Override
                    public void onSelection(String key, String newValue) {
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                        sharedPreferences.edit().putString(key, newValue).apply();
                    }
                });
                df.show(getFragmentManager(), null);
            }
        } else super.onDisplayPreferenceDialog(preference);

    }

    /**
     * Listens for changes in preferences to update the display.
     * @param sharedPreferences the set of preferences to read from
     * @param key the preference that was changed
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getResources().getString(R.string.pref_animation_style_key)))
            mAnimationPreference.setSummary();

        // This block listens for changes in the ListPreference and changes the SeekBarPreferences
        // according to which preset the user chose
        if (key.equals(getResources().getString(R.string.pref_list_breath_style_key))) {
            ListPreference listPreference = findPreference(key);
            String currentValue = listPreference.getValue();
            CustomSeekBarPreference inhaleSeekBar = getPreferenceManager().findPreference(getResources().getString(R.string.pref_seekbar_inhale_key));
            CustomSeekBarPreference exhaleSeekBar = getPreferenceManager().findPreference(getResources().getString(R.string.pref_seekbar_exhale_key));
            CustomSeekBarPreference holdSeekBar = getPreferenceManager().findPreference(getResources().getString(R.string.pref_seekbar_hold_key));
            CustomSeekBarPreference pauseSeekBar = getPreferenceManager().findPreference(getResources().getString(R.string.pref_seekbar_pause_key));
            if (currentValue.equals(getResources().getString(R.string.pref_list_uplifting_breathing_value))) {
                inhaleSeekBar.setValue(getResources().getInteger(R.integer.inhale_uplifting_default));
                exhaleSeekBar.setValue(getResources().getInteger(R.integer.exhale_uplifting_default));
                holdSeekBar.setValue(getResources().getInteger(R.integer.hold_uplifting_default));
                pauseSeekBar.setValue(getResources().getInteger(R.integer.pause_uplifting_default));
            } else if (currentValue.equals(getResources().getString(R.string.pref_list_relaxing_breathing_value))) {
                inhaleSeekBar.setValue(getResources().getInteger(R.integer.inhale_relaxing_default));
                exhaleSeekBar.setValue(getResources().getInteger(R.integer.exhale_relaxing_default));
                holdSeekBar.setValue(getResources().getInteger(R.integer.hold_relaxing_default));
                pauseSeekBar.setValue(getResources().getInteger(R.integer.pause_relaxing_default));
            } else if (currentValue.equals(getResources().getString(R.string.pref_list_meditative_breathing_value))) {
                inhaleSeekBar.setValue(getResources().getInteger(R.integer.inhale_meditative_default));
                exhaleSeekBar.setValue(getResources().getInteger(R.integer.exhale_meditative_default));
                holdSeekBar.setValue(getResources().getInteger(R.integer.hold_meditative_default));
                pauseSeekBar.setValue(getResources().getInteger(R.integer.pause_meditative_default));
            }
        }
        // This block listens for changes in any of the SeekBars and changes the ListPreference
        else if (key.equals(getResources().getString(R.string.pref_seekbar_inhale_key)) ||
                 key.equals(getResources().getString(R.string.pref_seekbar_exhale_key)) ||
                 key.equals(getResources().getString(R.string.pref_seekbar_hold_key)) ||
                 key.equals(getResources().getString(R.string.pref_seekbar_pause_key))){
            ListPreference listPreference = getPreferenceManager()
                    .findPreference(getResources().getString(R.string.pref_list_breath_style_key));
            int inhaleTime = ((CustomSeekBarPreference) getPreferenceManager().findPreference(
                    getResources().getString(R.string.pref_seekbar_inhale_key))).getValue();
            int exhaleTime = ((CustomSeekBarPreference) getPreferenceManager().findPreference(
                    getResources().getString(R.string.pref_seekbar_exhale_key))).getValue();
            int holdTime = ((CustomSeekBarPreference) getPreferenceManager().findPreference(
                    getResources().getString(R.string.pref_seekbar_hold_key))).getValue();
            int pauseTime = ((CustomSeekBarPreference) getPreferenceManager().findPreference(
                    getResources().getString(R.string.pref_seekbar_pause_key))).getValue();
            // This block listens for changes in the preset options and signals an update to
            // the SeekBars appropriately
            if (inhaleTime == getResources().getInteger(R.integer.inhale_uplifting_default) &&
                    exhaleTime == getResources().getInteger(R.integer.exhale_uplifting_default) &&
                    holdTime == getResources().getInteger(R.integer.hold_uplifting_default) &&
                    pauseTime == getResources().getInteger(R.integer.pause_uplifting_default)) {
                listPreference.setValue(getResources().getString(R.string.pref_list_uplifting_breathing_value));
            } else if (inhaleTime == getResources().getInteger(R.integer.inhale_relaxing_default) &&
                    exhaleTime == getResources().getInteger(R.integer.exhale_relaxing_default) &&
                    holdTime == getResources().getInteger(R.integer.hold_relaxing_default) &&
                    pauseTime == getResources().getInteger(R.integer.pause_relaxing_default)) {
                listPreference.setValue(getResources().getString(R.string.pref_list_relaxing_breathing_value));
            } else if (inhaleTime == getResources().getInteger(R.integer.inhale_meditative_default) &&
                    exhaleTime == getResources().getInteger(R.integer.exhale_meditative_default) &&
                    holdTime == getResources().getInteger(R.integer.hold_meditative_default) &&
                    pauseTime == getResources().getInteger(R.integer.pause_meditative_default)) {
                listPreference.setValue(getResources().getString(R.string.pref_list_meditative_breathing_value));
            } else
                listPreference.setValue(getResources().getString(R.string.pref_list_custom_breathing_value));
        }
    }
}
