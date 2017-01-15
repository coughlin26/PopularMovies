package com.example.android.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Matt on 4/16/2016.
 */
public class Movie implements Parcelable {
    String id;
    String title;
    String description;
    String posterLocation;
    String userRating;
    String releaseDate;
    Review[] reviews;
    Trailer[] trailers;

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(id);
        out.writeString(title);
        out.writeString(description);
        out.writeString(posterLocation);
        out.writeString(userRating);
        out.writeString(releaseDate);
        out.writeArray(reviews);
        out.writeArray(trailers);
    }

    private Movie(Parcel in) {
        id = in.readString();
        title = in.readString();
        description = in.readString();
        posterLocation = in.readString();
        userRating = in.readString();
        releaseDate = in.readString();
        reviews = (Review[]) in.readArray(getClass().getClassLoader());
        trailers = (Trailer[]) in.readArray(getClass().getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    public Movie(String id, String title, String description, String posterLocation, String userRating, String releaseDate, Review[] reviews, Trailer[] trailers) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.posterLocation = posterLocation;
        this.userRating = userRating;
        this.releaseDate = releaseDate;
        this.reviews = reviews;
        this.trailers = trailers;
    }
}
