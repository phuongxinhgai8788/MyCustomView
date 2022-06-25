package com.example.mycustomview;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private CustomView customView;
    private TextView tvSleepTime;
    private TextView tvWakeTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

        customView.setListener(new CustomView.OnTimeChange() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onWakeupTimeChange(float wakeupTime) {
                int h = (int) wakeupTime;
                int m = (int) ((wakeupTime - h) * 60);
                String s = String.format("%02d:%02d", h, m);
                tvWakeTime.setText(s);
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onSleepTimeChange(float sleepTime) {
                int h = (int) sleepTime;
                int m = (int) ((sleepTime - h) * 60);
                String s = String.format("%02d:%02d", h, m);
                tvSleepTime.setText(s);
            }
        });


    }

    public void init() {
        customView = findViewById(R.id.customView);
        tvSleepTime = findViewById(R.id.tvSleepTime);
        tvWakeTime = findViewById(R.id.tvWakeTime);
    }
}