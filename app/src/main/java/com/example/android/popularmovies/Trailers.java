package com.example.android.popularmovies;

/**
 * Created by meets on 7/16/2018.
 */

public class Trailers {

    private String mTrailerName;
    private String mTrailerKey;

    public Trailers(String mTrailerName, String mTrailerKey) {
        this.mTrailerName = mTrailerName;
        this.mTrailerKey = mTrailerKey;
    }

    public String getmTrailerName() {
        return mTrailerName;
    }

    public String getmTrailerKey() {
        return mTrailerKey;
    }
}
