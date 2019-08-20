package com.axel.breatheandrelax.activity

import android.content.Intent
import androidx.fragment.app.DialogFragment
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager

import com.axel.breatheandrelax.R
import com.axel.breatheandrelax.fragment.SingleMessageDialogFragment

/**
 * Activity class for the splash page. Features 3 buttons for navigation:
 * Settings, main meditation activity, and help dialog.
 */

class MainMenuActivity : AppCompatActivity(), SingleMessageDialogFragment.DialogFinishedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (supportActionBar != null) supportActionBar!!.hide()
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main_menu)

        // If we passed data back from the meditation activity, show a dialog with that parsed data
        // This happens if the user chose a specified time to meditate for
        if (intent.hasExtra(MeditationActivity.CURRENT_MEDITATION_TIME) && savedInstanceState == null) {
            val medTime = intent.getLongExtra(MeditationActivity.CURRENT_MEDITATION_TIME, -1)

            val minutes = (medTime / 1000 / 60).toInt()
            val seconds = (medTime / 1000).toInt() - minutes * 60
            val messageToDisplay: String
            if (medTime == (-1).toLong() || minutes == 0 && seconds < 2)
                messageToDisplay = resources.getString(R.string.meditation_finished_summary)
            else {
                val minutesText = when(minutes) {
                    0 -> ""
                    1-> " " + minutes + " " + resources.getString(R.string.minutes_singular)
                    else -> " " + minutes + " " + resources.getString(R.string.minutes)
                }
                val secondsText = when (seconds) {
                    0 -> ""
                    1 -> " " + seconds + " " + resources.getString(R.string.seconds_singular)
                    else -> " " + seconds + " " + resources.getString(R.string.seconds)
                }

                messageToDisplay = (resources.getString(R.string.meditation_time_summary1)
                        + minutesText
                        + (if (minutes != 0 && seconds != 0) " " + resources.getString(R.string.and) else "")
                        + secondsText
                        + " " + resources.getString(R.string.meditation_time_summary2))
            }

            // Create dialog and pack arguments into it
            val dialog = SingleMessageDialogFragment()

            val args = Bundle()
            args.putString(SingleMessageDialogFragment.MESSAGE_KEY, messageToDisplay)
            args.putString(SingleMessageDialogFragment.BUTTON_LABEL_KEY, resources.getString(R.string.button_continue))
            args.putString(SingleMessageDialogFragment.TITLE_KEY, resources.getString(R.string.title_med_finished))
            dialog.arguments = args
            dialog.show(supportFragmentManager, "finished")
        }
    }

    /**
     * Button listener to start the breathing activity.
     * @param view is the button that was clicked
     */
    fun onClickStart(view: View) {
        val i = Intent(this, MeditationActivity::class.java)
        startActivity(i)
    }

    /**
     * Button listener to start the settings activity.
     * @param view is the button that was clicked
     */
    fun onClickSettings(view: View) {
        val i = Intent(this, SettingsActivity::class.java)
        startActivity(i)
    }

    /**
     * Button listener to launch the help dialog.
     * @param view is the button that was clicked
     */
    fun onClickHelp(view: View) {
        val help = SingleMessageDialogFragment()

        // Pack arguments into the newly created dialog
        val args = Bundle()
        args.putString(SingleMessageDialogFragment.MESSAGE_KEY, resources.getString(R.string.help_text))
        args.putString(SingleMessageDialogFragment.BUTTON_LABEL_KEY, resources.getString(R.string.acknowledgement_got_it))
        args.putString(SingleMessageDialogFragment.TITLE_KEY, resources.getString(R.string.title_help_text))
        help.arguments = args
        help.show(supportFragmentManager, "help")
    }

    /**
     * Necessary implementation for interface function from SingleMessageDialogFragment.
     * We don't need to do anything when the dialog is dismissed.
     */
    override fun onDialogDismissed() {
        // do nothing
    }
}
