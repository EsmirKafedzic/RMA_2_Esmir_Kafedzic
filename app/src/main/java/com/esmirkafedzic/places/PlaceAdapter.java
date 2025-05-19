package com.esmirkafedzic.places;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Map;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder> {

    private Context context;
    private List<Place> placeList;
    private Map<String, String> placeIdMap;
    private String currentUserId;
    private OnRatingChangeListener ratingChangeListener;

    public interface OnRatingChangeListener {
        void onRatingChanged(String placeId, float rating);
    }

    public PlaceAdapter(Context context, List<Place> placeList, Map<String, String> placeIdMap,
                        String currentUserId, OnRatingChangeListener ratingChangeListener) {
        this.context = context;
        this.placeList = placeList;
        this.placeIdMap = placeIdMap;
        this.currentUserId = currentUserId;
        this.ratingChangeListener = ratingChangeListener;
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_place, parent, false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        Place place = placeList.get(position);
        holder.tvName.setText(place.getName());
        Glide.with(context).load(place.getImageUrl()).into(holder.imageView);

        holder.ratingBar.setOnRatingBarChangeListener(null);

        holder.ratingBar.setRating((float) place.getAverageRating());

        boolean alreadyRated = place.getRatedBy() != null && place.getRatedBy().contains(currentUserId);

        if (alreadyRated) {
            holder.ratingBar.setIsIndicator(true);
        } else {
            holder.ratingBar.setIsIndicator(false);
            holder.ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
                if (fromUser && ratingChangeListener != null) {
                    String placeId = placeIdMap.get(place.getName());
                    if (placeId != null) {
                        ratingChangeListener.onRatingChanged(placeId, rating);
                    }
                }
            });
        }

        // Klik na ime, ne cijeli item
        holder.tvName.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), PlaceDetailActivity.class);
            intent.putExtra("placeName", place.getName());
            intent.putExtra("imageUrl", place.getImageUrl());
            intent.putExtra("averageRating", place.getAverageRating());
            intent.putExtra("description", place.getDescription());
            v.getContext().startActivity(intent);
        });
    }





    @Override
    public int getItemCount() {
        return placeList != null ? placeList.size() : 0;
    }

    public void updateList(List<Place> newList) {
        if (placeList != null) {
            placeList.clear();
            if (newList != null) {
                placeList.addAll(newList);
            }
            notifyDataSetChanged();
        }
    }

    public class PlaceViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageView imageView;
        RatingBar ratingBar;

        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.nameTextView);
            imageView = itemView.findViewById(R.id.placeImageView);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }
    }


}
