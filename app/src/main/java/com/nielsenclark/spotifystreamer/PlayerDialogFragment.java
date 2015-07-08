package com.nielsenclark.spotifystreamer;


import android.app.Dialog;
import android.content.Intent;
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

import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.concurrent.TimeUnit;

import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

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

    public List<Track> listOfTopTenTracks;
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
                    position = listOfTopTenTracks.size() - 1;
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
                if (position == listOfTopTenTracks.size() - 1) {
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

        isPlaying = true;
        MusicService.setSong(trackURLString, trackName, thumbnailUrl);

        Intent musicIntent = new Intent(getActivity(), MusicService.class);
        musicIntent.setAction(MusicService.ACTION_PLAY);
        getActivity().startService(musicIntent);

        ibtnPlayPause.setImageResource(R.drawable.ic_pause_black_48dp);

    }

    private void playTrack() {

        try {

            MusicService.setSong(trackURLString, trackName, thumbnailUrl);

            Intent musicIntent = new Intent(getActivity(), MusicService.class);
        //    musicIntent.putExtra("Progress", curProgress);
            musicIntent.setAction(MusicService.ACTION_PLAY);
            getActivity().startService(musicIntent);
            isPlaying = true;

            sbrProgress.setProgress(0);

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

    private void getTrackDetails() {

        if (listOfTopTenTracks != null) {

            Track aTrack = listOfTopTenTracks.get(position);


            // ensure your object has not null
            if (aTrack != null) {

                artistName = aTrack.artists.get(0).name;
                albumName = aTrack.album.name;
                trackName = aTrack.name;
                trackURLString = aTrack.preview_url;
                durationMS = (int) aTrack.duration_ms;

                tvPlayerArtist.setText(artistName);
                tvPlayerAlbum.setText(albumName);
                List<Image> images = listOfTopTenTracks.get(position).album.images;

                if (images != null && images.size() > 0) {
                    thumbnailUrl = listOfTopTenTracks.get(position).album.images.get(0).url;
                    imgAlbumArt.setImageBitmap(null);
                    if (thumbnailUrl != null) {
                        Picasso.with(getActivity()).load(thumbnailUrl).into(imgAlbumArt);
                    }
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

    @Override
    public void onStop() {
        super.onStop();

        stopTrack();

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
