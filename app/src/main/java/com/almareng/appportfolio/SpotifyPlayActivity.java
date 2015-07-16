package com.almareng.appportfolio;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.almareng.appportfolio.Objects.MusicItem;

import java.util.ArrayList;

public class SpotifyPlayActivity extends AppCompatActivity{

    public final static String SONG_PREF = "song_preferences";

    private ShareActionProvider mShareActionProvider;

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

        String artistName = extras.getString(SpotifyMainActivity.ARTIST_NAME);

        ArrayList<MusicItem> musicItems = extras.getParcelableArrayList(SpotifyMainActivity.TRACKS);

        if(musicItems != null) {

            if(savedInstanceState == null) {
                SpotifyPlayFragment playFragment = new SpotifyPlayFragment();

                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList(SpotifyMainActivity.TRACKS, musicItems);
                bundle.putString(SpotifyMainActivity.ARTIST_NAME, artistName);
                bundle.putBoolean(SpotifyMainActivity.NOW_PLAYING_STATUS, extras.getBoolean(SpotifyMainActivity.NOW_PLAYING_STATUS));
                bundle.putInt(SpotifyMainActivity.TRACK_POSITION, extras.getInt(SpotifyMainActivity.TRACK_POSITION));
                playFragment.setArguments(bundle);

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.add(R.id.player_container, playFragment);
                transaction.commit();
            }
        }

    }

}
