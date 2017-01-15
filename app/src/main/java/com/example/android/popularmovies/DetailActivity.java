package com.example.android.popularmovies;

import android.content.Intent;
import android.net.Uri;
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

            try {
                JSONArray reviewArray = GetReviewsOrVideos("reviews", movie.id);
                JSONArray videosArray = GetReviewsOrVideos("videos", movie.id);
                Review[] reviews = {new Review("Null", "Null", "Null", "Null")};
                Trailer[] trailers = {new Trailer("Null", "Null", "Null", "Null")};

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

                    movie.reviews = reviews;
                    movie.trailers = trailers;
                }
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
            }

            ArrayList<Review> reviewList = new ArrayList<>(Arrays.asList(movie.reviews));

            mReviewAdapter = new ReviewAdapter(getActivity(), reviewList);

            ListView reviewListView = (ListView) rootView.findViewById(R.id.listview_review);
            reviewListView.setAdapter(mReviewAdapter);

            return rootView;
        }

        private JSONArray GetReviewsOrVideos(String query, String movieID) {
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

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.menu_detail, menu);
        }
    }

}
