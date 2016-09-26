package com.example.android.popularmovies.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.example.android.popularmovies.BuildConfig;
import com.example.android.popularmovies.Movie;
import com.example.android.popularmovies.R;
import com.example.android.popularmovies.Utility;
import com.example.android.popularmovies.data.MovieContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

/**
 * Created by Matt on 8/14/2016.
 */
public class PopularMoviesSyncAdapter extends AbstractThreadedSyncAdapter {

    public final String LOG_TAG = PopularMoviesSyncAdapter.class.getSimpleName();

    // Interval to sync with the movie db
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;
    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 *24;
    private static final int MOVIE_NOTIFICATION_ID = 3004;

    public PopularMoviesSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account,
                              Bundle extras,
                              String authority,
                              ContentProviderClient provider,
                              SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting sync");

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String movieJsonStr = null;
        String sortOrder = Utility.getPreferredSort(getContext());

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

            Log.d("TESTING", "The url is: " + url);

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                return;
            }

            movieJsonStr = buffer.toString();
            getMoviesFromJson(movieJsonStr);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
    }

    private void getMoviesFromJson(String moviesJsonStr) throws JSONException {

        final String MDB_TITLE = "title";
        final String MDB_DESCRIPTION = "overview";
        final String MDB_POSTER_PATH = "poster_path";
        final String MDB_RATING = "vote_average";
        final String MDB_RELEASE_DATE = "release_date";
        final String MDB_LIST = "results";

        try {
            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            JSONArray moviesArray = moviesJson.getJSONArray(MDB_LIST);

            Vector<ContentValues> cVVector = new Vector<ContentValues>(moviesArray.length());

            Movie[] movies = new Movie[moviesArray.length()];

            for (int i = 0; i < moviesArray.length(); i++) {
                String title;
                String description;
                String posterPath;
                String rating;
                String releaseDate;

                JSONObject jsonMovie = moviesArray.getJSONObject(i);

                title = jsonMovie.getString(MDB_TITLE);
                description = jsonMovie.getString(MDB_DESCRIPTION);
                posterPath = jsonMovie.getString(MDB_POSTER_PATH);
                rating = jsonMovie.getString(MDB_RATING);
                releaseDate = jsonMovie.getString(MDB_RELEASE_DATE);

                ContentValues movieValues = new ContentValues();

                movieValues.put(MovieContract.MovieEntry.COLUMN_TITLE, title);
                movieValues.put(MovieContract.MovieEntry.COLUMN_DESC, description);
                movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER_LOC, posterPath);
                movieValues.put(MovieContract.MovieEntry.COLUMN_USER_RATING, rating);
                movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, releaseDate);

                cVVector.add(movieValues);
            }

            if (cVVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                Log.d("TESTING", "Starting bulk insert");
                getContext().getContentResolver().bulkInsert(MovieContract.MovieEntry.CONTENT_URI,
                        cvArray);
            }

            Log.d("TESTING", "Sync Complete. " + cVVector.size() + " Inserted");

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SyncRequest request = new SyncRequest.Builder()
                    .syncPeriodic(syncInterval, flexTime)
                    .setSyncAdapter(account, authority)
                    .setExtras(new Bundle()).build();
        } else {
            ContentResolver.addPeriodicSync(account, authority, new Bundle(), syncInterval);
        }
    }

    public static void syncImmediately(Context context) {
        Log.d("TESTING", "In syncImmediately");
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        Log.d("TESTING", "Requesting Sync");
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority),
                bundle);
        Log.d("TESTING", "Done with immediate sync");
    }

    public static Account getSyncAccount(Context context) {
        Log.d("TESTING", "Getting sync account");
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        Log.d("TESTING", "Creating new account");
        Account newAccount = new Account(
                context.getString(R.string.app_name),
                context.getString(R.string.sync_account_type));
        Log.d("TESTING", "Getting password " + accountManager.getPassword(newAccount));
        Log.d("TESTING", "Password is null " + (accountManager.getPassword(newAccount) == null));
        if (null == accountManager.getPassword(newAccount)) {
            Log.d("TESTING", "Password null");
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                Log.d("TESTING", "Failed to add new account");
                return null;
            }
            onAccountCreated(newAccount, context);
        }
        Log.d("TESTING", "Returning new account " + newAccount.toString());
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        Log.d("TESTING", "In onAccountCreated");
        PopularMoviesSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);
        Log.d("TESTING", "Setting sync time");
        ContentResolver.setSyncAutomatically(newAccount,
                context.getString(R.string.content_authority),
                true);
        Log.d("TESTING", "Syncing immediately from creating the account");
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }
}
