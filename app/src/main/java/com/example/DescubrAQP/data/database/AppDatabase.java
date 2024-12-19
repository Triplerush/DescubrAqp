package com.example.DescubrAQP.data.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

import com.example.DescubrAQP.data.dao.building.Building;
import com.example.DescubrAQP.data.dao.building.BuildingDao;
import com.example.DescubrAQP.data.dao.categoria.Categoria;
import com.example.DescubrAQP.data.dao.categoria.CategoriaDao;
import com.example.DescubrAQP.data.dao.comment.Comment;
import com.example.DescubrAQP.data.dao.comment.CommentDao;
import com.example.DescubrAQP.data.dao.mansion.MansionDao;
import com.example.DescubrAQP.data.dao.mansion.Door;
import com.example.DescubrAQP.data.dao.mansion.RoomEntity;
import com.example.DescubrAQP.data.dao.mansion.RoomInfo;
import com.example.DescubrAQP.data.dao.user.User;
import com.example.DescubrAQP.data.dao.user.UserDao;

@Database(entities = {RoomEntity.class, Door.class, RoomInfo.class, Comment.class, Categoria.class, Building.class, User.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract CommentDao commentDao();
    public abstract MansionDao mansionDao();
    public abstract CategoriaDao categoriaDao();
    public abstract BuildingDao buildingDao();
    public abstract UserDao userDao();

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


