/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sharkdev98.materialdesignpractice;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

import java.util.concurrent.TimeUnit;

/**
 * Schedule a countdown until a time in the future, with
 * regular notifications on intervals along the way.
 *
 * Example of showing a 30 second countdown in a text field:
 *
 * <pre class="prettyprint">
 * new CountDownTimer(30000, 1000) {
 *
 *     public void onTick(long millisUntilFinished) {
 *         mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);
 *     }
 *
 *     public void onFinish() {
 *         mTextField.setText("done!");
 *     }
 *  }.start();
 * </pre>
 *
 * The calls to {@link #onTick(long)} are synchronized to this object so that
 * one call to {@link #onTick(long)} won't ever occur before the previous
 * callback is complete.  This is only relevant when the implementation of
 * {@link #onTick(long)} takes an amount of time to execute that is significant
 * compared to the countdown interval.
 */
public abstract class mPauseAbleCountDownTimer {


    private String time;

    /**
     * Millis since epoch when alarm should stop.
     */
    private long mMillisInFuture;

    /**
     * The interval in millis that the user receives callbacks
     */
    private final long mCountdownInterval;

    private long mStopTimeInFuture;

    /**
     * boolean representing if the timer was cancelled
     */
    private boolean mCancelled = false;

    public enum TimerState {READY,RUNNING,PAUSE,STOPPED};

    //Running State OR Paused State
    public TimerState mState;


    /**
     * @param countDownInterval The interval along the way to receive
     *   {@link #onTick(long)} callbacks.
     */
    public mPauseAbleCountDownTimer(long countDownInterval) {
        mMillisInFuture = 10000;// By Default Timer = 10 Secs
        time = secondsToString(msToSecond(mMillisInFuture));
        mCountdownInterval = countDownInterval;
        mState = TimerState.READY;
    }

    public boolean isTimerStateReady()
    {
        return mState == TimerState.READY;
    }

    public boolean isTimerStateRunning()
    {
        return mState == TimerState.RUNNING;
    }

    public boolean isTimerStatePause()
    {
        return mState == TimerState.PAUSE;
    }

    public boolean isTimerStateStopped()
    {
        return mState == TimerState.STOPPED;
    }

    public boolean ismCancelled() {
        return mCancelled;
    }

    public long getmMillisInFuture() {
        return mMillisInFuture;
    }

    public void setmMillisInFuture(long seconds) {
        this.mMillisInFuture = sToMilisec(seconds);
        time = secondsToString(seconds);
    }

    public void setTime(String time) {
        this.time = time;
        mMillisInFuture =  sToMilisec(stringToSeconds(time));
    }

    public String getTime() {
        return time;
    }

    public synchronized final void resume(){
        if(isTimerStatePause())
        {
            start();
        }
    }

    public synchronized final void pause(){
        if(isTimerStatePause())return;

        mState = TimerState.PAUSE;
        mMillisInFuture = mStopTimeInFuture - SystemClock.elapsedRealtime();
        if(mMillisInFuture > 1000)
        {
            mMillisInFuture = mMillisInFuture - 1000;
        }
        this.icancel();
    }


    /**
     * Cancel the countdown.
     */
    public synchronized final void cancel() {
        mCancelled = true;
        mHandler.removeMessages(MSG);
        mState = TimerState.STOPPED;
    }


    /**
     * Cancel the countdown.
     */
    private synchronized final void icancel() {
        mCancelled = true;
        mHandler.removeMessages(MSG);
    }

    /**
     * Start the countdown.
     */
    public synchronized final mPauseAbleCountDownTimer start() {
        mCancelled = false;
//My Code Start
        if(isTimerStateRunning())return this;

        if(isTimerStateStopped())
        {
            mMillisInFuture = sToMilisec(stringToSeconds(time));
        }
        mState = TimerState.RUNNING;
//My Code End
        if (mMillisInFuture <= 0) {
            onFinish();
            mState = TimerState.STOPPED;
            return this;
        }
        mStopTimeInFuture = SystemClock.elapsedRealtime() + mMillisInFuture;
        mHandler.sendMessage(mHandler.obtainMessage(MSG));
        return this;
    }


    /**
     * Callback fired on regular interval.
     * @param millisUntilFinished The amount of time until finished.
     */
    public abstract void onTick(long millisUntilFinished);

    /**
     * Callback fired when the time is up.
     */
    public void onFinish(){
        cancel();
    }

    public static long msToSecond(long ms)
    {
        return ms/1000;
    }

    public static long sToMilisec(long ms)
    {
        return ms*1000;
    }

    public static long stringToSeconds(String time)
    {
        long timeToSeconds = 0;

        String[] units = time.split(":"); //will break the string up into an array
        int minutes = Integer.parseInt(units[0]); //first element
        int seconds = Integer.parseInt(units[1]); //second element
        timeToSeconds = 60 * minutes + seconds; //add up our values

        return timeToSeconds;
    }

    public static String secondsToString(long seconds) {
        long minute = TimeUnit.SECONDS.toMinutes(seconds) - (TimeUnit.SECONDS.toHours(seconds)* 60);
        long second = TimeUnit.SECONDS.toSeconds(seconds) - (TimeUnit.SECONDS.toMinutes(seconds) *60);

        System.out.println(" Minute " + minute + " Seconds " + second);
        return minute + ":" + second;
    }




    private static final int MSG = 1;


    // handles counting down
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            synchronized (mPauseAbleCountDownTimer.this) {
                if (mCancelled) {
                    return;
                }

                final long millisLeft = mStopTimeInFuture - SystemClock.elapsedRealtime();

                if (millisLeft <= 0) {
                    onFinish();
                    mState = TimerState.STOPPED;
                } else {
                    long lastTickStart = SystemClock.elapsedRealtime();
                    onTick(millisLeft);

                    // take into account user's onTick taking time to execute
                    long lastTickDuration = SystemClock.elapsedRealtime() - lastTickStart;
                    long delay;

                    if (millisLeft < mCountdownInterval) {
                        // just delay until done
                        delay = millisLeft - lastTickDuration;

                        // special case: user's onTick took more than interval to
                        // complete, trigger onFinish without delay
                        if (delay < 0) delay = 0;
                    } else {
                        delay = mCountdownInterval - lastTickDuration;

                        // special case: user's onTick took more than interval to
                        // complete, skip to next interval
                        while (delay < 0) delay += mCountdownInterval;
                    }

                    sendMessageDelayed(obtainMessage(MSG), delay);
                }
            }
        }
    };
}
