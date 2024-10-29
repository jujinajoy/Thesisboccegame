package com.example.bocce;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.util.HashMap;
import java.util.Map;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;
import com.robotemi.sdk.listeners.OnRobotReadyListener;
import java.util.Queue;
import java.util.LinkedList;

public class ShowPlain extends AppCompatActivity implements OnRobotReadyListener, RobotAsrErrorListener{

    private String[] playerNames;
    private String[] ballColors;

    private TextView playerNameTextView;
    private TextView ballColorTextView;
    private TextView timerTextView;
    private Button scorecardButton;
    private Button nextPlayerButton;

    private int currentPlayerIndex = 0;
    private int currentRound;
    private int noOfRound;
    private CountDownTimer timer;
    private static final String SERVER_URL = "http://139.174.111.33/runsss.php"; // Replace with your server URL
    private static final String TAG = "ShowPlain";
    private Robot sRobot;
    final Queue<String> queue = new LinkedList<>();

    private SendHttpRequestTask sendHttpRequestTask;

    @SuppressLint("SetTextI18n")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_plain);
        sRobot = Robot.getInstance();
        sRobot.addOnRobotReadyListener((OnRobotReadyListener)this);

        TextView roundTextView = findViewById(R.id.roundTextView);
        playerNameTextView = findViewById(R.id.playerNameTextView);
        ballColorTextView = findViewById(R.id.ballColorTextView);
       // timerTextView = findViewById(R.id.timerTextView);
        scorecardButton = findViewById(R.id.scorecardButton);
        nextPlayerButton = findViewById(R.id.nextPlayerButton);


        playerNames = getIntent().getStringArrayExtra("playerNames");
        ballColors = getIntent().getStringArrayExtra("ballColors");
        noOfRound = getIntent().getIntExtra("selectedRoundCount", 1);
        currentRound = getIntent().getIntExtra("roundCount", 1);
        roundTextView.setText("ROUND " + currentRound );
        showNextPlayerInfo();

        scorecardButton.setOnClickListener(v -> {
            // Execute server script when the button is clicked
            Intent intent = new Intent(ShowPlain.this, wait.class);
            intent.putExtra("selectedRoundCount",noOfRound);
            intent.putExtra("roundCount", currentRound);
            intent.putExtra("playerNames", playerNames);
            intent.putExtra("ballColors", ballColors);
            sendHttpRequestTask = new SendHttpRequestTask();
            sendHttpRequestTask.execute();


            // Pass the current round count

            //roundTextView.setText("Round " +currentRound);
            // = new SendHttpRequestTask();
            //sendHttpRequestTask.execute();
            startActivity(intent);
        });
        nextPlayerButton.setOnClickListener(v -> showNextPlayerInfo());


        // showNextPlayerInfo();
    }
    public void scorecardButtonClicked() {
        Intent intent = new Intent(ShowPlain.this, wait.class);
        intent.putExtra("selectedRoundCount", noOfRound);
        intent.putExtra("roundCount", currentRound);
        intent.putExtra("playerNames", playerNames);
        intent.putExtra("ballColors", ballColors);
        sendHttpRequestTask = new SendHttpRequestTask();
        sendHttpRequestTask.execute();
        startActivity(intent);
    }
    private void askToContinue() {
        sendHttpRequestTask = new SendHttpRequestTask();
        sendHttpRequestTask.execute();


        // Ask the user if they want to continue
        //sRobot.askQuestion("Let's continue, if so, please say yes.");
        //sRobot.askQuestion("Ready to view the results?");

    }


    private void showNextPlayerInfo() {
        if (currentPlayerIndex < playerNames.length) {
            playerNameTextView.setText(playerNames[currentPlayerIndex]);
            ballColorTextView.setText(ballColors[currentPlayerIndex]);
            String textToSpeak = "PLAYER " +playerNames[currentPlayerIndex] + " , STEP FORWARD AND THROW BALL " + ballColors[currentPlayerIndex];
            //sRobot.speak(TtsRequest.create(textToSpeak, true));
            sRobot.speak(TtsRequest.create(textToSpeak, true));
            currentPlayerIndex++;
            //nextPlayerButton.setVisibility(View.VISIBLE);
            if (currentPlayerIndex < playerNames.length) {
                nextPlayerButton.setVisibility(View.VISIBLE);
                scorecardButton.setVisibility(View.GONE);
            } else {
                nextPlayerButton.setVisibility(View.GONE);
                scorecardButton.setVisibility(View.VISIBLE);
            }



        }

        else {

            currentPlayerIndex = 0;
            currentRound++;
            noOfRound++;
            if (currentRound >=0) {

                scorecardButton.setVisibility(View.VISIBLE);
                askToContinue();
                timerTextView.setText("");
                if (timer != null) {
                    timer.cancel();
                }
                return;
            }
        }
        //startTimer();
    }
    /*private void startTimer() {
        //timer = new CountDownTimer(14000, 1000) {
        timer = new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerTextView.setText(String.valueOf(millisUntilFinished / 1000));
            }


            @Override
            public void onFinish() {
                showNextPlayerInfo();
            }

        };
        timer.start();
    }*/

    /*@Override
    public void onAsrResult(@NonNull String asrResult) {
        Log.d(TAG, "ASR Result: " + asrResult);
        asrResult = asrResult.toLowerCase(); // Convert the result to lowercase for easy comparison

        // Define a map of keywords and their corresponding actions
        Map<String, Runnable> actions = new HashMap<>();

        actions.put("yes", this::scorecardButtonClicked);
        actions.put("yeah", this::scorecardButtonClicked);
        actions.put("sure", this::scorecardButtonClicked);

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
        askToContinue();

        // Command not recognized, ask again
        //askQuestion("Sorry, I didn't understand. Please say it again");
        //askQuestion("Apologies, could you repeat it again?");

    }*/
    /*@Override
    public void onAsrResult(@NonNull String asrResult) {
        Log.d(TAG, "ASR Result: " + asrResult);
        switch (asrResult.toLowerCase()) {
            case "yes":
                scorecardButtonClicked();
                sRobot.finishConversation();
                Log.d(TAG, "User said 'yes'. Proceeding...");
                break;
            default:
                askToContinue();
                break;
        }
    }*/
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
    public void onRobotReady(boolean isReady) {
        if (isReady) {
            Log.i(TAG, "Robot is ready");
            sRobot.hideTopBar();
            //askNextQuestion();// hide temi's top action bar when skill is active
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Remove robot event listeners
        sRobot.removeOnRobotReadyListener(this);
        //sRobot.removeAsrListener(this);
    }

    private class SendHttpRequestTask extends AsyncTask<Void, Void, String> {
        private static final int CONNECTION_TIMEOUT = 70000; // 10 seconds
        private static final int READ_TIMEOUT = 70000; // 10 seconds
        @Override
        protected String doInBackground(Void... voids) {
            StringBuilder response = new StringBuilder();
            try {
                URL url = new URL(SERVER_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setRequestMethod("GET");

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                } else {
                    // Handle non-successful response
                    response.append("Error: ").append(responseCode);
                }
                conn.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
                response.append("Error: ").append(e.getMessage());
            }
            return response.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            // Handle the response from the server
            Log.d(TAG, "Server Response: " + result);
            // Optionally, display toast or update UI based on the response
            //Toast.makeText(ShowPlain.this, "Server Response: " + result, Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(timer!=null){
            timer.cancel();
        }
    }
}

