package com.esmirkafedzic.places;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class PlaceDetailActivity extends AppCompatActivity {

    private TextView placeNameTextView;
    private ImageView placeImageView;
    private RatingBar placeRatingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_detail);

        // Pronalazimo UI komponente prema ID-ovima iz XML-a
        TextView tvName = findViewById(R.id.tvPlaceName);
        ImageView imageView = findViewById(R.id.imageViewPlace);
        RatingBar ratingBar = findViewById(R.id.detailRatingBar);
        TextView tvDescription = findViewById(R.id.tvDescription);

        // Preuzimamo podatke iz Intenta
        String placeName = getIntent().getStringExtra("placeName");
        String imageUrl = getIntent().getStringExtra("imageUrl");
        double avgRating = getIntent().getDoubleExtra("averageRating", 0.0);
        String description = getIntent().getStringExtra("description");
        ratingBar.setRating((float) avgRating);
        ratingBar.setIsIndicator(true); // za svaki slučaj
        // Provjera i postavljanje imena mjesta
        if (placeName != null) {
            tvName.setText(placeName);
        } else {
            tvName.setText("Naziv nije dostupan");
        }

       ratingBar.setRating((float) avgRating);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            imageView.setImageURI(Uri.parse(imageUrl));
        } else {
            imageView.setImageResource(R.drawable.licensed_image); // podrazumijevana slika
        }

        if (description != null && !description.isEmpty()) {
            tvDescription.setText(description);
        } else {
            tvDescription.setText("Opis nije dostupan");
        }
    }}
