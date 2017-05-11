package com.waveprogress;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    private WaveProgress mWave;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWave = (WaveProgress) findViewById(R.id.wave);
        mWave.setProgress(50);
    }
}
