package com.mycompany.recipeapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import java.util.List;

public class FavoriteAdapter extends BaseAdapter {
    private Context context;
    private List<Favorite> favoriteList;

    public FavoriteAdapter(Context context, List<Favorite> favoriteList) {
        this.context = context;
        this.favoriteList = favoriteList;
    }

    @Override
    public int getCount() {
        return favoriteList != null ? favoriteList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        try {
            return favoriteList.get(position);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_favorite, parent, false);
            }

            ImageView recipeImage = convertView.findViewById(R.id.recipeImage);
            TextView recipeName = convertView.findViewById(R.id.recipeName);
            TextView recipeCategory = convertView.findViewById(R.id.recipeCategory);

            Favorite favorite = favoriteList.get(position);
            if (favorite != null) {
                recipeName.setText(favorite.getStrMeal() != null ? favorite.getStrMeal() : "");
                recipeCategory.setText(favorite.getStrCategory() != null ? favorite.getStrCategory() : "");

                // Using Glide to load images with error handling
                Glide.with(context)
                     .load(favorite.getStrMealThumb())
                     .placeholder(R.drawable.ic_launcher) // Add a placeholder image
                     .error(R.drawable.ic_launcher) // Add an error image
                     .into(recipeImage);
            }

            return convertView;
        } catch (Exception e) {
            e.printStackTrace();
            return convertView != null ? convertView : new View(context);
        }
    }
}
