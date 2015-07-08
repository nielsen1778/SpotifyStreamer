package com.nielsenclark.spotifystreamer;

import android.os.AsyncTask;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;

public class FetchTracksTask extends AsyncTask<String, Void, List<Track>> {

    private final String LOG_TAG = FetchTracksTask .class.getSimpleName();

    private List<Track> listTopTenTracks;


    @Override
    protected List<Track> doInBackground(String... params) {

        if (params.length == 0) {
            return null;
        }
        try {
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();


            HashMap<String,Object> queryString = new HashMap<>();
            queryString.put(SpotifyService.COUNTRY, Locale.getDefault().getCountry());

            Tracks results = spotify.getArtistTopTrack(params[0], queryString);

            listTopTenTracks = results.tracks;
            for(Track element : listTopTenTracks){
                String name = element.name;
                Log.d(LOG_TAG, "Name" + name);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception:" + e.getMessage());
        }

        //((TopTracksActivity )  getActivity()).listOfArtistsTopTenTracks = listOfArtistsTopTenTracks;


        return listTopTenTracks;
    }


}
