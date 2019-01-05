package com.example.android.popularmovies;

import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popularmovies.data.FavoriteMoviesContract;
import com.example.android.popularmovies.data.FavoriteMoviesDbHelper;
import com.squareup.picasso.Picasso;

import java.util.List;

import static com.example.android.popularmovies.MainActivity.API_KEY;
import static com.example.android.popularmovies.QueryUtils.KEY_IMAGE_URL;
import static com.example.android.popularmovies.QueryUtils.KEY_MOVIE_ID;
import static com.example.android.popularmovies.QueryUtils.KEY_MOVIE_TITLE;
import static com.example.android.popularmovies.QueryUtils.KEY_PLOT;
import static com.example.android.popularmovies.QueryUtils.KEY_RELEASE_DATE;
import static com.example.android.popularmovies.QueryUtils.KEY_VOTE_AVERAGE;

/**
 * Created by meets on 3/12/2018.
 */

public class MovieDetailsActivity extends AppCompatActivity implements TrailersAdapter.TrailerItemClickListener {
    private static final String LOG_TAG = MovieDetailsActivity.class.getSimpleName();
    private String mMovieTitle;
    private String mImageURL = null;
    private String mReleaseDate = null;
    private String mVoteAverage = null;
    private String mPlot = null;
    private String mMovieId = null;
    private RecyclerView mReviewRecyclerView;
    private ReviewsAdapter mReviewsAdapter;
    private TrailersAdapter mTrailersAdapter;
    private TextView mReviewsHeaderTextView;
    private TextView mTrailersHeaderTextView;
    private NestedScrollView nestedScrollView;
    private Button mFavoritesBtn;
    Uri mCurrentMovieUri;
    int movieIdFromDb;
    private RecyclerView mTrailerRecyclerView;
    private static final String BASE_URL_FRONT = "http://api.themoviedb.org/3/movie/";
    private static final String BASE_URL_REVIEWS_REAR = "/reviews?api_key=";
    private static final String BASE_URL_TRAILERS_REAR = "/videos?api_key=";
    private static final String NESTED_SCROLL_POSITION = "NESTED_SCROLL_POSITION";
    private static List<Trailers> mTrailersFromClick = null;
    FavoriteMoviesDbHelper mFavoriteMoviesDbHelper;
    int[] position;
    private TextView noInternetTextView;
    Uri insertUri = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate Method is called now");
        setContentView(R.layout.activity_movies_details);
        mFavoriteMoviesDbHelper = new FavoriteMoviesDbHelper(this);
        ImageView mImageView;
        TextView mReleaseDateTextView;
        TextView mVoteAverageTextView;
        TextView mPlotTextView;
        TextView mMovieTitleTextView;
        mImageView = findViewById(R.id.iv_movies_poster);
        mReleaseDateTextView = findViewById(R.id.tv_release_date_value);
        mVoteAverageTextView = findViewById(R.id.tv_vote_avg_value);
        mPlotTextView = findViewById(R.id.tv_plot_value);
        mMovieTitleTextView = findViewById(R.id.tv_movie_title);
        mReviewRecyclerView = findViewById(R.id.rv_reviews);
        mTrailerRecyclerView = findViewById(R.id.rv_trailers);
        mReviewsHeaderTextView = findViewById(R.id.tv_reviews_header);
        mTrailersHeaderTextView = findViewById(R.id.tv_trailer_header);
        mFavoritesBtn = findViewById(R.id.btn_favorite);
        nestedScrollView = findViewById(R.id.scrollview);
        noInternetTextView = findViewById(R.id.no_internet);
        Intent intent = getIntent();
        mCurrentMovieUri = intent.getData();
        if (intent.hasExtra(KEY_MOVIE_TITLE)) {
            mMovieTitle = intent.getStringExtra(KEY_MOVIE_TITLE);
        }
        if (intent.hasExtra(KEY_IMAGE_URL)) {
            mImageURL = intent.getStringExtra(KEY_IMAGE_URL);
        }
        if (intent.hasExtra(KEY_RELEASE_DATE)) {
            mReleaseDate = intent.getStringExtra(KEY_RELEASE_DATE).substring(0, 4);
        }
        if (intent.hasExtra(KEY_VOTE_AVERAGE)) {
            mVoteAverage = intent.getStringExtra(KEY_VOTE_AVERAGE) + " / 10";
        }
        if (intent.hasExtra(KEY_PLOT)) {
            mPlot = intent.getStringExtra(KEY_PLOT);
        }
        if (intent.hasExtra(KEY_MOVIE_ID)) {
            mMovieId = intent.getStringExtra(KEY_MOVIE_ID);
        }
        setTitle(getResources().getText(R.string.movie_detail_header));
        Picasso.with(this)
                .load(mImageURL)
                .into(mImageView);
        mMovieTitleTextView.setText(mMovieTitle);
        mReleaseDateTextView.setText(mReleaseDate);
        mVoteAverageTextView.setText(mVoteAverage);
        mPlotTextView.setText(mPlot);
        if (isNetworkConnected()) {
            noInternetTextView.setVisibility(View.GONE);
            loadReviews();
            loadTrailers();
        } else {
            noInternetTextView.setVisibility(View.VISIBLE);
            noInternetTextView.setText(R.string.no_internet);
            mTrailersHeaderTextView.setVisibility(View.GONE);
            mReviewsHeaderTextView.setVisibility(View.GONE);
        }
        new CheckMovieIsInDBTask().execute(mMovieId);
        mFavoritesBtn.setOnClickListener(new View.OnClickListener() {

            /**
             * Called when a view has been clicked.
             *
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
                if (mFavoritesBtn.getText() == v.getResources().getString(R.string.mark_as_favorite)) {
                    saveMovieDetailsToFavoritesDb();
                } else {
                    deleteMovieDetailsFromFavoritesDb();
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(LOG_TAG, "onSaveInstanceState Method is called now");
        outState.putIntArray(NESTED_SCROLL_POSITION,
                new int[]{nestedScrollView.getScrollX(), nestedScrollView.getScrollY()});
    }


    /**
     * This method is called after {@link #onStart} when the activity is
     * being re-initialized from a previously saved state, given here in
     * <var>savedInstanceState</var>.  Most implementations will simply use {@link #onCreate}
     * to restore their state, but it is sometimes convenient to do it here
     * after all of the initialization has been done or to allow subclasses to
     * decide whether to use your default implementation.  The default
     * implementation of this method performs a restore of any view state that
     * had previously been frozen by {@link #onSaveInstanceState}.
     * <p>
     * <p>This method is called between {@link #onStart} and
     * {@link #onPostCreate}.
     *
     * @param savedInstanceState the data most recently supplied in {@link #onSaveInstanceState}.
     * @see #onCreate
     * @see #onPostCreate
     * @see #onResume
     * @see #onSaveInstanceState
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(LOG_TAG, "onRestoreInstanceState Method is called now");
        position = savedInstanceState.getIntArray(NESTED_SCROLL_POSITION);
        if (position != null)
            nestedScrollView.post(new Runnable() {
                public void run() {
                    nestedScrollView.scrollTo(position[0], position[1]);
                }
            });
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
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are <em>not</em> resumed.  This means
     * that in some cases the previous state may still be saved, not allowing
     * fragment transactions that modify the state.  To correctly interact
     * with fragments in their proper state, you should instead override
     * {@link #onResumeFragments()}.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (!isNetworkConnected()) {
            noInternetTextView.setVisibility(View.VISIBLE);
            noInternetTextView.setText(R.string.no_internet);
            mTrailersHeaderTextView.setVisibility(View.GONE);
            mReviewsHeaderTextView.setVisibility(View.GONE);
            mReviewRecyclerView.setVisibility(View.GONE);
            mTrailerRecyclerView.setVisibility(View.GONE);
        }
    }

    /**
     * Async task to check the movie in DB
     * Takes movie id as parameter to check if its present
     */
    public class CheckMovieIsInDBTask extends AsyncTask<String, Void, Cursor> {

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
        protected Cursor doInBackground(String... strings) {
            Log.d(LOG_TAG, "CheckMovieIsInDBTask's doInBackground  Method is called now");
            Cursor cursor = null;
            cursor = queryDB(strings[0]);
            return cursor;
        }

        /**
         * <p>Runs on the UI thread after {@link #doInBackground}. The
         * specified result is the value returned by {@link #doInBackground}.</p>
         * <p>
         * <p>This method won't be invoked if the task was cancelled.</p>
         *
         * @param cursor The result of the operation computed by {@link #doInBackground}.
         * @see #onPreExecute
         * @see #doInBackground
         * @see #onCancelled(Object)
         */
        @Override
        protected void onPostExecute(Cursor cursor) {
            super.onPostExecute(cursor);
            Log.d(LOG_TAG, "CheckMovieIsInDBTask's onPostExecute  Method is called now");
            try {
                cursor.moveToFirst();
                movieIdFromDb = cursor.getInt(cursor.getColumnIndex(FavoriteMoviesContract.FavoriteMoviesEntry._ID));
                mFavoritesBtn.setText(getString(R.string.remove_from_favorite));
            } catch (CursorIndexOutOfBoundsException e) {
                mFavoritesBtn.setText(getString(R.string.mark_as_favorite));
            }
            if (position != null)
                nestedScrollView.post(new Runnable() {
                    public void run() {
                        nestedScrollView.scrollTo(position[0], position[1]);
                    }
                });
        }
    }

    public Cursor queryDB(String movieId) {
        Log.d(LOG_TAG, "queryDB  Method is called now");
        SQLiteDatabase database = mFavoriteMoviesDbHelper.getReadableDatabase();
        Cursor cursor = null;
        String selection = FavoriteMoviesContract.FavoriteMoviesEntry.COLUMN_MOVIE_ID + "=?";
        String[] selectionArgs = new String[]{movieId};
        cursor = database.query(FavoriteMoviesContract.FavoriteMoviesEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null);
        return cursor;
    }

    /**
     * wrapper function to call Async task of loading Reviews
     */
    private void loadReviews() {
        new FetchReviewDetailsTask().execute(mMovieId);
    }


    @Override
    public void onTrailerItemClick(int clickedItemIndex) {
        String videoId = mTrailersFromClick.get(clickedItemIndex).getmTrailerKey();
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoId));
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://www.youtube.com/watch?v=" + videoId));
        try {
            this.startActivity(appIntent);
        } catch (ActivityNotFoundException ex) {
            this.startActivity(webIntent);
        }
    }

