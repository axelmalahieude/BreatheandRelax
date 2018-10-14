package com.axel.breatheandrelax.activity;

import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.axel.breatheandrelax.R;
import com.axel.breatheandrelax.fragment.SingleMessageDialogFragment;

/**
 * Activity class for the splash page. Features 3 buttons for navigation:
 * Settings, main meditation activity, and help dialog.
 */

public class MainMenuActivity extends AppCompatActivity implements
        SingleMessageDialogFragment.DialogFinishedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main_menu);

        // If we passed data back from the meditation activity, show a dialog with that parsed data
        // This happens if the user chose a specified time to meditate for
        if (getIntent().hasExtra(MeditationActivity.CURRENT_MEDITATION_TIME) && savedInstanceState == null){
            long medTime = getIntent().getLongExtra(MeditationActivity.CURRENT_MEDITATION_TIME, -1);

            int minutes = (int) (medTime / 1000 / 60);
            int seconds = (int) (medTime / 1000) - (minutes * 60);
            String messageToDisplay;
            if (medTime == -1 || (minutes == 0 && seconds < 2))
                messageToDisplay = getResources().getString(R.string.meditation_finished_summary);
            else {
                String minutesText, secondsText;
                if (minutes == 0) minutesText = "";
                else if (minutes == 1) minutesText = " " + minutes + " " + getResources().getString(R.string.minutes_singular);
                else minutesText = " " + minutes + " " + getResources().getString(R.string.minutes);

                if (seconds == 0) secondsText = "";
                else if (seconds == 1) secondsText = " " + seconds + " " + getResources().getString(R.string.seconds_singular);
                else secondsText = " " + seconds + " " + getResources().getString(R.string.seconds);
                messageToDisplay = getResources().getString(R.string.meditation_time_summary1)
                        + minutesText
                        + ((minutes != 0 && seconds != 0) ? " " + getResources().getString(R.string.and) : "")
                        + secondsText
                        + " " + getResources().getString(R.string.meditation_time_summary2);
            }

            // Create dialog and pack arguments into it
            DialogFragment dialog = new SingleMessageDialogFragment();

            Bundle args = new Bundle();
            args.putString(SingleMessageDialogFragment.MESSAGE_KEY, messageToDisplay);
            args.putString(SingleMessageDialogFragment.BUTTON_LABEL_KEY, getResources().getString(R.string.button_continue));
            args.putString(SingleMessageDialogFragment.TITLE_KEY, getResources().getString(R.string.title_med_finished));
            dialog.setArguments(args);
            dialog.show(getSupportFragmentManager(), "finished");
        }
    }

    /**
     * Button listener to start the breathing activity.
     * @param view is the button that was clicked
     */
    public void onClickStart(View view) {
        Intent i = new Intent(this, MeditationActivity.class);
        startActivity(i);
    }

    /**
     * Button listener to start the settings activity.
     * @param view is the button that was clicked
     */
    public void onClickSettings(View view) {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
    }

    /**
     * Button listener to launch the help dialog.
     * @param view is the button that was clicked
     */
    public void onClickHelp(View view) {
        DialogFragment help = new SingleMessageDialogFragment();

        // Pack arguments into the newly created dialog
        Bundle args = new Bundle();
        args.putString(SingleMessageDialogFragment.MESSAGE_KEY, getResources().getString(R.string.help_text));
        args.putString(SingleMessageDialogFragment.BUTTON_LABEL_KEY, getResources().getString(R.string.acknowledgement_got_it));
        args.putString(SingleMessageDialogFragment.TITLE_KEY, getResources().getString(R.string.title_help_text));
        help.setArguments(args);
        help.show(getSupportFragmentManager(), "help");
    }

    /**
     * Necessary implementation for interface function from SingleMessageDialogFragment.
     * We don't need to do anything when the dialog is dismissed.
     */
    @Override
    public void onDialogDismissed() {
        // do nothing
    }
}
