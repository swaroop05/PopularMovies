package com.example.android.popularmovies;

/**
 * Created by meets on 3/7/2018.
 */

public class Movies {

    private String mImageUrl;
    private String mMovieTitle;
    private String mReleaseDate;
    private String mVoteAverage;
    private String mPlot;
    private String mMovieId;

    /**
     * constructor for Movies Class
     *
     * @param mImageUrl:    String parameter which holds image url
     * @param mMovieTitle:  String parameter which holds Movie Title value
     * @param mReleaseDate: String parameter which holds release date value
     * @param mVoteAverage: String parameter which holds rating value
     * @param mPlot:        String parameter which holds plot value
     */
    public Movies(String mMovieId, String mImageUrl, String mMovieTitle, String mReleaseDate, String mVoteAverage, String mPlot) {
        this.mMovieId = mMovieId;
        this.mImageUrl = mImageUrl;
        this.mMovieTitle = mMovieTitle;
        this.mReleaseDate = mReleaseDate;
        this.mVoteAverage = mVoteAverage;
        this.mPlot = mPlot;
    }

    public String getmImageUrl() {
        return mImageUrl;
    }

    public String getmMovieTitle() {
        return mMovieTitle;
    }

    public String getmReleaseDate() {
        return mReleaseDate;
    }

    public String getmVoteAverage() {
        return mVoteAverage;
    }

    public String getmPlot() {
        return mPlot;
    }

    public String getmMovieId() {
        return mMovieId;
    }
}
