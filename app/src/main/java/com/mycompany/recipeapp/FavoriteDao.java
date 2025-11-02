package com.mycompany.recipeapp;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface FavoriteDao {
    @Insert
    void insert(Favorite favorite);

    @Delete
    void delete(Favorite favorite);

    @Query("SELECT * FROM favorites")
    List<Favorite> getAllFavorites();

    @Query("SELECT * FROM favorites WHERE idMeal = :mealId")
    Favorite getFavoriteById(String mealId);

    @Query("DELETE FROM favorites WHERE idMeal = :mealId")
    void deleteById(String mealId);
}
