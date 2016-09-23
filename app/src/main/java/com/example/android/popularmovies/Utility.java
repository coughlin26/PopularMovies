package com.example.android.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Matt on 8/14/2016.
 */
public class Utility {
    public static String getPreferredSort(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString("sort", "popularity.desc");
    }


}
