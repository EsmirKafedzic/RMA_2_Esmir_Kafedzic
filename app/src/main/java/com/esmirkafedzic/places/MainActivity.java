package com.esmirkafedzic.places;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseFirestore db;

    TextView welcomeText;
    Button logoutBtn, addPlaceBtn;
    RecyclerView placesRecyclerView;

    List<Place> placeList = new ArrayList<>();
    Map<String, String> placeIdMap = new HashMap<>();
    PlaceAdapter adapter;
    private Spinner ratingFilterSpinner;
    private double currentMinRating = 0; // Početna minimalna ocjena

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        welcomeText = findViewById(R.id.welcomeText);
        logoutBtn = findViewById(R.id.logoutBtn);
        addPlaceBtn = findViewById(R.id.addPlaceBtn);
        placesRecyclerView = findViewById(R.id.placesRecyclerView);
        ratingFilterSpinner = findViewById(R.id.ratingFilterSpinner);


        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            welcomeText.setText("Hello, " + email);
            Log.d("AUTH", "Korisnik je ulogovan: " + email);
        } else {
            welcomeText.setText("Please log in");
            Log.d("AUTH", "Niko nije ulogovan");
        }

        String[] ratingOptions = {"Sve ocjene", "Ocjena 4 i više", "Ocjena 3 i više", "Ocjena 2 i više"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, ratingOptions) {
            @Override
            public android.view.View getView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                view.setTextColor(android.graphics.Color.WHITE);
                view.setPadding(20, 20, 20, 20);
                return view;
            }

            @Override
            public android.view.View getDropDownView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                view.setTextColor(android.graphics.Color.WHITE);
                view.setBackgroundColor(android.graphics.Color.parseColor("#222222"));
                view.setPadding(20, 20, 20, 20);
                return view;
            }
        };
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ratingFilterSpinner.setAdapter(spinnerAdapter);

        ratingFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                switch (position) {
                    case 0:
                        currentMinRating = 0;
                        break;
                    case 1:
                        currentMinRating = 4.0;
                        break;
                    case 2:
                        currentMinRating = 3.0;
                        break;
                    case 3:
                        currentMinRating = 2.0;
                        break;
                }
                filterAndDisplayPlaces();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                currentMinRating = 0;
                filterAndDisplayPlaces();
            }
        });

        // Logout dugme
        logoutBtn.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(MainActivity.this, "Odjavljeni ste", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });

        // Dodavanje mjesta dugme
        addPlaceBtn.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AddPlaceActivity.class));
        });

        String currentUserId = user != null ? user.getUid() : "";
        adapter = new PlaceAdapter(this, new ArrayList<>(), placeIdMap, currentUserId, this::updateRating);

        placesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        placesRecyclerView.setAdapter(adapter);

        Button settingsBtn = findViewById(R.id.settingsBtn);
        settingsBtn.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        });

        loadPlaces();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPlaces();
    }

    // dokucivanje mjesta iz baze
    private void loadPlaces() {
        db.collection("places")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    placeList.clear();
                    placeIdMap.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Place place = doc.toObject(Place.class);
                        placeList.add(place);
                        placeIdMap.put(place.getName(), doc.getId());
                    }
                    filterAndDisplayPlaces();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Greška: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void filterAndDisplayPlaces() {
        List<Place> filteredList = new ArrayList<>();
        for (Place place : placeList) {
            if (place.getAverageRating() >= currentMinRating) {
                filteredList.add(place);
            }
        }
        adapter.updateList(filteredList);
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
                                // Ažuriraj ratedBy i averageRating
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
