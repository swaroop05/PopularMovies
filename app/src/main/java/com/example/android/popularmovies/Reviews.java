package com.example.android.popularmovies;

/**
 * Created by meets on 7/15/2018.
 */

public class Reviews {

    private String mReviewerName;
    private String mReview;


    public Reviews(String mReviewerName, String mReview) {
        this.mReviewerName = mReviewerName;
        this.mReview = mReview;
    }

    public String getmReviewerName() {
        return mReviewerName;
    }

    public String getmReview() {
        return mReview;
    }

}
