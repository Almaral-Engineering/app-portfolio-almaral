package com.almareng.appportfolio.Objects;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by almaral17@gmail.com on 6/17/2015.
 */
public class MusicItem implements Parcelable {

    private String id;
    private String name;
    private String smallImageUrl;

    public MusicItem(String id, String name, String smallImageUrl) {
        this.id = id;
        this.name = name;
        this.smallImageUrl = smallImageUrl;
    }

    public MusicItem(){

    }

    public String getId(){ return id; }

    public String getName() {
        return name;
    }

    public String getSmallImageUrl(){ return smallImageUrl; }


    protected MusicItem(Parcel in) {
        id = in.readString();
        name = in.readString();
        smallImageUrl = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(smallImageUrl);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<MusicItem> CREATOR = new Parcelable.Creator<MusicItem>() {
        @Override
        public MusicItem createFromParcel(Parcel in) {
            return new MusicItem(in);
        }

        @Override
        public MusicItem[] newArray(int size) {
            return new MusicItem[size];
        }
    };
}
