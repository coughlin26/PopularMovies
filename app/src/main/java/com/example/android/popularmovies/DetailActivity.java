package com.example.android.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

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

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailActivityFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {

            //startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class DetailActivityFragment extends Fragment {

        private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();

        private boolean settingReviews = false;
        private boolean settingTrailers = false;
        private ReviewAdapter mReviewAdapter;
        private TrailerAdapter mTrailerAdapter;

        public DetailActivityFragment() {

            setHasOptionsMenu(true);

        }

        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container,
                                 Bundle savedInstanceState) {
            Intent intent = getActivity().getIntent();
            Movie movie = intent.getParcelableExtra("EXTRA_MOVIE");
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

            ((TextView) rootView.findViewById(R.id.detail_title))
                    .setText(movie.title);

            ((TextView) rootView.findViewById(R.id.detail_description))
                    .setText(movie.description);

            ((TextView) rootView.findViewById(R.id.detail_user_rating))
                    .setText("Rating: " + movie.userRating);

            ((TextView) rootView.findViewById(R.id.detail_release_date))
                    .setText("Release Date: " + movie.releaseDate);

            String baseUrl = "http://image.tmdb.org/t/p/";
            String posterSize = "w185";
            String posterUrl = baseUrl + posterSize + movie.posterLocation;

            ImageView posterView = (ImageView) rootView.findViewById(R.id.detail_poster);
            Picasso.with(getContext()).load(posterUrl).into(posterView);

            Review[] reviews = {new Review("Null", "Null", "Not Available", "Null")};
            Trailer[] trailers = {new Trailer("Null", "Null", "Not Available", "Null")};

            ArrayList<Review> reviewList = new ArrayList<>(Arrays.asList(reviews));
            mReviewAdapter = new ReviewAdapter(getActivity(), reviewList);

            updateReviews(movie.id);

            ListView reviewListView = (ListView) rootView.findViewById(R.id.listview_review);
            reviewListView.setAdapter(mReviewAdapter);
            reviewListView.setFocusable(false);

            ArrayList<Trailer> trailerArrayList = new ArrayList<>(Arrays.asList(trailers));
            mTrailerAdapter = new TrailerAdapter(getActivity(), trailerArrayList);

            updateVideos(movie.id);

            ListView trailerListView = (ListView) rootView.findViewById(R.id.listview_video);
            trailerListView.setAdapter(mTrailerAdapter);
            trailerListView.setFocusable(false);

            trailerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        String videoURL = "http://www.youtube.com/watch?v=" +
                                mTrailerAdapter.getItem(position).key;
                        Intent videoIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoURL));

                        startActivity(videoIntent);
                    } catch (NullPointerException e) {
                        Log.e(LOG_TAG, e.getMessage(), e);
                    }
                }
            });

            return rootView;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.menu_detail, menu);
        }

        public class FetchReviewsTask extends AsyncTask<String, Void, JSONArray> {
            private final String LOG_TAG = FetchVideosTask.class.getSimpleName();

            @Override
            protected JSONArray doInBackground(String... params) {
                Uri.Builder builder = new Uri.Builder();
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;
                URL url;
                JSONArray returnArray;
                String movieID = params[0];

                try {
                    try {
                        builder.scheme("http")
                                .authority("api.themoviedb.org")
                                .appendPath("3")
                                .appendPath("movie")
                                .appendPath(movieID)
                                .appendPath("reviews")
                                .appendQueryParameter("api_key", BuildConfig.MOVIE_DB_API_KEY);
                    } catch (NullPointerException e) {
                        Log.e(LOG_TAG, e.getMessage(), e);
                        return new JSONArray("{\"id\":\"Not Available\"," +
                                "\"content\":\"There are no reviews.\"}");
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

                        JSONObject jsonObject = new JSONObject(stringBuffer.toString());
                        returnArray = jsonObject.getJSONArray("results");

                        return returnArray;
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
                    }
                } catch (JSONException | MalformedURLException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                    return null;
                }
                return null;
            }

            @Override
            protected void onPostExecute(JSONArray resultArray) {
                if (resultArray != null) {
                    mReviewAdapter.clear();
                    Review[] reviews = new Review[resultArray.length()];

                    try {
                        for (int i = 0; i < resultArray.length(); i++) {
                            String id = resultArray.getJSONObject(i).getString("id");
                            String author = resultArray.getJSONObject(i).getString("author");
                            String content = resultArray.getJSONObject(i).getString("content");
                            String url = resultArray.getJSONObject(i).getString("url");

                            reviews[i] = new Review(id, author, content, url);
                        }

                        for (Review review : reviews) {
                            mReviewAdapter.add(review);
                        }
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, e.getMessage(), e);
                    }
                }
            }
        }

        public class FetchVideosTask extends AsyncTask<String, Void, JSONArray> {
            private final String LOG_TAG = FetchVideosTask.class.getSimpleName();

            @Override
            protected JSONArray doInBackground(String... params) {
                Uri.Builder builder = new Uri.Builder();
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;
                URL url;
                JSONArray returnArray;
                String movieID = params[0];

                try {
                    try {
                        builder.scheme("http")
                                .authority("api.themoviedb.org")
                                .appendPath("3")
                                .appendPath("movie")
                                .appendPath(movieID)
                                .appendPath("videos")
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

                        JSONObject jsonObject = new JSONObject(stringBuffer.toString());
                        returnArray = jsonObject.getJSONArray("results");

                        return returnArray;
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
                    }
                } catch (JSONException | MalformedURLException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                    return null;
                }
                return null;
            }

            @Override
            protected void onPostExecute(JSONArray resultArray) {
                if (resultArray != null) {
                    mTrailerAdapter.clear();
                    Trailer[] trailers = new Trailer[resultArray.length()];

                    try {
                        for (int i = 0; i < resultArray.length(); i++) {
                            String id = resultArray.getJSONObject(i).getString("id");
                            String key = resultArray.getJSONObject(i).getString("key");
                            String name = resultArray.getJSONObject(i).getString("name");
                            String site = resultArray.getJSONObject(i).getString("site");

                            trailers[i] = new Trailer(id, key, name, site);
                        }

                        for (Trailer trailer : trailers) {
                            mTrailerAdapter.add(trailer);
                        }
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, e.getMessage(), e);
                    }
                }
            }
        }

        private void updateReviews(String movieID) {
            FetchReviewsTask fetchReviewsTask = new FetchReviewsTask();

            fetchReviewsTask.execute(movieID);
        }

        private void updateVideos(String movieID) {
            FetchVideosTask fetchVideosTask = new FetchVideosTask();

            fetchVideosTask.execute(movieID);
        }
    }

}