    /**
     * Async task to fetch the reviews of movie
     * Takes movie id as parameter
     */
    public class FetchReviewDetailsTask extends AsyncTask<String, Void, List<Reviews>> {

        @Override
        protected List<Reviews> doInBackground(String... params) {
            Log.d(LOG_TAG, "FetchReviewDetailsTask's doInBackground  Method is called now");
            List<Reviews> reviewsList = null;
            if (params.length == 0) {
                return null;
            }
            String movieID = params[0];
            String finalReviewsURL = BASE_URL_FRONT + movieID + BASE_URL_REVIEWS_REAR + API_KEY;
            try {
                reviewsList = QueryUtils.fetchReviewInfos(finalReviewsURL);
            } catch (Exception e) {
                Log.d(LOG_TAG, e.getMessage());
            }
            return reviewsList;
        }

        @Override
        protected void onPostExecute(List<Reviews> reviews) {
            super.onPostExecute(reviews);
            Log.d(LOG_TAG, "FetchReviewDetailsTask's onPostExecute  Method is called now");
            if (!reviews.isEmpty()) {
                mReviewsAdapter = new ReviewsAdapter(reviews);
                LinearLayoutManager layoutManager = new LinearLayoutManager(MovieDetailsActivity.this);
                mReviewRecyclerView.setLayoutManager(layoutManager);
                mReviewRecyclerView.setNestedScrollingEnabled(false);
                mReviewRecyclerView.setAdapter(mReviewsAdapter);
            } else {
                mReviewsHeaderTextView.setVisibility(View.GONE);
            }
        }
    }


