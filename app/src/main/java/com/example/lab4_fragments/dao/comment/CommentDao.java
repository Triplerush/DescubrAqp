package com.example.lab4_fragments.dao.comment;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CommentDao {
    @Insert
    void insertComment(Comment comment);

    @Query("SELECT * FROM comments WHERE buildingId = :buildingId")
    List<Comment> getCommentsForBuilding(int buildingId);

    @Query("DELETE FROM comments WHERE id = :commentId")
    void deleteComment(int commentId);
}