package com.sunmeng.mediaplayer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            MediaPlayerFragment fragment;
            if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
                fragment = MediaPlayerFragment.getInstance(getIntent().getData().getPath());
            } else {
                fragment = MediaPlayerFragment.getInstance(Uri.parse(Constants.REMOTE_URL));
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.player_fragment, fragment)
                    .commit();
        }
    }
}