    /**
     * wrapper function to execute the async task of updating trailer information
     */
    private void loadTrailers() {
        new FetchTrailerDetailsTask().execute(mMovieId);
    }

    /**
     * Async task to fetch the trailers of movie
     * Takes movie id as parameter
     */
    public class FetchTrailerDetailsTask extends AsyncTask<String, Void, List<Trailers>> {

        @Override
        protected List<Trailers> doInBackground(String... params) {
            Log.d(LOG_TAG, "FetchTrailerDetailsTask's doInBackground  Method is called now");
            List<Trailers> trailersList = null;
            if (params.length == 0) {
                return null;
            }
            String movieID = params[0];
            String finalTrailersURL = BASE_URL_FRONT + movieID + BASE_URL_TRAILERS_REAR + API_KEY;
            try {
                trailersList = QueryUtils.fetchTrailerInfos(finalTrailersURL);
            } catch (Exception e) {
                Log.d(LOG_TAG, e.getMessage());
            }
            return trailersList;
        }

        @Override
        protected void onPostExecute(List<Trailers> trailers) {
            super.onPostExecute(trailers);
            Log.d(LOG_TAG, "FetchTrailerDetailsTask's onPostExecute  Method is called now");
            if (!trailers.isEmpty()) {
                mTrailersFromClick = trailers;
                mTrailersAdapter = new TrailersAdapter(trailers, MovieDetailsActivity.this);
                LinearLayoutManager layoutManager = new LinearLayoutManager(MovieDetailsActivity.this);
                mTrailerRecyclerView.setLayoutManager(layoutManager);
                mTrailerRecyclerView.setNestedScrollingEnabled(false);
                mTrailerRecyclerView.setAdapter(mTrailersAdapter);
            } else {
                mTrailersHeaderTextView.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Function to save movie to DB
     */
    public void saveMovieDetailsToFavoritesDb() {
        Log.d(LOG_TAG, "saveMovieDetailsToFavoritesDb  Method is called now");
        ContentValues values = new ContentValues();
        values.put(FavoriteMoviesContract.FavoriteMoviesEntry.COLUMN_MOVIE_ID, mMovieId);
        values.put(FavoriteMoviesContract.FavoriteMoviesEntry.COLUMN_IMAGE_URL, mImageURL);
        values.put(FavoriteMoviesContract.FavoriteMoviesEntry.COLUMN_MOVIE_TITLE, mMovieTitle);
        values.put(FavoriteMoviesContract.FavoriteMoviesEntry.COLUMN_RELEASE_DATE, mReleaseDate);
        values.put(FavoriteMoviesContract.FavoriteMoviesEntry.COLUMN_VOTE, mVoteAverage);
        values.put(FavoriteMoviesContract.FavoriteMoviesEntry.COLUMN_PLOT, mPlot);
        insertUri = getContentResolver().insert(FavoriteMoviesContract.FavoriteMoviesEntry.CONTENT_URI, values);
        if (insertUri != null) {
            displayToastMessage(getString(R.string.movie_insert_item_successful));
            mFavoritesBtn.setText(getString(R.string.remove_from_favorite));
        } else {
            displayToastMessage(getString(R.string.movie_insert_or_delete_item_failed));
        }
    }

    /**
     * Function to delete movie to DB
     */
    private void deleteMovieDetailsFromFavoritesDb() {
        Log.d(LOG_TAG, "deleteMovieDetailsFromFavoritesDb  Method is called now");
        Uri deleteUri = null;
        if (insertUri != null) {
            deleteUri = insertUri;
        } else if (mCurrentMovieUri != null) {
            deleteUri = mCurrentMovieUri;
        } else {
            deleteUri = ContentUris.withAppendedId(FavoriteMoviesContract.FavoriteMoviesEntry.CONTENT_URI, movieIdFromDb);
        }

        int delID = getContentResolver().delete(deleteUri, null, null);
        if (delID != 0) {
            displayToastMessage(getString(R.string.movie_remove_item_successful));
            mFavoritesBtn.setText(getResources().getString(R.string.mark_as_favorite));
        } else {
            displayToastMessage(getString(R.string.movie_insert_or_delete_item_failed));
        }
    }

    /**
     * Method to show Toast messages
     *
     * @param message
     */
    public void displayToastMessage(String message) {
        Log.d(LOG_TAG, "displayToastMessage  Method is called now");
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.show();
    }
}
