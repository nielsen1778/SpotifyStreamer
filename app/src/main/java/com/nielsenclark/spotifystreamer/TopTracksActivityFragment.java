package com.nielsenclark.spotifystreamer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;



public class TopTracksActivityFragment extends Fragment {

    private final String LOG_TAG = TopTracksActivityFragment .class.getSimpleName();

    ArrayList<ArtistTrack> artistTracks;


    private RecyclerView rvArtistsTopTenTracks;
    private RecyclerView.Adapter mArtistsTopTenTracksAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private Artist artist;
    public static String spotifyID;


    boolean mIsLargeLayout;

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

        mArtistsTopTenTracksAdapter = new MyAdapter(artistTracks);
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
        outState.putParcelableArrayList("ArtistTracks", artistTracks);


    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {

            spotifyID = savedInstanceState.getString("SpotifyID");

            if(!savedInstanceState.containsKey("ArtistTracks")) {

                updateTracks();

            }
            else {
                artistTracks = savedInstanceState.getParcelableArrayList("ArtistTracks");
            }


        }
    }

    void onArtistChanged(String aSpotifyID) {

        updateTracks();

    }


    private void updateTracks() {

        // get the list of tracks for the artist
        List<Track> listOfArtistsTopTenTracks = null;

        if (isNetworkAvailable()) {
            if (spotifyID != null) {
                if (!spotifyID.isEmpty()) {
                    FetchTracksTask tracksTask = new FetchTracksTask();
                    try {
                        listOfArtistsTopTenTracks = tracksTask.execute(spotifyID).get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }


                    // Create a new artistTracks ArrayList
                    artistTracks = new ArrayList<ArtistTrack>();

                    // Iterate through the List<Track> and put into artistTracks ( ArrayList<ArtistTrack> )
                    for (int i = 0; i < listOfArtistsTopTenTracks.size(); i++) {

                        // Get the track info
                        String spotifyId = spotifyID;
                        String artistName = listOfArtistsTopTenTracks.get(i).artists.get(0).name;
                        String albumName = listOfArtistsTopTenTracks.get(i).album.name;
                        String trackName = listOfArtistsTopTenTracks.get(i).name;
                        String imageThumbnailUri = "";
                        String imageLargeUri = "";

                        Track atemp = listOfArtistsTopTenTracks.get(i);
                        if (atemp != null) {
                            List<Image> images = listOfArtistsTopTenTracks.get(i).album.images;

                            if (images != null && images.size() > 0) {

                                imageThumbnailUri = listOfArtistsTopTenTracks.get(i).album.images.get(2).url;

                                imageLargeUri = listOfArtistsTopTenTracks.get(i).album.images.get(0).url;

                            }

                        }

                        String trackPreviewUrl = listOfArtistsTopTenTracks.get(i).preview_url;
                        int durationMS = (int) listOfArtistsTopTenTracks.get(i).duration_ms;


                        // Put the track info into the ArrayList
                        artistTracks.add(new ArtistTrack(spotifyId,
                                artistName,
                                albumName,
                                trackName,
                                imageThumbnailUri,
                                imageLargeUri,
                                trackPreviewUrl,
                                durationMS));

                    }


                }
            }

        }


        if (artistTracks != null) {
            mArtistsTopTenTracksAdapter = new MyAdapter(artistTracks);
            rvArtistsTopTenTracks.setAdapter(mArtistsTopTenTracksAdapter);
        } else {
            Toast.makeText(getActivity(), "Tracks not found. ", Toast.LENGTH_SHORT).show();

            if (artistTracks != null) {
                artistTracks.clear();
                mArtistsTopTenTracksAdapter = new MyAdapter(artistTracks);
                rvArtistsTopTenTracks.setAdapter(mArtistsTopTenTracksAdapter);
            }

        }

    }

    @Override
    public void onStart() {
        super.onStart();
        updateTracks();
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

        ArrayList<ArtistTrack>  tracks;

        MyAdapter(ArrayList<ArtistTrack> tracks){
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

        public void add(int position, ArtistTrack track) {
            artistTracks.add(position, track);
            notifyItemInserted(position);
        }

        public void remove(ArtistTrack track) {
            int position = artistTracks.indexOf(track);
            artistTracks.remove(position);
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

            ArtistTrack atemp = artistTracks.get(position);

            if (atemp != null) {

                String thumbnailUrl = atemp.getImageThumbnailUri();
                holder.ivAlbumPhoto.setImageBitmap(null);
                if (thumbnailUrl != null) {
                    Picasso.with(getActivity()).load(thumbnailUrl).into(holder.ivAlbumPhoto);
                }

                holder.tvAlbum.setText(atemp.getAlbumName());
                holder.tvTrackName.setText(atemp.getTrackName());

            }


        }

        @Override
        public int getItemCount() {
            if (artistTracks != null) {
                return artistTracks.size();
            } else {
                return 0;
            }

        }

    }

    public void showDialog(int position) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        PlayerDialogFragment newFragment = new PlayerDialogFragment();


        Bundle bundles = new Bundle();
        ArtistTrack aTrack = artistTracks.get(position);

        // ensure your object has not null
        if (aTrack != null) {

            bundles.putParcelableArrayList("ArtistTracks", artistTracks);

            Log.e("aTrack", "is valid");
        } else {
            Log.e("aTrack", "is null");
        }
        newFragment.setArguments(bundles);



        if (mIsLargeLayout) {
            // The device is using a large layout, so show the fragment as a dialog
            newFragment.show(fragmentManager, "dialog");
//            newFragment.artistTracks = artistTracks;
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


//            newFragment.artistTracks = artistTracks;
            newFragment.position = position;
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
