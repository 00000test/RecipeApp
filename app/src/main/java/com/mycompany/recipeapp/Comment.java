package com.mycompany.recipeapp;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "comments")
public class Comment {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String mealId;
    private String userId;
    private String comment;
    private long timestamp;
    private int rating; // 1 for like, -1 for dislike

    public Comment(String mealId, String userId, String comment, long timestamp, int rating) {
        this.mealId = mealId;
        this.userId = userId;
        this.comment = comment;
        this.timestamp = timestamp;
        this.rating = rating;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getMealId() { return mealId; }
    public void setMealId(String mealId) { this.mealId = mealId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
}
