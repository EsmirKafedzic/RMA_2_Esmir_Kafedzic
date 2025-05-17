package com.esmirkafedzic.places;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddPlaceActivity extends AppCompatActivity {

    EditText placeNameEditText;
    RatingBar placeRatingBar;
    Button savePlaceBtn;

    FirebaseFirestore db;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_place);

        placeNameEditText = findViewById(R.id.placeNameEditText);
        placeRatingBar = findViewById(R.id.ratingBar);
        savePlaceBtn = findViewById(R.id.savePlaceBtn);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        savePlaceBtn.setOnClickListener(v -> {
            String name = placeNameEditText.getText().toString().trim();
            float rating = placeRatingBar.getRating();

            if (name.isEmpty() || rating == 0.0f) {
                Toast.makeText(this, "Unesite ime i ocjenu", Toast.LENGTH_SHORT).show();
                return;
            }

            savePlace(name, rating);
        });
    }

    private void savePlace(String name, float rating) {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Morate biti ulogovani da biste dodali mjesto", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Objavljivanje mjesta...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        Map<String, Object> place = new HashMap<>();
        place.put("name", name);
        place.put("rating", rating);
        place.put("userId", auth.getCurrentUser().getUid());

        db.collection("places")
                .add(place)
                .addOnSuccessListener(doc -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Mjesto objavljeno!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AddPlaceActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Greška kod Firestore: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
