package com.nielsenclark.spotifystreamer;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;


public class TopTracksActivity extends ActionBarActivity {

    // flag for Internet connection status
    Boolean isInternetPresent = false;

    // Connection detector class
    ConnectionDetector cd;

    public static String spotifyID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);


        spotifyID = getIntent().getExtras().getString("SpotifyID");


        if (savedInstanceState == null) {

            Bundle arguments = new Bundle();
            arguments.putString("SpotifyID", spotifyID);


            TopTracksActivityFragment fragment = new TopTracksActivityFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.flTopTracks, fragment)
                    .commit();
        }

    }


}
