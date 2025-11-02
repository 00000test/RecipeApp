package com.mycompany.recipeapp;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Favorite.class, Comment.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract FavoriteDao favoriteDao();
    public abstract CommentDao commentDao();

    private static volatile AppDatabase INSTANCE;

    // Add migration from version 1 to 2
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS comments (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "mealId TEXT NOT NULL, " +
                "userId TEXT NOT NULL, " +
                "comment TEXT NOT NULL, " +
                "timestamp INTEGER NOT NULL, " +
                "rating INTEGER NOT NULL)"
            );
        }
    };

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "recipe_database")
                            .addMigrations(MIGRATION_1_2)
                            .fallbackToDestructiveMigration() // This will recreate tables if migration fails
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
