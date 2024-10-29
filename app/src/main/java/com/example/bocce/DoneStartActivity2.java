package com.example.bocce;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.graphics.Typeface;
import android.graphics.Color;
import android.text.InputFilter;
import com.robotemi.sdk.TtsRequest;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.util.Log;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import com.robotemi.sdk.Robot;
import java.util.HashMap;
import java.util.Map;

import com.robotemi.sdk.listeners.OnRobotReadyListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DoneStartActivity2 extends AppCompatActivity implements Robot.AsrListener, OnRobotReadyListener, RobotAsrErrorListener {
    private static final String TAG = DoneStartActivity2.class.getSimpleName();

    private LinearLayout playerContainer;
    private int numberOfPlayers = 4;
    private final List<String> selectedColors = new ArrayList<>();
    private final Set<String> availableColors = new HashSet<>(Arrays.asList("NONE", "Red", "Blue", "Green", "Orange"));
    private String[] playerNames;
    private String[] ballColors;
    private int noOfRound;
    private Robot sRobot;
    private Button doneButton;
    private Button backButton;

    private String[] getPlayerNames() {
        String[] names = new String[numberOfPlayers];
        for (int i = 0; i < 4; i++) {
            View childView = playerContainer.getChildAt(i * 2); // Adjust index to account for adding both EditText and Spinner
            if (childView instanceof EditText) {
                EditText playerNameEditText = (EditText) childView;
                names[i] = playerNameEditText.getText().toString();
            } else if (childView instanceof Spinner) {
                // Handle Spinner case if needed
            }
        }
        return names;
    }
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startdone2);
        sRobot = Robot.getInstance();

        doneButton = findViewById(R.id.doneButton);
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(this::backButtonClicked);
        playerContainer = findViewById(R.id.playerContainer);
        noOfRound = getIntent().getIntExtra("selectedRoundCount", 1);
        playerNames = getPlayerNames();
        //noOfRound = getIntent().getIntExtra("selectedRoundCount", 0);
        TtsRequest ttsRequest = TtsRequest.create("PLAYERS PLEASE COME FORWARD IN THE PLAYING ORDER AND FILL THE FIELDS WITH YOUR NAMES AND CHOOSE BALL COLOUR FROM THE GIVEN OPTIONS", true);
        sRobot.speak(ttsRequest);
        //sRobot.askQuestion("Players please come forward and fill the fields with your names and choose ball colour from the given options");
        sRobot.addOnRobotReadyListener(this);
        sRobot.addAsrListener(this);

        for (int i = 0; i < 4; i++) {
            final int playerIndex = i;

            EditText playerNameEditText = new EditText(this);
            playerNameEditText.setId(View.generateViewId());
            playerNameEditText.setHint("Player " + (playerIndex + 1) + " Name");
            playerNameEditText.setTextSize(28); // Set the font size
            Typeface typeface = Typeface.create("Comic-Sans-Ms", Typeface.NORMAL); // Example of setting a custom font style
            playerNameEditText.setTypeface(typeface);
            int brickRedColor = Color.parseColor("#006400"); // Hexadecimal color code for brick red #93E9BE  B22222
            playerNameEditText.setTextColor(brickRedColor);


            playerNameEditText.setFilters(new InputFilter[]{new InputFilter.AllCaps()});

            // Example: Setting font color to red
            playerContainer.addView(playerNameEditText);

            selectedColors.add("");

            Spinner playerBallColorSpinner = new Spinner(this);
            playerBallColorSpinner.setId(View.generateViewId());

            //ArrayAdapter<String> colorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.ball_colors));
            // Use the custom layout for the spinner items
            ArrayAdapter<String> colorAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, getResources().getStringArray(R.array.ball_colors));



            //colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Optionally, use a different layout for the dropdown view
            colorAdapter.setDropDownViewResource(R.layout.spinner_item);
            playerBallColorSpinner.setAdapter(colorAdapter);

            playerBallColorSpinner.setSelection(colorAdapter.getPosition(getString(R.string.default_color)));
            playerBallColorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedColor = (String) parent.getItemAtPosition(position);
                    String previousColor = selectedColors.get(playerIndex);

                    // Check if the selected color is already chosen by another player
                    if (selectedColor != "NONE" && !selectedColor.equals(previousColor) && selectedColors.contains(selectedColor)) {
                        showColorAlreadySelectedAlert(selectedColor);
                        // Revert to the previous valid color
                        playerBallColorSpinner.setSelection(((ArrayAdapter<String>) playerBallColorSpinner.getAdapter()).getPosition(previousColor));
                    } else {
                        // If the color is valid, update the selected colors
                        if (!previousColor.equals("NONE")) {
                            availableColors.add(previousColor); // Add the previous color back to available colors
                        }
                        selectedColors.set(playerIndex, selectedColor);
                        availableColors.remove(selectedColor); // Remove the newly selected color from available colors
                        // Notify the adapter of the changes
                        colorAdapter.notifyDataSetChanged();
                    }
                    // Triggering the check after a valid selection
                    if (areAllFieldsFilled() && areAllColorsSelected()) {
                        askIfUsersWantToContinue(); // Calls this method when all conditions are met





                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });


            /*playerBallColorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedColor = (String) parent.getItemAtPosition(position);
                    if (selectedColor!= "NONE" && selectedColors.contains(selectedColor)) {
                        showColorAlreadySelectedAlert(selectedColor);
                    } else {
                        selectedColors.set(playerIndex, selectedColor);
                        availableColors.remove(selectedColor);
                    }
                    colorAdapter.notifyDataSetChanged();

                    // Check if all fields are filled and colors are selected, then ask the next question
                    if (areAllFieldsFilled() && areAllColorsSelected()) {
                        askIfUsersWantToContinue();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });*/

            playerContainer.addView(playerBallColorSpinner);
        }

        doneButton.setOnClickListener(v -> {
            proceedIfReady();
        });
    }
    public void backButtonClicked(View view) {
        Intent intent = new Intent(this, Activity2.class);
        startActivity(intent);
    }
    private void askQuestion(String question) {
        Log.d(TAG, "Asking question: " + question);
        sRobot.askQuestion(question);

    }


    private boolean areAllFieldsFilled() {
        for (int i = 0; i < playerContainer.getChildCount(); i++) {
            View childView = playerContainer.getChildAt(i);
            if (childView instanceof EditText) {
                EditText editText = (EditText) childView;
                if (editText.getText().toString().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean areAllColorsSelected() {
        for (String color : selectedColors) {
            if (color == "NONE" || color.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void showFillAllFieldsAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please fill in all the fields before proceeding.")
                .setTitle("Incomplete Fields")
                .setPositiveButton("OK", (dialog, id) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showSelectAllColorsAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please select a color other than 'NONE' for each player before proceeding.")
                .setTitle("Color Selection Incomplete")
                .setPositiveButton("OK", (dialog, id) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showColorAlreadySelectedAlert(String color) {
        if (color != "NONE" && !color.isEmpty() && !color.equals("NONE")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("The color " + color + " has already been selected by another player. Please choose a different color.")
                    .setTitle("Color Already Selected")
                    .setPositiveButton("OK", (dialog, id) -> dialog.dismiss());
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    private void showSelectValidColorAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please select a color other than 'NONE' for each player before proceeding.")
                .setTitle("Invalid Color Selection")
                .setPositiveButton("OK", (dialog, id) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showMinimumPlayerAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("At least four players are required to start the game.")
                .setTitle("Insufficient Players")
                .setPositiveButton("OK", (dialog, id) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //private void askIfUsersWantToContinue() {
     //   sRobot.askQuestion("if you want to continue say yes or say no to go back ");
    //}

   /* private void proceedIfReady() {
        if (areAllFieldsFilled()) {
            if (areAllColorsSelected()) {
                if (!selectedColors.contains("Null")) {
                    playerNames = getPlayerNames();
                    ballColors = selectedColors.toArray(new String[0]);


                    // Check if there are at least 2 players
                    if (numberOfPlayers >= 4) {
                        Log.d("DoneStartActivity2", "Navigating to ShowPlain activity");
                        Intent intent = new Intent(DoneStartActivity2.this, LetSgo.class);
                        intent.putExtra("playerNames", playerNames);
                        intent.putExtra("ballColors", ballColors);
                        intent.putExtra("selectedRoundCount", noOfRound);
                        // Add error handling
                        startActivity(intent);
                    } else {
                        showMinimumPlayerAlert();
                    }
                } else {
                    showSelectValidColorAlert();
                }
            } else {
                showSelectAllColorsAlert();
            }
        } else {
            showFillAllFieldsAlert();
        }
    };*/
    //backButton.setOnClickListener(v -> onBackPressed());
   private void askIfUsersWantToContinue() {
       //sRobot.askQuestion("Ready to continue to the next activity?");
       sRobot.askQuestion("THANK YOU FOR ENTERING THE NAMES AND CHOOSING COLOURS. NOW DO YOU WANT TO CONTINUE THE GAME?");
   }

    private void proceedIfReady() {
        if (areAllFieldsFilled()) {
            if (areAllColorsSelected()) {
                if (!selectedColors.contains("NONE")) {
                    playerNames = getPlayerNames();
                    ballColors = selectedColors.toArray(new String[0]);

                    // Check if there are at least 2 players
                    if (numberOfPlayers >= 4) {
                        Log.d("DoneStartActivity2", "Navigating to LETSGO activity");
                        Intent intent = new Intent(DoneStartActivity2.this, LetSgo.class);
                        intent.putExtra("playerNames", playerNames);
                        intent.putExtra("ballColors", ballColors);
                        intent.putExtra("selectedRoundCount", noOfRound);
                        // Add error handling
                        startActivity(intent);
                    } else {
                        showMinimumPlayerAlert();
                    }
                } else {
                    showSelectValidColorAlert();
                }
            } else {
                showSelectAllColorsAlert();
            }
        } else {
            showFillAllFieldsAlert();
        }
    }
    @Override
    public void onAsrResult(@NonNull String asrResult) {
        Log.d(TAG, "ASR Result: " + asrResult);
        asrResult = asrResult.toLowerCase(); // Convert the result to lowercase for easy comparison

        // Define a map of keywords and their corresponding actions
        Map<String, Runnable> actions = new HashMap<>();
        actions.put("yes", this::proceedIfReady);
        actions.put("yh", this::proceedIfReady);
        actions.put("yeah", this::proceedIfReady);
        actions.put("ok", this::proceedIfReady);
        actions.put("sure", this::proceedIfReady);
        actions.put("next", this::proceedIfReady);
        actions.put("go", this::proceedIfReady);
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
        askQuestion("Apologies, could you repeat that?");

    }
   /* @Override
    public void onAsrResult(@NonNull String asrResult) {
        Log.d(TAG, "ASR Result: " + asrResult);
        switch (asrResult.toLowerCase()) {
            case "yes":
                proceedIfReady();
                sRobot.finishConversation();
                Log.d(TAG, "User said 'yes'. Proceeding...");
                break;
            case "no":
                Log.d(TAG, "User said 'no'. Handling...");
                backButtonClicked(null);
                sRobot.finishConversation();// Handle the "no" response if needed
                break;
            default:
                askQuestion("Sorry, I didn't understand. Please say 'yes' or 'no'");
                break;
        }

    }*/

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
}

