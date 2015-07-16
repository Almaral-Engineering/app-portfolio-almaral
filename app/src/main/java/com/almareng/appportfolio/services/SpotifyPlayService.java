package com.almareng.appportfolio.services;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.almareng.appportfolio.Objects.MusicItem;
import com.almareng.appportfolio.Objects.TrackItem;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Almaral17@gmail.com on 7/13/2015.
 */
public class SpotifyPlayService extends Service implements MediaPlayer.OnCompletionListener,
MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener{

    private final String LOG_TAG = SpotifyPlayService.class.getSimpleName();

    private MediaPlayer mPlayer;
    private ArrayList<MusicItem> mTracks;

    private int mTrackPosition;

    private TrackItem currentTrack;

    private final IBinder mBinder = new SpotifyPlayBinder();

    @Override
    public void onCreate() {
        super.onCreate();

        mTrackPosition = 0;
        mPlayer = new MediaPlayer();
        initMusicPlayer();

    }

    public class SpotifyPlayBinder extends Binder {

        public SpotifyPlayService getService(){

            return SpotifyPlayService.this;

        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPlayer.stop();
        mPlayer.release();
        mPlayer = null;
    }

    private void initMusicPlayer(){

        mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnErrorListener(this);

    }

    public void setListOfTracks(ArrayList<MusicItem> tracks){
        mTracks = tracks;
        currentTrack = (TrackItem) mTracks.get(mTrackPosition);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mTracks.size() > (mTrackPosition + 1)) {
            ++mTrackPosition;
        }
        else {
            mTrackPosition = 0;
        }

        playTrack();

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mPlayer.start();
    }

    public void seekTo(int position){
        mPlayer.seekTo(position);
    }

    public void playTrack(){

        if(mPlayer == null){
            mPlayer = new MediaPlayer();
            initMusicPlayer();
        } else {
            mPlayer.reset();
        }

        currentTrack = (TrackItem) mTracks.get(mTrackPosition);

        String previewUrl = currentTrack.getPreviewUrl();

        try {
            mPlayer.setDataSource(previewUrl);
            mPlayer.prepareAsync();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error setting data source, maybe url doesn't exist");
        }

    }

    public int getPlayerPosition(){

        if(mPlayer != null){
            return mPlayer.getCurrentPosition();
        } else{
            return 0;
        }

    }

    public int getTrackDuration(){

        if(mPlayer != null){

            return mPlayer.getDuration();

        } else{

            return 30;

        }

    }

    public void resumeTrack(){
        mPlayer.start();
    }

    public void pauseTrack(){
        mPlayer.pause();
    }

    public void setTrack(int trackIndex){
        mTrackPosition = trackIndex;
    }

    public boolean playerIsPlaying(){
        return mPlayer != null && mPlayer.isPlaying();
    }

    public int getTrackPosition(){
        return mTrackPosition;
    }

    public ArrayList<MusicItem> getTracks(){

        return mTracks;

    }

    public String getTrackId(){

        if (currentTrack!=null) {
            return currentTrack.getId();
        } else{
            return "";
        }

    }

}
