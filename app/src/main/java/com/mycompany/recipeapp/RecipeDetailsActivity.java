package com.mycompany.recipeapp;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
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
import java.util.List;

public class RecipeDetailsActivity extends Activity {
    private static final String TAG = "RecipeDetailsActivity";
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
    private FirebaseAuth mAuth;

    // Comments and ratings views
    private ListView commentsListView;
    private EditText commentInput;
    private Button btnSubmitComment;
    private TextView likesCount;
    private TextView dislikesCount;
    private ImageButton btnLike;
    private ImageButton btnDislike;
    private CommentAdapter commentAdapter;
    private CommentDao commentDao;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details);

        try {
            Log.d(TAG, "Starting RecipeDetailsActivity");

            // Initialize Firebase Auth
            mAuth = FirebaseAuth.getInstance();

            // Check if user is logged in
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                Log.w(TAG, "User not logged in");
                // User is not logged in, redirect to login
                Intent intent = new Intent(RecipeDetailsActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                return;
            }
            currentUserId = currentUser.getUid();
            Log.d(TAG, "User ID: " + currentUserId);

            // Initialize views
            initializeViews();

            // Get meal ID from intent
            mealId = getIntent().getStringExtra("mealId");
            if (mealId == null || mealId.isEmpty()) {
                Log.e(TAG, "No meal ID provided");
                Toast.makeText(this, "Invalid recipe", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            Log.d(TAG, "Meal ID: " + mealId);

            // Set up click listeners
            setupClickListeners();

            // Load recipe details
            new FetchRecipeDetailsTask().execute("https://www.themealdb.com/api/json/v1/1/lookup.php?i=" + mealId);

            // Load comments and ratings
            loadCommentsAndRatings();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Error loading recipe", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        try {
            Log.d(TAG, "Initializing views");

            recipeImage = findViewById(R.id.recipeImage);
            recipeTitle = findViewById(R.id.recipeTitle);
            recipeCategory = findViewById(R.id.recipeCategory);
            recipeArea = findViewById(R.id.recipeArea);
            recipeInstructions = findViewById(R.id.recipeInstructions);
            recipeIngredients = findViewById(R.id.recipeIngredients);
            btnSaveFavorite = findViewById(R.id.btnSaveFavorite);
            progressBar = findViewById(R.id.progressBar);

            // Initialize comment views
            commentsListView = findViewById(R.id.commentsListView);
            commentInput = findViewById(R.id.commentInput);
            btnSubmitComment = findViewById(R.id.btnSubmitComment);
            likesCount = findViewById(R.id.likesCount);
            dislikesCount = findViewById(R.id.dislikesCount);
            btnLike = findViewById(R.id.btnLike);
            btnDislike = findViewById(R.id.btnDislike);

            // Initialize database
            commentDao = AppDatabase.getDatabase(this).commentDao();

            Log.d(TAG, "Views initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
            throw e;
        }
    }

    private void setupClickListeners() {
        try {
            Log.d(TAG, "Setting up click listeners");

            // Set save favorite button click listener
            btnSaveFavorite.setOnClickListener(v -> {
                Log.d(TAG, "Save favorite button clicked");
                saveToFavorites();
            });

            // Set up comment submission
            btnSubmitComment.setOnClickListener(v -> {
                Log.d(TAG, "Submit comment button clicked");
                submitComment();
            });

            // Set up like/dislike buttons
            btnLike.setOnClickListener(v -> {
                Log.d(TAG, "Like button clicked");
                updateRating(1);
            });

            btnDislike.setOnClickListener(v -> {
                Log.d(TAG, "Dislike button clicked");
                updateRating(-1);
            });

            Log.d(TAG, "Click listeners set up successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up click listeners", e);
            throw e;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            Log.d(TAG, "Resuming activity");

            // Check if user is still logged in
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                Log.w(TAG, "User not logged in on resume");
                // User is not logged in, redirect to login
                Intent intent = new Intent(RecipeDetailsActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onResume", e);
        }
    }

    private void saveToFavorites() {
        try {
            Log.d(TAG, "Saving to favorites");

            if (currentRecipe == null) {
                Log.w(TAG, "No recipe to save");
                Toast.makeText(this, "No recipe to save", Toast.LENGTH_SHORT).show();
                return;
            }

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    try {
                        Favorite favorite = new Favorite(
                            currentRecipe.getId(),
                            currentRecipe.getTitle(),
                            currentRecipe.getImageUrl(),
                            currentRecipe.getCategory(),
                            currentRecipe.getArea()
                        );
                        AppDatabase.getDatabase(RecipeDetailsActivity.this)
                            .favoriteDao()
                            .insert(favorite);
                        Log.d(TAG, "Recipe saved to favorites");
                    } catch (Exception e) {
                        Log.e(TAG, "Error saving to favorites", e);
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    Toast.makeText(RecipeDetailsActivity.this, "Recipe saved to favorites!", Toast.LENGTH_SHORT).show();
                }
            }.execute();
        } catch (Exception e) {
            Log.e(TAG, "Error in saveToFavorites", e);
            Toast.makeText(this, "Error saving favorite", Toast.LENGTH_SHORT).show();
        }
    }

    private void submitComment() {
        try {
            Log.d(TAG, "Submitting comment");

            String commentText = commentInput.getText().toString().trim();
            if (commentText.isEmpty()) {
                Log.w(TAG, "Empty comment");
                Toast.makeText(this, "Please enter a comment", Toast.LENGTH_SHORT).show();
                return;
            }

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    try {
                        Comment comment = new Comment(
                            mealId,
                            currentUserId,
                            commentText,
                            System.currentTimeMillis(),
                            0 // No rating for comment only
                        );
                        commentDao.insert(comment);
                        Log.d(TAG, "Comment saved");
                    } catch (Exception e) {
                        Log.e(TAG, "Error saving comment", e);
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    commentInput.setText("");
                    loadCommentsAndRatings();
                    Toast.makeText(RecipeDetailsActivity.this, "Comment added", Toast.LENGTH_SHORT).show();
                }
            }.execute();
        } catch (Exception e) {
            Log.e(TAG, "Error in submitComment", e);
            Toast.makeText(this, "Error submitting comment", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateRating(int rating) {
        try {
            Log.d(TAG, "Updating rating: " + rating);

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    try {
                        commentDao.updateUserRating(mealId, currentUserId, rating);
                        Log.d(TAG, "Rating updated");
                    } catch (Exception e) {
                        Log.e(TAG, "Error updating rating", e);
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    loadCommentsAndRatings();
                }
            }.execute();
        } catch (Exception e) {
            Log.e(TAG, "Error in updateRating", e);
            Toast.makeText(this, "Error updating rating", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadCommentsAndRatings() {
        try {
            Log.d(TAG, "Loading comments and ratings");

            new AsyncTask<Void, Void, Void>() {
                private List<Comment> comments;
                private int likes;
                private int dislikes;
                private Integer userRating;

                @Override
                protected Void doInBackground(Void... voids) {
                    try {
                        comments = commentDao.getCommentsForMeal(mealId);
                        likes = commentDao.getLikesCount(mealId);
                        dislikes = commentDao.getDislikesCount(mealId);
                        userRating = commentDao.getUserRating(mealId, currentUserId);
                    } catch (Exception e) {
                        Log.e(TAG, "Error loading comments", e);
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    updateCommentsList(comments);
                    updateRatingButtons(likes, dislikes, userRating);
                }
            }.execute();
        } catch (Exception e) {
            Log.e(TAG, "Error in loadCommentsAndRatings", e);
            Toast.makeText(this, "Error loading comments", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateCommentsList(List<Comment> comments) {
        try {
            Log.d(TAG, "Updating comments list");

            if (comments != null) {
                commentAdapter = new CommentAdapter(this, comments);
                commentsListView.setAdapter(commentAdapter);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating comments list", e);
            Toast.makeText(this, "Error updating comments", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateRatingButtons(int likes, int dislikes, Integer userRating) {
        try {
            Log.d(TAG, "Updating rating buttons");

            likesCount.setText(String.valueOf(likes));
            dislikesCount.setText(String.valueOf(dislikes));

            // Update button states based on user's rating
            btnLike.setSelected(userRating != null && userRating == 1);
            btnDislike.setSelected(userRating != null && userRating == -1);
        } catch (Exception e) {
            Log.e(TAG, "Error updating rating buttons", e);
        }
    }

    private class FetchRecipeDetailsTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                progressBar.setVisibility(View.VISIBLE);
                Log.d(TAG, "Starting recipe fetch");
            } catch (Exception e) {
                Log.e(TAG, "Error in onPreExecute", e);
            }
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                Log.d(TAG, "Fetching recipe from: " + urls[0]);

                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(15000); // 15 seconds
                connection.setReadTimeout(10000); // 10 seconds
                connection.connect();

                int responseCode = connection.getResponseCode();
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

                    Log.d(TAG, "Recipe data fetched successfully");
                    return result.toString();
                } else {
                    Log.e(TAG, "HTTP error: " + responseCode);
                }
            } catch (IOException e) {
                Log.e(TAG, "Error fetching recipe", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                progressBar.setVisibility(View.GONE);

                if (result != null) {
                    JSONObject jsonObject = new JSONObject(result);
                    if (jsonObject.has("meals")) {
                        JSONArray meals = jsonObject.getJSONArray("meals");
                        if (meals.length() > 0) {
                            JSONObject meal = meals.getJSONObject(0);

                            // Create recipe details object
                            currentRecipe = new RecipeDetails(
                                mealId,
                                meal.optString("strMeal", ""),
                                meal.optString("strCategory", ""),
                                meal.optString("strArea", ""),
                                meal.optString("strInstructions", ""),
                                meal.optString("strMealThumb", "")
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
                        } else {
                            Log.w(TAG, "No recipe found");
                            Toast.makeText(RecipeDetailsActivity.this, "Recipe not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Invalid recipe data format");
                        Toast.makeText(RecipeDetailsActivity.this, "Invalid recipe data", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "No recipe data received");
                    Toast.makeText(RecipeDetailsActivity.this, "Error fetching recipe data", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing recipe data", e);
                Toast.makeText(RecipeDetailsActivity.this, "Error parsing recipe data", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, "Error in onPostExecute", e);
                Toast.makeText(RecipeDetailsActivity.this, "Error loading recipe", Toast.LENGTH_SHORT).show();
            }
        }

        private void updateUI(RecipeDetails recipe) {
            try {
                Log.d(TAG, "Updating UI");

                if (recipe == null) {
                    Log.e(TAG, "No recipe data to display");
                    return;
                }

                recipeTitle.setText(recipe.getTitle());
                recipeCategory.setText("Category: " + recipe.getCategory());
                recipeArea.setText("Area: " + recipe.getArea());
                recipeInstructions.setText(recipe.getInstructions());
                recipeIngredients.setText(recipe.getIngredients());

                // Load image using Glide with error handling
                Glide.with(RecipeDetailsActivity.this)
                     .load(recipe.getImageUrl())
                     .placeholder(R.drawable.ic_launcher)
                     .error(R.drawable.ic_launcher)
                     .into(recipeImage);

                Log.d(TAG, "UI updated successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error updating UI", e);
                Toast.makeText(RecipeDetailsActivity.this, "Error displaying recipe", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
