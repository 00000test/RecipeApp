package com.mycompany.recipeapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends Activity {
    private EditText emailInput;
    private EditText passwordInput;
    private EditText confirmPasswordInput;
    private Button registerButton;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Check if Firebase is initialized
        try {
            mAuth = FirebaseAuth.getInstance();
        } catch (Exception e) {
            Toast.makeText(this, "Firebase initialization failed", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        registerButton = findViewById(R.id.registerButton);
        progressBar = findViewById(R.id.progressBar);

        registerButton.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        registerButton.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                progressBar.setVisibility(View.GONE);
                registerButton.setEnabled(true);

                if (task.isSuccessful()) {
                    // Registration successful
                    FirebaseUser user = mAuth.getCurrentUser();
                    Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();

                    // Send email verification
                    if (user != null) {
                        user.sendEmailVerification()
                            .addOnCompleteListener(verificationTask -> {
                                if (verificationTask.isSuccessful()) {
                                    Toast.makeText(RegisterActivity.this,
                                        "Verification email sent to " + user.getEmail(),
                                        Toast.LENGTH_LONG).show();
                                }
                            });
                    }

                    // Navigate to HomeActivity
                    Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    // Registration failed
                    String errorMessage = "Registration failed";
                    if (task.getException() != null) {
                        String exceptionMessage = task.getException().getMessage();
                        if (exceptionMessage != null) {
                            if (exceptionMessage.contains("CONFIGURATION_NOT_FOUND")) {
                                errorMessage = "Firebase configuration error. Please contact support.";
                            } else if (exceptionMessage.contains("WEAK_PASSWORD")) {
                                errorMessage = "Password is too weak. Please choose a stronger password.";
                            } else if (exceptionMessage.contains("EMAIL_EXISTS")) {
                                errorMessage = "An account with this email already exists.";
                            } else if (exceptionMessage.contains("INVALID_EMAIL")) {
                                errorMessage = "Invalid email address.";
                            } else {
                                errorMessage = exceptionMessage;
                            }
                        }
                    }
                    Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            });
    }
}
