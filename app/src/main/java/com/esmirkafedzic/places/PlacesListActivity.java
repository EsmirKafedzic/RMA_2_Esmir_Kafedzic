package com.esmirkafedzic.places;

import android.os.Bundle;
import android.util.Log;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlacesListActivity extends AppCompatActivity {

    private RecyclerView placesRecyclerView;
    private RatingBar ratingFilter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private List<Place> placeList = new ArrayList<>();
    private List<Place> fullPlaceList = new ArrayList<>();
    private Map<String, String> placeIdMap = new HashMap<>();
    private PlaceAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places_list);

        placesRecyclerView = findViewById(R.id.placesRecyclerView);
        ratingFilter = findViewById(R.id.ratingFilter);

        placesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        String currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";

        adapter = new PlaceAdapter(this, placeList, placeIdMap, currentUserId, this::updateRating);
        placesRecyclerView.setAdapter(adapter);

        ratingFilter.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if (fromUser) {
                Log.d("FILTER_DEBUG", "Rating filter changed to: " + rating);
                filterPlacesByRating(rating);
            }
        });

        loadPlaces();
    }

    private void loadPlaces() {
        Log.d("FILTER_DEBUG", "loadPlaces() pozvana - učitavanje svih mjesta iz baze...");
        db.collection("places")
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    placeList.clear();
                    fullPlaceList.clear();
                    placeIdMap.clear();

                    for (QueryDocumentSnapshot doc : querySnapshots) {
                        Place place = doc.toObject(Place.class);
                        Log.d("FILTER_DEBUG", "Učitano mjesto: " + place.getName() + ", Ocjena: " + place.getAverageRating());
                        placeList.add(place);
                        fullPlaceList.add(place);
                        placeIdMap.put(place.getName(), doc.getId());
                    }
                    Log.d("FILTER_DEBUG", "Učitano mjesta: " + placeList.size());
                    adapter.updateList(placeList);
                    placesRecyclerView.invalidate();
                })
                .addOnFailureListener(e -> {
                    Log.e("FILTER_DEBUG", "Greška pri učitavanju mjesta: " + e.getMessage());
                    Toast.makeText(this, "Greška: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void filterPlacesByRating(float minRating) {
        Log.d("FILTER_DEBUG", "filterPlacesByRating() sa minimalnom ocjenom: " + minRating);
        List<Place> filteredList = new ArrayList<>();

        if (minRating == 0) {
            filteredList.addAll(fullPlaceList);
            Log.d("FILTER_DEBUG", "Prikaz svih mjesta, ukupno: " + filteredList.size());
        } else {
            for (Place place : fullPlaceList) {
                if (place.getAverageRating() >= minRating) {
                    Log.d("FILTER_DEBUG", "Filtrirano mjesto: " + place.getName() + ", Ocjena: " + place.getAverageRating());
                    filteredList.add(place);
                }
            }
            Log.d("FILTER_DEBUG", "Prikaz filtriranih mjesta: " + filteredList.size());
        }

        adapter.updateList(filteredList);
        placesRecyclerView.invalidate();
    }

    private void updateRating(String placeId, float rating) {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null) {
            Toast.makeText(this, "Morate biti ulogovani da biste ocijenili", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("places").document(placeId)
                .get()
                .addOnSuccessListener(doc -> {
                    Place place = doc.toObject(Place.class);
                    if (place != null && place.getRatedBy().contains(userId)) {
                        Toast.makeText(this, "Već ste ocijenili ovo mjesto", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Map<String, Object> ratingData = new HashMap<>();
                    ratingData.put("userId", userId);
                    ratingData.put("rating", (double) rating);

                    db.collection("places").document(placeId)
                            .collection("ratings").document(userId)
                            .set(ratingData)
                            .addOnSuccessListener(unused -> {
                                List<String> ratedBy = place.getRatedBy();
                                ratedBy.add(userId);
                                db.collection("places").document(placeId)
                                        .update("ratedBy", ratedBy)
                                        .addOnSuccessListener(aVoid -> calculateAverageRating(placeId))
                                        .addOnFailureListener(e -> Toast.makeText(this, "Greška: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Greška kod spremanja ocjene: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                });
    }

    private void calculateAverageRating(String placeId) {
        db.collection("places").document(placeId)
                .collection("ratings")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    double totalRating = 0;
                    int ratingCount = querySnapshot.size();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Double rating = doc.getDouble("rating");
                        if (rating != null) {
                            totalRating += rating;
                        }
                    }

                    double averageRating = ratingCount > 0 ? totalRating / ratingCount : 0;
                    db.collection("places").document(placeId)
                            .update("averageRating", averageRating)
                            .addOnSuccessListener(aVoid -> loadPlaces())
                            .addOnFailureListener(e -> Toast.makeText(this, "Greška kod ažuriranja prosjeka: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Greška kod dohvatanja ocjena: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}