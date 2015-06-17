package com.almareng.appportfolio;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);

        setSupportActionBar(mainToolbar);

        Button spotifyBtn = (Button) findViewById(R.id.spotify_btn);

        spotifyBtn.setOnClickListener(this);

        mainToolbar.setTitle(getString(R.string.app_name));

    }


    public void displayToast(View view){

        Button clickedButton = (Button) view;

        String appName = clickedButton.getText().toString();

        Toast.makeText(this, getString(R.string.button_will_launch_app) + appName, Toast.LENGTH_SHORT).show();

    }


    @Override
    public void onClick(View v) {

        int buttonId = v.getId();

        switch(buttonId){

            case R.id.spotify_btn:
                Intent intent = new Intent(this, SpotifyMainActivity.class);
                startActivity(intent);

        }

    }
}
