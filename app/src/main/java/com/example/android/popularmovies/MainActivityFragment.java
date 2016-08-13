package com.example.android.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private MovieAdapter mMovieAdapter;

    public MainActivityFragment() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        Movie[] movies = {
                new Movie("Null", "Null", "Null", "Null", "Null")
        };

        ArrayList<Movie> movieArrayList = new ArrayList<>(Arrays.asList(movies));

        mMovieAdapter = new MovieAdapter(getActivity(), movieArrayList);

        GridView gridView = (GridView) rootView.findViewById(R.id.gridview_movies);
        gridView.setAdapter(mMovieAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String title = mMovieAdapter.getItem(position).title;
                String description = mMovieAdapter.getItem(position).description;
                String posterLocation = mMovieAdapter.getItem(position).posterLocation;
                String userRating = "Average Rating: " + mMovieAdapter.getItem(position).userRating;
                String releaseDate = "Release: " + mMovieAdapter.getItem(position).releaseDate;

                Bundle extrasBundle = new Bundle();
                extrasBundle.putString("EXTRA_TITLE", title);
                extrasBundle.putString("EXTRA_DESCRIPTION", description);
                extrasBundle.putString("EXTRA_POSTER", posterLocation);
                extrasBundle.putString("EXTRA_USER_RATING", userRating);
                extrasBundle.putString("EXTRA_RELEASE_DATE", releaseDate);


                Intent detailIntent = new Intent(getActivity(), DetailActivity.class)
                        .putExtras(extrasBundle);
                startActivity(detailIntent);
            }
        });

        return rootView;
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
        FetchMoviesTask moviesTask = new FetchMoviesTask();
        String sortOrder = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString(getString(R.string.pref_sort_key),
                        getString(R.string.pref_sort_popular));

        moviesTask.execute(sortOrder);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovies();
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, Movie[]> {
        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        private Movie[] getMoviesFromJson(String moviesJsonStr) throws JSONException {

            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            JSONArray moviesArray = moviesJson.getJSONArray("results");

            Movie[] movies = new Movie[moviesArray.length()];

            for (int i = 0; i < moviesArray.length(); i++) {
                JSONObject jsonMovie = moviesArray.getJSONObject(i);

                movies[i] = new Movie(jsonMovie.getString("title"),
                        jsonMovie.getString("overview"),
                        jsonMovie.getString("poster_path"),
                        jsonMovie.getString("vote_average"),
                        jsonMovie.getString("release_date"));
            }

            return movies;
        }

        @Override
        protected Movie[] doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String movieJsonStr = null;
            String sortOrder = null;

            if (params[0].equals(getString(R.string.pref_sort_popular))) {
                sortOrder = "popularity.desc";
            } else if (params[0].equals(getString(R.string.pref_sort_rating))) {
                sortOrder = "vote_average.desc";
            }

            try {
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("http")
                        .authority("api.themoviedb.org")
                        .appendPath("3")
                        .appendPath("discover")
                        .appendPath("movie")
                        .appendQueryParameter("sort_by", sortOrder)
                        .appendQueryParameter("api_key", BuildConfig.MOVIE_DB_API_KEY);

                URL url = new URL(builder.build().toString());

                Log.v(LOG_TAG, "The url is: " + url);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }

                movieJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e("MovieFragment", "Error ", e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("MovieFragment", "Error ", e);
                    }
                }
            }

            try {
                return getMoviesFromJson(movieJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Movie[] result) {
            if (result != null) {
                mMovieAdapter.clear();
                for (Movie movie : result) {
                    mMovieAdapter.add(movie);
                }
            }
        }
    }
}
