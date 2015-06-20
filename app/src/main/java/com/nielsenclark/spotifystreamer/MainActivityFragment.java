package com.nielsenclark.spotifystreamer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;


public class MainActivityFragment extends Fragment {

    private final String LOG_TAG = MainActivityFragment .class.getSimpleName();

    List<Artist> listOfArtists;

    private EditText etArtistSearch;


    private RecyclerView rvArtists;
    private RecyclerView.Adapter mArtistsAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private String currentArtist;


    // flag for Internet connection status
    Boolean isInternetPresent = false;

    // Connection detector class
    ConnectionDetector cd;


    public MainActivityFragment() {

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

        if (currentArtist == null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            currentArtist = prefs.getString(getString(R.string.pref_last_artist_search_key), getString(R.string.pref_last_artist_search_default));
            //etArtistSearch.setText(currentArtist);
        }


        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        etArtistSearch = (EditText) rootView.findViewById(R.id.etArtistSearch);
        etArtistSearch.setText(currentArtist);
        etArtistSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count > 3) {
                    currentArtist = s.toString();

                    Log.d(LOG_TAG, "On Text Changed: " + currentArtist);


                    if (isNetworkAvailable()) {
                        FetchArtistsTask artistsTask = new FetchArtistsTask();
                        artistsTask.execute(currentArtist);
                    }

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(getString(R.string.pref_last_artist_search_key), s.toString());
                    editor.commit();

                } else if (count == 0) {
                    Toast.makeText(getActivity(), "Enter to search for an artist. ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                /*
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(getString(R.string.pref_last_artist_search_key), s.toString());
                editor.commit();
                */
            }
        });
        etArtistSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etArtistSearch.setText("");
            }
        });


        rvArtists = (RecyclerView) rootView.findViewById(R.id.rvArtists);

        mLayoutManager = new LinearLayoutManager(getActivity());
        rvArtists.setLayoutManager(mLayoutManager);

        mArtistsAdapter = new MyAdapter(listOfArtists);
        rvArtists.setAdapter(mArtistsAdapter);

        final GestureDetector mGestureDetector = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }

        });


        rvArtists.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
                View child = recyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());


                if (child != null && mGestureDetector.onTouchEvent(motionEvent)) {


                    String spotifyID = listOfArtists.get(recyclerView.getChildPosition(child)).id;

                    Intent topTenTracks = new Intent(getActivity(), TopTracksActivity.class)
                            .putExtra("SpotifyID", spotifyID);
                    getActivity().startActivity(topTenTracks);

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

        outState.putString("CurrentArtist", currentArtist);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {

            currentArtist = savedInstanceState.getString("CurrentArtist");
            etArtistSearch.setText(currentArtist);
            //FetchArtistsTask artistsTask = new FetchArtistsTask();
            //artistsTask.execute(currentArtist);
        }
    }

    private void updateArtists() {
        Log.d(LOG_TAG, "Update Artists");
        if (isNetworkAvailable()) {
            FetchArtistsTask artistsTask = new FetchArtistsTask();
            artistsTask.execute(currentArtist);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "On Start");
        updateArtists();
    }

    public class FetchArtistsTask extends AsyncTask<String, Void, List<Artist>> {

        private final String LOG_TAG = FetchArtistsTask .class.getSimpleName();


        @Override
        protected List<Artist> doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            try {
                SpotifyApi api = new SpotifyApi();
                SpotifyService spotify = api.getService();


                ArtistsPager results = spotify.searchArtists(params[0]);

                listOfArtists = results.artists.items;
                for (Artist element : listOfArtists) {
                    String name = element.name;
                    Log.d(LOG_TAG, "Name" + name);
                }

            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception:" + e.getMessage());
            }

            if (listOfArtists.size() > 0) {
                return listOfArtists;
            } else {
                return null;
            }



        }

        @Override
        protected void onPostExecute(List<Artist>  result) {
            if (result != null) {
                mArtistsAdapter = new MyAdapter(listOfArtists);
                rvArtists.setAdapter(mArtistsAdapter);
            } else {
                Toast.makeText(getActivity(), "Artist not found. ", Toast.LENGTH_SHORT).show();

                listOfArtists.clear();
                mArtistsAdapter = new MyAdapter(listOfArtists);
                rvArtists.setAdapter(mArtistsAdapter);

            }

        }


    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

        List<Artist> artists;

        MyAdapter(List<Artist> artists){
            this.artists = artists;
        }


        public class ViewHolder extends RecyclerView.ViewHolder {

            public ImageView ivArtistPhoto;
            public TextView tvArtistName;

            public ViewHolder(View v) {
                super(v);
                ivArtistPhoto = (ImageView) v.findViewById(R.id.ivArtistPhoto);
                tvArtistName = (TextView) v.findViewById(R.id.tvArtistName);
            }
        }

        public void add(int position, Artist artist) {
            listOfArtists.add(position, artist);
            notifyItemInserted(position);
        }

        public void remove(Artist artist) {
            int position = listOfArtists.indexOf(artist);
            listOfArtists.remove(position);
            notifyItemRemoved(position);
        }


        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.artist_line_item, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            Artist atemp = listOfArtists.get(position);
            if (atemp != null) {
                List<Image> images = listOfArtists.get(position).images;

                if (images != null && images.size() > 0) {
                    String thumbnailUrl = listOfArtists.get(position).images.get(2).url;
                    holder.ivArtistPhoto.setImageBitmap(null);
                    if (thumbnailUrl != null) {
                        Picasso.with(getActivity()).load(listOfArtists.get(position).images.get(2).url).into(holder.ivArtistPhoto);
                    }
                }
            }

            holder.tvArtistName.setText(listOfArtists.get(position).name);

        }


        @Override
        public int getItemCount() {
            if (listOfArtists != null) {
                return listOfArtists.size();
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
