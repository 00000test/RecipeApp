package com.mycompany.recipeapp;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.List;

public class FavoritesActivity extends Activity {
    private ListView listView;
    private ProgressBar progressBar;
    private FavoriteAdapter adapter;
    private AppDatabase database;
    private FavoriteDao favoriteDao;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        try {
            // Initialize Firebase Auth
            mAuth = FirebaseAuth.getInstance();

            // Check if user is logged in
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                // User is not logged in, redirect to login
                Intent intent = new Intent(FavoritesActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                return;
            }

            listView = findViewById(R.id.listView);
            progressBar = findViewById(R.id.progressBar);

            // Initialize database
            database = AppDatabase.getDatabase(this);
            favoriteDao = database.favoriteDao();

            // Set up long click listener for removing favorites
            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        Favorite favorite = (Favorite) parent.getItemAtPosition(position);
                        if (favorite != null) {
                            new DeleteFavoriteTask().execute(favorite);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(FavoritesActivity.this, "Error removing favorite", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            });

            // Set up item click listener to view recipe details
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        Favorite favorite = (Favorite) parent.getItemAtPosition(position);
                        if (favorite != null && favorite.getIdMeal() != null) {
                            Intent intent = new Intent(FavoritesActivity.this, RecipeDetailsActivity.class);
                            intent.putExtra("mealId", favorite.getIdMeal());
                            startActivity(intent);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(FavoritesActivity.this, "Error opening recipe", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            // Load favorites
            loadFavorites();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error initializing activity", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            // Check if user is still logged in
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                // User is not logged in, redirect to login
                Intent intent = new Intent(FavoritesActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                // Refresh favorites list
                loadFavorites();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error checking user status", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadFavorites() {
        try {
            new LoadFavoritesTask().execute();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading favorites", Toast.LENGTH_SHORT).show();
        }
    }

    private class LoadFavoritesTask extends AsyncTask<Void, Void, List<Favorite>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                progressBar.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected List<Favorite> doInBackground(Void... voids) {
            try {
                return favoriteDao.getAllFavorites();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Favorite> favorites) {
            super.onPostExecute(favorites);
            try {
                progressBar.setVisibility(View.GONE);

                if (favorites != null && !favorites.isEmpty()) {
                    adapter = new FavoriteAdapter(FavoritesActivity.this, favorites);
                    listView.setAdapter(adapter);
                } else {
                    Toast.makeText(FavoritesActivity.this, "No favorites yet", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(FavoritesActivity.this, "Error displaying favorites", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class DeleteFavoriteTask extends AsyncTask<Favorite, Void, Void> {
        @Override
        protected Void doInBackground(Favorite... favorites) {
            try {
                if (favorites != null && favorites[0] != null) {
                    favoriteDao.delete(favorites[0]);
                }
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            try {
                Toast.makeText(FavoritesActivity.this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                loadFavorites(); // Refresh the list
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(FavoritesActivity.this, "Error updating favorites", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
