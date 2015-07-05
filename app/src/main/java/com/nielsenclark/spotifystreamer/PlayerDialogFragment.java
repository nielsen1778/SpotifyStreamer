package com.nielsenclark.spotifystreamer;


import android.app.Dialog;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.concurrent.TimeUnit;

import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

public class PlayerDialogFragment extends DialogFragment {


    TextView tvPlayerArtist;
    TextView tvPlayerAlbum;
    ImageView imgAlbumArt;
    TextView tvPlayerSong;
    TextView tvDurationEnd;
    SeekBar sbrProgress;
    ImageButton ibtnPrevious;
    ImageButton ibtnPlayPause;
    ImageButton ibtnNext;

    public List<Track> listOfTopTenTracks;
    public int position;


    private MediaPlayer mediaPlayer;
    private int mediaFileLengthInMilliseconds; // this value contains the song duration in milliseconds. Look at getDuration() method in MediaPlayer class

    private final Handler handler = new Handler();

    String artistName;
    String albumName;
    String trackName;
    String trackURLString = " http://www.hrupin.com/wp-content/uploads/mp3/testsong_20_sec.mp3";
    String trackDuration = "0.00";


    /** The system calls this to get the DialogFragment's layout, regardless
     of whether it's being displayed as a dialog or an embedded fragment. */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle bundle = getArguments();
        artistName = bundle.getString("ArtistName");
        albumName = bundle.getString("AlbumName");
        trackName = bundle.getString("TrackName");

        // Inflate the layout to use as dialog or embedded fragment
        View rootView = inflater.inflate(R.layout.dialog_fragment_player, container, false);

        tvPlayerArtist = (TextView) rootView.findViewById(R.id.tvPlayerArtist);

        tvPlayerAlbum = (TextView) rootView.findViewById(R.id.tvPlayerAlbum);
        imgAlbumArt = (ImageView) rootView.findViewById(R.id.imgAlbumArt);
        tvPlayerSong = (TextView) rootView.findViewById(R.id.tvPlayerSong);
        tvDurationEnd = (TextView) rootView.findViewById(R.id.tvDurationEnd);
        tvDurationEnd.setText(trackDuration);
        sbrProgress = (SeekBar) rootView.findViewById(R.id.sbrProgress);
        sbrProgress.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                /** Seekbar onTouch event handler. Method which seeks MediaPlayer to seekBar primary progress position*/
                if (mediaPlayer.isPlaying()) {
                    SeekBar sb = (SeekBar) v;
                    int playPositionInMillisecconds = (mediaFileLengthInMilliseconds / 100) * sb.getProgress();
                    mediaPlayer.seekTo(playPositionInMillisecconds);
                }
                return false;
            }
        });

