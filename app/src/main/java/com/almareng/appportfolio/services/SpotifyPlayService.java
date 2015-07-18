package com.almareng.appportfolio.services;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.almareng.appportfolio.Objects.MusicItem;
import com.almareng.appportfolio.Objects.TrackItem;
import com.almareng.appportfolio.R;
import com.almareng.appportfolio.SpotifyMainActivity;
import com.almareng.appportfolio.SpotifyPlayActivity;
import com.almareng.appportfolio.SpotifyTopTracksActivity;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Almaral17@gmail.com on 7/13/2015.
 */
public class SpotifyPlayService extends Service implements MediaPlayer.OnCompletionListener,
MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener{

    private final String LOG_TAG = SpotifyPlayService.class.getSimpleName();

    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_NEXT = "action_next";
    public static final String ACTION_PREVIOUS = "action_previous";

    private MediaSessionManager mManager;
    private MediaSession mSession;
    private MediaController mController;

    private final int CURRENT_NOTIFICATION_ID = 4563;

    private MediaPlayer mPlayer;
    private ArrayList<MusicItem> mTracks;

    private int mTrackPosition;

    private TrackItem currentTrack;

    private final IBinder mBinder = new SpotifyPlayBinder();

    private BroadcastReceiver mReceiver;

    @Override
    public void onCreate() {
        super.onCreate();

        mTrackPosition = 0;

        mPlayer = new MediaPlayer();

        initMusicPlayer();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {

            IntentFilter filter = new IntentFilter();
            filter.addAction(getString(R.string.action_previous));
            filter.addAction(getString(R.string.action_play));
            filter.addAction(getString(R.string.action_next));

            mReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction().equals(getString(R.string.action_previous))) {
                        playPreviousTrack();
                    } else if (intent.getAction().equals(getString(R.string.action_play))) {
                        if (playerIsPlaying()) {
                            mPlayer.pause();
                        } else {
                            resumeTrack();
                        }
                    } else if (intent.getAction().equals(getString(R.string.action_next))) {
                        playNextTrack();
                    }

                }
            };

            registerReceiver(mReceiver, filter);
        }

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void handleIntent( Intent intent ) {
        if( intent == null || intent.getAction() == null )
            return;

        String action = intent.getAction();

        if( action.equalsIgnoreCase( ACTION_PAUSE ) ) {
            mController.getTransportControls().pause();
        }else if( action.equalsIgnoreCase( ACTION_PREVIOUS ) ) {
            mController.getTransportControls().skipToPrevious();
        } else if( action.equalsIgnoreCase( ACTION_NEXT ) ) {
            mController.getTransportControls().skipToNext();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mManager == null) {
                initMediaSessions();
            }

            handleIntent(intent);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void initMediaSessions() {

        mSession = new MediaSession(getApplicationContext(), "simple player session");
        mController =new MediaController(getApplicationContext(), mSession.getSessionToken());

        mSession.setCallback(new MediaSession.Callback() {

                                 @Override
                                 public void onPause() {
                                     super.onPause();
                                     if(playerIsPlaying()) {
                                         pauseTrack();
                                         buildNotification(generateAction(android.R.drawable.ic_media_play, "Play", ACTION_PAUSE));
                                     } else{
                                         resumeTrack();
                                         buildNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE));
                                     }
                                 }

                                 @Override
                                 public void onSkipToNext() {
                                     super.onSkipToNext();
                                     playNextTrack();
                                     buildNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE));
                                 }

                                 @Override
                                 public void onSkipToPrevious() {
                                     super.onSkipToPrevious();
                                     playPreviousTrack();
                                     buildNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE));
                                 }

                             }
        );
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private Notification.Action generateAction( int icon, String title, String intentAction ) {
        Intent intent = new Intent( getApplicationContext(), SpotifyPlayService.class );
        intent.setAction(intentAction);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        return new Notification.Action.Builder( icon, title, pendingIntent ).build();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void buildNotification( Notification.Action action ) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean showNotification = prefs.getBoolean(getString(R.string.pref_enable_notifications_key), true);

        if(showNotification) {

            Intent notificationIntent;
            if (getApplicationContext().getResources().getBoolean(R.bool.large_layout)) {
                notificationIntent = new Intent(this, SpotifyMainActivity.class);
            } else{
                notificationIntent = new Intent(this, SpotifyPlayActivity.class);
            }
            notificationIntent.putParcelableArrayListExtra(SpotifyMainActivity.TRACKS, mTracks);
            notificationIntent.putExtra(SpotifyMainActivity.NOW_PLAYING_STATUS, true);
            notificationIntent.putExtra(SpotifyMainActivity.TRACK_POSITION, mTrackPosition);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            // Adds the back stack for the Intent (but not the Intent itself)
            if (!getApplicationContext().getResources().getBoolean(R.bool.large_layout)) {
                stackBuilder.addParentStack(SpotifyTopTracksActivity.class);
            }
            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(notificationIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );

            Notification.MediaStyle style = new Notification.MediaStyle();

            //Intent intent = new Intent(getApplicationContext(), SpotifyPlayService.class);
            //PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
            Notification.Builder mBuilder = new Notification.Builder(this);
            mBuilder.setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(currentTrack.getArtistName())
                    .setContentText(currentTrack.getName())
                    //.setDeleteIntent(pendingIntent)
                    .setStyle(style);
            Bitmap notBitmap = loadBitmap();

            if (notBitmap != null) {
                mBuilder.setLargeIcon(notBitmap);
            } else {
                mBuilder.setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                        R.mipmap.ic_launcher));
            }

            mBuilder.setVisibility(1);

            mBuilder.setContentIntent(resultPendingIntent);

            mBuilder.addAction(generateAction(android.R.drawable.ic_media_previous, "Previous", ACTION_PREVIOUS));
            mBuilder.addAction(action);
            mBuilder.addAction(generateAction(android.R.drawable.ic_media_next, "Next", ACTION_NEXT));
            style.setShowActionsInCompactView(0, 1, 2);

            startForeground(CURRENT_NOTIFICATION_ID, mBuilder.build());
        }
    }

    private void displayNotification(){

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean showNotification = prefs.getBoolean(getString(R.string.pref_enable_notifications_key), true);

        if(showNotification) {

            Intent notificationIntent;
            if (getApplicationContext().getResources().getBoolean(R.bool.large_layout)) {
                notificationIntent = new Intent(this, SpotifyMainActivity.class);
            } else{
                notificationIntent = new Intent(this, SpotifyPlayActivity.class);
            }
            notificationIntent.putParcelableArrayListExtra(SpotifyMainActivity.TRACKS, mTracks);
            notificationIntent.putExtra(SpotifyMainActivity.NOW_PLAYING_STATUS, true);
            notificationIntent.putExtra(SpotifyMainActivity.TRACK_POSITION, mTrackPosition);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            // Adds the back stack for the Intent (but not the Intent itself)
            if (!getApplicationContext().getResources().getBoolean(R.bool.large_layout)) {
                stackBuilder.addParentStack(SpotifyTopTracksActivity.class);
            }
            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(notificationIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getBaseContext());
            mBuilder.setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(currentTrack.getArtistName())
                    .setContentText(currentTrack.getName());
            Bitmap notBitmap = loadBitmap();

            if (notBitmap != null) {
                mBuilder.setLargeIcon(notBitmap);
            } else {
                mBuilder.setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                        R.mipmap.ic_launcher));
            }

            mBuilder.setVisibility(1);

            mBuilder.setContentIntent(resultPendingIntent);

            Intent prevIntent = new Intent(getString(R.string.action_previous));
            PendingIntent actionPrevPendingIntent = PendingIntent.getBroadcast(this, 0, prevIntent, 0);
            Intent playIntent = new Intent(getString(R.string.action_play));
            PendingIntent actionPlayPendingIntent = PendingIntent.getBroadcast(this, 0, playIntent, 0);
            Intent nextIntent = new Intent(getString(R.string.action_next));
            PendingIntent actionNextPendingIntent = PendingIntent.getBroadcast(this, 0, nextIntent, 0);

            mBuilder.addAction(android.R.drawable.ic_media_previous, "", actionPrevPendingIntent);

            mBuilder.addAction(android.R.drawable.ic_media_play, "", actionPlayPendingIntent);

            mBuilder.addAction(android.R.drawable.ic_media_next, "", actionNextPendingIntent);

            Notification notification = mBuilder.build();
            startForeground(CURRENT_NOTIFICATION_ID, notification);
        }

    }

    private void playPreviousTrack(){

        if (mTrackPosition > 0 ) {
            --mTrackPosition;
        }
        else {
            mTrackPosition = mTracks.size() - 1;
        }

        playTrack();

    }

    private void playNextTrack(){

        if (mTracks.size() > (mTrackPosition + 1)) {
            ++mTrackPosition;
        }
        else {
            mTrackPosition = 0;
        }

        playTrack();

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
        unregisterReceiver(mReceiver);
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

        playNextTrack();

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mPlayer.start();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            displayNotification();
        } else{
            buildNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE));
        }
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

    private com.squareup.picasso.Target loadTarget;
    private Bitmap notificationBitmap;

    private Bitmap loadBitmap(){

        if (loadTarget == null) loadTarget = new com.squareup.picasso.Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                notificationBitmap = bitmap;
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };

        Picasso.with(this).load(currentTrack.getBigImageUrl()).into(loadTarget);
        return notificationBitmap;
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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            displayNotification();
        }
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

    public TrackItem getCurrentTrack(){

        return currentTrack;

    }

    public void dispatchNotification(){

        stopForeground(true);

    }

}
