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

        private String mMovieTitleStr;
        private ReviewAdapter mReviewAdapter;

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
                    .setText("Release: " + movie.releaseDate);

            String baseUrl = "http://image.tmdb.org/t/p/";
            String posterSize = "w185";
            String posterUrl = baseUrl + posterSize + movie.posterLocation;

            ImageView posterView = (ImageView) rootView.findViewById(R.id.detail_poster);
            Picasso.with(getContext()).load(posterUrl).into(posterView);

            Review[] reviews = {new Review("Null", "Null", "Not Available", "Null")};
            Trailer[] trailers = {new Trailer("Null", "Null", "Null", "Null")};

            ArrayList<Review> reviewList = new ArrayList<>(Arrays.asList(reviews));
            mReviewAdapter = new ReviewAdapter(getActivity(), reviewList);

            updateReviewsAndVideos(movie.id);

            ListView reviewListView = (ListView) rootView.findViewById(R.id.listview_review);
            reviewListView.setAdapter(mReviewAdapter);
            reviewListView.setFocusable(false);

            return rootView;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.menu_detail, menu);
        }

        public class FetchReviewsTask extends AsyncTask<String, Void, JSONArray> {
            private final String LOG_TAG = DetailActivityFragment.FetchReviewsTask.class.getSimpleName();

            @Override
            protected JSONArray doInBackground(String... params) {
                Uri.Builder builder = new Uri.Builder();
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;
                URL url;
                JSONArray returnArray = null;
                String query = params[0];
                String movieID = params[1];

                if (!query.equals("reviews") && !query.equals("videos")) {
                    Log.e(LOG_TAG, "Incorrect parameter: " + query);
                    return null;
                }

                try {
                    try {
                        builder.scheme("http")
                                .authority("api.themoviedb.org")
                                .appendPath("3")
                                .appendPath("movie")
                                .appendPath(movieID)
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

                        JSONObject reviewObject = new JSONObject(stringBuffer.toString());
                        returnArray = reviewObject.getJSONArray("results");

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
            protected void onPostExecute(JSONArray reviewArray) {
                if (reviewArray != null) {
                    mReviewAdapter.clear();
                    Review[] reviews = new Review[reviewArray.length()];

                    try {
                        for (int i = 0; i < reviewArray.length(); i++) {
                            String id = reviewArray.getJSONObject(i).getString("id");
                            String author = reviewArray.getJSONObject(i).getString("author");
                            String content = reviewArray.getJSONObject(i).getString("content");
                            String url = reviewArray.getJSONObject(i).getString("url");

                            reviews[i] = new Review(id, author, content, url);
                        }
                        ArrayList<Review> reviewArrayList = new ArrayList<>(Arrays.asList(reviews));
                        for (Review review : reviews) {
                            mReviewAdapter.add(review);
                        }
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, e.getMessage(), e);
                    }
                }
            }
        }

        private void updateReviewsAndVideos(String movieID) {
            FetchReviewsTask fetchReviewsTask = new FetchReviewsTask();

            fetchReviewsTask.execute("reviews", movieID);
        }
    }

}
