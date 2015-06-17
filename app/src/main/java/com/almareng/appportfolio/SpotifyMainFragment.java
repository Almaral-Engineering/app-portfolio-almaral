package com.almareng.appportfolio;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.almareng.appportfolio.Objects.MusicItem;
import com.almareng.appportfolio.Objects.MusicItems;
import com.almareng.appportfolio.adapters.MusicAdapter;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * A placeholder fragment containing a simple view.
 */
public class SpotifyMainFragment extends Fragment {

    private final static String LOG_TAG = SpotifyMainFragment.class.getSimpleName();

    private SpotifyApi mSpotifyApi = new SpotifyApi();
    private SpotifyService mSpotify = mSpotifyApi.getService();

    private EditText searchEdt;

    private MusicItems mMusicItems = new MusicItems();

    private ListView artistList;
    private MusicAdapter mMusicAdapter;

    public SpotifyMainFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_spotify_main, container, false);

        searchEdt = (EditText) rootView.findViewById(R.id.search_edt);
        artistList = (ListView) rootView.findViewById(R.id.artist_list);

        mMusicAdapter = new MusicAdapter(getActivity(), mMusicItems);

        artistList.setAdapter(mMusicAdapter);

        searchEdt.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_ENTER){

                    if(searchEdt.getText().toString().equals("")){

                        Toast.makeText(getActivity(), getString(R.string.must_insert_artirs_message), Toast.LENGTH_SHORT).show();

                    } else {

                        searchArtist(searchEdt.getText().toString());

                    }

                } else if(keyCode == KeyEvent.KEYCODE_BACK){

                    getActivity().onBackPressed();

                }

                return true;
            }
        });

        if(savedInstanceState != null) {

            if (savedInstanceState.containsKey(SpotifyMainActivity.SEARCH_TEXT_KEY)) {

                searchEdt.setText(savedInstanceState.getString(SpotifyMainActivity.SEARCH_TEXT_KEY));

            }

            if (savedInstanceState.containsKey(SpotifyMainActivity.TRACKS_KEY)) {

                ArrayList<MusicItem> myM = savedInstanceState.getParcelableArrayList(SpotifyMainActivity.TRACKS_KEY);
                mMusicItems.clear();
                mMusicItems.addAll(myM);
                mMusicAdapter.notifyDataSetChanged();

            }
        }

        artistList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MusicItem chosenArtist = (MusicItem) mMusicAdapter.getItem(position);

                Intent intent = new Intent(getActivity(), SpotifyTopTracksActivity.class);

                intent.putExtra(SpotifyMainActivity.ARTIST_ID, chosenArtist.getId());

                startActivity(intent);

            }
        });

        return rootView;

    }

    public void searchArtist(String artist){

        mSpotify.searchArtists(artist, new Callback<ArtistsPager>() {
            @Override
            public void success(ArtistsPager artistsPager, Response response) {

                mMusicItems.clear();

                if (artistsPager.artists.items.size() == 0) {

                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getActivity(), getString(R.string.no_artists_found), Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {

                    for (Artist artist : artistsPager.artists.items) {

                        if (artist.images != null) {

                            if (artist.images.size() > 1) {

                                mMusicItems.add(new MusicItem(artist.id, artist.name, artist.images.get(1).url));

                            } else if (artist.images.size() > 0) {

                                mMusicItems.add(new MusicItem(artist.id, artist.name, artist.images.get(0).url));

                            } else {

                                mMusicItems.add(new MusicItem(artist.id, artist.name, null));

                            }

                        }

                    }

                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            mMusicAdapter.notifyDataSetChanged();
                        }
                    });

                }

            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("Album failed", error.toString());
            }
        });

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putParcelableArrayList(SpotifyMainActivity.TRACKS_KEY, mMusicItems);

        outState.putString(SpotifyMainActivity.SEARCH_TEXT_KEY, searchEdt.getText().toString());

        super.onSaveInstanceState(outState);

    }

}
