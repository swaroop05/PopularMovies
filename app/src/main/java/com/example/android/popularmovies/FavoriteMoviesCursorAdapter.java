package com.example.android.popularmovies;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.android.popularmovies.data.FavoriteMoviesContract;
import com.squareup.picasso.Picasso;

/**
 * Created by meets on 7/19/2018.
 */

public class FavoriteMoviesCursorAdapter extends CursorAdapter {

    Context mcontext;

    /**
     * Constructor that always enables auto-requery.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     * @deprecated This option is discouraged, as it results in Cursor queries
     * being performed on the application's UI thread and thus can cause poor
     * responsiveness or even Application Not Responding errors.  As an alternative,
     * use {@link LoaderManager} with a {@link CursorLoader}.
     */
    public FavoriteMoviesCursorAdapter(Context context, Cursor c) {
        super(context, c);
    }

    /**
     * Makes a new view to hold the data pointed to by cursor.
     *
     * @param context Interface to application's global information
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        mcontext = context;
        return LayoutInflater.from(context).inflate(R.layout.movies_item, parent, false);

    }

    /**
     * Bind an existing view to the data pointed to by cursor
     *
     * @param view    Existing view, returned earlier by newView
     * @param context Interface to application's global information
     * @param cursor  The cursor from which to get the data. The cursor is already
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ImageView movieImageView = view.findViewById(R.id.iv_movies_image);
        String imageUrl = cursor.getString(cursor.getColumnIndex(FavoriteMoviesContract.FavoriteMoviesEntry.COLUMN_IMAGE_URL));
        Picasso.with(mContext)
                .load(imageUrl)
                .into(movieImageView);
    }
}
