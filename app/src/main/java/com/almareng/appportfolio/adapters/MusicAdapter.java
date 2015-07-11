package com.almareng.appportfolio.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.almareng.appportfolio.Objects.MusicItem;
import com.almareng.appportfolio.Objects.TrackItem;
import com.almareng.appportfolio.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Almaral17@gmail.com on 6/15/2015.
 */
public class MusicAdapter extends BaseAdapter{

    private Context mContext;
    private ArrayList<MusicItem> musicItems;

    public MusicAdapter(Context context, ArrayList<MusicItem> musicItems){

        mContext = context;
        this.musicItems = musicItems;

    }

    @Override
    public int getCount() {

        if(musicItems == null) {

            return 0;

        } else{

            return musicItems.size();

        }

    }

    @Override
    public Object getItem(int position) {
        return musicItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        MusicHolder holder = new MusicHolder();

        if(convertView == null){

            LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
            convertView = inflater.inflate(R.layout.music_list_item, parent, false);

            holder.nameTxt = (TextView) convertView.findViewById(R.id.artist_name);
            holder.image = (ImageView) convertView.findViewById(R.id.artist_image);

            convertView.setTag(holder);

        } else{

            holder = (MusicHolder) convertView.getTag();

        }

        if (musicItems.get(position) instanceof TrackItem){

            TrackItem musicItem = (TrackItem) musicItems.get(position);

            if(musicItem != null){

                Picasso.with(mContext).load(musicItem.getSmallImageUrl()).placeholder(R.mipmap.ic_launcher).into(holder.image);
                holder.nameTxt.setText(musicItem.getAlbumName() + "\n" + musicItem.getName());

            }

        } else{

            MusicItem musicItem = musicItems.get(position);

            if(musicItem != null){

                Picasso.with(mContext).load(musicItem.getSmallImageUrl()).placeholder(R.mipmap.ic_launcher).into(holder.image);
                holder.nameTxt.setText(musicItem.getName());

            }

        }

        return convertView;

    }

    private class MusicHolder{

        TextView nameTxt;
        ImageView image;

    }

}
