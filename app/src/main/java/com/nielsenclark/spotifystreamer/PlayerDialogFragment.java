package com.nielsenclark.spotifystreamer;


import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class PlayerDialogFragment extends DialogFragment{


    TextView tvPlayerArtist;
    TextView tvPlayerAlbum;
    ImageView imgAlbumArt;
    TextView tvPlayerSong;
    TextView tvDurationEnd;
    SeekBar sbrProgress;
    ImageButton ibtnPrevious;
    ImageButton ibtnPlayPause;
    ImageButton ibtnNext;

    public ArrayList<ArtistTrack> artistTracks;

    public int position;
    public int curProgress = 0;

    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound=false;


    //    private MediaPlayer mediaPlayer;
    private int mediaFileLengthInMilliseconds; // this value contains the song duration in milliseconds. Look at getDuration() method in MediaPlayer class

    private final Handler handler = new Handler();

    String artistName;
    String albumName;
    String trackName;
    String trackURLString = "";
    int durationMS = 0;
    String trackDuration = "0.00";
    String thumbnailUrl;

    Boolean isPlaying = false;

    // Seekbar
    private int seekMax;
    private static int songEnded = 0;
    boolean mBroadcastIsRegistered;

    // --Set up constant ID for broadcast of seekbar position--
    public static final String BROADCAST_SEEKBAR = "com.nielsenclark.spotifystreamer.sendseekbar";
    Intent intent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- set up seekbar intent for broadcasting new position to service ---
        intent = new Intent(BROADCAST_SEEKBAR);

    }

    /** The system calls this to get the DialogFragment's layout, regardless
     of whether it's being displayed as a dialog or an embedded fragment. */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle bundle = getArguments();

        artistTracks = bundle.getParcelableArrayList("ArtistTracks");

        // Inflate the layout to use as dialog or embedded fragment
        View rootView = inflater.inflate(R.layout.dialog_fragment_player, container, false);

        tvPlayerArtist = (TextView) rootView.findViewById(R.id.tvPlayerArtist);

        tvPlayerAlbum = (TextView) rootView.findViewById(R.id.tvPlayerAlbum);
        imgAlbumArt = (ImageView) rootView.findViewById(R.id.imgAlbumArt);
        tvPlayerSong = (TextView) rootView.findViewById(R.id.tvPlayerSong);
        tvDurationEnd = (TextView) rootView.findViewById(R.id.tvDurationEnd);
        tvDurationEnd.setText(trackDuration);
        sbrProgress = (SeekBar) rootView.findViewById(R.id.sbrProgress);
        sbrProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (fromUser) {
                    curProgress = progress;
                    seekTo(progress);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        ibtnPrevious = (ImageButton) rootView.findViewById(R.id.ibtnPrevious);
        ibtnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTrack();
                if (position > 0) {
                    position = position - 1;
                } else {
                    position = artistTracks.size() - 1;
                }
                getTrackDetails();
                playTrack();
            }
        });

        ibtnNext = (ImageButton) rootView.findViewById(R.id.ibtnNext);
        ibtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTrack();
                if (position == artistTracks.size() - 1) {
                    position = 0;
                } else {
                    position = position + 1;
                }
                getTrackDetails();
                playTrack();
            }
        });

        ibtnPlayPause = (ImageButton) rootView.findViewById(R.id.ibtnPlayPause);
        ibtnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isPlaying) {
                    playTrack();
                    ibtnPlayPause.setImageResource(R.drawable.ic_pause_black_48dp);
                } else {
                    pauseTrack();
                    ibtnPlayPause.setImageResource(R.drawable.ic_play_arrow_black_48dp);
                }

            }
        });

        MusicService.setMainActivity(PlayerDialogFragment.this);

        return rootView;

    }

    // -- Broadcast Receiver to update position of seekbar from service --
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent serviceIntent) {
            updateUI(serviceIntent);
        }
    };

    private void updateUI(Intent serviceIntent) {
        String counter = serviceIntent.getStringExtra("counter");
        String mediamax = serviceIntent.getStringExtra("mediamax");
        String strSongEnded = serviceIntent.getStringExtra("song_ended");
        int seekProgress = Integer.parseInt(counter);
        seekMax = Integer.parseInt(mediamax);
        songEnded = Integer.parseInt(strSongEnded);
        sbrProgress.setMax(seekMax);
        sbrProgress.setProgress(seekProgress);


        if (songEnded == 1) {

 //           ibtnPlayPause.setImageResource(R.drawable.ic_play_arrow_black_48dp);

            isPlaying = false;

            // no repeat or shuffle ON - play next song
            if(position < (artistTracks.size() - 1)){
                getTrackDetails();
                playTrack();
                position = position + 1;
            }else{
                // play first song
                getTrackDetails();
                playTrack();
                position = 0;
            }


        }


    }

    // --End of seekbar update code--

    private void seekTo(int progress) {

        Intent musicIntent = new Intent(getActivity(), MusicService.class);
        musicIntent.putExtra("Progress", progress);
        musicIntent.setAction(MusicService.ACTION_PROGRESS);
        getActivity().startService(musicIntent);

    }

    /** The system calls this only when creating the layout in a dialog. */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }


    @Override
    public void onStart() {
        super.onStart();

        getTrackDetails();
        playTrack();

    }

    private void playTrack() {

        try {

            MusicService.setSong(trackURLString, trackName, thumbnailUrl);

            Intent musicIntent = new Intent(getActivity(), MusicService.class);
            musicIntent.setAction(MusicService.ACTION_PLAY);
            getActivity().startService(musicIntent);
            isPlaying = true;


            // -- Register receiver for seekbar--
            getActivity().registerReceiver(broadcastReceiver, new IntentFilter(
                    MusicService.BROADCAST_ACTION));
            mBroadcastIsRegistered = true;


            // Changing Button Image to pause image
            ibtnPlayPause.setImageResource(R.drawable.ic_pause_black_48dp);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void pauseTrack() {
        try {
            Intent musicIntent = new Intent(getActivity(), MusicService.class);
            musicIntent.setAction(MusicService.ACTION_PAUSE);
            getActivity().startService(musicIntent);
            isPlaying = false;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void stopTrack() {
        try {
            Intent musicIntent = new Intent(getActivity(), MusicService.class);
            musicIntent.setAction(MusicService.ACTION_STOP);
            getActivity().startService(musicIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // Get all of the inforamtiona and image links for the track selected.
    private void getTrackDetails() {

        if (artistTracks != null) {

            ArtistTrack aTrack = artistTracks.get(position);

            // ensure aTrack is not null
            if (aTrack != null) {

                artistName = aTrack.getArtistName();
                albumName = aTrack.getAlbumName();
                trackName = aTrack.getTrackName();
                trackURLString = aTrack.getTrackPreviewUrl();
                durationMS = (int) aTrack.getDurationMS();

                tvPlayerArtist.setText(artistName);
                tvPlayerAlbum.setText(albumName);

                String thumbnailUrl = aTrack.getImageLargeUri();
                imgAlbumArt.setImageBitmap(null);
                if (thumbnailUrl != null) {
                    Picasso.with(getActivity()).load(thumbnailUrl).into(imgAlbumArt);
                }

                tvPlayerSong.setText(trackName);


                mediaFileLengthInMilliseconds = durationMS; // gets the song length in milliseconds from URL
                trackDuration = "" + TimeUnit.MILLISECONDS.toMinutes(mediaFileLengthInMilliseconds) + "." + (TimeUnit.MILLISECONDS.toSeconds(mediaFileLengthInMilliseconds) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(mediaFileLengthInMilliseconds)));
                tvDurationEnd.setText(trackDuration);

            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("Position", position);
        outState.putParcelableArrayList("ArtistTracks", artistTracks);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {

            artistTracks = savedInstanceState.getParcelableArrayList("ArtistTracks");
            position = savedInstanceState.getInt("Position");

        }
    }

    @Override
    public void onStop() {
        super.onStop();

        stopTrack();

        // --Unregister broadcastReceiver for seekbar
        if (mBroadcastIsRegistered) {
            try {
                getActivity().unregisterReceiver(broadcastReceiver);
                mBroadcastIsRegistered = false;
            } catch (Exception e) {
                // Log.e(TAG, "Error in Activity", e);
                // TODO Auto-generated catch block

                e.printStackTrace();
                Toast.makeText(
                        getActivity().getApplicationContext(),
                        e.getClass().getName() + " " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        }

    }

    @Override
    public void onPause() {

        // --Unregister broadcastReceiver for seekbar
        if (mBroadcastIsRegistered) {
            try {
                getActivity().unregisterReceiver(broadcastReceiver);
                mBroadcastIsRegistered = false;
            } catch (Exception e) {
                // Log.e(TAG, "Error in Activity", e);
                // TODO Auto-generated catch block

                e.printStackTrace();
                Toast.makeText(

                        getActivity().getApplicationContext(),

                        e.getClass().getName() + " " + e.getMessage(),

                        Toast.LENGTH_LONG).show();
            }
        }

        super.onPause();
    }

    @Override
    public void onResume() {

        // -- Register receiver for seekbar--
        if (!mBroadcastIsRegistered) {
            getActivity().registerReceiver(broadcastReceiver, new IntentFilter(
                    MusicService.BROADCAST_ACTION));
            mBroadcastIsRegistered = true;
        }

        super.onResume();
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setOnDismissListener(null);
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

}
