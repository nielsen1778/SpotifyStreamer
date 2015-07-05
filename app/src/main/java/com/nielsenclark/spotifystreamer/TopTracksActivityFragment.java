package com.nielsenclark.spotifystreamer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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

import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;


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
    public static String spotifyID;



    boolean mIsLargeLayout;

    public TopTracksActivityFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mIsLargeLayout = getResources().getBoolean(R.bool.large_layout);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);

        Bundle arguments = getArguments();
        if (arguments != null) {
            spotifyID = arguments.getString("SpotifyID");
        }



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

                    int mPosition = recyclerView.getChildPosition(child);

                    showDialog(mPosition);

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
/*
            spotifyID = savedInstanceState.getString("SpotifyID");
            if (isNetworkAvailable()) {
                if (!spotifyID.isEmpty()) {
                    FetchTracksTask tracksTask = new FetchTracksTask();
                    tracksTask.execute(spotifyID);
                }
            }
            */

        }
    }

    void onArtistChanged(String aSpotifyID) {
        /*
        if (isNetworkAvailable()) {
            if (!aSpotifyID.isEmpty()) {
                FetchTracksTask tracksTask = new FetchTracksTask();
                tracksTask.execute(aSpotifyID);
            }
        }

        */
    }


    private void updateTracks() {

        String className = getActivity().getClass().getSimpleName();

        if (className != null) {
            if (!className.isEmpty()) {



                if (className.equals("TopTracksActivity")) {
                    listOfArtistsTopTenTracks = ((TopTracksActivity) getActivity()).listOfArtistsTopTenTracks;
                } else if (className.equals("MainActivity")) { // if (getActivity().getClass().getSimpleName() == "MainActivity") {
                    listOfArtistsTopTenTracks = ((MainActivity) getActivity()).listOfArtistsTopTenTracks;
                }


                if (listOfArtistsTopTenTracks != null) {
                    mArtistsTopTenTracksAdapter = new MyAdapter(listOfArtistsTopTenTracks);
                    rvArtistsTopTenTracks.setAdapter(mArtistsTopTenTracksAdapter);
                } else {
                    Toast.makeText(getActivity(), "Tracks not found. ", Toast.LENGTH_SHORT).show();

                    if (listOfArtistsTopTenTracks != null) {
                        listOfArtistsTopTenTracks.clear();
                        mArtistsTopTenTracksAdapter = new MyAdapter(listOfArtistsTopTenTracks);
                        rvArtistsTopTenTracks.setAdapter(mArtistsTopTenTracksAdapter);
                    }

                }


            }
        }


    }

    @Override
    public void onStart() {
        super.onStart();
        updateTracks();
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

    public void showDialog(int position) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        PlayerDialogFragment newFragment = new PlayerDialogFragment();


        Bundle bundles = new Bundle();
        Track aTrack = listOfArtistsTopTenTracks.get(position);



// ensure your object has not null
        if (aTrack != null) {
            bundles.putString("ArtistName", aTrack.artists.get(0).name);
            bundles.putString("AlbumName", aTrack.album.name);
            bundles.putString("TrackName", aTrack.name);

            Log.e("aTrack", "is valid");
        } else {
            Log.e("aTrack", "is null");
        }
        newFragment.setArguments(bundles);



        if (mIsLargeLayout) {
            // The device is using a large layout, so show the fragment as a dialog
            newFragment.show(fragmentManager, "dialog");
            newFragment.listOfTopTenTracks = listOfArtistsTopTenTracks;
            newFragment.position = position;
        } else {
            // The device is smaller, so show the fragment fullscreen
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            // For a little polish, specify a transition animation
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            // To make it fullscreen, use the 'content' root view as the container
            // for the fragment, which is always the root view for the activity
            transaction.add(android.R.id.content, newFragment)
                    .addToBackStack(null).commit();


            newFragment.listOfTopTenTracks = listOfArtistsTopTenTracks;
            newFragment.position = position;
        }
    }


}
