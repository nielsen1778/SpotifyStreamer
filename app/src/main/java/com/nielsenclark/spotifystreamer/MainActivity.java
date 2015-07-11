package com.nielsenclark.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;


public class MainActivity extends ActionBarActivity implements MainActivityFragment.ArtistEventCallback {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String TOPTRACKFRAGMENT_TAG = "TTFTAG";

    private boolean mTwoPane = false;

    // flag for Internet connection status
    Boolean isInternetPresent = false;


    private Fragment mContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (findViewById(R.id.flTopTracks) != null) {
            mTwoPane = true;
            if (savedInstanceState == null) {

                getSupportFragmentManager().beginTransaction()
                        .add(R.id.flTopTracks, new TopTracksActivityFragment(), TOPTRACKFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mContent != null ) {
            getSupportFragmentManager().putFragment(outState, "mContent", mContent);
        }
    }

    @Override
    public void onArtistSelected(String spotifyID) {

        if (mTwoPane) {
            // In two-pane mode, show the top tracks view in this activity by
            // adding or replacing the top tracks fragment using a
            // fragment transaction.
            Bundle args = new Bundle();
            args.putString("SpotifyID", spotifyID);

            TopTracksActivityFragment fragment = new TopTracksActivityFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.flTopTracks, fragment, TOPTRACKFRAGMENT_TAG)
                    .commit();

        } else {
            Intent topTenTracks = new Intent(this, TopTracksActivity.class)
                    .putExtra("SpotifyID", spotifyID);
            startActivity(topTenTracks);
        }

    }


}
