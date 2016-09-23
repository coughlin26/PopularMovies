package com.example.android.popularmovies.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Matt on 8/15/2016.
 */
public class PopularMoviesAuthenticatorService extends Service {
    private PopularMoviesAuthenticator popularMoviesAuthenticator;

    @Override
    public void onCreate() {
        popularMoviesAuthenticator = new PopularMoviesAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return popularMoviesAuthenticator.getIBinder();
    }
}
