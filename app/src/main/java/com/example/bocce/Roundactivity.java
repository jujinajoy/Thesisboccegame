package com.example.bocce;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;
import com.robotemi.sdk.listeners.OnRobotReadyListener;
import java.util.HashMap;
import java.util.Map;

import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.util.Log;
import androidx.appcompat.app.AlertDialog;

public class Roundactivity extends AppCompatActivity implements Robot.AsrListener, OnRobotReadyListener, RobotAsrErrorListener {

    private Robot sRobot;
    private int selectedRoundCount = -1; // Default to -1 indicating no selection
    private static final String TAG = Roundactivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_round);

        // Initialize Temi robot instance
        sRobot = Robot.getInstance();

        // Ask a question when the activity is created
        //sRobot.askQuestion("Select the round number 1 or 2 or 3");
        sRobot.askQuestion("Select the round number 1 or 2 or 3");

        // Register this activity as a listener for robot readiness
        sRobot.addOnRobotReadyListener(this);
        sRobot.addAsrListener(this);
        Button doneButton = findViewById(R.id.doneButton);
        Button backButton = findViewById(R.id.backButton);
        doneButton.setOnClickListener(this::doneButtonClicked);
        backButton.setOnClickListener(this::backButtonClicked);
        RadioButton round1 = findViewById(R.id.roundRadio1);
        RadioButton round2 = findViewById(R.id.roundRadio2);
        RadioButton round3 = findViewById(R.id.roundRadio3);

        RadioGroup roundsRadioGroup = findViewById(R.id.roundsRadioGroup);
        roundsRadioGroup.setOnCheckedChangeListener((group, checkedId) -> selectedRoundCount = getSelectedRoundCount(group));
    }

    private void showSelectRoundCountAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please select the number of rounds before proceeding.")
                .setTitle("Round Count Not Selected")
                .setPositiveButton("OK", (dialog, id) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private int getSelectedRoundCount(RadioGroup radioGroup) {
        int selectedId = radioGroup.getCheckedRadioButtonId();
        if (selectedId == -1) { // No radio button selected
            // Show an alert dialog
            showSelectRoundCountAlert();
            return -1; // Indicate that no round count is selected
        } else {
            RadioButton selectedRadioButton = findViewById(selectedId);
            if (selectedRadioButton != null) {
                String selectedRoundCount = selectedRadioButton.getText().toString();
                try {
                    return Integer.parseInt(selectedRoundCount); // Parse the selected round count string to integer
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        return 1; // Default to 1 round if parsing fails
    }

    // Handle the "Start" button click
    public void doneButtonClicked(View view) {
        if (selectedRoundCount == -1) {
            showSelectRoundCountAlert();
        } else {
            onStop();
            Intent intent = new Intent(this, DoneStartActivity2.class);
            intent.putExtra("selectedRoundCount", selectedRoundCount);
            startActivity(intent);
        }
    }
    // Overloaded doneButtonClicked method for use as a Runnable
    private void doneButtonClicked() {
        if (selectedRoundCount == -1) {
            showSelectRoundCountAlert();
        } else {
            onStop();
            Intent intent = new Intent(this, DoneStartActivity2.class);
            intent.putExtra("selectedRoundCount", selectedRoundCount);
            startActivity(intent);
        }
    }

    // Handle the "Instruct" button click
    public void backButtonClicked(View view) {

        Intent intent = new Intent(this, Activity2.class);
        startActivity(intent);
    }
    private void backButtonClicked() {
        Intent intent = new Intent(this, Activity2.class);
        startActivity(intent);
    }

    // Process user's response
    @Override
    public void onAsrResult(@NonNull String asrResult) {
        Log.d(TAG, "ASR Result: " + asrResult);
        asrResult = asrResult.toLowerCase(); // Convert the result to lowercase for easy comparison

        // Define a list of words and corresponding actions
        Map<String, Runnable> actions = new HashMap<>();
        actions.put("one", () -> selectRound(1));
        actions.put("1", () -> selectRound(1));
        actions.put("two", () -> selectRound(2));
        actions.put("2", () -> selectRound(2));
        actions.put("to", () -> selectRound(2));
        actions.put("three", () -> selectRound(3));
        actions.put("3", () -> selectRound(3));
        actions.put("yes", this::doneButtonClicked);
        actions.put("go", this::doneButtonClicked);
        actions.put("sure", this::doneButtonClicked);
        actions.put("yeah", this::doneButtonClicked);
        actions.put("next", this::doneButtonClicked);
        actions.put("move", this::doneButtonClicked);
        actions.put("no",this::backButtonClicked);
        actions.put("back",this::backButtonClicked);
        actions.put("stay",this::backButtonClicked);


        // Split the ASR result into words
        String[] words = asrResult.split("\\s+");

        // Check if any of the words match the predefined actions
        for (String word : words) {
            Runnable action = actions.get(word);
            if (action != null) {
                action.run();
                sRobot.finishConversation();
                askToContinue();
                return;
            }
        }

        // If no matching word is found, ask the question again
        sRobot.askQuestion("Invalid input. Please say one, two, or three.");
        askToContinue();
    }

    private void selectRound(int round) {
        int radioId;
        switch (round) {
            case 1:
                RadioButton round1 = findViewById(R.id.roundRadio1);
                round1.setChecked(true);
                selectedRoundCount = 1;
                sRobot.finishConversation();
                break;
            case 2:
                RadioButton round2 = findViewById(R.id.roundRadio2);
                round2.setChecked(true);
                selectedRoundCount = 2;
                sRobot.finishConversation();
                break;
            case 3:
                RadioButton round3 = findViewById(R.id.roundRadio3);
                round3.setChecked(true);
                selectedRoundCount = 3;
                sRobot.finishConversation();
                break;
            default:
                throw new IllegalArgumentException("Invalid round: " + round);
        }
        /*Log.d(TAG, "ASR Result: " + asrResult);
        switch (asrResult.toLowerCase()) {
            case "1":
            case "one":
                RadioButton round1 = findViewById(R.id.roundRadio1);
                round1.setChecked(true);
                selectedRoundCount = 1;
                sRobot.finishConversation();
                break;
            case "2":
            case "two":
                RadioButton round2 = findViewById(R.id.roundRadio2);
                round2.setChecked(true);
                selectedRoundCount = 2;
                sRobot.finishConversation();
                break;
            case "3":
            case "three":
                RadioButton round3 = findViewById(R.id.roundRadio3);
                round3.setChecked(true);
                selectedRoundCount = 3;
                sRobot.finishConversation();
                break;
            case "yes":
                doneButtonClicked(null);
                sRobot.finishConversation();
                break;
            default:
                //Log.e(TAG, "Invalid round number: " + asrResult);
                sRobot.askQuestion("Invalid round number. Please say one, two, or three.");
                break;
        }*/
        //askToContinue();
    }


    private void askToContinue() {
        // Ask the user if they want to continue
        sRobot.askQuestion("LET'S CONTINUE IF SO PLEASE SAY YES");
    }

    // Handle the user's response to continue or not


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
}
