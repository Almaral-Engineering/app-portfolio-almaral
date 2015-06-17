package com.almareng.appportfolio;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.almareng.appportfolio.Objects.TrackItem;
import com.squareup.picasso.Picasso;

public class SpotifyPlayActivity extends AppCompatActivity {

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

        TrackItem trackItem = extras.getParcelable(SpotifyMainActivity.TRACK_DATA);

        TextView albumAndTrackTxt = (TextView) findViewById(R.id.album_and_track);

        ImageView albumImage = (ImageView) findViewById(R.id.album_image);

        albumAndTrackTxt.setText(trackItem.getName() + "\n" + trackItem.getAlbumName());

        Picasso.with(this).load(trackItem.getBigImageUrl()).placeholder(R.mipmap.ic_launcher).into(albumImage);

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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
