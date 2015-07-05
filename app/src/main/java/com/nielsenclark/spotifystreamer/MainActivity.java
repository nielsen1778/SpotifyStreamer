package com.nielsenclark.spotifystreamer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;
import java.util.concurrent.ExecutionException;

import kaaes.spotify.webapi.android.models.Track;


public class MainActivity extends ActionBarActivity implements MainActivityFragment.ArtistEventCallback {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String TOPTRACKFRAGMENT_TAG = "TTFTAG";

    private boolean mTwoPane = false;
    public List<Track> listOfArtistsTopTenTracks;

    // flag for Internet connection status
    Boolean isInternetPresent = false;

    // Connection detector class
    ConnectionDetector cd;


    private Fragment mContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // creating connection detector class instance
        cd = new ConnectionDetector(this);

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

            if (isNetworkAvailable()) {
//            if (!spotifyID.isEmpty()) {
                FetchTracksTask tracksTask = new FetchTracksTask();
                try {
                    listOfArtistsTopTenTracks = tracksTask.execute(spotifyID).get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
//            }
            }

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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean isNetworkAvailable() {

        // get Internet status
        isInternetPresent = cd.isConnectingToInternet();

        // check for Internet status
        if (isInternetPresent) {
            return true;
        } else {
            // Internet connection is not present
            // Ask user to connect to Internet
            showAlertDialog(this, "No Internet Connection",
                    "You don't have internet connection.  Please try again when connection resumes.", false);
            return false;
        }

    }

    public void showAlertDialog(Context context, String title, String message, Boolean status) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        // Setting Dialog Title
        alertDialog.setTitle(title);

        // Setting Dialog Message
        alertDialog.setMessage(message);

        // Setting alert dialog icon
//        alertDialog.setIcon((status) ? R.drawable.success : R.drawable.fail);

        // Setting OK Button
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }


}
