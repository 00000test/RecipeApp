package com.mycompany.recipeapp;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
    private ListView listView;
    private ProgressBar progressBar;
    private RecipeAdapter adapter;
    private ArrayList<RecipeModel> recipeList;
    private String currentCategory = "Seafood";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        listView = findViewById(R.id.listView);
        progressBar = findViewById(R.id.progressBar);
        recipeList = new ArrayList<>();

        // Setup category buttons
        Button btnSeafood = findViewById(R.id.btnSeafood);
        Button btnDessert = findViewById(R.id.btnDessert);
        Button btnVegetarian = findViewById(R.id.btnVegetarian);

        btnSeafood.setOnClickListener(v -> loadRecipes("Seafood"));
        btnDessert.setOnClickListener(v -> loadRecipes("Dessert"));
        btnVegetarian.setOnClickListener(v -> loadRecipes("Vegetarian"));

        // Set up list view click listener
        listView.setOnItemClickListener((parent, view, position, id) -> {
            RecipeModel recipe = (RecipeModel) parent.getItemAtPosition(position);
            Intent intent = new Intent(HomeActivity.this, RecipeDetailsActivity.class);
            intent.putExtra("mealId", recipe.getIdMeal());
            startActivity(intent);
        });

        // Load initial recipes
        loadRecipes(currentCategory);
    }

    private void loadRecipes(String category) {
        currentCategory = category;
        new FetchRecipesTask().execute("https://www.themealdb.com/api/json/v1/1/filter.php?c=" + category);
    }

    private class FetchRecipesTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
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
            listView.setVisibility(View.VISIBLE);

            if (result != null) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    JSONArray meals = jsonObject.getJSONArray("meals");
                    recipeList.clear();

                    for (int i = 0; i < meals.length(); i++) {
                        JSONObject meal = meals.getJSONObject(i);
                        String idMeal = meal.getString("idMeal");
                        String strMeal = meal.getString("strMeal");
                        String strMealThumb = meal.getString("strMealThumb");

                        recipeList.add(new RecipeModel(idMeal, strMeal, strMealThumb));
                    }

                    adapter = new RecipeAdapter(HomeActivity.this, recipeList);
                    listView.setAdapter(adapter);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(HomeActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(HomeActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
