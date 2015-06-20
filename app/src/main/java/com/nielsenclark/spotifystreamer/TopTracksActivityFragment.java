package com.nielsenclark.spotifystreamer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;


/**
 * A placeholder fragment containing a simple view.
 */
public class TopTracksActivityFragment extends Fragment {

    private final String LOG_TAG = MainActivityFragment .class.getSimpleName();

    List<Track> listOfArtistsTopTenTracks;


    private RecyclerView rvArtistsTopTenTracks;
    private RecyclerView.Adapter mArtistsTopTenTracksAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private Artist artist;
    String spotifyID;

    // flag for Internet connection status
    Boolean isInternetPresent = false;

    // Connection detector class
    ConnectionDetector cd;

    public TopTracksActivityFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // creating connection detector class instance
        cd = new ConnectionDetector(getActivity());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);

        spotifyID = getActivity().getIntent().getExtras().getString("SpotifyID");


        rvArtistsTopTenTracks = (RecyclerView) rootView.findViewById(R.id.rvArtistsTopTenTracks);

        mLayoutManager = new LinearLayoutManager(getActivity());
        rvArtistsTopTenTracks.setLayoutManager(mLayoutManager);

        mArtistsTopTenTracksAdapter = new MyAdapter(listOfArtistsTopTenTracks);
        rvArtistsTopTenTracks.setAdapter(mArtistsTopTenTracksAdapter);

        final GestureDetector mGestureDetector = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }

        });

        rvArtistsTopTenTracks.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
                View child = recyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());


                if (child != null && mGestureDetector.onTouchEvent(motionEvent)) {


                    //    String spotifyID = listOfArtists.get(recyclerView.getChildPosition(child)).id;

                    //    Toast.makeText(getActivity(), "The Item Clicked is: " + recyclerView.getChildPosition(child), Toast.LENGTH_SHORT).show();

                    //    Intent playerIntent = new Intent(getActivity(), PlayerActivity.class)
                    //            .putExtra("SpotifyID", spotifyID);
                    //    getActivity().startActivity(playerIntent);

                    return true;

                }

                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {

            }
        });


        return  rootView;

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("SpotifyID", spotifyID);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {

            spotifyID = savedInstanceState.getString("SpotifyID");
            if (isNetworkAvailable()) {
                FetchTracksTask tracksTask = new FetchTracksTask();
                tracksTask.execute(spotifyID);
            }
        }
    }

    private void updateTracks() {
        if (isNetworkAvailable()) {
            FetchTracksTask tracksTask = new FetchTracksTask();
            tracksTask.execute(spotifyID);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        updateTracks();
    }

    public class FetchTracksTask extends AsyncTask<String, Void, List<Track>> {

        private final String LOG_TAG = FetchTracksTask .class.getSimpleName();


        @Override
        protected List<Track> doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();


            HashMap<String,Object> queryString = new HashMap<>();
            queryString.put(SpotifyService.COUNTRY, Locale.getDefault().getCountry());

            Tracks results = spotify.getArtistTopTrack(params[0], queryString);

            listOfArtistsTopTenTracks = results.tracks;
            for(Track element : listOfArtistsTopTenTracks){
                String name = element.name;
                Log.d(LOG_TAG, "Name" + name);
            }

            return listOfArtistsTopTenTracks;
        }

        @Override
        protected void onPostExecute(List<Track>  result) {
            if (result != null) {
                mArtistsTopTenTracksAdapter = new MyAdapter(listOfArtistsTopTenTracks);
                rvArtistsTopTenTracks.setAdapter(mArtistsTopTenTracksAdapter);
            } else {
                Toast.makeText(getActivity(), "Tracks not found. ", Toast.LENGTH_SHORT).show();

                listOfArtistsTopTenTracks.clear();
                mArtistsTopTenTracksAdapter = new MyAdapter(listOfArtistsTopTenTracks);
                rvArtistsTopTenTracks.setAdapter(mArtistsTopTenTracksAdapter);

            }

        }


    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

        List<Track> tracks;

        MyAdapter(List<Track> tracks){
            this.tracks = tracks;
        }


        public class ViewHolder extends RecyclerView.ViewHolder {
            public ImageView ivAlbumPhoto;
            public TextView tvAlbum;
            public TextView tvTrackName;

            public ViewHolder(View v) {
                super(v);
                ivAlbumPhoto = (ImageView) v.findViewById(R.id.ivAlbumPhoto);
                tvAlbum = (TextView) v.findViewById(R.id.tvAlbum);
                tvTrackName = (TextView) v.findViewById(R.id.tvTrackName);
            }
        }

        public void add(int position, Track track) {
            listOfArtistsTopTenTracks.add(position, track);
            notifyItemInserted(position);
        }

        public void remove(Track track) {
            int position = listOfArtistsTopTenTracks.indexOf(track);
            listOfArtistsTopTenTracks.remove(position);
            notifyItemRemoved(position);
        }

        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.track_line_item, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }


        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            Track atemp = listOfArtistsTopTenTracks.get(position);
            if (atemp != null) {
                List<Image> images = listOfArtistsTopTenTracks.get(position).album.images;

                if (images != null && images.size() > 0) {
                    String thumbnailUrl = listOfArtistsTopTenTracks.get(position).album.images.get(2).url;
                    holder.ivAlbumPhoto.setImageBitmap(null);
                    if (thumbnailUrl != null) {
                        Picasso.with(getActivity()).load(listOfArtistsTopTenTracks.get(position).album.images.get(2).url).into(holder.ivAlbumPhoto);
                    }
                }

            }
            holder.tvAlbum.setText(listOfArtistsTopTenTracks.get(position).album.name);
            holder.tvTrackName.setText(listOfArtistsTopTenTracks.get(position).name);

        }

        @Override
        public int getItemCount() {
            if (listOfArtistsTopTenTracks != null) {
                return listOfArtistsTopTenTracks.size();
            } else {
                return 0;
            }

        }

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
            showAlertDialog(getActivity(), "No Internet Connection",
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
