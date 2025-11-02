package com.mycompany.recipeapp;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class SearchActivity extends Activity {
    private EditText searchInput;
    private ListView listView;
    private ProgressBar progressBar;
    private RecipeAdapter adapter;
    private ArrayList<RecipeModel> recipeList;
    private RadioGroup searchTypeGroup;
    private Button btnFilter;
    private boolean showFilters = false;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Check if user is logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // User is not logged in, redirect to login
            Intent intent = new Intent(SearchActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        searchInput = findViewById(R.id.searchInput);
        listView = findViewById(R.id.listView);
        progressBar = findViewById(R.id.progressBar);
        searchTypeGroup = findViewById(R.id.searchTypeGroup);
        btnFilter = findViewById(R.id.btnFilter);

        recipeList = new ArrayList<>();
        adapter = new RecipeAdapter(this, recipeList);
        listView.setAdapter(adapter);

        // Search text listener
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 3) { // Start searching after 3 characters
                    performSearch(s.toString());
                } else if (s.length() == 0) {
                    recipeList.clear();
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Filter button click listener
        btnFilter.setOnClickListener(v -> {
            showFilters = !showFilters;
            searchTypeGroup.setVisibility(showFilters ? View.VISIBLE : View.GONE);
        });

        // List item click listener
        listView.setOnItemClickListener((parent, view, position, id) -> {
            RecipeModel recipe = (RecipeModel) parent.getItemAtPosition(position);
            Intent intent = new Intent(SearchActivity.this, RecipeDetailsActivity.class);
            intent.putExtra("mealId", recipe.getIdMeal());
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check if user is still logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // User is not logged in, redirect to login
            Intent intent = new Intent(SearchActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void performSearch(String query) {
        int selectedId = searchTypeGroup.getCheckedRadioButtonId();
        String searchType = "s"; // default search by name

        if (selectedId == R.id.radioIngredient) {
            searchType = "i"; // search by ingredient
        }

        try {
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            String url = "https://www.themealdb.com/api/json/v1/1/search.php?" + searchType + "=" + encodedQuery;
            new SearchTask().execute(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class SearchTask extends AsyncTask<String, Void, String> {
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

                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(SearchActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(SearchActivity.this, "No results found", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
