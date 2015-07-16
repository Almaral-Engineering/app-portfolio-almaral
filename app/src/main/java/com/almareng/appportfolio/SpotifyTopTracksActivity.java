package com.almareng.appportfolio;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.almareng.appportfolio.Objects.MusicItem;
import com.almareng.appportfolio.services.SpotifyPlayService;

import java.util.ArrayList;


public class SpotifyTopTracksActivity extends AppCompatActivity {

    private SpotifyPlayService mSpotifyPlayService;
    private boolean mBound = false;

    private Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spotify_top_tracks);

        Toolbar topTracksToolbar = (Toolbar) findViewById(R.id.top_tracks_toolbar);

        topTracksToolbar.setTitle(getString(R.string.top_tracks));

        setSupportActionBar(topTracksToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        topTracksToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Bundle extras = getIntent().getExtras();

        MusicItem chosenArtist = extras.getParcelable(SpotifyMainActivity.CHOSEN_ARTIST);

        Bundle arguments = new Bundle();

        if(chosenArtist != null) {

            arguments.putParcelable(SpotifyMainActivity.CHOSEN_ARTIST, chosenArtist);

            topTracksToolbar.setTitle(chosenArtist.getName());

        }

        SpotifyTopTracksFragment spotifyTopTracksFragment = new SpotifyTopTracksFragment();

        spotifyTopTracksFragment.setArguments(arguments);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.top_tracks_container, spotifyTopTracksFragment)
                    .commit();
        }

    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if(mBound){

            if (mSpotifyPlayService.playerIsPlaying()) {
                menu.findItem(R.id.action_now_playing).setVisible(true);
            } else {
                menu.findItem(R.id.action_now_playing).setVisible(false);
            }

        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = new Intent(this, SpotifyPlayService.class);
        getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        startService(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(mBound){

            getApplicationContext().unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_spotify_main, menu);
        mMenu = menu;
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
            return true;
        } else if(id == R.id.action_now_playing){
            if(mSpotifyPlayService.playerIsPlaying()){

                ArrayList<MusicItem> tracks = mSpotifyPlayService.getTracks();

                if(tracks != null) {

                    SharedPreferences nowPlayingPrefs = getSharedPreferences(SpotifyMainActivity.NOW_PLAYING_PREFS, 0);

                    Intent intent = new Intent(this, SpotifyPlayActivity.class);

                    intent.putParcelableArrayListExtra(SpotifyMainActivity.TRACKS, tracks);
                    int currentTrackPosition = mSpotifyPlayService.getTrackPosition();
                    intent.putExtra(SpotifyMainActivity.TRACK_POSITION, currentTrackPosition);
                    intent.putExtra(SpotifyMainActivity.ARTIST_NAME, nowPlayingPrefs.getString(SpotifyMainActivity.NOW_PLAYING_ARTIST_NAME, ""));
                    intent.putExtra(SpotifyMainActivity.NOW_PLAYING_STATUS, true);

                    startActivity(intent);

                }
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public String getCurrentTrackId(){

        return mSpotifyPlayService.getTrackId();

    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {

            SpotifyPlayService.SpotifyPlayBinder binder = (SpotifyPlayService.SpotifyPlayBinder) service;
            mSpotifyPlayService = binder.getService();

            if(mMenu != null) {
                if (mSpotifyPlayService.playerIsPlaying()) {
                    mMenu.findItem(R.id.action_now_playing).setVisible(true);
                } else {
                    mMenu.findItem(R.id.action_now_playing).setVisible(false);
                }
            }

            mBound = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

}
