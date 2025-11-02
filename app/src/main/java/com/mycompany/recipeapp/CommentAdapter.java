package com.mycompany.recipeapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends BaseAdapter {
    private Context context;
    private List<Comment> commentList;

    public CommentAdapter(Context context, List<Comment> commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    @Override
    public int getCount() {
        return commentList.size();
    }

    @Override
    public Object getItem(int position) {
        return commentList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        }

        TextView commentText = convertView.findViewById(R.id.commentText);
        TextView timestampText = convertView.findViewById(R.id.timestampText);
        TextView ratingText = convertView.findViewById(R.id.ratingText);

        Comment comment = commentList.get(position);
        commentText.setText(comment.getComment());

        // Format timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        timestampText.setText(sdf.format(comment.getTimestamp()));

        // Set rating text
        String ratingSymbol = comment.getRating() == 1 ? "👍" : "👎";
        ratingText.setText(ratingSymbol);

        return convertView;
    }
}
