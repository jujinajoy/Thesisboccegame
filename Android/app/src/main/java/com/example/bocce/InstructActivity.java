package com.example.bocce;

import android.content.Intent;
import android.os.Bundle;
import java.util.HashMap;
import java.util.Map;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.util.Log;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import android.widget.TextView;
import com.robotemi.sdk.TtsRequest;
import com.robotemi.sdk.Robot;
import com.robotemi.sdk.listeners.OnRobotReadyListener;
import com.robotemi.sdk.listeners.OnRobotReadyListener;
import org.jetbrains.annotations.NotNull;

public class InstructActivity extends AppCompatActivity implements
        OnRobotReadyListener, RobotAsrErrorListener, Robot.AsrListener{
    private final Queue<String> queue1 = new LinkedList<>(Arrays.asList());
    private Robot sRobot;
    private static final String TAG = InstructActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instruct);

        // Initialize views
        TextView instruction1 = findViewById(R.id.instruction1);
        TextView instruction2 = findViewById(R.id.instruction2);
        TextView instruction3 = findViewById(R.id.instruction3);
        TextView instruction4 = findViewById(R.id.instruction4);
        TextView instruction5 = findViewById(R.id.instruction5);
        TextView instruction6 = findViewById(R.id.instruction6);

        // Get the instruction strings
        String instructionText1 = instruction1.getText().toString();
        String instructionText2 = instruction2.getText().toString();
        String instructionText3 = instruction3.getText().toString();
        String instructionText4 = instruction4.getText().toString();
        String instructionText5 = instruction5.getText().toString();
        String instructionText6 = instruction6.getText().toString();

        // Initialize Robot
        sRobot = Robot.getInstance();


        // Add instruction strings to the queue
        queue1.add(instructionText1);
        queue1.add(instructionText2);
        queue1.add(instructionText3);
        queue1.add(instructionText4);
        queue1.add(instructionText5);
        queue1.add(instructionText6);



        // Set up TTS listener
        sRobot.addTtsListener(ttsRequest -> {
            if (ttsRequest.getStatus() == TtsRequest.Status.COMPLETED && !queue1.isEmpty()) {
                // Speak the next instruction in the queue
                sRobot.speak(TtsRequest.create(queue1.remove(), false));
            } else if (queue1.isEmpty()) {
                //backButtonClicked();
                // All instructions have been spoken, ask user to continue

                //sRobot.askQuestion("Let's head to the next screen to choose your rounds!");
                sRobot.addOnRobotReadyListener(this);
                sRobot.addAsrListener(this);

            }
        });


        // Start speaking the first instruction
        sRobot.speak(TtsRequest.create(queue1.remove(), false));
    }


    public void backButtonClicked(View view) {
        queue1.clear();
        Intent intent = new Intent(this, DoneStartActivity2.class);
        startActivity(intent);
    }
    private void backButtonClicked() {
        queue1.clear();
        Intent intent = new Intent(this, DoneStartActivity2.class);
        startActivity(intent);
    }


    /*@Override
    public void onAsrResult(@NonNull String asrResult) {
        if ("yes".equalsIgnoreCase(asrResult)) {
            sRobot.finishConversation();
            backButtonClicked(null);
        } else {// Command not recognized, ask again
            askToContinue();
        }

    }*/

   /* private void askQuestion(String question) {
        Log.d(TAG, "Asking question: " + question);
        sRobot.askQuestion(question);

    }*/
    @Override
    public void onAsrResult(@NonNull String asrResult) {
        Log.d(TAG, "ASR Result: " + asrResult);
        asrResult = asrResult.toLowerCase(); // Convert the result to lowercase for easy comparison

        // Define a map of keywords and their corresponding actions
        Map<String, Runnable> actions = new HashMap<>();
        actions.put("yes", this::backButtonClicked);
        actions.put("yeah", this::backButtonClicked);
        actions.put("sure", this::backButtonClicked);
        actions.put("ok", this::backButtonClicked);
        actions.put("next", this::backButtonClicked);
        actions.put("go", this::backButtonClicked);
        actions.put("back", () -> backButtonClicked(null));
        actions.put("no", () -> backButtonClicked(null));

        // Check if any keyword is contained within the asrResult and execute the corresponding action
        for (Map.Entry<String, Runnable> entry : actions.entrySet()) {
            String keyword = entry.getKey();
            if (asrResult.contains(keyword)) {
                entry.getValue().run();
                sRobot.finishConversation();
                Log.d(TAG, "User said '" + keyword + "'.");
                return; // Exit the method after executing the action
            }
        }

        // Command not recognized, ask again
        //askQuestion("Sorry, I didn't understand. Please say it again");
        //askQuestion("Sorry, I didn't understand. Please repeat it");
    }


    @Override
    public void onAsrError(int errorCode, String errorMessage) {
        Log.e(TAG, "ASR Error - Code: " + errorCode + ", Message: " + errorMessage);
    }

    @Override
    public void onRobotReady(boolean isReady) {
        if (isReady) {
            // Hide Temi's top action bar when skill is active
            sRobot.hideTopBar();
        } else {
            Log.w(TAG, "Robot is not ready.");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: Adding ASR and Robot Ready Listeners");
        sRobot.addAsrListener(this);
        sRobot.addOnRobotReadyListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.d(TAG, "onStop: Removing ASR and Robot Ready Listeners");
        sRobot.removeAsrListener(this);
        sRobot.removeOnRobotReadyListener(this);
    }
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Cleaning up");
        sRobot.removeAsrListener(this);
        sRobot.removeOnRobotReadyListener(this);
    }



}