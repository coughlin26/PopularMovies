package com.example.android.popularmovies;

/**
 * Created by Matt on 1/14/2017.
 */

public class Review {
    String id;
    String author;
    String content;
    String url;

    public Review(String id, String author, String content, String url) {
        this.id = id;
        this.author = author;
        this.content = content;
        this.url = url;
    }
}
