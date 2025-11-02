package com.mycompany.recipeapp;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface CommentDao {
    @Insert
    void insert(Comment comment);

    @Query("SELECT * FROM comments WHERE mealId = :mealId ORDER BY timestamp DESC")
    List<Comment> getCommentsForMeal(String mealId);

    @Query("SELECT COUNT(*) FROM comments WHERE mealId = :mealId AND rating = 1")
    int getLikesCount(String mealId);

    @Query("SELECT COUNT(*) FROM comments WHERE mealId = :mealId AND rating = -1")
    int getDislikesCount(String mealId);

    @Query("SELECT rating FROM comments WHERE mealId = :mealId AND userId = :userId LIMIT 1")
    Integer getUserRating(String mealId, String userId);

    @Query("UPDATE comments SET rating = :rating WHERE mealId = :mealId AND userId = :userId")
    void updateUserRating(String mealId, String userId, int rating);
}
