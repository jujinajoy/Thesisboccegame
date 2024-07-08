package com.example.bocce;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import java.util.HashMap;
import java.util.Map;
import com.robotemi.sdk.Robot;
import com.robotemi.sdk.listeners.OnRobotReadyListener;
import org.jetbrains.annotations.NotNull;
import android.widget.TextView;
//import com.robotemi.sdk.listeners.OnConversationStatusChangedListener;
import android.util.Log;

public class Activity2 extends AppCompatActivity implements Robot.AsrListener, OnRobotReadyListener,
        RobotAsrErrorListener {

    private Robot sRobot;
    private static final String TAG = Activity2.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2);



        // Initialize Temi robot instance
        sRobot = Robot.getInstance();

        // Ask a question when the activity is created
        sRobot.askQuestion("Hello , would you like to start the game, read instructions, or exit?");

        // Register this activity as a listener for robot readiness
        sRobot.addOnRobotReadyListener(this);
        sRobot.addAsrListener(this);
    }

    // Handle the "Start" button click
    public void startButtonClicked(View view) {
        Intent intent = new Intent(this, Roundactivity.class);
        startActivity(intent);
    }
    private void startButtonClicked() {
        Intent intent = new Intent(this, Roundactivity.class);
        startActivity(intent);
    }


    // Handle the "Instruct" button click
    public void instructButtonClicked(View view) {
        Intent intent = new Intent(this, InstructActivity.class);
        startActivity(intent);
    }
    private void instructButtonClicked() {
        Intent intent = new Intent(this, InstructActivity.class);
        startActivity(intent);
    }

    // Handle the "Exit" button click
    public void exitButtonClicked(View view) {
        finishAffinity(); // Close the app
    }
    private void exitButtonClicked() {
        finishAffinity(); // Close the app
    }

    // Ask a question
    private void askQuestion(String question) {
        Log.d(TAG, "Asking question: " + question);
        sRobot.askQuestion(question);

    }
    @Override
    public void onAsrResult(@NonNull String asrResult) {
        Log.d(TAG, "ASR Result: " + asrResult);
        asrResult = asrResult.toLowerCase(); // Convert the result to lowercase for easy comparison

        // Define a map of keywords and their corresponding actions
        Map<String, Runnable> actions = new HashMap<>();
        actions.put("start", this::startButtonClicked);
        actions.put("sure", this::startButtonClicked);
        actions.put("game", this::startButtonClicked); // "game" triggers startButtonClicked
        actions.put("instructions", this::instructButtonClicked);
        actions.put("instruction", this::instructButtonClicked);
        actions.put("read", this::instructButtonClicked);
        actions.put("exit", this::exitButtonClicked);

        // Check if any keyword is contained within the asrResult and execute the corresponding action
        for (Map.Entry<String, Runnable> entry : actions.entrySet()) {
            String keyword = entry.getKey();
            if (asrResult.contains(keyword)) {
                entry.getValue().run();
                sRobot.finishConversation();
                return; // Exit the method after executing the action
            }
        }

        // Command not recognized, ask again
        askQuestion("Sorry, I didn't understand. Please repeat it");
    }

    // Process user's response
    /*@Override
    public void onAsrResult(@NonNull String asrResult) {
        Log.d(TAG, "ASR Result: " + asrResult);
        switch (asrResult.toLowerCase()) {
            case "start the game":
                startButtonClicked(null);
                sRobot.finishConversation();
                break;
            case "read instructions":
                instructButtonClicked(null);
                sRobot.finishConversation();
                break;
            case "exit":
                exitButtonClicked(null);
                sRobot.finishConversation();
                break;
            default:
                // Command not recognized, ask again
                askQuestion("Sorry, I didn't understand. Please say 'start the game', 'read instructions', or 'exit'.");
                break;
        }

    }*/

    @Override
    public void onAsrError(int errorCode, String errorMessage) {
        Log.e(TAG, "ASR Error - Code: " + errorCode + ", Message: " + errorMessage);
    }


    // Callback method invoked when the robot is ready
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
        sRobot.addAsrListener(this);
        sRobot.addOnRobotReadyListener(this);
        //sRobot.addOnConversationStatusChangedListener(this);
    }

   @Override
    protected void onStop() {
        super.onStop();

        Log.d(TAG, "onStop: Removing ASR and Robot Ready Listeners");

        sRobot.removeAsrListener(this);
        sRobot.removeOnRobotReadyListener(this);

       // sRobot.removeOnConversationStatusChangedListener(this);
    }
    /*@Override
    public void onConversationStatusChanged(int status, @NotNull String text) {
        final TextView textView = findViewById(R.id.conversationStatus);

        switch (status) {
            case IDLE:
                Log.i(TAG, "Status: IDLE | Text: " + text);
                textView.setText("Status: IDLE | Text: " + text);
                break;
            case LISTENING:
                Log.i(TAG, "Status: LISTENING | Text: " + text);
                textView.setText("Status: LISTENING | Text: " + text);
                break;
            case THINKING:
                Log.i(TAG, "Status: THINKING | Text: " + text);
                textView.setText("Status: THINKING | Text: " + text);
                break;
            case SPEAKING:
                Log.i(TAG, "Status: SPEAKING | Text: " + text);
                textView.setText("Status: SPEAKING | Text: " + text);
                break;
            default:
                Log.i(TAG, "Status: UNKNOWN | Text: " + text);
                textView.setText("Status: UNKNOWN | Text: " + text);
                break;
        }
    }*/
}
