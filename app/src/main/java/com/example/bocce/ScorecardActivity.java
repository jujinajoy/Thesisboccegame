package com.example.bocce;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Looper;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.graphics.Color;
import android.app.AlertDialog;

import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import android.preference.PreferenceManager;

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
import java.util.*;
import android.view.Gravity; // Add this import statement
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import android.content.SharedPreferences;
import android.util.Log;
import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;
import com.robotemi.sdk.listeners.OnRobotReadyListener;

public class ScorecardActivity extends AppCompatActivity implements OnRobotReadyListener, RobotAsrErrorListener {


    private static final String TAG = "ScorecardActivity";

    private String[] playerNames;
    private String[] ballColors;
    int noOfRound;
    private int currentRound;
    private int selectedRoundCount;

    private Robot sRobot;


    private static final String MINIMUM_DISTANCE_KEY = "minimum_distance";
    private static final String NAME_KEY = "name";

    private double minimumDistance = Double.MAX_VALUE;

    // Constants for SharedPreferences keys
    private static final String PLAYER_SCORES_KEY = "player_scores";
    private static final String PREFERENCES_NAME = "ScorecardPrefs";


    private String Name = "";
    private TimeLogger timeLogger;
    private Map<String, Integer> playerScores;

    //private TextView firstPlayerNameTextView;

    //String Name1;

