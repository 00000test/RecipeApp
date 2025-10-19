package com.mycompany.recipeapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;

public class RecipeAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<RecipeModel> recipeList;

    public RecipeAdapter(Context context, ArrayList<RecipeModel> recipeList) {
        this.context = context;
        this.recipeList = recipeList;
    }

    @Override
    public int getCount() {
        return recipeList.size();
    }

    @Override
    public Object getItem(int position) {
        return recipeList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_recipe, parent, false);
        }

        ImageView recipeImage = convertView.findViewById(R.id.recipeImage);
        TextView recipeName = convertView.findViewById(R.id.recipeName);

        RecipeModel recipe = recipeList.get(position);
        recipeName.setText(recipe.getStrMeal());

        // Using Glide to load images
        Glide.with(context)
             .load(recipe.getStrMealThumb())
             .into(recipeImage);

        return convertView;
    }
}
