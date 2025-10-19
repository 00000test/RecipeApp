package com.mycompany.recipeapp;

public class RecipeModel {
    private String idMeal;
    private String strMeal;
    private String strMealThumb;

    public RecipeModel(String idMeal, String strMeal, String strMealThumb) {
        this.idMeal = idMeal;
        this.strMeal = strMeal;
        this.strMealThumb = strMealThumb;
    }

    public String getIdMeal() {
        return idMeal;
    }

    public String getStrMeal() {
        return strMeal;
    }

    public String getStrMealThumb() {
        return strMealThumb;
    }
}
