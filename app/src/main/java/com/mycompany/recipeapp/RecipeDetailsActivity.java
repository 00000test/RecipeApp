package com.mycompany.recipeapp;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class RecipeDetailsActivity extends Activity {
    private ImageView recipeImage;
    private TextView recipeTitle;
    private TextView recipeCategory;
    private TextView recipeArea;
    private TextView recipeInstructions;
    private TextView recipeIngredients;
    private Button btnSaveFavorite;
    private ProgressBar progressBar;
    private String mealId;
    private RecipeDetails currentRecipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details);

        // Initialize views
        recipeImage = findViewById(R.id.recipeImage);
        recipeTitle = findViewById(R.id.recipeTitle);
        recipeCategory = findViewById(R.id.recipeCategory);
        recipeArea = findViewById(R.id.recipeArea);
        recipeInstructions = findViewById(R.id.recipeInstructions);
        recipeIngredients = findViewById(R.id.recipeIngredients);
        btnSaveFavorite = findViewById(R.id.btnSaveFavorite);
        progressBar = findViewById(R.id.progressBar);

        // Get meal ID from intent
        mealId = getIntent().getStringExtra("mealId");
        if (mealId != null) {
            new FetchRecipeDetailsTask().execute("https://www.themealdb.com/api/json/v1/1/lookup.php?i=" + mealId);
        }

        // Set save favorite button click listener
        btnSaveFavorite.setOnClickListener(v -> saveToFavorites());
    }

    private void saveToFavorites() {
        if (currentRecipe != null) {
            // TODO: Save to Room database
            Toast.makeText(this, "Recipe saved to favorites!", Toast.LENGTH_SHORT).show();
        }
    }

    private class FetchRecipeDetailsTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                return result.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressBar.setVisibility(View.GONE);

            if (result != null) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    JSONArray meals = jsonObject.getJSONArray("meals");
                    if (meals.length() > 0) {
                        JSONObject meal = meals.getJSONObject(0);

                        // Create recipe details object
                        currentRecipe = new RecipeDetails(
                            mealId,
                            meal.getString("strMeal"),
                            meal.getString("strCategory"),
                            meal.getString("strArea"),
                            meal.getString("strInstructions"),
                            meal.getString("strMealThumb")
                        );

                        // Get ingredients
                        StringBuilder ingredients = new StringBuilder();
                        for (int i = 1; i <= 20; i++) {
                            String ingredient = meal.optString("strIngredient" + i, "");
                            String measure = meal.optString("strMeasure" + i, "");
                            if (!ingredient.isEmpty()) {
                                ingredients.append("\u2022 ").append(ingredient);
                                if (!measure.isEmpty()) {
                                    ingredients.append(" - ").append(measure);
                                }
                                ingredients.append("\n");
                            }
                        }
                        currentRecipe.setIngredients(ingredients.toString());

                        // Update UI
                        updateUI(currentRecipe);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(RecipeDetailsActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(RecipeDetailsActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show();
            }
        }

        private void updateUI(RecipeDetails recipe) {
            recipeTitle.setText(recipe.getTitle());
            recipeCategory.setText("Category: " + recipe.getCategory());
            recipeArea.setText("Area: " + recipe.getArea());
            recipeInstructions.setText(recipe.getInstructions());
            recipeIngredients.setText(recipe.getIngredients());

            // Load image using Glide
            Glide.with(RecipeDetailsActivity.this)
                 .load(recipe.getImageUrl())
                 .into(recipeImage);
        }
    }

    // Recipe details model class
    private static class RecipeDetails {
        private String id;
        private String title;
        private String category;
        private String area;
        private String instructions;
        private String imageUrl;
        private String ingredients;

        public RecipeDetails(String id, String title, String category, String area,
                           String instructions, String imageUrl) {
            this.id = id;
            this.title = title;
            this.category = category;
            this.area = area;
            this.instructions = instructions;
            this.imageUrl = imageUrl;
        }

        // Getters
        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getCategory() { return category; }
        public String getArea() { return area; }
        public String getInstructions() { return instructions; }
        public String getImageUrl() { return imageUrl; }
        public String getIngredients() { return ingredients; }

        // Setters
        public void setIngredients(String ingredients) { this.ingredients = ingredients; }
    }
}
