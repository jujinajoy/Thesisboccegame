package com.example.bocce;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.lang.StringBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import android.os.Handler;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import android.content.SharedPreferences;
import android.util.Log;
import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;
import com.robotemi.sdk.listeners.OnRobotReadyListener;

public class ScorecardActivity extends AppCompatActivity implements OnRobotReadyListener, Robot.AsrListener, RobotAsrErrorListener {


    private static final String TAG = "ScorecardActivity";

    private String[] playerNames;
    private String[] ballColors;
    int noOfRound;
    private int currentRound;
    private Robot sRobot;
    private static final String MINIMUM_DISTANCE_KEY = "minimum_distance";
    private static final String NAME_KEY = "name";

    private double minimumDistance = Double.MAX_VALUE;
    private String Name = "";
    private TimeLogger timeLogger;

    //String Name1;

    private static final String SERVER_URL = "http://139.174.104.200/ball_game/getResults.php"; // Replace with your server URL

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scorecard);
        sRobot = Robot.getInstance();

        sRobot.addOnRobotReadyListener((OnRobotReadyListener)this);
        sRobot.addAsrListener(this);
        timeLogger=new TimeLogger();

        SharedPreferences sharedPreferences =getSharedPreferences("TimeLog",MODE_PRIVATE);
        long startTime =sharedPreferences.getLong("startTime",0);
        timeLogger.startLogging();


        // Retrieve round count and player data from intent extras
        currentRound = getIntent().getIntExtra("roundCount", 0);
        playerNames = getIntent().getStringArrayExtra("playerNames");
        ballColors = getIntent().getStringArrayExtra("ballColors");
        noOfRound = getIntent().getIntExtra("selectedRoundCount", 0);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        minimumDistance = preferences.getFloat(MINIMUM_DISTANCE_KEY, Float.MAX_VALUE);
        Log.i(TAG,"Name at the beginiing of game is " + Name);
        Log.i(TAG,"minimum distance  at the beginiing of game is " + minimumDistance);
        Name = preferences.getString(NAME_KEY, "");
        if (playerNames != null) {
            for (String playerName : playerNames) {
                Log.d("Player Name", "Player Name: " + playerName);
            }
        } else {
            Log.d("Player Name", "Player Names array is null");
        }
        //clearHistory();


        // Execute AsyncTask to fetch results from server
        new GetResultsTask().execute();

        // Update roundTextView with the current round number
        @SuppressLint("CutPasteId") TextView roundTextView = findViewById(R.id.nextRoundButton);
        roundTextView.setText("Round");


        // Button to go to ShowPlain activity and update to next round
        @SuppressLint("CutPasteId") Button nextRoundButton = findViewById(R.id.nextRoundButton);
        nextRoundButton.setOnClickListener(v -> {
            if (currentRound <= noOfRound) {
                Intent intent = new Intent(ScorecardActivity.this, ShowPlain.class);
                intent.putExtra("roundCount", currentRound);
                intent.putExtra("playerNames", playerNames);
                intent.putExtra("ballColors", ballColors);
                intent.putExtra("selectedRoundCount",noOfRound);
                startActivity(intent);
            }
        });
        if (currentRound > noOfRound) {
            nextRoundButton.setEnabled(false);

        }


        // Button to restart game from StartActivity
        Button restartButton = findViewById(R.id.restartButton);
        restartButton.setOnClickListener(v -> {
            clearMemory();
            startActivity(new Intent(ScorecardActivity.this, Roundactivity.class));
        });

        // Button to exit the app
        Button exitButton = findViewById(R.id.exitButton);
        exitButton.setOnClickListener(v ->{
            timeLogger.stopLogging();
            long elapsedTime=timeLogger.getLoggedTime();
            logElapsedTime(elapsedTime);
            clearMemory();
            finishAffinity();
        });
    }
    public void nextRoundButtonClicked(View view) {
        Intent intent = new Intent(this, ShowPlain.class);
        intent.putExtra("roundCount", currentRound);
        intent.putExtra("playerNames", playerNames);
        intent.putExtra("ballColors", ballColors);
        intent.putExtra("selectedRoundCount", noOfRound);
        startActivity(intent);

    }

    public void restartButtonClicked(View view) {
        clearMemory();
        Intent intent = new Intent(this, Roundactivity.class);
        startActivity(intent);

    }

    public void exitButtonClicked(View view) {
        clearMemory();
        finishAffinity();
        // Close the app
    }
    private void logElapsedTime(long elapsedTime){
        String logMessage = "Elapsed time:"+ elapsedTime + "ms";
        Log.d("TimeLogger", logMessage);
    }
    private void askQuestion(String question) {
        Log.d(TAG, "Asking question: " + question);
        sRobot.askQuestion(question);

    }


    public class GetResultsTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(SERVER_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                // Connect to the PHP script
                connection.connect();

                // Read response from the PHP script
                InputStream inputStream = connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    result.append(line);
                }

                // Close connections
                bufferedReader.close();
                inputStream.close();
                connection.disconnect();
            } catch (IOException e) {
                Log.e("GetResultsTask", "Error connecting to PHP script: " + e.getMessage());
            }

            return result.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            // Handle the result from the PHP script here
            Log.d("GetResultsTask", "Result from PHP script: " + result);
            displayServerResponse(result);
        }

    }


    private void displayServerResponse(String serverResponse) {
        try {
            // Initialize distance to a high value

            JSONObject jsonResponse = new JSONObject(serverResponse);
            JSONArray dataArray = jsonResponse.getJSONArray("data");

            // Load existing game history from SharedPreferences
            SharedPreferences preferences = getSharedPreferences("GameHistory", MODE_PRIVATE);
            String existingHistory = preferences.getString("history", "");

            // Append new game results to the existing history
            StringBuilder resultBuilder = new StringBuilder(existingHistory);
            // Append current game results to the result builder
            resultBuilder.append("\n\n\n"); // Add separator between games



            // Iterate through the ball colors and check for matches with player's selected ball color
            for (int i = 0; i < dataArray.length(); i++) {

                JSONObject dataObject = dataArray.getJSONObject(i);
                String winningBall1 = dataObject.getString("winning_ball");
                String winningDistanceStr = dataObject.getString("winning_distance");
                double winningDistance1 = Double.parseDouble(winningDistanceStr);
                String playerName1 = getPlayerNameForBallColor(winningBall1);

                String winningBall = dataObject.getString("winning_ball");
                String winningDistance = dataObject.getString("winning_distance");
                String secondBall = dataObject.getString("second_ball");
                String secondDistance = dataObject.getString("second_distance");
                String thirdBall = dataObject.getString("third_ball");
                String thirdDistance = dataObject.getString("third_distance");
                String fourthBall = dataObject.getString("fourth_ball");
                String fourthDistance = dataObject.getString("fourth_distance");
                Robot sRobot = Robot.getInstance();

                // Determine the player name for each ball
                String playerName = getPlayerNameForBallColor(winningBall);
                String playerNameSecond = getPlayerNameForBallColor(secondBall);
                String playerNameThird = getPlayerNameForBallColor(thirdBall);
                String playerNameFourth = getPlayerNameForBallColor(fourthBall);
                int columnWidth = 20; // Adjust this as per your requirement
                resultBuilder.append(String.format("%-" + columnWidth + "s%-50s%-50s\n", "PLAYER", "BALL", "DISTANCE")); // Increased spacing

                resultBuilder.append(String.format("%-" + columnWidth + "s%-50s%-50s\n",
                        playerName.trim(), winningBall.trim(), winningDistance.trim()));
                resultBuilder.append(String.format("%-" + columnWidth + "s%-50s%-50s\n",
                        playerNameSecond.trim(), secondBall.trim(), secondDistance.trim()));
                resultBuilder.append(String.format("%-" + columnWidth + "s%-50s%-50s\n",
                        playerNameThird.trim(), thirdBall.trim(), thirdDistance.trim()));
                resultBuilder.append(String.format("%-" + columnWidth + "s%-50s%-50s\n",
                        playerNameFourth.trim(), fourthBall.trim(), fourthDistance.trim()));
                Log.i(TAG,"currentround is " + currentRound);

                /*if (currentRound > 2) {

                    Distances = dataObject.getString("winning_distance");
                    distancess = Double.parseDouble(Distances);
                    winningBalls = dataObject.getString("winning_ball");
                    Name1 = getPlayerNameForBallColor(winningBalls);
                    if (distancess < distance) {
                        //distancess = Double.parseDouble(winningDistance);
                        distance = distancess;
                        Name = Name1;
                    }

                    else{
                        Log.i(TAG,"Name is " + Name);
                        Name1=Name;
                        if (Name1==null){
                            Log.i(TAG, "name1  is null");}
                    }

                } else {
                    Log.i(TAG,"currentround is l");
                    //String Distances = dataObject.getString("winning_distance");
                    Distances = dataObject.getString("winning_distance");
                    distance = Double.parseDouble(Distances);
                    distancess = Double.parseDouble(Distances);
                    winningBalls = dataObject.getString("winning_ball");
                    Name = getPlayerNameForBallColor(winningBalls);
                    Log.i(TAG, "name"+ Name);
                    Name1 = getPlayerNameForBallColor(winningBalls);

                    if (Name == null ) {
                        Log.i(TAG, "Winner name or winning ball is null in initial round data");
                        //continue; // Skip this iteration if data is invalid
                    }
                }*/
                //double minimumDistance ;
                if (currentRound == 2) {
                    // In the first round, initialize Name and minimumDistance
                    Name = playerName1;

                    Log.i(TAG,"Name is " + Name);
                    minimumDistance = winningDistance1;
                    Log.i(TAG,"minimum Distance is " + minimumDistance);
                } else {
                    // In subsequent rounds, update if a closer distance is found
                    if (winningDistance1 < minimumDistance) {
                        Log.i(TAG,"winning Distance is " + winningDistance1);
                        minimumDistance = winningDistance1;
                        Name = playerName1;
                        Log.i(TAG,"playerName is " + playerName1);
                    }
                }

                if (currentRound > noOfRound) {
                    Log.i(TAG,"alert name is " + Name);

                    // Display winner popup only when current round is 3
                    displayWinnerPopup(Name);
                    Log.i(TAG, "not going to here");
                    //clearMemory();
                } else {
                    //sRobot.askQuestion("Would you like to go to the next round, restart the game, or exit?");
                    sRobot.askQuestion("Would you prefer to proceed to the next round, restart the game, or exit?");
                }

            }


            //}

            // Save updated game history to SharedPreferences
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("history", resultBuilder.toString());
            editor.apply();

            // Populate TextView with the result
            TextView textViewResult = findViewById(R.id.textViewResult);
            textViewResult.setText(resultBuilder.toString());
            //speakTtsQueue(ttsQueue);

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error parsing server response", Toast.LENGTH_SHORT).show();
        }
    }
    private String getPlayerNameForBallColor(String ballColor) {
        for (int i = 0; i < ballColors.length; i++) {
            if (ballColors[i].equals(ballColor)) {
                return playerNames[i];
            }
        }
        return "";
    }
    private void displayWinnerPopup(String Name) {
        /*if (Name == null ) {
            Log.e(TAG, "Winner name is null, cannot display popup");
            return;
        }*/
        Log.d(TAG, "Winner name: " + Name);
        //Log.d(TAG, "Winning ball: " + winningBall);
        // Set up the custom view for the AlertDialog
        View customView = getLayoutInflater().inflate(R.layout.custom_dialog, null);
        TextView titleTextView = customView.findViewById(R.id.dialog_title);
        TextView messageTextView = customView.findViewById(R.id.dialog_message);

// Set title and message
        titleTextView.setText("Winner");
        messageTextView.setText("The winner of the game is: " + Name);

// Build the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(customView);  // Set custom view
        builder.setPositiveButton("OK", (dialogInterface, i) -> {
            // Dismiss the dialog when OK is clicked
            dialogInterface.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.show();

// Speak the winner's name using Temi's TTS
        TtsRequest winnerNameRequest = TtsRequest.create(Name, false);
        sRobot.speak(winnerNameRequest);

// Dismiss the dialog automatically after 50 seconds
        new Handler().postDelayed(() -> {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }, 50000);



        /*AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Winner");
        builder.setMessage("The winner of the game is: " + Name);

        TtsRequest winnerNameRequest = TtsRequest.create(Name, false);

        // Speak the winner's name immediately when the popup is shown
        sRobot.speak(winnerNameRequest);

        builder.setPositiveButton("OK", (dialogInterface, i) -> {
            // Dismiss the dialog manually and ask the question after 50 seconds
            dialogInterface.dismiss();
            // Post a delayed message to ask the question after 50 seconds

        });

        AlertDialog dialog = builder.create();
        dialog.show();*/
        //sRobot.askQuestion("Would you like to, restart the game, or exit?");

        // Dismiss the dialog automatically after 50 seconds
        // new Handler().postDelayed(dialog::dismiss, 50000);
    }
    private void clearMemory() {
        SharedPreferences preferences = getSharedPreferences("GameHistory", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }




    // Method to determine the player name based on the ball color


    // Method to limit the number of history entries to 5

    /* private void speakTtsQueue(final Queue<TtsRequest> ttsQueue) {
         sRobot.addTtsListener(new Robot.TtsListener() {
             @Override
             public void onTtsStatusChanged(@NotNull TtsRequest ttsRequest) {
                 if (ttsRequest.getStatus() == TtsRequest.Status.COMPLETED) {
                     // Dequeue the next TTS request if the completed request was from the queue
                     if (!ttsQueue.isEmpty()) {
                         sRobot.speak(ttsQueue.remove());
                     }
                 }
             }
         });

         // Speak the first request from the queue
         if (!ttsQueue.isEmpty()) {
             sRobot.speak(ttsQueue.remove());
         }
     }*/
    /*@Override
    public void onAsrResult(@NonNull String asrResult) {
        Log.d(TAG, "ASR Result: " + asrResult);
        switch (asrResult.toLowerCase()) {
            case "next round":
            case "next route":
            case "Next Round":
                nextRoundButtonClicked(null);
                sRobot.finishConversation();
                break;
            case "restart the game":
            case "Restart the game":
                restartButtonClicked(null);
                sRobot.finishConversation();
                break;
            case "exit":
                exitButtonClicked(null);
                sRobot.finishConversation();
                break;
            default:
                // Command not recognized, ask again
                askQuestion("Sorry, I didn't understand. Please say 'Next round', 'Restart the game', or 'exit'.");
                break;
        }
        //askQuestion();

    }*/
    @Override
    public void onAsrResult(@NonNull String asrResult) {
        Log.d(TAG, "ASR Result: " + asrResult);
        asrResult = asrResult.toLowerCase(); // Convert the result to lowercase for easy comparison

        // Define a map of keywords and their corresponding actions
        Map<String, Runnable> actions = new HashMap<>();
        actions.put("next", () -> nextRoundButtonClicked(null));
        actions.put("round", () -> nextRoundButtonClicked(null));
        actions.put("route", () -> nextRoundButtonClicked(null));
        actions.put("restart",  () -> restartButtonClicked(null));
        actions.put("new",()-> restartButtonClicked(null));
        actions.put("exit", () -> exitButtonClicked(null));
        actions.put("stop", () -> exitButtonClicked(null));

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
        askQuestion("Apologies, could you repeat it again?");

    }

    @Override
    public void onAsrError(int errorCode, String errorMessage) {
        Log.e(TAG, "ASR Error - Code: " + errorCode + ", Message: " + errorMessage);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Add robot event listeners
        sRobot.addOnRobotReadyListener(this);

        //sRobot.addAsrListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Remove robot event listeners
        sRobot.removeOnRobotReadyListener(this);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(MINIMUM_DISTANCE_KEY, (float) minimumDistance);
        editor.putString(NAME_KEY, Name);
        editor.apply();
        //sRobot.removeAsrListener(this);
    }

    @Override
    public void onRobotReady(boolean isReady) {
        if (isReady) {
            Log.i(TAG, "Robot is ready");
            sRobot.hideTopBar();
            // hide temi's top action bar when skill is active
        }
    }


}
