package com.example.android.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

import static com.example.android.popularmovies.QueryUtils.KEY_IMAGE_URL;
import static com.example.android.popularmovies.QueryUtils.KEY_MOVIE_ID;
import static com.example.android.popularmovies.QueryUtils.KEY_MOVIE_TITLE;
import static com.example.android.popularmovies.QueryUtils.KEY_PLOT;
import static com.example.android.popularmovies.QueryUtils.KEY_RELEASE_DATE;
import static com.example.android.popularmovies.QueryUtils.KEY_VOTE_AVERAGE;

/**
 * Created by meets on 3/7/2018.
 */

public class MoviesArrayAdapter extends ArrayAdapter<Movies> {

    private final Context mContext;

    public MoviesArrayAdapter(@NonNull Context context, int resource, List<Movies> movies) {
        super(context, 0, movies);
        mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        GridView mMoviesGridView;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.movies_item, parent, false);

        }
        try {
            Movies currentMovieInstance = getItem(position);
            mMoviesGridView = parent.findViewById(R.id.movies_grid);
            /*TextView titleTextView = convertView.findViewById(R.id.tv_movies_title);
            titleTextView.setText(currentMovieInstance.getmMovieTitle());*/
            ImageView moviePosterImageView = convertView.findViewById(R.id.iv_movies_image);
            Picasso.with(mContext)
                    .load(currentMovieInstance.getmImageUrl())
                    .into(moviePosterImageView);
            mMoviesGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    String imageUrl = getItem(i).getmImageUrl();
                    String movieTitle = getItem(i).getmMovieTitle();
                    String releaseDate = getItem(i).getmReleaseDate();
                    String voteAverage = getItem(i).getmVoteAverage();
                    String plot = getItem(i).getmPlot();
                    String movieID = getItem(i).getmMovieId();
                    Intent movieDetailsActivity = new Intent(mContext, MovieDetailsActivity.class);
                    movieDetailsActivity.putExtra(KEY_IMAGE_URL, imageUrl);
                    movieDetailsActivity.putExtra(KEY_MOVIE_TITLE, movieTitle);
                    movieDetailsActivity.putExtra(KEY_RELEASE_DATE, releaseDate);
                    movieDetailsActivity.putExtra(KEY_VOTE_AVERAGE, voteAverage);
                    movieDetailsActivity.putExtra(KEY_PLOT, plot);
                    movieDetailsActivity.putExtra(KEY_MOVIE_ID, movieID);
                    mContext.startActivity(movieDetailsActivity);
                }
            });
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return convertView;
    }
}
