package com.mycompany.recipeapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity {
    private Switch darkModeSwitch;
    private TextView aboutText;
    private TextView contactText;
    private TextView rateText;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "RecipeAppPrefs";
    private static final String DARK_MODE_KEY = "dark_mode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Initialize views
        darkModeSwitch = findViewById(R.id.darkModeSwitch);
        aboutText = findViewById(R.id.aboutText);
        contactText = findViewById(R.id.contactText);
        rateText = findViewById(R.id.rateText);

        // Set current theme state
        boolean isDarkMode = sharedPreferences.getBoolean(DARK_MODE_KEY, false);
        darkModeSwitch.setChecked(isDarkMode);

        // Dark mode toggle listener
        darkModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Save preference
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(DARK_MODE_KEY, isChecked);
                editor.apply();

                // Apply theme change
                if (isChecked) {
                    // Apply dark theme
                    setTheme(R.style.DarkTheme);
                } else {
                    // Apply light theme
                    setTheme(R.style.AppTheme);
                }

                // Restart activity to apply theme
                recreate();
            }
        });

        // About text click listener
        aboutText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAboutDialog();
            }
        });

        // Contact text click listener
        contactText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setType("text/plain");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"developer@recipeapp.com"});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Recipe App Feedback");
                startActivity(Intent.createChooser(emailIntent, "Send Email"));
            }
        });

        // Rate app click listener
        rateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rateApp();
            }
        });
    }

    private void showAboutDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("About Recipe App")
               .setMessage("Version 1.0\n\nA simple and elegant recipe app to discover and save your favorite recipes.\n\n© 2024 RecipeApp Inc.")
               .setPositiveButton("OK", null)
               .show();
    }

    private void rateApp() {
        Toast.makeText(this, "Thank you for your interest!", Toast.LENGTH_SHORT).show();
        // In a real app, you would implement actual rating functionality
        // For example, redirect to Play Store or implement in-app rating
    }
}
