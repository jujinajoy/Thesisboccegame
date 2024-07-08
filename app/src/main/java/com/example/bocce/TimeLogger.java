package com.example.bocce;

public class TimeLogger {
    private long startTime;
    private long endTime;

    public void startLogging(){
        startTime= System.currentTimeMillis();
    }
    public void stopLogging(){
        endTime=System.currentTimeMillis();
    }
    public long getLoggedTime(){
        return endTime-startTime;
    }
    public void setStartTime(long startTime){
        this.startTime = startTime;
    }

    public long getStartTime() {
        return startTime;
    }
}
