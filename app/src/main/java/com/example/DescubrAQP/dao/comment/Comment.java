package com.example.DescubrAQP.dao.comment;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "comments")
public class Comment {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int buildingId;
    private String username;
    private String text;
    private float rating;

    public Comment(int buildingId, String username, String text, float rating) {
        this.buildingId = buildingId;
        this.username = username;
        this.text = text;
        this.rating = rating;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBuildingId() {
        return buildingId;
    }

    public String getUsername() {
        return username;
    }

    public String getText() {
        return text;
    }

    public float getRating() {
        return rating;
    }
}
