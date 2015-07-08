package com.nielsenclark.spotifystreamer;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.widget.SeekBar;

import java.io.IOException;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, OnBufferingUpdateListener{


    public static final String ACTION_PLAY = "PLAY";
    public static final String ACTION_PAUSE = "PAUSE";
    public static final String ACTION_STOP = "STOP";
    public static final String ACTION_PROGRESS = "PROGRESS";
    private static String mUrl;
    private static MusicService mInstance = null;

    private MediaPlayer mediaPlayer = null;    // The Media Player
    private int mBufferPosition;
    private static String mSongTitle;
    private static String mSongPicUrl;

    NotificationManager mNotificationManager;
    Notification mNotification = null;
    final int NOTIFICATION_ID = 1;


    // indicates the state our service:
    enum State {
        Retrieving, // the MediaRetriever is retrieving music
        Stopped, // media player is stopped and not prepared to play
        Preparing, // media player is preparing...
        Playing, // playback active (media player ready!). (but the media player may actually be
        // paused in this state if we don't have audio focus. But we stay in this state
        // so that we know we have to resume playback once we get focus back)
        Paused
        // playback paused (media player ready!)
    };

    State mState = State.Retrieving;

    private static PlayerDialogFragment  MAIN_ACTIVITY;

    public static void setMainActivity(PlayerDialogFragment mainActivity){
        MAIN_ACTIVITY=mainActivity;
    }


    @Override
    public void onCreate() {
        mInstance = this;
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(ACTION_PLAY)) {
            processPlayRequest();

        } else if (intent.getAction().equals(ACTION_PAUSE)) {
            processPauseRequest();
        } else if (intent.getAction().equals(ACTION_STOP)) {
            processStopRequest();
        } else if (intent.getAction().equals(ACTION_PROGRESS)) {
            Bundle extras = intent.getExtras();

            if (extras != null) {

                // get data via the key
                int seekProgress = extras.getInt("Progress");
                if (seekProgress > -1) {
                    processProgressRequest(seekProgress);
                }

            }
        }
        return START_STICKY;
    }

    void processPlayRequest() {

        mediaPlayer = new MediaPlayer(); // initialize it here
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        initMediaPlayer();

        if (mState == State.Paused) {
            // If we're paused, just continue playback and restore the 'foreground service' state.
            mState = State.Playing;
            setUpAsForeground(mSongTitle + " (playing)");
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
        }

    }

    void processPauseRequest() {

        if (mState == State.Playing) {
                      // Pause media player and cancel the 'foreground service' state.
            mState = State.Paused;
             mediaPlayer.pause();
            relaxResources(false); // while paused, we always retain the MediaPlayer
             // do not give up audio focus
        }

    }

    void processStopRequest() {

        if (mState == State.Playing) {
            // Pause media player and cancel the 'foreground service' state.
            mState = State.Stopped;
            mediaPlayer.stop();
            relaxResources(false); // while paused, we always retain the MediaPlayer
            // do not give up audio focus
        }

    }

    void processProgressRequest(int progress){
        mediaPlayer.seekTo(progress);
    }


    void relaxResources(boolean releaseMediaPlayer) {
        // stop being a foreground service
        stopForeground(true);

        // stop and release the Media Player, if it's available
        if (releaseMediaPlayer && mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }



    private void initMediaPlayer() {
        try {
            mediaPlayer.setDataSource(mUrl);
        } catch (IllegalArgumentException e) {
            // ...
        } catch (IllegalStateException e) {
            // ...
        } catch (IOException e) {
            // ...
        }

        try {
            mediaPlayer.prepareAsync(); // prepare async to not block main thread
        } catch (IllegalStateException e) {
            // ...
        }
        mState = State.Preparing;
    }

    public void restartMusic() {
        // Restart music
    }

    protected void setBufferPosition(int progress) {
        mBufferPosition = progress;
    }

    /** Called when MediaPlayer is ready */
    @Override
    public void onPrepared(MediaPlayer player) {
        // Begin playing music
        mState = State.Playing;
        startMusic();

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mState = State.Retrieving;
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void pauseMusic() {
        if (mState.equals(State.Playing)) {
            mediaPlayer.pause();
            mState = State.Paused;
            updateNotification(mSongTitle + "(paused)");
        }
    }

    public void startMusic() {
        if (!mState.equals(State.Preparing) &&!mState.equals(State.Retrieving)) {
            mediaPlayer.start();
            mState = State.Playing;
            updateNotification(mSongTitle + "(playing)");

           // final TextView tv_test=(TextView)MAIN_ACTIVITY.findViewById(R.id.textview);
            final SeekBar sbrProgress = (SeekBar) MAIN_ACTIVITY.sbrProgress;
            sbrProgress.setProgress(0);
            //sbrProgress.setMax(100);
            int duration = mediaPlayer.getDuration();
            sbrProgress.setMax(duration);
            new CountDownTimer(duration, 250) {
                public void onTick(long millisUntilFinished) {
                    if (sbrProgress.getProgress() == mediaPlayer.getDuration()) {
                        mediaPlayer.reset();
                        sbrProgress.setProgress(0);
                    } else {
                        sbrProgress.setProgress(mediaPlayer.getCurrentPosition());
                    }

                    //sbrProgress.setProgress(sbrProgress.getProgress() + 250);
                }
                public void onFinish() {}
            }.start();
        }
    }

    public boolean isPlaying() {
        if (mState.equals(State.Playing)) {
            return true;
        }
        return false;
    }

    public int getMusicDuration() {
        // Return current music duration
        return mediaPlayer.getDuration();
    }

    public int getCurrentPosition() {
        // Return current position
        return mediaPlayer.getCurrentPosition();
    }

    public int getBufferPercentage() {
        return mBufferPosition;
    }

    public void seekMusicTo(int pos) {
        // Seek music to pos
    }

    public static MusicService getInstance() {
        return mInstance;
    }

    public static void setSong(String url, String title, String songPicUrl) {
        mUrl = url;
        mSongTitle = title;
        mSongPicUrl = songPicUrl;
    }

    public String getSongTitle() {
        return mSongTitle;
    }

    public String getSongPicUrl() {
        return mSongPicUrl;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        setBufferPosition(percent * getMusicDuration() / 100);
    }

    /** Updates the notification. */
    void updateNotification(String text) {
        // Notify NotificationManager of new intent
    }

    /**
     * Configures service as a foreground service. A foreground service is a service that's doing something the user is
     * actively aware of (such as playing music), and must appear to the user as a notification. That's why we create
     * the notification here.
     */
    void setUpAsForeground(String text) {
        PendingIntent pi =
                PendingIntent.getActivity(getApplicationContext(), 0, new Intent(getApplicationContext(), PlayerDialogFragment.class),
                        PendingIntent.FLAG_UPDATE_CURRENT);
        mNotification = new Notification();
        mNotification.tickerText = text;
        mNotification.icon = R.drawable.ic_play_arrow_black_48dp;
        mNotification.flags |= Notification.FLAG_ONGOING_EVENT;
        mNotification.setLatestEventInfo(getApplicationContext(), getResources().getString(R.string.app_name), text, pi);
        startForeground(NOTIFICATION_ID, mNotification);
    }

}
