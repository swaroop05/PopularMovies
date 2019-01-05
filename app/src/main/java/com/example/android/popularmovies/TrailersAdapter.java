package com.example.android.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by meets on 7/16/2018.
 */

public class TrailersAdapter extends RecyclerView.Adapter<TrailersAdapter.TrailerViewsHolder> {
    final private TrailerItemClickListener mTrailerOnClickListener;
    private static final String TAG = TrailersAdapter.class.getSimpleName();
    private List<Trailers> mTrailers;

    public interface TrailerItemClickListener {
        void onTrailerItemClick(int clickedItemIndex);
    }

    public TrailersAdapter(List<Trailers> mTrailers, TrailerItemClickListener trailerItemClickListener) {
        this.mTrailers = mTrailers;
        this.mTrailerOnClickListener = trailerItemClickListener;
    }

    @Override
    public TrailerViewsHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForTrailersList = R.layout.trailer_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;
        View view = inflater.inflate(layoutIdForTrailersList, viewGroup, shouldAttachToParentImmediately);
        TrailerViewsHolder trailerViewsHolder = new TrailerViewsHolder(view);
        return trailerViewsHolder;
    }

    @Override
    public void onBindViewHolder(TrailerViewsHolder holder, int position) {
        Trailers trailers = mTrailers.get(position);
        holder.bind(trailers);
    }

    @Override
    public int getItemCount() {
        return mTrailers.size();
    }


    class TrailerViewsHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView trailerTitleTextView;
        String trailerId;

        public TrailerViewsHolder(View itemView) {
            super(itemView);
            trailerTitleTextView = itemView.findViewById(R.id.tv_trailer_title);
            itemView.setOnClickListener(this);
        }

        void bind(Trailers trailers) {
            trailerTitleTextView.setText(trailers.getmTrailerName());
            trailerId = trailers.getmTrailerKey();
        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            mTrailerOnClickListener.onTrailerItemClick(clickedPosition);
        }
    }

}
