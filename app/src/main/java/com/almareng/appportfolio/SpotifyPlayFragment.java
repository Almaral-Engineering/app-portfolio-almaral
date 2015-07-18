package com.almareng.appportfolio;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.almareng.appportfolio.Objects.MusicItem;
import com.almareng.appportfolio.Objects.TrackItem;
import com.almareng.appportfolio.services.SpotifyPlayService;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class SpotifyPlayFragment extends DialogFragment implements View.OnClickListener{

    private SpotifyPlayService mSpotifyPlayService;
    private boolean mBound = false;

    private final String LOG_TAG = SpotifyPlayActivity.class.getSimpleName();
    private final String PLAYING_FLAG = "playing_flag";
    private final String CURRENT_TRACK_POSITION = "current_track_position";

    private Handler mHandler;
    private Runnable mRunnable;

    private boolean mPlaying;
    private boolean mWasPaused;

    private ImageButton mPlayButton;
    private ImageButton mNextButton;
    private ImageButton mPrevButton;

    private ProgressBar mLoadingWheel;
    private SeekBar mPlayBar;
    private TextView mTrackProgress;
    private TextView mTrackDuration;

    private ArrayList<MusicItem> mMusicItems;
    private int mTrackPosition;
    private TrackItem mTrackItem;

    private int mPlayerCurrentPosition = 0;

    private TextView mTrackTxt;
    private TextView mArtistTxt;
    private TextView mAlbumTxt;
    private ImageView mAlbumImage;

    private ShareActionProvider mShareActionProvider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        mWasPaused = false;

        mPlaying = getArguments().getBoolean(SpotifyMainActivity.NOW_PLAYING_STATUS);

        mMusicItems = getArguments().getParcelableArrayList(SpotifyMainActivity.TRACKS);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_spotify_play, container, false);

        mArtistTxt = (TextView) rootView.findViewById(R.id.artist_name);
        mTrackTxt = (TextView) rootView.findViewById(R.id.track);
        mAlbumTxt = (TextView) rootView.findViewById(R.id.album);
        mAlbumImage = (ImageView) rootView.findViewById(R.id.album_image);

        mPlayBar = (SeekBar) rootView.findViewById(R.id.play_bar);
        mTrackProgress = (TextView) rootView.findViewById(R.id.progress_text);
        mTrackDuration = (TextView) rootView.findViewById(R.id.song_duration);

        mPlayButton = (ImageButton) rootView.findViewById(R.id.button_play);
        mNextButton = (ImageButton) rootView.findViewById(R.id.button_next);
        mPrevButton = (ImageButton) rootView.findViewById(R.id.button_previous);

        mPlayButton.setOnClickListener(this);
        mNextButton.setOnClickListener(this);
        mPrevButton.setOnClickListener(this);

        mLoadingWheel = (ProgressBar) rootView.findViewById(R.id.loading_wheel);

        mPlayBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                mPlayerCurrentPosition = progress * 1000;

                if ((mPlayerCurrentPosition / 1000) < 10)
                    mTrackProgress.setText("0:0" + mPlayerCurrentPosition / 1000);
                else
                    mTrackProgress.setText("0:" + mPlayerCurrentPosition / 1000);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();

                mPlayerCurrentPosition = progress * 1000;

                if (mSpotifyPlayService.playerIsPlaying()) {
                    mSpotifyPlayService.seekTo(mPlayerCurrentPosition);

                    if ((mPlayerCurrentPosition / 1000) < 10)
                        mTrackProgress.setText("0:0" + mPlayerCurrentPosition / 1000);
                    else
                        mTrackProgress.setText("0:" + mPlayerCurrentPosition / 1000);

                } else {

                    mPlayBar.setProgress(0);
                    Toast.makeText(getActivity(), "Wait fot the song to start", Toast.LENGTH_SHORT).show();

                }

            }
        });

        mPlayBar.setProgress(0);

        if(savedInstanceState != null){

            mTrackPosition = savedInstanceState.getInt(CURRENT_TRACK_POSITION);

            mPlaying = savedInstanceState.getBoolean(PLAYING_FLAG);

        } else{

            mTrackPosition = getArguments().getInt(SpotifyMainActivity.TRACK_POSITION);

        }

        mTrackItem = (TrackItem) mMusicItems.get(mTrackPosition);

        setViews(mTrackItem);

        if(mPlaying){

            mPlayButton.setImageResource(android.R.drawable.ic_media_pause);

        }

        return rootView;
    }

    public void setViews(TrackItem trackItem) {

        mTrackItem = trackItem;

        if (mTrackItem != null) {

            mArtistTxt.setText(mTrackItem.getArtistName());

            mTrackTxt.setText(mTrackItem.getName());
            mAlbumTxt.setText(mTrackItem.getAlbumName());
            Picasso.with(getActivity()).load(mTrackItem.getBigImageUrl()).placeholder(R.mipmap.ic_launcher).into(mAlbumImage);

            mPlayBar.setMax(30);

            SharedPreferences playingNowPref = getActivity().getApplicationContext().getSharedPreferences(SpotifyMainActivity.NOW_PLAYING_PREFS, 0);

            SharedPreferences.Editor editor = playingNowPref.edit();

            editor.putString(SpotifyMainActivity.NOW_PLAYING_ARTIST_NAME, mTrackItem.getArtistName());
            editor.putInt(SpotifyMainActivity.NOW_PLAYING_TRACK_POSITION, mTrackPosition);
            editor.putString(SpotifyMainActivity.NOW_PLAYING_TRACK_ID, trackItem.getId());
            editor.apply();

        }
    }

    @Override
    public void onClick(View v) {

        int buttonId = v.getId();

        switch (buttonId) {

            case R.id.button_play:
                if (mPlaying) {

                    if(mBound && mSpotifyPlayService.playerIsPlaying()) {
                        mPlayButton.setImageResource(android.R.drawable.ic_media_play);
                        mPlaying = false;
                        mWasPaused = true;

                        mSpotifyPlayService.pauseTrack();
                    }

                } else {

                    mPlayButton.setImageResource(android.R.drawable.ic_media_pause);
                    mPlaying = true;

                    if(mWasPaused){
                        mSpotifyPlayService.resumeTrack();
                        mWasPaused = false;
                    } else {
                        mSpotifyPlayService.setTrack(mTrackPosition);
                        mSpotifyPlayService.playTrack();
                    }

                }
                break;

            case R.id.button_next:
                if (mMusicItems.size() > (mTrackPosition + 1)) {
                    ++mTrackPosition;
                }
                else {
                    mTrackPosition = 0;
                }

                setTrackToPlay();

                break;

            case R.id.button_previous:
                if (mTrackPosition > 0 ) {
                    --mTrackPosition;
                }
                else {
                    mTrackPosition = mMusicItems.size() - 1;
                }

                setTrackToPlay();

                break;
        }

    }

    public void setTrackToPlay() {

        mPlaying = true;
        mWasPaused = false;

        mSpotifyPlayService.setTrack(mTrackPosition);

        mSpotifyPlayService.playTrack();
        mPlayButton.setImageResource(android.R.drawable.ic_media_pause);

        mTrackItem = (TrackItem) mMusicItems.get(mTrackPosition);

        setViews(mTrackItem);

    }

    @Override
    public void onStart() {
        super.onStart();

        Intent intent = new Intent(getActivity(), SpotifyPlayService.class);
        intent.setAction(SpotifyPlayService.ACTION_PLAY );
        getActivity().getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        getActivity().startService(intent);

    }

    @Override
    public void onStop() {
        super.onStop();

        if(mHandler != null) {
            mHandler.removeCallbacks(mRunnable);
        }

        if(!mSpotifyPlayService.playerIsPlaying()){
            mSpotifyPlayService.dispatchNotification();
        }

        if(mBound){

            getActivity().getApplicationContext().unbindService(mConnection);
            mBound = false;

        }

        mPlaying = true;

    }

    private int timerCount = 0;

    public void animateSeekBar(){

        mHandler = new Handler();

        mRunnable = new Runnable() {
            public void run() {

                mPlayerCurrentPosition = mSpotifyPlayService.getPlayerPosition();
                int currentPositionInSecs = mPlayerCurrentPosition / 1000;
                mPlayBar.setProgress(currentPositionInSecs);

                if(mTrackPosition != mSpotifyPlayService.getTrackPosition()) {
                    if (mMusicItems.size() > (mTrackPosition + 1)) {
                        ++mTrackPosition;
                    }
                    else {
                        mTrackPosition = 0;
                    }
                    setTrackToPlay();
                }

                ++timerCount;
                if(timerCount == 5) {
                    if (currentPositionInSecs < 10)
                        mTrackProgress.setText("0:0" + currentPositionInSecs);
                    else
                        mTrackProgress.setText("0:" + currentPositionInSecs);

                    timerCount = 0;
                }

                mHandler.postDelayed(this, 200);

            }
        };

        mHandler.postDelayed(mRunnable, 200);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putBoolean(PLAYING_FLAG, mPlaying);
        outState.putInt(CURRENT_TRACK_POSITION, mTrackPosition);

        super.onSaveInstanceState(outState);
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {

            SpotifyPlayService.SpotifyPlayBinder binder = (SpotifyPlayService.SpotifyPlayBinder) service;
            mSpotifyPlayService = binder.getService();
            mSpotifyPlayService.setListOfTracks(mMusicItems);
            mBound = true;

            if(!mPlaying) {
                mPlayButton.setImageResource(android.R.drawable.ic_media_pause);
                mSpotifyPlayService.setTrack(mTrackPosition);
                mSpotifyPlayService.playTrack();
                mPlaying = true;
            } else if(mTrackPosition != mSpotifyPlayService.getTrackPosition() && !mWasPaused) {
                mTrackPosition = mSpotifyPlayService.getTrackPosition();
                mTrackItem = (TrackItem) mMusicItems.get(mTrackPosition);
                mPlayButton.setImageResource(android.R.drawable.ic_media_pause);
                setViews(mTrackItem);
            } else if(mWasPaused){
                mPlaying = false;
            } else if(!mSpotifyPlayService.playerIsPlaying()){
                mPlayButton.setImageResource(android.R.drawable.ic_media_pause);
                mSpotifyPlayService.setTrack(mTrackPosition);
                mSpotifyPlayService.playTrack();
                mPlaying = true;
            }

            if(mShareActionProvider != null){

                mShareActionProvider.setShareIntent(createShareTrackIntent());

            }

            animateSeekBar();

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_spotify_share, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);

        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if(mTrackItem != null){

            mShareActionProvider.setShareIntent(createShareTrackIntent());

        }
    }

    private Intent createShareTrackIntent(){

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, String.format(getString(R.string.spotify_share_string),mTrackItem.getName(), mTrackItem.getArtistName(), mTrackItem.getPreviewUrl()));

        return shareIntent;

    }

}
