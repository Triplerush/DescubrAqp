package com.example.DescubrAQP.data.dao.mansion;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "room_info")
public class RoomInfo {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private String description;
    private int imageUrl;

    public RoomInfo(String title, String description, int imageUrl) {
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getImageUrl() {
        return imageUrl;
    }

}

