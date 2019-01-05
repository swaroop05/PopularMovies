package com.example.android.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by meets on 7/15/2018.
 */

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewViewsHolder> {
    private static final String TAG = ReviewsAdapter.class.getSimpleName();

    private List<Reviews> mReviews;

    public ReviewsAdapter(List<Reviews> reviews) {
        mReviews = reviews;
    }

    @Override
    public ReviewViewsHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForReviewList = R.layout.reviews_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;
        View view = inflater.inflate(layoutIdForReviewList, viewGroup, shouldAttachToParentImmediately);
        ReviewViewsHolder reviewViewsHolder = new ReviewViewsHolder(view);
        return reviewViewsHolder;
    }

    @Override
    public void onBindViewHolder(ReviewViewsHolder holder, int position) {
        Reviews reviews = mReviews.get(position);
        holder.bind(reviews);
    }

    @Override
    public int getItemCount() {
        return mReviews.size();
    }

    class ReviewViewsHolder extends RecyclerView.ViewHolder {
        TextView authorTextView;
        TextView contentOfReviewTextView;

        public ReviewViewsHolder(View itemView) {
            super(itemView);
            authorTextView = itemView.findViewById(R.id.tv_reviewer_name);
            contentOfReviewTextView = itemView.findViewById(R.id.tv_review_content);
        }

        void bind(Reviews reviews) {
            authorTextView.setText(reviews.getmReviewerName());
            contentOfReviewTextView.setText(reviews.getmReview());
        }
    }
}
