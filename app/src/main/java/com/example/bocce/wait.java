// WaitActivity.java
package com.example.bocce;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;
import com.robotemi.sdk.listeners.OnRobotReadyListener;

public class wait extends AppCompatActivity implements OnRobotReadyListener{
    //private static final String SERVER_URL = "http://192.168.0.100/runss.php"; // Replace with you serverURL
    private static final long WAIT_TIME_MS = 35_000; // 4 minutes
    private static final String TAG = wait.class.getSimpleName();
    private Robot sRobot;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait);
        sRobot = Robot.getInstance();
        sRobot.addOnRobotReadyListener(this);
        //sRobot.addAsrListener(this);

        //sRobot.addOnRobotReadyListener((OnRobotReadyListener)this);
        TtsRequest ttsRequest = TtsRequest.create("Let's unveil the champion of the game!", true);
        sRobot.finishConversation();
        sRobot.speak(ttsRequest);

        // Retrieve data passed from ScorecardActivity
        Intent intent = getIntent();
        if (intent != null) {
            // Retrieve all the necessary data
            int noOfRoundCount = intent.getIntExtra("selectedRoundCount", 1);
            int roundCount = intent.getIntExtra("roundCount", 1);
            String[] playerNames = intent.getStringArrayExtra("playerNames");
            String[] ballColors = intent.getStringArrayExtra("ballColors");

            // Start the countdown timer
            new CountDownTimer(WAIT_TIME_MS, WAIT_TIME_MS) {
                @Override
                public void onTick(long millisUntilFinished) {
                    // Not needed for this countdown timer
                }

                @Override
                public void onFinish() {
                    // Transition back to ScorecardActivity
                    Intent scorecardIntent = new Intent(wait.this, ScorecardActivity.class);
                    scorecardIntent.putExtra("selectedRoundCount", noOfRoundCount);
                    scorecardIntent.putExtra("roundCount", roundCount);
                    scorecardIntent.putExtra("playerNames", playerNames);
                    scorecardIntent.putExtra("ballColors", ballColors);
                    startActivity(scorecardIntent);
                    finish(); // Close this activity
                }
            }.start();
        }
    }
    @Override
    public void onRobotReady(boolean isReady) {
        if (isReady) {
            // Hide Temi's top action bar when skill is active
            sRobot.hideTopBar();
        }else {
            Log.w(TAG, "Robot is not ready.");
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: Adding ASR and Robot Ready Listeners");
       // sRobot.addAsrListener(this);
        sRobot.addOnRobotReadyListener(this);
        //sRobot.addOnConversationStatusChangedListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: Removing ASR and Robot Ready Listeners");
       // sRobot.removeAsrListener(this);
        sRobot.removeOnRobotReadyListener(this);
        // sRobot.removeOnConversationStatusChangedListener(this);
    }
}
