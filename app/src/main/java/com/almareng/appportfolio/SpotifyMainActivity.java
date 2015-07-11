package com.almareng.appportfolio;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.almareng.appportfolio.Objects.MusicItem;

import java.util.ArrayList;


public class SpotifyMainActivity extends AppCompatActivity implements SpotifyMainFragment.MainFragmentCallback, SpotifyTopTracksFragment.SpotifyTopTracksCallback {

    public final static String ARTIST_NAME = "artist_name";

    public final static String TRACKS_KEY = "tracks_key";

    public final static String SEARCH_TEXT_KEY = "search_text_key";

    public final static String TRACK_POSITION = "track_position";

    public final static String CHOSEN_ARTIST = "chosen_artist";

    public final static String TRACKS = "tracks";

    public final String TOP_TRACKS_FRAGMENT_TAG = "TTFT";

    public final static String PLAY_FRAGMENT_TAG = "PFT";

    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spotify_main);

        Toolbar mSpotifyToolbar = (Toolbar) findViewById(R.id.spotify_toolbar);

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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_spotify_main, menu);
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

        } else{

            Intent intent = new Intent(this, SpotifyTopTracksActivity.class);

            intent.putExtra(SpotifyMainActivity.CHOSEN_ARTIST, chosenArtist);

            startActivity(intent);

        }

    }

    @Override
    public void showPlayFragment(ArrayList<MusicItem> musicItems, int position, String artistName) {

        SpotifyPlayFragment spotifyPlayFragment = new SpotifyPlayFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(SpotifyMainActivity.TRACKS, musicItems);
        bundle.putString(SpotifyMainActivity.ARTIST_NAME, artistName);
        bundle.putInt(SpotifyMainActivity.TRACK_POSITION, position);
        spotifyPlayFragment.setArguments(bundle);

        spotifyPlayFragment.show(getSupportFragmentManager(), PLAY_FRAGMENT_TAG);

    }
}
