package com.example.android.popularmovies;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.popularmovies.data.MovieContract;
import com.squareup.picasso.Picasso;

/**
 * Created by Matt on 9/27/2016.
 */

public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();
    static final String DETAIL_URI = "URI";

    private String mMovieTitleStr;
    private Uri mUri;

    private static final int DETAIL_LOADER = 0;

    private static final String[] DETAIL_COLUMNS = {
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_POSTER_LOC,
            MovieContract.MovieEntry.COLUMN_DESC,
            MovieContract.MovieEntry.COLUMN_USER_RATING,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE
    };

    public static final int COL_MOVIE_ID = 0;
    public static final int COL_TITLE = 1;
    public static final int COL_POSTER_LOC = 2;
    public static final int COL_DESC = 3;
    public static final int COL_USER_RATING = 4;
    public static final int COL_RELEASE_DATE = 5;

    private ImageView mPosterView;
    private TextView mTitleView;
    private TextView mDescView;
    private TextView mRatingView;
    private TextView mReleaseDateView;

    public DetailActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailActivityFragment.DETAIL_URI);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mPosterView = (ImageView) rootView.findViewById(R.id.detail_poster);
        mTitleView = (TextView) rootView.findViewById(R.id.detail_title);
        mDescView = (TextView) rootView.findViewById(R.id.detail_description);
        mRatingView = (TextView) rootView.findViewById(R.id.detail_user_rating);
        mReleaseDateView = (TextView) rootView.findViewById(R.id.detail_release_date);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_detail, menu);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mUri != null) {
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            int movieId = data.getInt(COL_MOVIE_ID);

            String baseUrl = "http://image.tmdb.org/t/p/";
            String posterSize = "w185";
            String posterUrl = baseUrl + posterSize + data.getString(COL_POSTER_LOC);

            Picasso.with(getContext()).load(posterUrl).into(mPosterView);

            mTitleView.setText(data.getString(COL_TITLE));
            mDescView.setText(data.getString(COL_DESC));
            mRatingView.setText(data.getString(COL_USER_RATING));
            mReleaseDateView.setText(data.getString(COL_RELEASE_DATE));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }
}
