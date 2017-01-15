package com.alexlytvynenko.unlockslideview.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.alexlytvynenko.unlockslideview.UnlockSlideView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final UnlockSlideView unlockSlideView = (UnlockSlideView) findViewById(R.id.view);
        unlockSlideView.setOnUnlockListener(new UnlockSlideView.OnUnlockListener() {
            @Override
            public void onUnlock() {
                Toast.makeText(MainActivity.this, "Unlocked!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
