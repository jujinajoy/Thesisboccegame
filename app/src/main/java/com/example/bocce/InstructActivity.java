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
    private final Queue<String> queue = new LinkedList<>(Arrays.asList());
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
        queue.add(instructionText1);
        queue.add(instructionText2);
        queue.add(instructionText3);
        queue.add(instructionText4);
        queue.add(instructionText5);
        queue.add(instructionText6);
        sRobot.addOnRobotReadyListener(this);
        sRobot.addAsrListener(this);

        // Set up TTS listener
        sRobot.addTtsListener(ttsRequest -> {
            if (ttsRequest.getStatus() == TtsRequest.Status.COMPLETED && !queue.isEmpty()) {
                // Speak the next instruction in the queue
                sRobot.speak(TtsRequest.create(queue.remove(), false));
            } else if (queue.isEmpty()) {
                // All instructions have been spoken, ask user to continue
                askToContinue();
            }
        });


        // Start speaking the first instruction
        sRobot.speak(TtsRequest.create(queue.remove(), false));
    }

    private void askToContinue() {
        // Ask the user if they want to continue
        sRobot.askQuestion("Let's head to the next screen to choose your rounds!");

    }

    public void backButtonClicked(View view) {
        Intent intent = new Intent(this, Roundactivity.class);
        startActivity(intent);
    }
    private void backButtonClicked() {
        Intent intent = new Intent(this, Roundactivity.class);
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
        actions.put("yes", this::backButtonClicked);
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
        askQuestion("I didn't catch that, could you say it again?");
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
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.d(TAG, "onStop: Removing ASR and Robot Ready Listeners");
        sRobot.removeAsrListener(this);
    }


}