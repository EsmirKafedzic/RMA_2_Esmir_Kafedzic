package com.esmirkafedzic.places;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingsActivity extends AppCompatActivity {

    EditText oldPasswordEditText, newPasswordEditText;
    Button changePasswordBtn, deleteAccountBtn;

    FirebaseAuth auth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        oldPasswordEditText = findViewById(R.id.oldPasswordEditText);
        newPasswordEditText = findViewById(R.id.newPasswordEditText);
        changePasswordBtn = findViewById(R.id.changePasswordBtn);
        deleteAccountBtn = findViewById(R.id.deleteAccountBtn);

        changePasswordBtn.setOnClickListener(v -> changePassword());
        deleteAccountBtn.setOnClickListener(v -> deleteAccount());
    }

    private void changePassword() {
        String oldPassword = oldPasswordEditText.getText().toString().trim();
        String newPassword = newPasswordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(oldPassword) || TextUtils.isEmpty(newPassword)) {
            Toast.makeText(this, "Popunite sva polja", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.length() < 6) {
            Toast.makeText(this, "Nova lozinka mora imati bar 6 karaktera", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(user.getEmail(), oldPassword)
                .addOnSuccessListener(authResult -> {
                    user.updatePassword(newPassword)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Lozinka promijenjena", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Greška: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Pogrešna stara lozinka", Toast.LENGTH_SHORT).show());
    }

    private void deleteAccount() {
        user.delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Nalog izbrisan", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Greška: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
