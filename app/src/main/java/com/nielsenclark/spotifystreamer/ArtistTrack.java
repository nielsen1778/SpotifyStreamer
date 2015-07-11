package com.nielsenclark.spotifystreamer;


import android.os.Parcel;
import android.os.Parcelable;


public class ArtistTrack implements Parcelable {


    private String spotifyId;
    private String artistName;
    private String albumName;
    private String trackName;
    private String imageThumbnailUri;
    private String imageLargeUri;
    private String trackPreviewUrl;
    private int durationMS = 0;


    // Constructor
    public ArtistTrack(String spotifyId, String artistName, String albumName, String trackName, String imageThumbnailUri, String imageLargeUri, String trackPreviewUrl, int durationMS) {
        this.spotifyId = spotifyId;
        this.artistName = artistName;
        this.albumName = albumName;
        this.trackName = trackName;
        this.imageThumbnailUri = imageThumbnailUri;
        this.imageLargeUri = imageLargeUri;
        this.trackPreviewUrl = trackPreviewUrl;
        this.durationMS = durationMS;
    }



    // Getters and Setters
    public String getSpotifyId() {
        return spotifyId;
    }

    public void setSpotifyId(String spotifyId) {
        this.spotifyId = spotifyId;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public String getImageThumbnailUri() {
        return imageThumbnailUri;
    }

    public void setImageThumbnailUri(String imageThumbnailUri) {
        this.imageThumbnailUri = imageThumbnailUri;
    }

    public String getImageLargeUri() {
        return imageLargeUri;
    }

    public void setImageLargeUri(String imageLargeUri) {
        this.imageLargeUri = imageLargeUri;
    }

    public String getTrackPreviewUrl() {
        return trackPreviewUrl;
    }

    public void setTrackPreviewUrl(String trackPreviewUrl) {
        this.trackPreviewUrl = trackPreviewUrl;
    }

    public int getDurationMS() {
        return durationMS;
    }

    public void setDurationMS(int durationMS) {
        this.durationMS = durationMS;
    }


    // Parcel specific code

    private ArtistTrack(Parcel in) {
        spotifyId = in.readString();
        artistName = in.readString();
        albumName = in.readString();
        trackName = in.readString();
        imageThumbnailUri = in.readString();
        imageLargeUri = in.readString();
        trackPreviewUrl = in.readString();
        durationMS = in.readInt();
    }

    public int describeContents() {
        return 0;
    }
/*
    @Override
    public String toString() {
        return number + ": " + color;
    }
*/
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(spotifyId);
        out.writeString(artistName);
        out.writeString(albumName);
        out.writeString(trackName);
        out.writeString(imageThumbnailUri);
        out.writeString(imageLargeUri);
        out.writeString(trackPreviewUrl);
        out.writeInt(durationMS);
    }

    public static final Parcelable.Creator<ArtistTrack> CREATOR = new Parcelable.Creator<ArtistTrack>() {
        public ArtistTrack createFromParcel(Parcel in) {
            return new ArtistTrack(in);
        }

        public ArtistTrack[] newArray(int size) {
            return new ArtistTrack[size];
        }
    };


}
