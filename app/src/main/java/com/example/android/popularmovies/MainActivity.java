package com.example.android.popularmovies;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.popularmovies.data.FavoriteMoviesContract;

import java.util.ArrayList;
import java.util.List;

import static com.example.android.popularmovies.QueryUtils.KEY_IMAGE_URL;
import static com.example.android.popularmovies.QueryUtils.KEY_MOVIE_ID;
import static com.example.android.popularmovies.QueryUtils.KEY_MOVIE_TITLE;
import static com.example.android.popularmovies.QueryUtils.KEY_PLOT;
import static com.example.android.popularmovies.QueryUtils.KEY_RELEASE_DATE;
import static com.example.android.popularmovies.QueryUtils.KEY_VOTE_AVERAGE;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private ProgressBar mProgressBar;
    private static String URL = null;
    private TextView mEmptyStateTextView;
    private TextView noInternetTextView;
    private GridView mMoviesGridView;
    private static final String LOG_TAG = MainActivity.class.getName();
    private static Parcelable STATE = null;
    private String mOrderByPrefFlag = null;
    private SharedPreferences sharedPrefs = null;
    FavoriteMoviesCursorAdapter favoriteMoviesCursorAdapter;
    private ArrayList<Movies> allMoviesFromDb;
    private static final String BASE_URL_POPULAR_MOVIES = "http://api.themoviedb.org/3/movie/popular?api_key=";
    private static final String BASE_URL_TOP_RATED_MOVIES = "http://api.themoviedb.org/3/movie/top_rated?api_key=";
    public static final String API_KEY = "df7988add67913754a7b1a30267ac2dc";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate Method is called now");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mEmptyStateTextView = findViewById(R.id.empty_view);
        mProgressBar = findViewById(R.id.loading_spinner);
        noInternetTextView = findViewById(R.id.no_internet);
        mMoviesGridView = findViewById(R.id.movies_grid);
        mProgressBar.setVisibility(View.GONE);
        if (isNetworkConnected()) {
            mProgressBar.setVisibility(View.VISIBLE);
            loadMoviesInGridView();
        } else {
            mProgressBar.setVisibility(View.GONE);
            mEmptyStateTextView.setVisibility(View.GONE);
            noInternetTextView.setVisibility(View.VISIBLE);
            noInternetTextView.setText(R.string.no_internet);
        }
    }

    /**
     * This function loads the movies into the GridView based on the Shared Preference's option
     * which is set
     */
    private void loadMoviesInGridView() {
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        String orderBy = sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default)
        );
        mOrderByPrefFlag = orderBy;

        if (orderBy.contentEquals(getString(R.string.settings_order_by_popularity_value))) {
            URL = BASE_URL_POPULAR_MOVIES;
            URL = URL + API_KEY;
            new LoadMoviesFromHttpTask().execute(URL);
        } else if (orderBy.contentEquals(getString(R.string.settings_order_by_top_rated_value))) {
            URL = BASE_URL_TOP_RATED_MOVIES;
            URL = URL + API_KEY;
            new LoadMoviesFromHttpTask().execute(URL);
        } else if (orderBy.contentEquals(getString(R.string.settings_order_by_favorites_value))) {
            favoriteMoviesCursorAdapter = new FavoriteMoviesCursorAdapter(this, null);
            mMoviesGridView.setAdapter(favoriteMoviesCursorAdapter);
            mMoviesGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Uri uri = ContentUris.withAppendedId(FavoriteMoviesContract.FavoriteMoviesEntry.CONTENT_URI, id);
                    Intent movieDetailsActivity = new Intent(MainActivity.this, MovieDetailsActivity.class);
                    movieDetailsActivity.setData(uri);
                    Movies currentInstanceMovie = allMoviesFromDb.get(position);
                    movieDetailsActivity.putExtra(KEY_IMAGE_URL, currentInstanceMovie.getmImageUrl());
                    movieDetailsActivity.putExtra(KEY_MOVIE_TITLE, currentInstanceMovie.getmMovieTitle());
                    movieDetailsActivity.putExtra(KEY_RELEASE_DATE, currentInstanceMovie.getmReleaseDate());
                    movieDetailsActivity.putExtra(KEY_VOTE_AVERAGE, currentInstanceMovie.getmVoteAverage());
                    movieDetailsActivity.putExtra(KEY_PLOT, currentInstanceMovie.getmPlot());
                    movieDetailsActivity.putExtra(KEY_MOVIE_ID, currentInstanceMovie.getmMovieId());
                    startActivity(movieDetailsActivity);
                }
            });
            getSupportLoaderManager().restartLoader(0, null, this);
        }
    }

    /**
     * checks the network connection
     *
     * @return boolean value
     */
    private boolean isNetworkConnected() {
        Log.d(LOG_TAG, "isNetworkConnected Method is called now");
        boolean isConnected;
        ConnectivityManager cm =
                (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * @param id   The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     * @return Return a new Loader instance that is ready to start loading.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(LOG_TAG, "onCreateLoader Method is called now");
        Uri baseUri;
        baseUri = FavoriteMoviesContract.FavoriteMoviesEntry.CONTENT_URI;
        return new CursorLoader(getApplicationContext(), baseUri, null, null, null, null);
    }

    /**
     * Called when a previously created loader has finished its load.  Note
     * that normally an application is <em>not</em> allowed to commit fragment
     * transactions while in this call, since it can happen after an
     * activity's state is saved.  See {@link FragmentManager#beginTransaction()
     * FragmentManager.openTransaction()} for further discussion on this.
     * <p>
     * <p>This function is guaranteed to be called prior to the release of
     * the last data that was supplied for this Loader.  At this point
     * you should remove all use of the old data (since it will be released
     * soon), but should not do your own release of the data since its Loader
     * owns it and will take care of that.  The Loader will take care of
     * management of its data so you don't have to.  In particular:
     * <p>
     * <ul>
     * <li> <p>The Loader will monitor for changes to the data, and report
     * them to you through new calls here.  You should not monitor the
     * data yourself.  For example, if the data is a {@link Cursor}
     * and you place it in a {@link CursorAdapter}, use
     * the {@link CursorAdapter#CursorAdapter(Context, * Cursor, int)} constructor <em>without</em> passing
     * in either {@link CursorAdapter#FLAG_AUTO_REQUERY}
     * or {@link CursorAdapter#FLAG_REGISTER_CONTENT_OBSERVER}
     * (that is, use 0 for the flags argument).  This prevents the CursorAdapter
     * from doing its own observing of the Cursor, which is not needed since
     * when a change happens you will get a new Cursor throw another call
     * here.
     * <li> The Loader will release the data once it knows the application
     * is no longer using it.  For example, if the data is
     * a {@link Cursor} from a {@link CursorLoader},
     * you should not call close() on it yourself.  If the Cursor is being placed in a
     * {@link CursorAdapter}, you should use the
     * {@link CursorAdapter#swapCursor(Cursor)}
     * method so that the old Cursor is not closed.
     * </ul>
     *
     * @param loader The Loader that has finished.
     * @param data   The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(LOG_TAG, "onLoadFinished Method is called now");
        allMoviesFromDb = new ArrayList<>();
        if (data != null) {
            if (data.getCount() > 0) {
                try {
                    while (data.moveToNext()) {
                        String movieIdFromDb = data.getString(data.getColumnIndex(FavoriteMoviesContract.FavoriteMoviesEntry.COLUMN_MOVIE_ID));
                        String moviePosterUrlFromDb = data.getString(data.getColumnIndex(FavoriteMoviesContract.FavoriteMoviesEntry.COLUMN_IMAGE_URL));
                        String movieTitleFromDb = data.getString(data.getColumnIndex(FavoriteMoviesContract.FavoriteMoviesEntry.COLUMN_MOVIE_TITLE));
                        String movieReleaseDateFromDb = data.getString(data.getColumnIndex(FavoriteMoviesContract.FavoriteMoviesEntry.COLUMN_RELEASE_DATE));
                        String movieRatingFromDb = data.getString(data.getColumnIndex(FavoriteMoviesContract.FavoriteMoviesEntry.COLUMN_VOTE));
                        String moviePlotFromDb = data.getString(data.getColumnIndex(FavoriteMoviesContract.FavoriteMoviesEntry.COLUMN_PLOT));
                        allMoviesFromDb.add(new Movies(movieIdFromDb, moviePosterUrlFromDb, movieTitleFromDb, movieReleaseDateFromDb, movieRatingFromDb, moviePlotFromDb));
                    }
                } finally {
                    mProgressBar.setVisibility(View.GONE);
                    if (STATE != null) {
                        mMoviesGridView.onRestoreInstanceState(STATE);
                    }
                }
            } else {
                mProgressBar.setVisibility(View.GONE);
                mEmptyStateTextView.setVisibility(View.VISIBLE);
                mEmptyStateTextView.setText(R.string.no_movies_found);
            }
        }
        favoriteMoviesCursorAdapter.swapCursor(data);
    }

    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.  The application should at this point
     * remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(LOG_TAG, "onLoaderReset Method is called now");
        favoriteMoviesCursorAdapter.swapCursor(null);
    }

    /**
     * Async task to load movies from http request
     * Takes input as url in string format
     */
    public class LoadMoviesFromHttpTask extends AsyncTask<String, Void, List<Movies>> {

        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param strings The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        @Override
        protected List<Movies> doInBackground(String... strings) {
            Log.d(LOG_TAG, "LoadMoviesFromHttpTask Class -  doInBackground method is called now");
            return QueryUtils.fetchMoviesInfos(strings[0]);
        }

        /**
         * <p>Runs on the UI thread after {@link #doInBackground}. The
         * specified result is the value returned by {@link #doInBackground}.</p>
         * <p>
         * <p>This method won't be invoked if the task was cancelled.</p>
         *
         * @param movies The result of the operation computed by {@link #doInBackground}.
         * @see #onPreExecute
         * @see #doInBackground
         * @see #onCancelled(Object)
         */
        @Override
        protected void onPostExecute(List<Movies> movies) {
            super.onPostExecute(movies);
            Log.d(LOG_TAG, "LoadMoviesFromHttpTask Class -  onPostExecute Method is called now");
            mProgressBar.setVisibility(View.GONE);
            if (movies == null) {
                updateUI(new ArrayList<Movies>());
            } else if (movies.size() == 0) {
                if (isNetworkConnected()) {
                    noInternetTextView.setVisibility(View.GONE);
                    updateUI(new ArrayList<Movies>());
                    mEmptyStateTextView.setVisibility(View.VISIBLE);
                    mEmptyStateTextView.setText(R.string.no_movies_found);
                } else {
                    updateUI(new ArrayList<Movies>());
                    mEmptyStateTextView.setVisibility(View.GONE);
                    noInternetTextView.setVisibility(View.VISIBLE);
                    noInternetTextView.setText(R.string.no_internet);
                }
            } else {
                if (isNetworkConnected()) {
                    noInternetTextView.setVisibility(View.GONE);
                    mEmptyStateTextView.setVisibility(View.GONE);
                    updateUI(movies);
                    if (STATE != null) {
                        mMoviesGridView.onRestoreInstanceState(STATE);
                    }
                } else {
                    updateUI(new ArrayList<Movies>());
                    mEmptyStateTextView.setVisibility(View.GONE);
                    noInternetTextView.setVisibility(View.VISIBLE);
                    noInternetTextView.setText(R.string.no_internet);
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        STATE = mMoviesGridView.onSaveInstanceState();
        Log.d(LOG_TAG, "onPause Method is called now");
    }

    @Override
    protected void onResume() {
        String orderBy = null;
        super.onResume();
        Log.d(LOG_TAG, "onResume Method is called now");
        if (isNetworkConnected()) {
            noInternetTextView.setVisibility(View.GONE);
            if (sharedPrefs != null) {
                orderBy = sharedPrefs.getString(
                        getString(R.string.settings_order_by_key),
                        getString(R.string.settings_order_by_default)
                );
                if (!orderBy.contentEquals(mOrderByPrefFlag)) {
                    STATE = null;
                    loadMoviesInGridView();
                }
            } else {
                loadMoviesInGridView();
            }
        } else {
            mProgressBar.setVisibility(View.GONE);
            mEmptyStateTextView.setVisibility(View.GONE);
            noInternetTextView.setVisibility(View.VISIBLE);
            noInternetTextView.setText(R.string.no_internet);
            updateUI(new ArrayList<Movies>());
        }
    }

    /**
     * updates UI with List Object of {@link Movies}
     *
     * @param moviesInfo: Movies Object of List
     */
    private void updateUI(List<Movies> moviesInfo) {
        Log.d(LOG_TAG, "updateUI Method is called now");
        MoviesArrayAdapter movieArrayAdapter = new MoviesArrayAdapter(this, R.color.colorAccent, moviesInfo);
        mMoviesGridView.setAdapter(movieArrayAdapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(LOG_TAG, "onCreateOptionsMenu Method is called now");
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Log.d(LOG_TAG, "onOptionsItemSelected Method is called now");
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
