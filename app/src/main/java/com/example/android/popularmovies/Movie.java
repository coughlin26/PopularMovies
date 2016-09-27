package com.example.android.popularmovies;

/**
 * Created by Matt on 4/16/2016.
 */
public class Movie {
    String title;
    String posterLocation;
    String description;
    String userRating;
    String releaseDate;

    public Movie(String title,
                 String description,
                 String posterLocation,
                 String userRating,
                 String releaseDate) {
        this.title = title;
        this.posterLocation = posterLocation;
        this.description = description;
        this.userRating = userRating;
        this.releaseDate = releaseDate;
    }
}
