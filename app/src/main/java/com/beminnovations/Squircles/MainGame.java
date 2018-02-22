package com.beminnovations.squircles;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import java.util.Random;

public class MainGame extends Activity {

    // Initial variables and conditions
    final int[] backgrounds = {R.drawable.button_1, R.drawable.button_2,
            R.drawable.button_3, R.drawable.button_4};
    Random randomi = new Random();
    int indexi = randomi.nextInt(backgrounds.length);
    int background = backgrounds[indexi];
    int scoreValue = 0; // Initial score
    double t0 = 4000; // Initial timer in milliseconds
    double t = t0; // Initial time in milliseconds
    int t1 = (int) t0; // Initial time in milliseconds as an integer
    TextView score;
    ProgressBar mProgressBar;
    RelativeLayout pushHolder;
    boolean nCDRuns = true; // Check to see if the countdown timer is running
    newCountDown nCD; // Reserve space for the countdown and make global
    int finalScore; // Reserve space for the final score and make global
    int cumulativeScore;
    int highScore;
    Context context;
    Animation inOut;

    // AdView
    private AdView mAdView;

    // Analytics
    public static GoogleAnalytics analytics;
    public static Tracker tracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_game);

        // Analytics setup
        analytics = GoogleAnalytics.getInstance(this);
        analytics.setLocalDispatchPeriod(1800);

        tracker = analytics.newTracker("UA-60525520-1");
        tracker.enableExceptionReporting(true);
        tracker.enableAdvertisingIdCollection(true);
        tracker.enableAutoActivityTracking(true);

        // Get IDs
        score = (TextView) findViewById(R.id.counter);
        mProgressBar = (ProgressBar) findViewById(R.id.timer);
        pushHolder = (RelativeLayout) findViewById(R.id.pushHolder);

        mProgressBar.setProgress(100); // Set timer at 100%
        score.setText(String.valueOf(scoreValue)); // Set score field
        pushHolder.setBackgroundResource(background);

        inOut = AnimationUtils.loadAnimation(getApplication(),
                R.anim.in_out);
        // Start timer
        nCDTimer(t1);

        // AdView setup
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();

        mAdView.loadAd(adRequest);

    }

        //debug

    protected void onRestart() {
        super.onRestart();

        Intent restart = new Intent(MainGame.this,SplashScreen.class);
        MainGame.this.startActivity(restart);
    }

    // What happens when a button is pressed
    public void onButtonClick(View v) {
        // Get ID
        Button button = (Button) v;


        // Speed up the timer
        if(t>0.5) {
            t = t * 0.98;
            t1 = (int) t;
        }

        // Check to see if the right button was pressed and adjust score
        scoreCheck(clickCheck(button));
        score.setText(String.valueOf(scoreValue));

        // Change button supposed to be pressed to another value randomly
        indexi = randomi.nextInt(backgrounds.length);
        background = backgrounds[indexi];
        pushHolder.setBackgroundResource(background);

        // Clear timer
        nCD.cancel();
        nCDTimer(t1);

        // Debug
    }

    // Countdown timer
    public void nCDTimer(final int t) {

        nCD = new newCountDown(t, 10) {
            @Override
            public void onTick(long millisUntilFinished) {
                if(nCDRuns) {
                    // Set the progress bar to be proportional for each t
                    float fraction = millisUntilFinished/(float) t;
                    mProgressBar.setProgress((int)(fraction*100));
                }
            }

            @Override
            public void onFinish() {
                if(nCDRuns) {
                    // When the timer runs out
                    nCDRuns = false;
                    finalScore = scoreValue; // Set the value to pass to the dialog box
                    updateAllScores(finalScore);
                    //debug.setText(String.valueOf(nCDRuns));
                    gameEnd(); // Run dialog box
                }
            }
        }.start();
    }

    // Check if the proper button was pressed
    private boolean clickCheck(Button button){
        return button.getBackground().getConstantState().equals(pushHolder.getBackground().getConstantState());
    }

    // Once button check is complete, use result to change score (and timer)
    private void scoreCheck(boolean check) {
        if (check && nCDRuns) { // Right button pressed
            scoreValue++;
            score.startAnimation(inOut);
        } else if(check &! nCDRuns) {
            finalScore = scoreValue; // Set the value to pass to the dialog box
            updateAllScores(finalScore);
        } else { // Wrong button pressed
            // Reset score and timer
            finalScore = scoreValue; // Set the value to pass to the dialog box
            updateAllScores(finalScore);
            nCDRuns = false;
            nCD.cancel();
            gameEnd(); // Run dialog box
        }
    }

    // Dialog at end of game

    private void gameEnd() {
        nCDRuns = false;

        AlertDialog.Builder builder = new AlertDialog.Builder(MainGame.this);
        builder
                .setTitle("You scored " + finalScore + " Squircles")
                .setMessage("High Score: " + highScore + " Squircles \nPlay again?")
                //.setMessage("High Score: " + highScore + " Squircles. \n" + cumulativeScore +  " total Squircles." + " \nPlay again?")
                .setCancelable(false)
                .setPositiveButton("Play", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Reset values
                        scoreValue = 0; // Initial score
                        t0 = 4000; // Initial timer in milliseconds
                        t = t0; // Initial time in milliseconds
                        t1 = (int) t0; // Initial time in milliseconds as an integer
                        score.setText(String.valueOf(scoreValue));

                        // Start timer again
                        nCD.cancel();
                        nCDRuns = true;
                        nCDTimer(t1);

                        dialog.cancel(); // Close the dialog box
                    }
                });
        AlertDialog endGame = builder.create();
        endGame.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_game, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        android.os.Process.killProcess(android.os.Process.myPid()); //kills process if you hit the back button
        //returning to the program from your running programs will cause the process to restart
    }


    public void updateAllScores(int lastScore){
        updateBanner();
        updateCumulative(lastScore);
        updateHighScore(lastScore);

    }
    public void updateCumulative(int lastScore) {
        SharedPreferences sharedPref = MainGame.this.getSharedPreferences(getString(R.string.cumulative_score), Context.MODE_PRIVATE);
        cumulativeScore = sharedPref.getInt(getString(R.string.cumulative_score),0);
        cumulativeScore += lastScore;
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.cumulative_score), cumulativeScore);
        editor.commit();
    }
    public void updateHighScore(int lastScore) {
        SharedPreferences sharedPref = MainGame.this.getSharedPreferences(getString(R.string.high_score), Context.MODE_PRIVATE);
        highScore = sharedPref.getInt(getString(R.string.high_score),0);

        if(highScore < lastScore) {
            highScore = lastScore;
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt(getString(R.string.high_score), highScore);
            editor.commit();
        }
    }

    private void updateBanner() {
        mAdView.setVisibility(View.VISIBLE);
        mAdView.loadAd(new AdRequest.Builder().build());
        //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build()
    }


}

