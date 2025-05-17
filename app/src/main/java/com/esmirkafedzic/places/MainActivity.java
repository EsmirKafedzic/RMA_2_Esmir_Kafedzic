package com.esmirkafedzic.places;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseFirestore db;

    TextView welcomeText;
    Button logoutBtn, addPlaceBtn;
    RecyclerView placesRecyclerView;

    List<Place> placeList = new ArrayList<>();
    PlaceAdapter adapter;

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

        // Postavi email korisnika
        if (auth.getCurrentUser() != null) {
            String email = auth.getCurrentUser().getEmail();
            welcomeText.setText("Hello, " + email);
        } else {
            welcomeText.setText("Please log in");
        }
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Log.d("AUTH", "Korisnik je ulogovan: " + user.getEmail());
        } else {
            Log.d("AUTH", "Niko nije ulogovan");
        }

        // Logout
        logoutBtn.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(MainActivity.this, "Odjavljeni ste", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });

        // Otvori AddPlaceActivity
        addPlaceBtn.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AddPlaceActivity.class));
        });

        // Postavi RecyclerView
        placeList = new ArrayList<>();
        adapter = new PlaceAdapter(this, placeList);
        placesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        placesRecyclerView.setAdapter(adapter);

        loadPlaces();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPlaces(); // ponovo učitaj podatke svaki put kada se vratiš na ovu aktivnost
    }

    private void loadPlaces() {
        db.collection("places")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    placeList.clear();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Place place = doc.toObject(Place.class);
                        placeList.add(place);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Greška: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
