package com.axel.breatheandrelax.fragment;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceGroupAdapter;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.PreferenceViewHolder;
import android.support.v7.preference.SeekBarPreference;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.axel.breatheandrelax.R;

import java.util.List;

public class SettingsFragment extends PreferenceFragmentCompat
implements SharedPreferences.OnSharedPreferenceChangeListener{

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.pref);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

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
     * @return the preference RecyclerView adapter
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

    private void setZeroPaddingToLayoutChildren(View view) {
        if (!(view instanceof ViewGroup))
            return;
        ViewGroup viewGroup = (ViewGroup) view;
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            setZeroPaddingToLayoutChildren(viewGroup.getChildAt(i));
            viewGroup.setPaddingRelative(0, viewGroup.getPaddingTop(), viewGroup.getPaddingEnd(), viewGroup.getPaddingBottom());
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // This block listens for changes in the ListPreference and changes the SeekBarPreferences
        // according to which preset the user chose
        if (key.equals(getResources().getString(R.string.pref_list_breath_style_key))) {
            ListPreference listPreference = (ListPreference) findPreference(key);
            String currentValue = listPreference.getValue();
            SeekBarPreference inhaleSeekBar = (SeekBarPreference) getPreferenceManager().findPreference(getResources().getString(R.string.pref_seekbar_inhale_key));
            SeekBarPreference exhaleSeekBar = (SeekBarPreference) getPreferenceManager().findPreference(getResources().getString(R.string.pref_seekbar_exhale_key));
            SeekBarPreference holdSeekBar = (SeekBarPreference) getPreferenceManager().findPreference(getResources().getString(R.string.pref_seekbar_hold_key));
            SeekBarPreference pauseSeekBar = (SeekBarPreference) getPreferenceManager().findPreference(getResources().getString(R.string.pref_seekbar_pause_key));
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
            ListPreference listPreference = (ListPreference) getPreferenceManager()
                    .findPreference(getResources().getString(R.string.pref_list_breath_style_key));
            int inhaleTime = ((SeekBarPreference) getPreferenceManager().findPreference(
                    getResources().getString(R.string.pref_seekbar_inhale_key))).getValue();
            int exhaleTime = ((SeekBarPreference) getPreferenceManager().findPreference(
                    getResources().getString(R.string.pref_seekbar_exhale_key))).getValue();
            int holdTime = ((SeekBarPreference) getPreferenceManager().findPreference(
                    getResources().getString(R.string.pref_seekbar_hold_key))).getValue();
            int pauseTime = ((SeekBarPreference) getPreferenceManager().findPreference(
                    getResources().getString(R.string.pref_seekbar_pause_key))).getValue();
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