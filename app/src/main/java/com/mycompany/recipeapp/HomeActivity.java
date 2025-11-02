package com.mycompany.recipeapp;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class HomeActivity extends Activity {
    private static final String TAG = "HomeActivity";  // Add this line
    private ListView listView;
    private ProgressBar progressBar;
    private RecipeAdapter adapter;
    private ArrayList<RecipeModel> recipeList;
    private String currentCategory = "Seafood";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        try {
            Log.d(TAG, "Starting HomeActivity");

            // Initialize views
            listView = findViewById(R.id.listView);
            progressBar = findViewById(R.id.progressBar);
            recipeList = new ArrayList<>();

            // Initialize adapter
            adapter = new RecipeAdapter(this, recipeList);
            listView.setAdapter(adapter);

            // Setup category buttons
            Button btnSeafood = findViewById(R.id.btnSeafood);
            Button btnDessert = findViewById(R.id.btnDessert);
            Button btnVegetarian = findViewById(R.id.btnVegetarian);
            Button btnFavorites = findViewById(R.id.btnFavorites);
            Button btnSearch = findViewById(R.id.btnSearch);
            Button btnSettings = findViewById(R.id.btnSettings);

            // Set up button click listeners
            btnSeafood.setOnClickListener(v -> {
                Log.d(TAG, "Seafood button clicked");
                loadRecipes("Seafood");
            });

            btnDessert.setOnClickListener(v -> {
                Log.d(TAG, "Dessert button clicked");
                loadRecipes("Dessert");
            });

            btnVegetarian.setOnClickListener(v -> {
                Log.d(TAG, "Vegetarian button clicked");
                loadRecipes("Vegetarian");
            });

            btnFavorites.setOnClickListener(v -> {
                Log.d(TAG, "Favorites button clicked");
                try {
                    Intent intent = new Intent(HomeActivity.this, FavoritesActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error opening favorites", e);
                    Toast.makeText(this, "Error opening favorites", Toast.LENGTH_SHORT).show();
                }
            });

            btnSearch.setOnClickListener(v -> {
                Log.d(TAG, "Search button clicked");
                try {
                    Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error opening search", e);
                    Toast.makeText(this, "Error opening search", Toast.LENGTH_SHORT).show();
                }
            });

            btnSettings.setOnClickListener(v -> {
                Log.d(TAG, "Settings button clicked");
                try {
                    Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error opening settings", e);
                    Toast.makeText(this, "Error opening settings", Toast.LENGTH_SHORT).show();
                }
            });

            // Set up list view click listener
            listView.setOnItemClickListener((parent, view, position, id) -> {
                try {
                    Log.d(TAG, "Recipe item clicked at position: " + position);
                    RecipeModel recipe = (RecipeModel) parent.getItemAtPosition(position);
                    if (recipe != null && recipe.getIdMeal() != null) {
                        Log.d(TAG, "Opening recipe with ID: " + recipe.getIdMeal());
                        Intent intent = new Intent(HomeActivity.this, RecipeDetailsActivity.class);
                        intent.putExtra("mealId", recipe.getIdMeal());
                        startActivity(intent);
                    } else {
                        Log.w(TAG, "Invalid recipe data");
                        Toast.makeText(this, "Invalid recipe", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error opening recipe details", e);
                    Toast.makeText(this, "Error opening recipe", Toast.LENGTH_SHORT).show();
                }
            });

            // Load initial recipes
            loadRecipes(currentCategory);
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Error initializing app", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadRecipes(String category) {
        try {
            Log.d(TAG, "Loading recipes for category: " + category);
            currentCategory = category;
            new FetchRecipesTask().execute("https://www.themealdb.com/api/json/v1/1/filter.php?c=" + category);
        } catch (Exception e) {
            Log.e(TAG, "Error loading recipes", e);
            Toast.makeText(this, "Error loading recipes", Toast.LENGTH_SHORT).show();
        }
    }

    private class FetchRecipesTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                progressBar.setVisibility(View.VISIBLE);
                listView.setVisibility(View.GONE);
            } catch (Exception e) {
                Log.e(TAG, "Error in onPreExecute", e);
            }
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                Log.d(TAG, "Fetching from: " + urls[0]);
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(15000); // 15 seconds
                connection.setReadTimeout(10000); // 10 seconds
                connection.connect();

                int responseCode = connection.getResponseCode();
                Log.d(TAG, "Response code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    reader.close();
                    inputStream.close();
                    connection.disconnect();

                    return result.toString();
                } else {
                    Log.e(TAG, "HTTP error: " + responseCode);
                }
            } catch (IOException e) {
                Log.e(TAG, "Error fetching recipes", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                progressBar.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);

                if (result != null) {
                    JSONObject jsonObject = new JSONObject(result);
                    if (jsonObject.has("meals")) {
                        JSONArray meals = jsonObject.getJSONArray("meals");
                        recipeList.clear();

                        for (int i = 0; i < meals.length(); i++) {
                            JSONObject meal = meals.getJSONObject(i);
                            String idMeal = meal.optString("idMeal", "");
                            String strMeal = meal.optString("strMeal", "");
                            String strMealThumb = meal.optString("strMealThumb", "");

                            if (!idMeal.isEmpty()) {
                                recipeList.add(new RecipeModel(idMeal, strMeal, strMealThumb));
                            }
                        }

                        adapter.notifyDataSetChanged();
                    } else {
                        Log.w(TAG, "No meals found in response");
                        Toast.makeText(HomeActivity.this, "No recipes found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Invalid response format");
                    Toast.makeText(HomeActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing JSON", e);
                Toast.makeText(HomeActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, "Error in onPostExecute", e);
                Toast.makeText(HomeActivity.this, "Error loading recipes", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
