package com.esmirkafedzic.places;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

public class AddPlaceActivity extends AppCompatActivity {

    private EditText placeNameEditText;
    private EditText placeDescriptionEditText;   // Dodatno polje za opis
    private Button selectImageBtn, savePlaceBtn;
    private ImageView placeImageView;
    private RatingBar ratingBar;

    private Uri selectedImageUri;
    private String localImagePath;

    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_place);

        placeNameEditText = findViewById(R.id.placeNameEditText);
        placeDescriptionEditText = findViewById(R.id.placeDescriptionEditText);  // Inicijalizacija
        selectImageBtn = findViewById(R.id.selectImageBtn);
        placeImageView = findViewById(R.id.placeImageView);
        ratingBar = findViewById(R.id.ratingBar);
        savePlaceBtn = findViewById(R.id.savePlaceBtn);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        selectImageBtn.setOnClickListener(v -> openImageChooser());
        savePlaceBtn.setOnClickListener(v -> savePlace());
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == Activity.RESULT_OK
                && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            placeImageView.setImageURI(selectedImageUri);
        }
    }

    private String saveImageLocally(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);

            File directory = new File(getFilesDir(), "images");
            if (!directory.exists()) {
                directory.mkdir();
            }

            String fileName = UUID.randomUUID().toString() + ".jpg";
            File file = new File(directory, fileName);

            OutputStream outputStream = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int length;

            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();

            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void savePlace() {
        String placeName = placeNameEditText.getText().toString().trim();
        String description = placeDescriptionEditText.getText().toString().trim();
        float rating = ratingBar.getRating();

        if (placeName.isEmpty()) {
            Toast.makeText(this, "Unesite naziv mjesta", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, "Odaberite sliku mjesta", Toast.LENGTH_SHORT).show();
            return;
        }

        localImagePath = saveImageLocally(selectedImageUri);
        if (localImagePath == null) {
            Toast.makeText(this, "Greška kod spremanja slike lokalno", Toast.LENGTH_SHORT).show();
            return;
        }

        Place newPlace = new Place();
        newPlace.setName(placeName);
        newPlace.setAverageRating(rating);
        newPlace.setRatedBy(new ArrayList<>());
        if (auth.getCurrentUser() != null) {
            newPlace.setUserId(auth.getCurrentUser().getUid());
        }
        newPlace.setImageUrl(localImagePath);
        newPlace.setImageName(new File(localImagePath).getName());
        newPlace.setDescription(description);

        firestore.collection("places")
                .add(newPlace)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AddPlaceActivity.this, "Mjesto uspješno objavljeno", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AddPlaceActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddPlaceActivity.this, "Greška kod spremanja mjesta: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
