package com.example.android.popularmovies.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

/**
 * Created by Matt on 9/8/2016.
 */
public class MovieProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDBHelper mOpenHelper;

    static final int MOVIE = 100;

    private static final SQLiteQueryBuilder sMovieQueryBuilder;

    static {
        sMovieQueryBuilder = new SQLiteQueryBuilder();

        sMovieQueryBuilder.setTables(
                MovieContract.MovieEntry.TABLE_NAME + " INNER JOIN " +
                        MovieContract.MovieEntry.TABLE_NAME +
                        " ON " + MovieContract.MovieEntry.TABLE_NAME +
                        "." + MovieContract.MovieEntry._ID);
    }

    private static final String sSortSettingSelection =
            MovieContract.MovieEntry.TABLE_NAME +
                    "." + MovieContract.MovieEntry.COLUMN_USER_RATING + " = ? ";

    private Cursor getMoviesBySortSetting(Uri uri, String[] projection) {
        String sortSetting = MovieContract.MovieEntry.getSortFromUri(uri);

        String[] selectionArgs = new String[]{sortSetting};
        String selection = sSortSettingSelection;

        return sMovieQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortSetting);
    }

    static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, MovieContract.PATH_MOVIE, MOVIE);
        return matcher;
    }

    @Override public boolean onCreate() {
        mOpenHelper = new MovieDBHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {

        if (sUriMatcher.match(uri) == MOVIE) {
            return MovieContract.MovieEntry.CONTENT_TYPE;
        } else {
            throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

    }

    @Override
    public Cursor query(Uri uri,
                        String[] projection,
                        String selection,
                        String[] selectionArgs,
                        String sortOrder) {
        Cursor retCursor;

        if (sUriMatcher.match(uri) == MOVIE) {
            retCursor = mOpenHelper.getReadableDatabase().query(
                    MovieContract.MovieEntry.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    sortOrder
            );
            retCursor.setNotificationUri(getContext().getContentResolver(), uri);
            return  retCursor;
        } else {
            throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Uri returnUri;

        if (sUriMatcher.match(uri) == MOVIE) {
            Log.d("TESTING", "Values: " + values.toString());
            long _id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, values);
            if (_id > 0) {
                returnUri = MovieContract.MovieEntry.buildMovieUri(_id);
            } else {
                throw new android.database.SQLException("Failed to insert row into " + uri);
            }

            getContext().getContentResolver().notifyChange(uri, null);
            return returnUri;
        } else {
            throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsDeleted;

        if (selection == null) {
            selection = "1";
        }

        if (sUriMatcher.match(uri) == MOVIE) {
            rowsDeleted = db.delete(MovieContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
            if (rowsDeleted != 0) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
            return rowsDeleted;
        } else {
            throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsUpdated;

        if (sUriMatcher.match(uri) == MOVIE) {
            rowsUpdated = db.update(MovieContract.MovieEntry.TABLE_NAME,
                    values,
                    selection,
                    selectionArgs);

            if (rowsUpdated != 0) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
            return rowsUpdated;
        } else {
            throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        if (sUriMatcher.match(uri) == MOVIE) {
            db.beginTransaction();
            int returnCount = 0;
            try {
                for (ContentValues value : values) {
                    Log.d("TESTING", "Value: " + value.toString());
                    long _id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, value);
                    if (_id != -1) {
                        returnCount++;
                    }
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            getContext().getContentResolver().notifyChange(uri, null);
            return returnCount;
        } else {
            return super.bulkInsert(uri, values);
        }

    }

    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }

}
