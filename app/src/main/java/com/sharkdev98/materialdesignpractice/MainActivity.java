package com.sharkdev98.materialdesignpractice;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.sharkdev98.materialdesignpractice.databinding.ActivityMainBinding;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener, View.OnFocusChangeListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "MainActivity";
    ActivityMainBinding act;
    InputFilter[] editTextFilterForSeconds;
    InputFilter[] editTextFilterForMinutes;
    boolean timerRunningBit;
    String timerRunning;
    String OnTime;
    String OffTime;
    boolean looping;
    int minSeconds = 1;
    int minMinutes = 0;
    int maxSeconds = 59;
    int maxMinutes = 59;

    mPauseAbleCountDownTimer t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        timerRunningBit = false;
        timerRunning = "Break Timer";
        looping = true;

        act = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(act.getRoot());

        editTextFilterForSeconds = new InputFilter[]{ new InputFilterMinMax("0", "59")};
        editTextFilterForMinutes = new InputFilter[]{ new InputFilterMinMax("0", "59")};

        act.onTimerMinTextInputEditText.setFilters(editTextFilterForMinutes);
        act.offTimerMinTextInputEditText.setFilters(editTextFilterForMinutes);

        act.onTimerSecTextInputEditText.setFilters(editTextFilterForSeconds);
        act.offTimerSecTextInputEditText.setFilters(editTextFilterForSeconds);

        act.startTimerMaterialButton.setOnClickListener(this);
        act.stopResetTimerMaterialButton.setOnClickListener(this);
        act.stopResetTimerMaterialButton.setOnLongClickListener(this);
        act.onTimerMinTextInputEditText.setOnFocusChangeListener(this);
        act.onTimerSecTextInputEditText.setOnFocusChangeListener(this);
        act.offTimerMinTextInputEditText.setOnFocusChangeListener(this);
        act.offTimerSecTextInputEditText.setOnFocusChangeListener(this);
        act.rootViewGridLayout.setOnFocusChangeListener(this);
        act.loopingCheckBox.setOnCheckedChangeListener(this);

        t = new mPauseAbleCountDownTimer(1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                String mmss = secondsToString(mPauseAbleCountDownTimer.msToSecond(millisUntilFinished));
                act.runningTimerMaterialTextView.setText(timerRunning +" "+ mmss);
            }

            @Override
            public void onFinish() {
                super.onFinish();
                Snackbar.make(act.getRoot(), "Finished", Snackbar.LENGTH_SHORT).show();
                if(timerRunningBit == true)
                {
                    setOffTimer();
                    t.start();
                }
                else if(looping == true)
                {
                    setOnTimer();
                    t.start();
                }
                else
                {
                    resetAll();
                }
            }
        };
    }

    void setOffTimer()
    {
        timerRunning = "Break Timer";
        timerRunningBit = false;
        t.setTime(OffTime);
    }

    void setOnTimer()
    {
        timerRunning = "Lecture Timer";
        timerRunningBit = true;
        t.setTime(OnTime);
    }

    void handleTimeSwitcher()
    {
        if(!timerRunningBit)
        {
            setOnTimer();
        }
        else if(timerRunningBit)
        {
            setOffTimer();
        }
    }

    void handleTimer()
    {
        Log.d(TAG, "onClick: "+ "Start Timer");
        if(act.startTimerMaterialButton.getText().toString().equals(getString(R.string.startTimerButtonText))
                || act.startTimerMaterialButton.getText().toString().equals(getString(R.string.resumeTimerButtonText)))
        {
            enableDisableAllTextInputEditText(false);

            if(t.isTimerStateReady() || t.isTimerStateStopped())
            {
                getOnOffTimeFromUI();
                handleTimeSwitcher();
                t.start();
            }
            else if(t.isTimerStatePause())
            {
                t.resume();
            }
            act.startTimerMaterialButton.setText(getString(R.string.pauseTimerButtonText));
        }
        else if(act.startTimerMaterialButton.getText().toString().equals(getString(R.string.pauseTimerButtonText)))
        {
            t.pause();
            act.startTimerMaterialButton.setText(R.string.resumeTimerButtonText);
            //enableDisableAllTextInputEditText(false);
        }
    }

    @Override
    public void onClick(View v) {
        hideKeyboard(v);
        switch(v.getId())
        {
            case R.id.start_timerMaterialButton:
                handleTimer();
                break;

            case R.id.stop_reset_timerMaterialButton:
                t.cancel();
                timerRunningBit = false;
                act.runningTimerMaterialTextView.setText("Time Switcher");
                Log.d(TAG, "onClick: "+ "Stop Timer");
                enableDisableAllTextInputEditText(true);
                act.startTimerMaterialButton.setText(R.string.startTimerButtonText);
                break;

        }
    }

    @Override
    public boolean onLongClick(View v) {
        hideKeyboard(v);
        act.runningTimerMaterialTextView.setText("Time Switcher");
        switch (v.getId())
        {
            case R.id.stop_reset_timerMaterialButton:
                Log.d(TAG, "onLongClick: "+"Reset Timer");
                resetAll();
                break;
        }
        return false;
    }

    public void getOnOffTimeFromUI()
    {
        OnTime = act.onTimerMinTextInputEditText.getText().toString()+":"+act.onTimerSecTextInputEditText.getText().toString();
        OffTime = act.offTimerMinTextInputEditText.getText().toString()+":"+act.offTimerSecTextInputEditText.getText().toString();
    }

    public void resetAll()
    {
        timerRunningBit = false;
        act.runningTimerMaterialTextView.setText("Time Switcher");
        act.startTimerMaterialButton.setText(R.string.startTimerButtonText);
        enableDisableAllTextInputEditText(true);
        t.cancel();
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    void enableDisableAllTextInputEditText(boolean enableDisableBit)
    {
        if(!enableDisableBit) setMinValueIfEmpty();
        act.onTimerMinTextInputEditText.setEnabled(enableDisableBit);
        act.onTimerSecTextInputEditText.setEnabled(enableDisableBit);
        act.offTimerMinTextInputEditText.setEnabled(enableDisableBit);
        act.offTimerSecTextInputEditText.setEnabled(enableDisableBit);
    }

    void setMinValueIfEmpty()
    {
        //min
        if(act.onTimerMinTextInputEditText.getText().toString().isEmpty())
        {
            act.onTimerMinTextInputEditText.setText(Integer.toString(minMinutes));
        }
        if(act.offTimerMinTextInputEditText.getText().toString().isEmpty())
        {
            act.offTimerMinTextInputEditText.setText(Integer.toString(minMinutes));
        }

        //sec
        if(act.onTimerSecTextInputEditText.getText().toString().isEmpty())
        {
            if(Integer.parseInt(act.onTimerMinTextInputEditText.getText().toString()) > 0)
            {
                act.onTimerSecTextInputEditText.setText(Integer.toString(0));
            }
            else
            {
                act.onTimerSecTextInputEditText.setText(Integer.toString(minSeconds));
            }
        }
        if(act.offTimerSecTextInputEditText.getText().toString().isEmpty())
        {
            if(Integer.parseInt(act.offTimerMinTextInputEditText.getText().toString()) > 0)
            {
                act.offTimerSecTextInputEditText.setText(Integer.toString(0));
            }
            else
            {
                act.offTimerSecTextInputEditText.setText(Integer.toString(minSeconds));
            }
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(!hasFocus)
        {
            hideKeyboard(v);
        }
        if(hasFocus && v instanceof TextInputEditText)
        {
            switch (v.getId())
            {
                case R.id.onTimerMinTextInputEditText:
                    act.runningTimerMaterialTextView.setText("Lecture Time (min)");
                    break;
                case R.id.onTimerSecTextInputEditText:
                    act.runningTimerMaterialTextView.setText("Lecture Time (sec)");
                    break;
                case R.id.offTimerMinTextInputEditText:
                    act.runningTimerMaterialTextView.setText("Break Time (min)");
                    break;
                case R.id.offTimerSecTextInputEditText:
                    act.runningTimerMaterialTextView.setText("Break Time (sec)");
                    break;
            }
        }
        if(hasFocus && !(v instanceof TextInputEditText) && !(t.isTimerStateRunning()))
        {
            act.runningTimerMaterialTextView.setText("Time Switcher");
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(buttonView.getId() == R.id.loopingCheckBox)
        {
            if(isChecked)
            {
                looping = true;
            }
            else
            {
                looping = false;
            }
        }
    }

    public enum TimerState {READY,RUNNING,PAUSE,STOPPED}

    //Input Filter Class in EditText
    public class InputFilterMinMax implements InputFilter {

        private int min, max;

        public InputFilterMinMax(int min, int max) {
            this.min = min;
            this.max = max;
        }

        public InputFilterMinMax(String min, String max) {
            this.min = Integer.parseInt(min);
            this.max = Integer.parseInt(max);
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            try {
                int input = Integer.parseInt(dest.toString() + source.toString());
                if (isInRange(min, max, input))
                    return null;
            } catch (NumberFormatException nfe) { }
            return "";
        }

        private boolean isInRange(int a, int b, int c) {
            return b > a ? c >= a && c <= b : c >= b && c <= a;
        }
    }

}