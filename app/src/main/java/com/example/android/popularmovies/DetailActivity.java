package com.example.android.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

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

        public DetailActivityFragment() {

            setHasOptionsMenu(true);

        }

        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container,
                                 Bundle savedInstanceState) {
            Intent intent = getActivity().getIntent();
            Bundle extras = intent.getExtras();
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

            ((TextView) rootView.findViewById(R.id.detail_title))
                    .setText(extras.getString("EXTRA_TITLE"));

            ((TextView) rootView.findViewById(R.id.detail_description))
                    .setText(extras.getString("EXTRA_DESCRIPTION"));

            ((TextView) rootView.findViewById(R.id.detail_user_rating))
                    .setText(extras.getString("EXTRA_USER_RATING"));

            ((TextView) rootView.findViewById(R.id.detail_release_date))
                    .setText(extras.getString("EXTRA_RELEASE_DATE"));

            String baseUrl = "http://image.tmdb.org/t/p/";
            String posterSize = "w185";
            String posterUrl = baseUrl + posterSize + extras.getString("EXTRA_POSTER");

            ImageView posterView = (ImageView) rootView.findViewById(R.id.detail_poster);
            Picasso.with(getContext()).load(posterUrl).into(posterView);

            return rootView;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.menu_detail, menu);
        }
    }

}
