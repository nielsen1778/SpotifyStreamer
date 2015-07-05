package com.nielsenclark.spotifystreamer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;
import java.util.concurrent.ExecutionException;

import kaaes.spotify.webapi.android.models.Track;


public class TopTracksActivity extends ActionBarActivity {


    public List<Track> listOfArtistsTopTenTracks;

    // flag for Internet connection status
    Boolean isInternetPresent = false;

    // Connection detector class
    ConnectionDetector cd;

    public static String spotifyID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);

        // creating connection detector class instance
        cd = new ConnectionDetector(this);


        spotifyID = getIntent().getExtras().getString("SpotifyID");

        if (isNetworkAvailable()) {
            FetchTracksTask tracksTask = new FetchTracksTask();
            try {
                listOfArtistsTopTenTracks = tracksTask.execute(spotifyID).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }


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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_top_tracks, menu);
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
