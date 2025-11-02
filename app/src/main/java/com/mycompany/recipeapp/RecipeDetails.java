package com.mycompany.recipeapp;

public class RecipeDetails {
    private String id;
    private String title;
    private String category;
    private String area;
    private String instructions;
    private String imageUrl;
    private String ingredients;

    public RecipeDetails(String id, String title, String category, String area,
                       String instructions, String imageUrl) {
        this.id = id != null ? id : "";
        this.title = title != null ? title : "";
        this.category = category != null ? category : "";
        this.area = area != null ? area : "";
        this.instructions = instructions != null ? instructions : "";
        this.imageUrl = imageUrl != null ? imageUrl : "";
    }

    // Getters
    public String getId() { return id != null ? id : ""; }
    public String getTitle() { return title != null ? title : ""; }
    public String getCategory() { return category != null ? category : ""; }
    public String getArea() { return area != null ? area : ""; }
    public String getInstructions() { return instructions != null ? instructions : ""; }
    public String getImageUrl() { return imageUrl != null ? imageUrl : ""; }
    public String getIngredients() { return ingredients != null ? ingredients : ""; }

    // Setters
    public void setIngredients(String ingredients) {
        this.ingredients = ingredients != null ? ingredients : "";
    }
}
