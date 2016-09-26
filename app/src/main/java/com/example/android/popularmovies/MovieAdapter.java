package com.example.android.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Created by Matt on 4/16/2016.
 */
public class MovieAdapter extends CursorAdapter {
    private static final String LOG_TAG = MovieAdapter.class.getSimpleName();

    public MovieAdapter(Activity context, Cursor c, int flags) {
        super(context, c, flags);
    }

    public static class ViewHolder {
        public final ImageView posterView;

        public ViewHolder(View view) {
            posterView = (ImageView) view.findViewById(R.id.grid_item_poster_imageview);
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        int layoutId = R.layout.list_item_movie;

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        String baseUrl = "http://image.tmdb.org/t/p/";
        String posterSize = "w780";
        String posterUrl = baseUrl + posterSize +
                cursor.getString(MainActivityFragment.COL_POSTER_LOC);
        Log.d("TESTING", "Poster URL is " + posterUrl);
        Picasso.with(context).load(posterUrl).into(viewHolder.posterView);
    }
}