    private static final String SERVER_URL = "http://139.174.111.33/ball_game/getResults.php"; // Replace with your server URL

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scorecard);
        //tableLayout = findViewById(R.id.table_layout);
        //firstPlayerNameTextView = findViewById(R.id.first_player_name);
        //tableLayout = findViewById(R.id.table_layout);
        sRobot = Robot.getInstance();

        sRobot.addOnRobotReadyListener((OnRobotReadyListener)this);
        //sRobot.addAsrListener(this);
        timeLogger=new TimeLogger();


        SharedPreferences sharedPreferences =getSharedPreferences("TimeLog",MODE_PRIVATE);
        long startTime =sharedPreferences.getLong("startTime",0);
        timeLogger.startLogging();
        // Load the table data
        //loadTableData();


        // Retrieve round count and player data from intent extras
        currentRound = getIntent().getIntExtra("roundCount", 0);
        playerNames = getIntent().getStringArrayExtra("playerNames");
        ballColors = getIntent().getStringArrayExtra("ballColors");
        noOfRound = getIntent().getIntExtra("selectedRoundCount", 0);
        //roundTitleTextView = findViewById(R.id.dialog_title);

        // Initialize player scores from SharedPreferences
        playerScores = loadPlayerScores();


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
        /*@SuppressLint("CutPasteId") TextView roundTextView = findViewById(R.id.nextRoundButton);
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

        }*/


        // Button to restart game from StartActivity
        Button restartButton = findViewById(R.id.restartButton);
        restartButton.setOnClickListener(v -> {
            //resetCurrentRoundScores();
            //clearMemory();
            //clearPersistentData();
            //startActivity(new Intent(ScorecardActivity.this, Roundactivity.class));

            Intent intent = new Intent(this, LetSgo.class);
            intent.putExtra("roundCount", currentRound);
            intent.putExtra("playerNames", playerNames);
            intent.putExtra("ballColors", ballColors);
            intent.putExtra("selectedRoundCount", noOfRound);
            startActivity(intent);

        });

        // Button to exit the app
        Button exitButton = findViewById(R.id.exitButton);
        exitButton.setOnClickListener(v ->{
            //clearMemory();
            //clearPersistentData();
           // clearPlayerScores();
            //finishAffinity();
            displayWinnerPopup(Name,minimumDistance);
            Log.i(TAG, "not going to here");
            timeLogger.stopLogging();
            long elapsedTime=timeLogger.getLoggedTime();
            logElapsedTime(elapsedTime);

        });
    }
    /*public void nextRoundButtonClicked(View view) {
        Intent intent = new Intent(this, ShowPlain.class);
        intent.putExtra("roundCount", currentRound);
        intent.putExtra("playerNames", playerNames);
        intent.putExtra("ballColors", ballColors);
        intent.putExtra("selectedRoundCount", noOfRound);
        startActivity(intent);
        clearMemory();
            clearPersistentData();
            finishAffinity();

    }*/

    public void restartButtonClicked(View view) {
        //clearMemory();
        //clearPersistentData();
        //resetCurrentRoundScores();

        Intent intent = new Intent(this, LetSgo.class);

        intent.putExtra("roundCount", currentRound);
        intent.putExtra("playerNames", playerNames);
        intent.putExtra("ballColors", ballColors);
        intent.putExtra("selectedRoundCount", noOfRound);
        startActivity(intent);

    }

    public void exitButtonClicked(View view) {
        Log.i(TAG,"alert name is " + Name);

        // Display winner popup only when current round is 3
        //clearMemory();
        //clearPersistentData();
        displayWinnerPopup(Name,minimumDistance);
        Log.i(TAG, "not going to here");

        //clearTableData();
        // Reset player scores after displaying the popup
        //clearPlayerScores();
        //finishAffinity();

        // Dismiss the dialog when OK is clicked
        //dialogInterface.dismiss();

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
    private void clearPersistentData() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(MINIMUM_DISTANCE_KEY);
        editor.remove(NAME_KEY);
        editor.apply();
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
            int maxPlayerNameLength = 0;
            int maxBallLength = 0;
            int maxDistanceLength = 0;
            // Initialize player scores map if not already initialized
            if (playerScores == null) {
                playerScores = new HashMap<>();
            }
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


                Log.d("Player Name", "Player Name: " + playerName);
                Log.d("Player Name", "Player Namesecond: " + playerNameSecond);
                if (playerNameSecond == null) {
                    Log.d("Debug Check", "playerNameSecond is null");
                } else {
                    Log.d("Player Name", "Player NameSecond: " + playerNameSecond);
                }

                /*addRowToTable(playerName, winningBall, winningDistance);
                addRowToTable(playerNameSecond, secondBall, secondDistance);
                addRowToTable(playerNameThird, thirdBall, thirdDistance);
                addRowToTable(playerNameFourth, fourthBall, fourthDistance);*/


                TtsRequest ttsRequest = TtsRequest.create("currently" +playerName+" has thrown shortest distance", false);
                Log.i(TAG,"playerName tts " + playerName);

                sRobot.speak(ttsRequest);
                // Assign scores based on position (1st, 2nd, 3rd, 4th)
                assignScoreToPlayer(playerName, 400);
                assignScoreToPlayer(playerNameSecond, 300);
                assignScoreToPlayer(playerNameThird, 200);
                assignScoreToPlayer(playerNameFourth, 100);

                //displayWinnerPopup(Name,minimumDistance);


                maxPlayerNameLength = Math.max(maxPlayerNameLength, playerName.length());
                maxPlayerNameLength = Math.max(maxPlayerNameLength, playerNameSecond.length());
                maxPlayerNameLength = Math.max(maxPlayerNameLength, playerNameThird.length());
                maxPlayerNameLength = Math.max(maxPlayerNameLength, playerNameFourth.length());

                maxBallLength = Math.max(maxBallLength, winningBall.length());
                maxBallLength = Math.max(maxBallLength, secondBall.length());
                maxBallLength = Math.max(maxBallLength, thirdBall.length());
                maxBallLength = Math.max(maxBallLength, fourthBall.length());

                maxDistanceLength = Math.max(maxDistanceLength, winningDistanceStr.length());
                maxDistanceLength = Math.max(maxDistanceLength, secondDistance.length());
                maxDistanceLength = Math.max(maxDistanceLength, thirdDistance.length());
                maxDistanceLength = Math.max(maxDistanceLength, fourthDistance.length());
                int playerNameWidth = Math.max(20, maxPlayerNameLength + 2); // Minimum width of 20, or max length + padding
                int ballWidth = Math.max(20, maxBallLength + 2); // Minimum width of 20, or max length + padding
                int distanceWidth = Math.max(20, maxDistanceLength + 2); // Minimum width of 20, or max length + padding

                // Append header row (only once)
                resultBuilder.append(String.format("%-" + playerNameWidth + "s%-" + ballWidth + "s%-" + distanceWidth + "s\n", "PLAYER", "BALL", "DISTANCE"));
                resultBuilder.append(String.format("%-" + playerNameWidth + "s%-" + ballWidth + "s%-" + distanceWidth + "s\n",
                        formatString(playerName, playerNameWidth),
                        formatString(winningBall, ballWidth),
                        formatString(winningDistance, distanceWidth)));

                resultBuilder.append(String.format("%-" + playerNameWidth + "s%-" + ballWidth + "s%-" + distanceWidth + "s\n",
                        formatString(playerNameSecond, playerNameWidth),
                        formatString(secondBall, ballWidth),
                        formatString(secondDistance, distanceWidth)));

                resultBuilder.append(String.format("%-" + playerNameWidth + "s%-" + ballWidth + "s%-" + distanceWidth + "s\n",
                        formatString(playerNameThird, playerNameWidth),
                        formatString(thirdBall, ballWidth),
                        formatString(thirdDistance, distanceWidth)));

                resultBuilder.append(String.format("%-" + playerNameWidth + "s%-" + ballWidth + "s%-" + distanceWidth + "s\n",
                        formatString(playerNameFourth, playerNameWidth),
                        formatString(fourthBall, ballWidth),
                        formatString(fourthDistance, distanceWidth)));

                // Add separator between game results (except after the last game)
                if (i < dataArray.length() - 1) {
                    resultBuilder.append("\n\n\n");
                    // Add separator between games
                }
                Log.i(TAG,"currentround is " + currentRound);


                //double minimumDistance ;
                if (currentRound == 2) {
                    currentRound++; // Increment the current round
                    noOfRound++;

                    // In the first round, initialize Name and minimumDistance
                    Name = playerName1;

                    Log.i(TAG,"Name is " + Name);
                    minimumDistance = winningDistance1;
                    Log.i(TAG,"minimum Distance is " + minimumDistance);

                    // sRobot.askQuestion("Would you like to continue the game, or exit?");
                    //sRobot.askQuestion("Would you prefer to continue the game, or exit?");
                    // When you want to ask the question with a delay
                    //askQuestionWithDelay("Would you prefer to continue the game, or exit?", 7000);

                } else {
                    currentRound++; // Increment the current round
                    noOfRound++;

                    // In subsequent rounds, update if a closer distance is found
                    //sRobot.askQuestion("Would you like to go to the next round, restart the game, or exit?");
                    //sRobot.askQuestion("Would you prefer to continue the game, or exit?",9000);
                    //askQuestionWithDelay("Would you prefer to continue the game, or exit?", 7000);
                    if (winningDistance1 < minimumDistance) {
                        Log.i(TAG,"winning Distance is " + winningDistance1);
                        minimumDistance = winningDistance1;
                        Name = playerName1;
                        Log.i(TAG,"playerName is " + playerName1);
                    }
                }

            }



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



    private Map<String, Integer> loadPlayerScores() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);
        String playerScoresJson = sharedPreferences.getString(PLAYER_SCORES_KEY, "");

        Map<String, Integer> scores = new HashMap<>();
        if (!playerScoresJson.isEmpty()) {
            try {
                JSONObject jsonObject = new JSONObject(playerScoresJson);
                // Iterate over keys in the JSONObject to get player names and scores
                Iterator<String> keys = jsonObject.keys();
                while (keys.hasNext()) {
                    String playerName = keys.next();
                    int score = jsonObject.getInt(playerName);
                    scores.put(playerName, score);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return scores;
    }
    //private Map<String, Integer> currentRoundScores = new HashMap<>();


    private void assignScoreToPlayer(String playerName, int score) {

        if (playerScores.containsKey(playerName)) {
            playerScores.put(playerName, playerScores.get(playerName) + score);
            Log.d(TAG, "assigning scores2");
            Log.d(TAG, "Updated current round score for " + playerName + ": " + score);
        } else {
            Log.d(TAG, "assigning scores3 in else case"+ score);
            playerScores.put(playerName, score);

        }
        // Save the updated scores to SharedPreferences
        savePlayerScores();
    }
    // Method to remove a view from its current parent




    private void savePlayerScores() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Convert playerScores map to JSON format
        JSONObject jsonObject = new JSONObject(playerScores);

        // Store JSON string representation of playerScores in SharedPreferences
        editor.putString(PLAYER_SCORES_KEY, jsonObject.toString());
        editor.apply();
    }


    // Method to determine the player name based on the ball color
    private String getPlayerNameForBallColor(String ballColor) {
        String ballColorLowerCase = ballColor.toLowerCase();
        for (int i = 0; i < ballColors.length; i++) {
            if (ballColors[i].toLowerCase().equals(ballColorLowerCase)) {
                return playerNames[i];
            }
        }
        return "";
    }



    private String getBallColorForPlayer(String playerName) {
        // Check if playerNames and ballColors arrays are not null and have the same length
        if (playerNames != null && ballColors != null && playerNames.length == ballColors.length) {
            // Iterate through the playerNames array
            for (int i = 0; i < playerNames.length; i++) {
                //Log.d("ScorecardActivity", "Player: " + playerNames[i] + ", Ball Color: " + ballColors[i]);

                // If the player name matches, return the corresponding ball color
                if (playerNames[i].equals(playerName)) {
                    return ballColors[i];
                }
            }
        }
        // Return an empty string if no matching player name is found
        return "";
    }


    private String formatString(String str, int width) {
        return String.format("%-" + width + "s", str);
    }
    private void askQuestionWithDelay(String question, int delayMillis) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            sRobot.askQuestion(question);
        }, delayMillis);
    }



    private void displayWinnerPopup(String Name,double minimumDistance) {
        View customView = getLayoutInflater().inflate(R.layout.custom_dialog, null);
        TextView titleTextView = customView.findViewById(R.id.dialog_title);
        //TextView messageTextView = customView.findViewById(R.id.dialog_message);
        TableLayout tableLayout = customView.findViewById(R.id.table_layout);
        //TextView messageTextView1 = customView.findViewById(R.id.dialog_message1);
        TextView firstPlayerNameTextView = customView.findViewById(R.id.first_player_name);


        /*if (Name == null ) {
            Log.e(TAG, "Winner name is null, cannot display popup");
            return;
        }*/

        Log.d(TAG, "crashing here");
        // Check if headerRow ex

        String firstPlayerName = null;
             // Retrieve sorted player scores
        Map<String, Integer> sortedPlayerScores = sortByValues(playerScores);
        // Get the player with the highest score (first entry in sorted map)
        String winnerName1 = sortedPlayerScores.keySet().iterator().next();
        // Find the highest score
        int highestScore = sortedPlayerScores.values().iterator().next();

        // Collect all players with the highest score
        List<String> highestScoringPlayers = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : sortedPlayerScores.entrySet()) {
            if (entry.getValue() == highestScore) {
                highestScoringPlayers.add(entry.getKey());
            } else {
                break;  // No need to continue as scores are sorted
            }
        }

        // Construct the winner's name string
        StringBuilder winnerNamesBuilder = new StringBuilder();
        for (String player : highestScoringPlayers) {
            if (winnerNamesBuilder.length() > 0) {
                winnerNamesBuilder.append(", ");
            }
            winnerNamesBuilder.append(player);
        }

        // Display the names of the winners in the center
        firstPlayerNameTextView.setText("WINNER(S): " + winnerNamesBuilder.toString());
        Log.d(TAG, "Winner name: " + Name);
        Log.d(TAG, "Winner name: " + winnerName1);
        //Log.d(TAG, "Winning ball: " + winningBall);

        // Sort player scores to determine positions

        StringBuilder messageBuilder = new StringBuilder();
        String[] positions = {"1st", "2nd", "3rd", "4th"};

        for (int i = 0; i < positions.length; i++) {
            if (i < sortedPlayerScores.size()) {
                String playerName = (String) sortedPlayerScores.keySet().toArray()[i];
                int score = sortedPlayerScores.get(playerName);
                String ballColor = getBallColorForPlayer(playerName);
                TableRow tableRow = new TableRow(this);

                // Set the background of the TableRow
                tableRow.setBackgroundResource(R.drawable.rounded_white_box);

                TextView playerNameTextView = new TextView(this);
                playerNameTextView.setText(playerName);
                playerNameTextView.setPadding(4, 4, 4, 4);
                playerNameTextView.setTextColor(Color.parseColor("#006400")); // Dark green
                playerNameTextView.setTextSize(18); // Set font size for player name
                playerNameTextView.setLayoutParams(new TableRow.LayoutParams(
                        0, TableRow.LayoutParams.WRAP_CONTENT, 0.4f)); // Adjust weight to control width



                TextView distanceTextView = new TextView(this);
                distanceTextView.setText(String.valueOf(score));
                distanceTextView.setPadding(4, 4, 4, 4);
                distanceTextView.setTextColor(Color.parseColor("#006400")); // Dark green
                distanceTextView.setTextSize(18); // Set font size for player name
                distanceTextView.setLayoutParams(new TableRow.LayoutParams(
                        0, TableRow.LayoutParams.WRAP_CONTENT, 0.2f)); // Adjust weight to control width
                tableRow.addView(playerNameTextView);

                tableRow.addView(distanceTextView);



                tableLayout.addView(tableRow);

                Log.d(TAG, "dispaling assigned  scores");
                // Store the first player's name
                if (i == 0) {
                    firstPlayerName = playerName;
                }
                //Log.d(TAG, "dispaling assigned  scores:" + playerName);
                //Log.d(TAG, "dispaling assigned  scores:" + score);
                //Log.d(TAG, "Winner Message: " + messageBuilder.toString());
               /* messageBuilder.append(positions[i]).append(": ")
                        .append(playerName).append(" (")
                        //.append(ballColor).append(" - ") // Append ball color
                        .append(score).append(" points)\n");
                Log.d(TAG, "Winner Message: " + messageBuilder.toString());*/
            }
        }
        // Display the stored first player's name in the center
        /*if (firstPlayerName != null) {
            firstPlayerNameTextView.setText("WINNER: " + firstPlayerName);
        }*/
        // Display the names of the winners in the center
        if (highestScoringPlayers.size() == 1) {
            firstPlayerNameTextView.setText("WINNER: " + highestScoringPlayers.get(0));
        } else {
            firstPlayerNameTextView.setText("WINNERS: " + winnerNamesBuilder.toString());
        }

        // Set up the custom view for the AlertDialog

