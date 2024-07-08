package com.example.bocce;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;
import com.robotemi.sdk.listeners.OnRobotReadyListener;

public class LetSgo extends AppCompatActivity implements OnRobotReadyListener {
    private static final long DELAY_MILLIS = 3700;
    String[] ballColors;
    String[] playerNames;
    int noOfRound;
    private Robot sRobot;
    private static final String TAG = "LetSgo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.letsgo);
        sRobot = Robot.getInstance();
        sRobot.addOnRobotReadyListener(this);
        //TtsRequest ttsRequest = TtsRequest.create("Every players please take their Balls and be ready to start the game", true);
        TtsRequest ttsRequest = TtsRequest.create("Players, please grab your balls and get ready to start.", true);
        sRobot.speak(ttsRequest);
        sRobot.finishConversation();

        playerNames = getIntent().getStringArrayExtra("playerNames");
        ballColors = getIntent().getStringArrayExtra("ballColors");
        noOfRound = getIntent().getIntExtra("selectedRoundCount", 1);
        new Handler().postDelayed(navigateToNextPageRunnable, DELAY_MILLIS);
    }

    @Override
    protected void onStart() {
        super.onStart();
        sRobot.addOnRobotReadyListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        sRobot.removeOnRobotReadyListener(this);
    }

    @Override
    /*public void onRobotReady(boolean isReady) {
        if (isReady) {
            Log.i(TAG, "Robot is ready");
            sRobot.hideTopBar();
            // Once the robot is ready, schedule navigation to the ShowPlain activity
            new Handler().postDelayed(navigateToNextPageRunnable, DELAY_MILLIS);
        }
    }*/
    public void onRobotReady(boolean isReady) {
        if (isReady) {
            // Hide Temi's top action bar when skill is active
            sRobot.hideTopBar();
        }else {
            Log.w(TAG, "Robot is not ready.");
        }
    }

    // Runnable to navigate to the ShowPlain activity
    private final Runnable navigateToNextPageRunnable = new Runnable() {
        @Override
        public void run() {
            // Create an Intent to start the ShowPlain activity
            Intent intent = new Intent(getApplicationContext(), ShowPlain.class);
            // Add extras to the Intent
            intent.putExtra("playerNames", playerNames);
            intent.putExtra("ballColors", ballColors);
            intent.putExtra("selectedRoundCount", noOfRound);
            // Start the ShowPlain activity
            startActivity(intent);
            // Finish the LetSgo activity to prevent it from being returned to
            finish();
        }
    };
}
