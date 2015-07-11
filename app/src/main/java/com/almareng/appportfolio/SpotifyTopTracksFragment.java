package com.almareng.appportfolio;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.almareng.appportfolio.Objects.MusicItem;
import com.almareng.appportfolio.Objects.TrackItem;
import com.almareng.appportfolio.adapters.MusicAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * A placeholder fragment containing a simple view.
 */
public class SpotifyTopTracksFragment extends Fragment {

    private final static String LOG_TAG = SpotifyTopTracksFragment.class.getSimpleName();

    private String mArtistId;
    private String mArtistName;

    private MusicItem mChosenArtist;

    private SpotifyApi mSpotifyApi = new SpotifyApi();
    private SpotifyService mSpotify = mSpotifyApi.getService();

    private MusicAdapter mMusicAdapter;

    private ArrayList<MusicItem> mMusicItems = new ArrayList<>();

    public SpotifyTopTracksFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if(arguments != null){

            mChosenArtist = arguments.getParcelable(SpotifyMainActivity.CHOSEN_ARTIST);

            if(mChosenArtist != null) {
                mArtistId = mChosenArtist.getId();
                mArtistName = mChosenArtist.getName();
            }

        }

        View rootView = inflater.inflate(R.layout.fragment_spotify_top_tracks, container, false);

        mMusicAdapter = new MusicAdapter(getActivity(), mMusicItems);

        ListView topTracksList = (ListView) rootView.findViewById(R.id.top_tracks_list);

        topTracksList.setAdapter(mMusicAdapter);

        if(savedInstanceState != null && savedInstanceState.containsKey(SpotifyMainActivity.TRACKS_KEY)) {

            ArrayList<MusicItem> myM = savedInstanceState.getParcelableArrayList(SpotifyMainActivity.TRACKS_KEY);
            mMusicItems.addAll(myM);
            mMusicAdapter.notifyDataSetChanged();

        } else{

            searchTopTracks(mArtistId);

        }

        topTracksList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                boolean mTwoPane = getResources().getBoolean(R.bool.large_layout);

                if(mTwoPane) {

                    ((SpotifyTopTracksCallback) getActivity()).showPlayFragment(mMusicItems, position, mArtistName);

                } else {

                    Intent intent = new Intent(getActivity(), SpotifyPlayActivity.class);

                    intent.putParcelableArrayListExtra(SpotifyMainActivity.TRACKS, mMusicItems);

                    intent.putExtra(SpotifyMainActivity.TRACK_POSITION, position);

                    intent.putExtra(SpotifyMainActivity.ARTIST_NAME, mArtistName);

                    startActivity(intent);
                }

            }
        });

        return rootView;
    }

    public interface SpotifyTopTracksCallback{

        void showPlayFragment(ArrayList<MusicItem> musicItems, int position, String artistName);

    }

    public void searchTopTracks(String artistId){

        Map<String, Object> map = new HashMap<>();
        String country = "US";
        map.put("country", country);

        mSpotify.getArtistTopTrack(artistId, map, new Callback<Tracks>() {
            @Override
            public void success(Tracks tracks, Response response) {

                mMusicItems.clear();

                for(Track track : tracks.tracks) {

                    if (track.album.images != null) {

                        if (track.album.images.size() > 1) {

                            mMusicItems.add(new TrackItem(track.id, track.name, track.duration_ms, track.album.images.get(0).url, track.album.images.get(1).url, track.album.name, track.preview_url));

                        } else if (track.album.images.size() > 0) {

                            mMusicItems.add(new TrackItem(track.id, track.name, track.duration_ms,track.album.images.get(0).url, track.album.images.get(0).url, track.album.name, track.preview_url));

                        } else {

                            mMusicItems.add(new TrackItem(track.id, track.name, track.duration_ms, null, null, track.album.name, track.preview_url));

                        }

                    }

                }

                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        if(mMusicAdapter != null) {
                            mMusicAdapter.notifyDataSetChanged();
                        }
                    }
                });
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d(LOG_TAG, error.toString());
            }
        });

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putParcelableArrayList(SpotifyMainActivity.TRACKS_KEY, mMusicItems);

        super.onSaveInstanceState(outState);

    }
}
