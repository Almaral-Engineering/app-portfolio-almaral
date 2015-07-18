package com.almareng.appportfolio;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.almareng.appportfolio.Objects.MusicItem;
import com.almareng.appportfolio.Objects.TrackItem;
import com.almareng.appportfolio.services.SpotifyPlayService;

import java.util.ArrayList;


public class SpotifyMainActivity extends AppCompatActivity implements SpotifyMainFragment.MainFragmentCallback, SpotifyTopTracksFragment.SpotifyTopTracksCallback {

    public final static String ARTIST_NAME = "artist_name";

    public final static String TRACKS_KEY = "tracks_key";

    public final static String SEARCH_TEXT_KEY = "search_text_key";

    public final static String TRACK_POSITION = "track_position";

    public final static String CHOSEN_ARTIST = "chosen_artist";

    public final static String TRACKS = "tracks";

    public final static String NOW_PLAYING_TRACK_POSITION = "now_playing_track_position";

    public final static String NOW_PLAYING_TRACK_ID = "now_playing_track_id";

    public final static String NOW_PLAYING_ARTIST_NAME = "now_playing_artist_name";

    public final static String NOW_PLAYING_PREFS = "now_playing_preferences";

    public final static String NOW_PLAYING_STATUS = "now_playing_status";

    public final String TOP_TRACKS_FRAGMENT_TAG = "TTFT";

    public final static String PLAY_FRAGMENT_TAG = "PFT";

    private boolean mTwoPane;

    private Toolbar mSpotifyToolbar;

    private SpotifyPlayService mSpotifyPlayService;

    private boolean mBound;

    private Menu mMenu;

    private boolean mPlayingStatus;

    private ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spotify_main);

        mSpotifyToolbar = (Toolbar) findViewById(R.id.spotify_toolbar);

        mSpotifyToolbar.setTitle(getString(R.string.spotify_streamer));

        setSupportActionBar(mSpotifyToolbar);

        if(findViewById(R.id.top_tracks_container) != null){

            mTwoPane = true;
            if(savedInstanceState == null){

                getSupportFragmentManager().beginTransaction()
                        .add(R.id.top_tracks_container, new SpotifyTopTracksFragment(), TOP_TRACKS_FRAGMENT_TAG)
                        .commit();

            }

        } else{

            mTwoPane = false;

        }

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
    public boolean onPrepareOptionsMenu(Menu menu) {

        if(mBound){

            if (mSpotifyPlayService.playerIsPlaying()) {
                menu.findItem(R.id.action_now_playing).setVisible(true);
            } else {
                menu.findItem(R.id.action_now_playing).setVisible(false);
            }

            if (getResources().getBoolean(R.bool.large_layout) && mSpotifyPlayService.playerIsPlaying()) {

                MenuItem menuItem = menu.findItem(R.id.action_share);

                menuItem.setVisible(true);

                mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

                TrackItem trackItem = mSpotifyPlayService.getCurrentTrack();

                if(trackItem != null) {

                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, String.format(getString(R.string.spotify_share_string), trackItem.getName(), trackItem.getArtistName(), trackItem.getPreviewUrl()));

                    mShareActionProvider.setShareIntent(shareIntent);
                }

            }
        }
        return super.onPrepareOptionsMenu(menu);

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
            startActivity(new Intent(this, SpotifySettingsActivity.class));
            return true;
        } else if(id == R.id.action_now_playing){

            if(mSpotifyPlayService.playerIsPlaying()){

                ArrayList<MusicItem> tracks = mSpotifyPlayService.getTracks();

                if(tracks != null) {

                    if (mTwoPane) {

                        showPlayFragment(tracks, mSpotifyPlayService.getTrackPosition());

                    } else {

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
            } else{
                Toast.makeText(this, getString(R.string.nothing_playing), Toast.LENGTH_SHORT).show();
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(MusicItem chosenArtist) {

        if(mTwoPane){

            Bundle args = new Bundle();

            SpotifyTopTracksFragment fragment = new SpotifyTopTracksFragment();

            args.putParcelable(SpotifyMainActivity.CHOSEN_ARTIST, chosenArtist);

            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.top_tracks_container, fragment, TOP_TRACKS_FRAGMENT_TAG)
                    .commit();

            mSpotifyToolbar.setSubtitle(chosenArtist.getName());

        } else{

            Intent intent = new Intent(this, SpotifyTopTracksActivity.class);

            intent.putExtra(SpotifyMainActivity.CHOSEN_ARTIST, chosenArtist);

            startActivity(intent);

        }

    }

    @Override
    public void showPlayFragment(ArrayList<MusicItem> musicItems, int position) {

        SpotifyPlayFragment spotifyPlayFragment = new SpotifyPlayFragment();

        TrackItem trackItem = (TrackItem) musicItems.get(position);

        String artistName = trackItem.getArtistName();

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(SpotifyMainActivity.TRACKS, musicItems);
        bundle.putString(SpotifyMainActivity.ARTIST_NAME, artistName);
        bundle.putInt(SpotifyMainActivity.TRACK_POSITION, position);

        if(trackItem.getId().equals(mSpotifyPlayService.getTrackId())){
            bundle.putBoolean(SpotifyMainActivity.NOW_PLAYING_STATUS, true);
        }
        else{
            bundle.putBoolean(SpotifyMainActivity.NOW_PLAYING_STATUS, false);
        }
        spotifyPlayFragment.setArguments(bundle);

        spotifyPlayFragment.show(getSupportFragmentManager(), PLAY_FRAGMENT_TAG);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(!mSpotifyPlayService.playerIsPlaying()){
            mSpotifyPlayService.dispatchNotification();
        }
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

            Bundle extras = getIntent().getExtras();

            if(extras != null){
                ArrayList<MusicItem> musicItems = extras.getParcelableArrayList(SpotifyMainActivity.TRACKS);
                mPlayingStatus = extras.getBoolean(SpotifyMainActivity.NOW_PLAYING_STATUS);
                int position = extras.getInt(SpotifyMainActivity.TRACK_POSITION);

                showPlayFragment(musicItems, position);
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
}
