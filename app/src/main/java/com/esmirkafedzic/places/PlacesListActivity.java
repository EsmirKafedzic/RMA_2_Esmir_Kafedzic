package com.esmirkafedzic.places;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class PlacesListActivity extends AppCompatActivity {

    RecyclerView placesRecyclerView;
    FirebaseFirestore db;
    List<Place> placeList = new ArrayList<>();
    PlaceAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places_list);

        placesRecyclerView = findViewById(R.id.placesRecyclerView);
        placesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        adapter = new PlaceAdapter(this, placeList);
        placesRecyclerView.setAdapter(adapter);

        loadPlaces();
    }

    private void loadPlaces() {
        db.collection("places")
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    placeList.clear();
                    for (DocumentSnapshot doc : querySnapshots) {
                        Place place = doc.toObject(Place.class);
                        placeList.add(place);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Greška: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