// Set title and message
        //titleTextView.setText("WINNER");
        //messageTextView.setText( Name + " has occupied with least distance of "+ minimumDistance);
        //messageTextView1.setText(messageBuilder.toString());
        //messageTextView.setGravity(Gravity.CENTER);

// Build the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(customView);  // Set custom view
        builder.setPositiveButton("OK", (dialogInterface, i) -> {
            clearMemory();
            clearPersistentData();
            //clearTableData();
            // Reset player scores after displaying the popup
            clearPlayerScores();
            finishAffinity();

            // Dismiss the dialog when OK is clicked
            dialogInterface.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.show();

// Speak the winner's name using Temi's TTS
       /* String ttsMessage = "Congrats" + winnerName1+" You did a great job and you are the winner of the game." ;
        TtsRequest winnerNameRequest = TtsRequest.create(ttsMessage, false);
        //TtsRequest winnerNameRequest = TtsRequest.create(Name, false);
        sRobot.speak(winnerNameRequest);*/
        // Construct the TTS message for the robot
        String ttsMessage = "Congrats " + winnerNamesBuilder.toString() +
                ". You did a great job and you are the winner" +
                (highestScoringPlayers.size() > 1 ? "s" : "") +
                " of the game.";
        TtsRequest winnerNameRequest = TtsRequest.create(ttsMessage, false);
        sRobot.speak(winnerNameRequest);

// Dismiss the dialog automatically after 50 seconds
        new Handler().postDelayed(() -> {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }, 140000);



        //sRobot.askQuestion("Would you like to, restart the game, or exit?");

        // Dismiss the dialog automatically after 50 seconds
        // new Handler().postDelayed(dialog::dismiss, 50000);
    }
    private Map<String, Integer> sortByValues(Map<String, Integer> map) {
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(map.entrySet());
        Collections.sort(entries, (o1, o2) -> o2.getValue().compareTo(o1.getValue())); // Sort descending

        Map<String, Integer> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : entries) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }
    private void clearPlayerScores() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(PLAYER_SCORES_KEY);
        editor.remove(PREFERENCES_NAME);
// Remove the player scores data
        editor.apply();
    }


    private void clearMemory() {
        SharedPreferences preferences = getSharedPreferences("GameHistory", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
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
