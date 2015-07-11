package com.almareng.appportfolio;

import android.support.v4.app.DialogFragment;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.almareng.appportfolio.Objects.MusicItem;
import com.almareng.appportfolio.Objects.TrackItem;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

public class SpotifyPlayFragment extends DialogFragment implements View.OnClickListener{

    private final String LOG_TAG = SpotifyPlayActivity.class.getSimpleName();
    private final String PLAYING_FLAG = "playing_flag";
    private final String PLAYER_POSITION = "playing_position";
    private final String CURRENT_TRACK_POSITION = "current_track_position";

    private String mPreviewUrl;

    private MediaPlayer mMediaPlayer;

    private boolean mPlaying = false;

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
    private String mArtistName;

    private int mPlayerCurrentPosition = 0;

    private TextView mTrackTxt;
    private TextView mArtistTxt;
    private TextView mAlbumTxt;
    private ImageView mAlbumImage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMusicItems = getArguments().getParcelableArrayList(SpotifyMainActivity.TRACKS);

        mArtistName = getArguments().getString(SpotifyMainActivity.ARTIST_NAME);

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

                if((mPlayerCurrentPosition/1000) < 10)
                    mTrackProgress.setText("0:0" + mPlayerCurrentPosition/1000);
                else
                    mTrackProgress.setText("0:" + mPlayerCurrentPosition/1000);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();

                mPlayerCurrentPosition = progress * 1000;
                if(mMediaPlayer != null){
                    mMediaPlayer.seekTo(mPlayerCurrentPosition);
                }

                if((mPlayerCurrentPosition/1000) < 10)
                    mTrackProgress.setText("0:0" + mPlayerCurrentPosition/1000);
                else
                    mTrackProgress.setText("0:" + mPlayerCurrentPosition/1000);

            }
        });

        if(savedInstanceState != null){

            mTrackPosition = savedInstanceState.getInt(CURRENT_TRACK_POSITION);

            mPlaying = savedInstanceState.getBoolean(PLAYING_FLAG);
            mPlayerCurrentPosition = savedInstanceState.getInt(PLAYER_POSITION);

            if((mPlayerCurrentPosition/1000) < 10)
                mTrackProgress.setText("0:0" + mPlayerCurrentPosition/1000);
            else
                mTrackProgress.setText("0:" + mPlayerCurrentPosition/1000);

        } else{

            mTrackPosition = getArguments().getInt(SpotifyMainActivity.TRACK_POSITION);

        }

        mTrackItem = (TrackItem) mMusicItems.get(mTrackPosition);

        setViews(mTrackItem);

        if(mPlaying){

            mPlayButton.setImageResource(android.R.drawable.ic_media_pause);
            if(mMediaPlayer == null)
                playPreview();
            else
                mMediaPlayer.start();

        }

        return rootView;
    }

    public void setViews(TrackItem trackItem) {

        mTrackItem = trackItem;

        if (mTrackItem != null) {

            mPreviewUrl = mTrackItem.getPreviewUrl();

            if(mArtistName == null) {
                mArtistName = getArguments().getString(SpotifyMainActivity.ARTIST_NAME);
            }
            else {
                mArtistTxt.setText(mArtistName);
            }
            mTrackTxt.setText(mTrackItem.getName());
            mAlbumTxt.setText(mTrackItem.getAlbumName());
            Picasso.with(getActivity()).load(mTrackItem.getBigImageUrl()).placeholder(R.mipmap.ic_launcher).into(mAlbumImage);

            mPlayBar.setMax(30);
        }
    }

    @Override
    public void onClick(View v) {

        int buttonId = v.getId();

        switch (buttonId) {

            case R.id.button_play:
                if (mPlaying) {

                    if(mMediaPlayer.isPlaying()) {
                        mPlayButton.setImageResource(android.R.drawable.ic_media_play);
                        mPlaying = false;

                        if(mMediaPlayer != null) {
                            mMediaPlayer.pause();
                        }
                    }

                } else {

                    mPlayButton.setImageResource(android.R.drawable.ic_media_pause);
                    mPlaying = true;

                    if (mMediaPlayer == null)
                        playPreview();
                    else
                        mMediaPlayer.start();

                }
                break;

            case R.id.button_next:
                if (mMusicItems.size() > (mTrackPosition + 1))
                    ++mTrackPosition;
                else
                    mTrackPosition = 0;

                setTrackToPlay();

                break;

            case R.id.button_previous:
                if (mTrackPosition > 0 )
                    --mTrackPosition;
                else
                    mTrackPosition = mMusicItems.size() - 1;

                setTrackToPlay();

                break;
        }

    }

    public void setTrackToPlay(){

        mTrackItem = (TrackItem) mMusicItems.get(mTrackPosition);

        setViews(mTrackItem);

        mPlayerCurrentPosition = 0;
        mTrackProgress.setText(getString(R.string.zero_time));
        mPlayBar.setProgress(0);

        stopMediaPlayer();

        mPlayerCurrentPosition = 0;

        if(mPlaying) {
            playPreview();
        }

    }

    public void playPreview(){

        try{
            mLoadingWheel.setVisibility(View.VISIBLE);
            String url = mPreviewUrl;
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDataSource(url);
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    int trackDuration = mMediaPlayer.getDuration();
                    mPlayBar.setMax(trackDuration/1000);
                    mTrackDuration.setText("0:" + trackDuration/1000);
                    mMediaPlayer.seekTo(mPlayerCurrentPosition);
                    mMediaPlayer.start();
                    mLoadingWheel.setVisibility(View.GONE);
                    animateSeekBar();
                }
            });
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if(mLoadingWheel.getVisibility() == View.GONE) {
                        mPlayButton.setImageResource(android.R.drawable.ic_media_play);
                        mPlaying = false;
                        mPlayerCurrentPosition = 0;
                        mTrackProgress.setText(getString(R.string.zero_time));
                        mPlayBar.setProgress(0);
                        mMediaPlayer.seekTo(0);
                    }
                }
            });

        } catch (IllegalArgumentException | IOException e){

            mLoadingWheel.setVisibility(View.GONE);
            Log.e(LOG_TAG, getString(R.string.IO_exception_media_stream));

        }

    }

    private Handler mHandler;

    public void animateSeekBar(){

        mHandler = new Handler();

        final Runnable runnable = new Runnable() {
            public void run() {
                if (mMediaPlayer != null) {
                    mPlayerCurrentPosition = mMediaPlayer.getCurrentPosition();
                    int currentPositionInSecs = mPlayerCurrentPosition / 1000;
                    mPlayBar.setProgress(currentPositionInSecs);
                    if (currentPositionInSecs < 10)
                        mTrackProgress.setText("0:0" + currentPositionInSecs);
                    else
                        mTrackProgress.setText("0:" + currentPositionInSecs);

                    mHandler.postDelayed(this, 1000);
                }
            }
        };

        mHandler.postDelayed(runnable, 1000);

    }

    @Override
    public void onDestroy() {
        stopMediaPlayer();
        super.onDestroy();
    }

    public void stopMediaPlayer(){
        if(mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putBoolean(PLAYING_FLAG, mPlaying);
        outState.putInt(CURRENT_TRACK_POSITION, mTrackPosition);

        if(mMediaPlayer != null) {
            outState.putInt(PLAYER_POSITION, mMediaPlayer.getCurrentPosition());
        } else{
            outState.putInt(PLAYER_POSITION, 0);
        }

        super.onSaveInstanceState(outState);
    }

}
