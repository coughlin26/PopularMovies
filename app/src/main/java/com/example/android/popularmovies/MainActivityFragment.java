package com.example.android.popularmovies;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.example.android.popularmovies.data.MovieContract;
import com.example.android.popularmovies.sync.PopularMoviesSyncAdapter;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private MovieAdapter mMovieAdapter;
    private GridView mGridView;
    private int mPosition = GridView.INVALID_POSITION;

    private static final String SELECTED_KEY = "selected_key";

    private static final int POPULAR_MOVIES_LOADER = 0;

    private static final String[] MOVIE_COLUMNS = {
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_POSTER_LOC,
            MovieContract.MovieEntry.COLUMN_DESC,
            MovieContract.MovieEntry.COLUMN_USER_RATING,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE
    };

    static final int COL_MOVIE_ID = 0;
    static final int COL_TITLE = 1;
    static final int COL_POSTER_LOC = 2;
    static final int COL_DESC = 3;
    static final int COL_USER_RATING = 4;
    static final int COL_RELEASE_DATE = 5;

    public interface Callback {
        public void onItemSelected(Uri movieUri);
    }

    public MainActivityFragment() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMovieAdapter = new MovieAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mGridView = (GridView) rootView.findViewById(R.id.gridview_movies);
        mGridView.setAdapter(mMovieAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (cursor != null) {
                    String sortSetting = Utility.getPreferredSort(getActivity());
                    ((Callback) getActivity())
                            .onItemSelected(MovieContract.MovieEntry.buildMovieWithSort(
                                    sortSetting, cursor.getString(COL_TITLE)
                            ));
                }
                mPosition = position;
            }
        });

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(POPULAR_MOVIES_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateMovies() {
        PopularMoviesSyncAdapter.syncImmediately(getActivity());
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovies();
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        if (mPosition != GridView.INVALID_POSITION) {
            bundle.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(bundle);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String sortOrder = "popularity.desc";

        Uri popularMoviesUri = null;

        return new CursorLoader(getActivity(),
                popularMoviesUri,
                null,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (mPosition != GridView.INVALID_POSITION) {
            mGridView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
