package com.example.gamenew;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /// Ánh xạ các nút bấm

        getSupportActionBar().hide();

        new CountDownTimer(2000, 1000) {
            public void onFinish() {
                Intent next = new Intent(MainActivity.this, DangNhap.class);
                startActivity(next);
            }

            public void onTick(long millisUntilFinished) {
                // millisUntilFinished    The amount of time until finished.
            }
        }.start();

    }



}