//        PlayTrack();

        ibtnPrevious = (ImageButton) rootView.findViewById(R.id.ibtnPrevious);
        ibtnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position > 0) {
                    position = position - 1;
                } else {
                    position = listOfTopTenTracks.size() - 1;
                }
                getTrackDetails();
                PlayTrack();
            }
        });

        ibtnNext = (ImageButton) rootView.findViewById(R.id.ibtnNext);
        ibtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position == listOfTopTenTracks.size() - 1) {
                    position = 0;
                } else {
                    position = position + 1;
                }
                getTrackDetails();
                PlayTrack();
            }
        });

        ibtnPlayPause = (ImageButton) rootView.findViewById(R.id.ibtnPlayPause);
        ibtnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!mediaPlayer.isPlaying()){
                    if(mediaPlayer!=null) {
                        mediaPlayer.start();
                        ibtnPlayPause.setImageResource(R.drawable.ic_pause_black_48dp);
                    }
                }else {
                    if(mediaPlayer!=null) {
                        mediaPlayer.pause();
                        ibtnPlayPause.setImageResource(R.drawable.ic_play_arrow_black_48dp);
                    }
                }


            }
        });

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                /** Method which updates the SeekBar secondary progress by current song loading from URL position*/
                sbrProgress.setSecondaryProgress(percent);
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                /** MediaPlayer onCompletion event handler. Method which calls then song playing is complete*/
                ibtnPlayPause.setImageResource(R.drawable.ic_play_arrow_black_48dp);

                // no repeat or shuffle ON - play next song
                if(position < (listOfTopTenTracks.size() - 1)){
                    getTrackDetails();
                    PlayTrack();
                    position = position + 1;
                }else{
                    // play first song
                    getTrackDetails();
                    PlayTrack();
                    position = 0;
                }


            }
        });


        return rootView;

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
        PlayTrack();
    }

    private void PlayTrack() {
        /** ImageButton onClick event handler. Method which start/pause mediaplayer playing */
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(trackURLString); // setup song from http://www.hrupin.com/wp-content/uploads/mp3/testsong_20_sec.mp3 URL to mediaplayer data source
            mediaPlayer.prepare(); // you must call this method after setup the datasource in setDataSource method. After calling prepare() the instance of MediaPlayer starts load data from URL to internal buffer.
            mediaPlayer.start();

            sbrProgress.setProgress(0);
            sbrProgress.setMax(100);

            // Changing Button Image to pause image
            ibtnPlayPause.setImageResource(R.drawable.ic_pause_black_48dp);


            mediaFileLengthInMilliseconds = mediaPlayer.getDuration(); // gets the song length in milliseconds from URL
            trackDuration = "" + TimeUnit.MILLISECONDS.toMinutes(mediaFileLengthInMilliseconds) + "." + (TimeUnit.MILLISECONDS.toSeconds(mediaFileLengthInMilliseconds) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(mediaFileLengthInMilliseconds)));
            tvDurationEnd.setText(trackDuration);

            primarySeekBarProgressUpdater();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void getTrackDetails() {

        Track aTrack = listOfTopTenTracks.get(position);


        // ensure your object has not null
        if (aTrack != null) {

            artistName = aTrack.artists.get(0).name;
            albumName = aTrack.album.name;
            trackName = aTrack.name;
            trackURLString = aTrack.preview_url;

            tvPlayerArtist.setText(artistName);
            tvPlayerAlbum.setText(albumName);
            List<Image> images = listOfTopTenTracks.get(position).album.images;

            if (images != null && images.size() > 0) {
                String thumbnailUrl = listOfTopTenTracks.get(position).album.images.get(0).url;
                imgAlbumArt.setImageBitmap(null);
                if (thumbnailUrl != null) {
                    Picasso.with(getActivity()).load(listOfTopTenTracks.get(position).album.images.get(0).url).into(imgAlbumArt);
                }
            }
            tvPlayerSong.setText(trackName);

        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("Position", position);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {

            position = savedInstanceState.getInt("Position");

            if (position > -1) {

                String className = getActivity().getClass().getSimpleName();

                if (className != null) {
                    if (!className.isEmpty()) {

                        if (className.equals("TopTracksActivity")) {
                            listOfTopTenTracks = ((TopTracksActivity) getActivity()).listOfArtistsTopTenTracks;
                        } else if (className.equals("MainActivity")) { // if (getActivity().getClass().getSimpleName() == "MainActivity") {
                            listOfTopTenTracks = ((MainActivity) getActivity()).listOfArtistsTopTenTracks;
                        }

                    }
                }
            }

        }
    }

    /** Method which updates the SeekBar primary progress by current song playing position*/
    private void primarySeekBarProgressUpdater() {
        if (mediaPlayer != null) {
            sbrProgress.setProgress((int) (((float) mediaPlayer.getCurrentPosition() / mediaFileLengthInMilliseconds) * 100)); // This math construction give a percentage of "was playing"/"song length"
            if (mediaPlayer.isPlaying()) {
                Runnable notification = new Runnable() {
                    public void run() {
                        primarySeekBarProgressUpdater();
                    }
                };
                handler.postDelayed(notification, 1000);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mediaPlayer.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
        mediaPlayer = null;
    }

}
