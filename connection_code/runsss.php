<?php
// Set the working directory to the location of your Python script
chdir("C:/Users/Jujina Joy/Downloads/project_ball/project_ball/");

// Set any necessary environment variables
putenv("MY_VARIABLE=value");

// Command to execute the Python script with timeout
$command = 'python "detect_ball.py" --conf 0.25 --weights best.pt --source 1';

// Execute the command and capture the output or errors
$output = shell_exec($command);

// Check if command execution was successful
if ($output === null) {
    // Command execution failed, handle error
    $error = error_get_last();
    echo "Failed to execute command. Error: " . $error['message'] . "\n";
} else {
    // Command executed successfully, output the result
    echo $output;
}
?>
