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

    EditText emailRegister, passwordRegister;
    Button registerBtn;
    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inicijalizacija Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Povezivanje UI elemenata
        emailRegister = findViewById(R.id.emailRegister);
        passwordRegister = findViewById(R.id.passwordRegister);
        registerBtn = findViewById(R.id.registerBtn);

        // Klik na dugme za registraciju
        registerBtn.setOnClickListener(v -> {
            String email = emailRegister.getText().toString().trim();
            String password = passwordRegister.getText().toString().trim();

            // Validacija
            if (email.isEmpty() || password.length() < 6) {
                Toast.makeText(this, "Unesite validan email i lozinku (min 6 karaktera)", Toast.LENGTH_SHORT).show();
                return;
            }

            // Firebase Auth - Kreiranje korisnika
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String userId = auth.getCurrentUser().getUid();

                            // Dodavanje podataka u Firestore
                            Map<String, Object> user = new HashMap<>();
                            user.put("email", email);

                            db.collection("users")
                                    .document(userId)
                                    .set(user)
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(this, "Registracija uspješna!", Toast.LENGTH_SHORT).show();

                                        // ✅ Redirekcija na Login ekran
                                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        finish(); // da se ne može vratiti na register
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Greška pri spremanju: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    });

                        } else {
                            Toast.makeText(this, "Registracija neuspješna: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }
}
