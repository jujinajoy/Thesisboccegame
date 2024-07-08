package com.example.bocce;

import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;
import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;
import com.robotemi.sdk.listeners.OnRobotReadyListener;
public class MainActivity extends AppCompatActivity implements OnRobotReadyListener {
    private static final long DELAY_MILLIS = 5998;
    private Robot mRobot;
    private static final String TAG = "MainActivity";
    private TimeLogger timeLogger;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Handler().postDelayed(navigateToNextPageRunnable, DELAY_MILLIS);
        mRobot = Robot.getInstance();
        timeLogger=new TimeLogger();
        timeLogger.startLogging();
        SharedPreferences sharedPreferences =getSharedPreferences("TimeLog",MODE_PRIVATE);
        SharedPreferences.Editor editor= sharedPreferences.edit();
        editor.putLong("startTime",timeLogger.getStartTime());
        editor.apply();
       // mRobot.addOnRobotReadyListener((OnRobotReadyListener)this);
       /* Robot sRobot = Robot.getInstance();
        TtsRequest ttsRequest = TtsRequest.create("Lets Begin the Game", true);
        sRobot.speak(ttsRequest);*/


    }
    public MainActivity() {
        navigateToNextPageRunnable = this::navigateToNextPage;
    }
    private final Runnable navigateToNextPageRunnable;
    private void navigateToNextPage() {
        Intent intent = new Intent(getApplicationContext(), Activity2.class);
        startActivity(intent);
        finish();
    }
    @Override
    protected void onStart() {
        super.onStart();

        // Add robot event listeners
        mRobot.addOnRobotReadyListener(this);
        //mRobot.addAsrListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Remove robot event listeners
        mRobot.removeOnRobotReadyListener(this);
        //mRobot.removeAsrListener(this);
    }

    @Override
    public void onRobotReady(boolean isReady) {
        if (isReady) {
            Log.i(TAG, "Robot is ready");

            mRobot.hideTopBar();
            TtsRequest ttsRequest = TtsRequest.create("Welcome to the game of bocce!", true);
            mRobot.speak(ttsRequest);
            //askNextQuestion();// hide temi's top action bar when skill is active
        }
    }

}