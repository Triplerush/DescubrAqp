package com.example.lab4_fragments.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

import com.example.lab4_fragments.dao.comment.Comment;
import com.example.lab4_fragments.dao.comment.CommentDao;
import com.example.lab4_fragments.dao.mansion.MansionDao;
import com.example.lab4_fragments.dao.mansion.Door;
import com.example.lab4_fragments.dao.mansion.RoomEntity;
import com.example.lab4_fragments.dao.mansion.RoomInfo;

@Database(entities = {RoomEntity.class, Door.class, RoomInfo.class, Comment.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract CommentDao commentDao();
    public abstract MansionDao mansionDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "app_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

