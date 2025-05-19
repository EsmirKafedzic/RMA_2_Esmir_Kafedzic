package com.esmirkafedzic.places;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    EditText emailRegister, passwordRegister, nameRegister;
    Button registerBtn;
    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailRegister = findViewById(R.id.emailRegister);
        passwordRegister = findViewById(R.id.passwordRegister);
        nameRegister = findViewById(R.id.nameRegister);
        registerBtn = findViewById(R.id.registerBtn);

        registerBtn.setOnClickListener(v -> {
            String email = emailRegister.getText().toString().trim();
            String password = passwordRegister.getText().toString().trim();
            String name = nameRegister.getText().toString().trim();

            if (email.isEmpty() || password.length() < 6 || name.isEmpty()) {
                Toast.makeText(this, "Unesite validan email, lozinku (min 6 karaktera) i ime", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Validacija neuspješna: email, lozinka ili ime neispravni");
                return;
            }

            Log.d(TAG, "Pokrenuta registracija za email: " + email + ", ime: " + name);

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Firebase Auth uspješan, userId: " + auth.getCurrentUser().getUid());
                            Toast.makeText(this, "Registracija uspješna!", Toast.LENGTH_SHORT).show();

                            // Spremi podatke u Firestore
                            if (auth.getCurrentUser() != null) {
                                String userId = auth.getCurrentUser().getUid();
                                Map<String, Object> user = new HashMap<>();
                                user.put("email", email);
                                user.put("name", name);

                                db.collection("users")
                                        .document(userId)
                                        .set(user)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d(TAG, "Firestore: Ime i email uspješno spremljeni");
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Greška pri pisanju u Firestore: " + e.getMessage());
                                            Toast.makeText(this, "Greška pri spremanju podataka: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                Log.e(TAG, "Korisnik je null nakon uspješne autentifikacije");
                            }

                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                            Log.d(TAG, "Prebačen na LoginActivity i završen RegisterActivity");
                        } else {
                            Log.e(TAG, "Firebase Auth neuspješan: " + task.getException().getMessage());
                            Toast.makeText(this, "Registracija neuspješna: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }
}