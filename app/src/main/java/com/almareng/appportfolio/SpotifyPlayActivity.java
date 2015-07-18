package com.almareng.appportfolio;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.almareng.appportfolio.Objects.MusicItem;

import java.util.ArrayList;

public class SpotifyPlayActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spotify_play);

        Toolbar playerToolbar = (Toolbar) findViewById(R.id.player_toolbar);

        playerToolbar.setTitle(getString(R.string.spotify_streamer));

        setSupportActionBar(playerToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        playerToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Bundle extras = getIntent().getExtras();

        ArrayList<MusicItem> musicItems = extras.getParcelableArrayList(SpotifyMainActivity.TRACKS);

        if(musicItems != null) {

            if(savedInstanceState == null) {
                SpotifyPlayFragment playFragment = new SpotifyPlayFragment();

                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList(SpotifyMainActivity.TRACKS, musicItems);
                bundle.putBoolean(SpotifyMainActivity.NOW_PLAYING_STATUS, extras.getBoolean(SpotifyMainActivity.NOW_PLAYING_STATUS));
                bundle.putInt(SpotifyMainActivity.TRACK_POSITION, extras.getInt(SpotifyMainActivity.TRACK_POSITION));
                playFragment.setArguments(bundle);

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.add(R.id.player_container, playFragment);
                transaction.commit();
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_spotify_play, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            startActivity(new Intent(this, SpotifySettingsActivity.class));

            return true;

        }
        return super.onOptionsItemSelected(item);
    }

}
