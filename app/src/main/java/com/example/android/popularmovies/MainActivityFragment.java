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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    // This has been removed for the upload. Add your own if you would like to test it.

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
                new Movie("Null", "Null", "Null", "Null", "Null", "Null")
        };

        ArrayList<Movie> movieArrayList = new ArrayList<>(Arrays.asList(movies));

        mMovieAdapter = new MovieAdapter(getActivity(), movieArrayList);

        GridView gridView = (GridView) rootView.findViewById(R.id.gridview_movies);
        gridView.setAdapter(mMovieAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent detailIntent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra("EXTRA_MOVIE", mMovieAdapter.getItem(position));
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

        @Override
        protected Movie[] doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String sortOrder = null;

            if (params[0].equals(getString(R.string.pref_sort_popular))) {
                sortOrder = "popular";
            } else if (params[0].equals(getString(R.string.pref_sort_rating))) {
                sortOrder = "top_rated";
            }

            try {
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("http")
                        .authority("api.themoviedb.org")
                        .appendPath("3")
                        .appendPath("movie")
                        .appendPath(sortOrder)
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

                JSONObject moviesJson = new JSONObject(buffer.toString());
                JSONArray moviesArray = moviesJson.getJSONArray("results");
                Movie[] movies = new Movie[moviesArray.length()];

                for (int i = 0; i < moviesArray.length(); i++) {
                    JSONObject jsonMovie = moviesArray.getJSONObject(i);

                    movies[i] = new Movie(jsonMovie.getString("id"),
                            jsonMovie.getString("title"),
                            jsonMovie.getString("overview"),
                            jsonMovie.getString("poster_path"),
                            jsonMovie.getString("vote_average"),
                            jsonMovie.getString("release_date"));
                }

                return movies;
            } catch (IOException|JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error ", e);
                    }
                }
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

        private void BuildMovies(StringBuffer buffer, Movie[] movies, JSONArray moviesArray) {
            for (int i = 0; i < moviesArray.length(); i++) {
                try {
                    JSONObject jsonMovie = moviesArray.getJSONObject(i);
                    int movieID = moviesArray.getJSONObject(i).getInt("id");
                    Log.d("TESTING", "Movie ID: " + movieID);

                    JSONArray reviewArray = GetReviewsOrVideos("reviews", buffer, movieID);
                    JSONArray videosArray = GetReviewsOrVideos("videos", buffer, movieID);
                    Review[] reviews = {new Review("Null", "Null", "Null", "Null")};
                    Trailer[] trailers = {new Trailer("Null", "Null", "Null", "Null")};

                    movies[i] = new Movie(jsonMovie.getString("id"),
                            jsonMovie.getString("title"),
                            jsonMovie.getString("overview"),
                            jsonMovie.getString("poster_path"),
                            jsonMovie.getString("vote_average"),
                            jsonMovie.getString("release_date"));

                    if (reviewArray != null) {
                        reviews = new Review[reviewArray.length()];

                        for (int j = 0; j < reviewArray.length(); j++) {
                            JSONObject reviewObject = reviewArray.getJSONObject(j);

                            String id = reviewObject.getString("id");
                            String author = reviewObject.getString("author");
                            String content = reviewObject.getString("content");
                            String reviewUrl = reviewObject.getString("url");

                            reviews[j] = new Review(id, author, content, reviewUrl);
                        }
                    }
                    if (videosArray != null) {
                        trailers = new Trailer[videosArray.length()];

                        for (int j = 0; j < videosArray.length(); j++) {
                            JSONObject trailerObject = videosArray.getJSONObject(j);

                            String id = trailerObject.getString("id");
                            String key = trailerObject.getString("key");
                            String name = trailerObject.getString("name");
                            String site = trailerObject.getString("site");

                            trailers[j] = new Trailer(id, key, name, site);
                        }
                    }
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                }
            }
        }

        private JSONArray GetReviewsOrVideos(String query, StringBuffer buffer, int movieID) {
            Uri.Builder builder = new Uri.Builder();
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            URL url;
            JSONArray returnArray = null;

            if (!query.equals("reviews") && !query.equals("videos")) {
                Log.e(LOG_TAG, "Incorrect parameter: " + query);
                return null;
            }

            try {
                Log.d(LOG_TAG, "Buffer: " + buffer.toString());

                try {
                    builder.scheme("http")
                            .authority("api.themoviedb.org")
                            .appendPath("3")
                            .appendPath("movie")
                            .appendPath(Integer.toString(movieID))
                            .appendPath(query)
                            .appendQueryParameter("api_key", BuildConfig.MOVIE_DB_API_KEY);
                } catch (NullPointerException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                    return new JSONArray("{\"id\":\"Not Available\"}");
                }

                url = new URL(builder.build().toString());

                Log.v(LOG_TAG, "The url is: " + url);

                try {
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuffer stringBuffer = new StringBuffer();
                    if (inputStream == null) {
                        return null;
                    }

                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuffer.append(line + "\n");
                    }

                    returnArray = new JSONArray(stringBuffer.toString());
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error in connection: " + e.getMessage(), e);
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (final IOException e) {
                            Log.e(LOG_TAG, e.getMessage(), e);
                        }
                    }
                    if (builder != null) {
                        builder = null;
                    }
                    if (url != null) {
                        url = null;
                    }
                    return returnArray;
                }
            } catch (JSONException | MalformedURLException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                return null;
            }
        }
    }
}
