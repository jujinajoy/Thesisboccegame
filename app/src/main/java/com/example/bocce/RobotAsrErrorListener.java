package com.example.bocce;
import org.jetbrains.annotations.NotNull;


public interface RobotAsrErrorListener {
    void onAsrError(int errorCode, @NotNull String errorMessage);
}
