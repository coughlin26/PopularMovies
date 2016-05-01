package com.example.android.popularmovies;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Matt on 4/16/2016.
 */
public class MovieAdapter extends ArrayAdapter<Movie> {
    private static final String LOG_TAG = MovieAdapter.class.getSimpleName();

    public MovieAdapter(Activity context, List<Movie> movies) {
        super(context, 0, movies);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Movie movie = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item_movie, parent, false);

        }

        String baseUrl = "http://image.tmdb.org/t/p/";
        String posterSize = "w185";
        String posterUrl = baseUrl + posterSize + movie.posterLocation;

        ImageView posterView = (ImageView) convertView.findViewById(R.id.grid_item_poster_imageview);
        Picasso.with(getContext()).load(posterUrl).into(posterView);

        return convertView;
    }
}
