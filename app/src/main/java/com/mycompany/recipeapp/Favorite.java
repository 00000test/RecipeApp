package com.mycompany.recipeapp;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "favorites")
public class Favorite {
    @PrimaryKey
    @NonNull
    private String idMeal;
    private String strMeal;
    private String strMealThumb;
    private String strCategory;
    private String strArea;

    public Favorite(@NonNull String idMeal, String strMeal, String strMealThumb, String strCategory, String strArea) {
        this.idMeal = idMeal;
        this.strMeal = strMeal;
        this.strMealThumb = strMealThumb;
        this.strCategory = strCategory;
        this.strArea = strArea;
    }

    // Getters
    @NonNull
    public String getIdMeal() { return idMeal; }
    public String getStrMeal() { return strMeal; }
    public String getStrMealThumb() { return strMealThumb; }
    public String getStrCategory() { return strCategory; }
    public String getStrArea() { return strArea; }

    // Setters
    public void setIdMeal(@NonNull String idMeal) { this.idMeal = idMeal; }
    public void setStrMeal(String strMeal) { this.strMeal = strMeal; }
    public void setStrMealThumb(String strMealThumb) { this.strMealThumb = strMealThumb; }
    public void setStrCategory(String strCategory) { this.strCategory = strCategory; }
    public void setStrArea(String strArea) { this.strArea = strArea; }
}
