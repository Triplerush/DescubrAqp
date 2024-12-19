package com.example.DescubrAQP.dao.mansion;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "doors")
public class Door {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private float x1, y1, x2, y2;

    public Door(float x1, float y1, float x2, float y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getX1() {
        return x1;
    }

    public float getY1() {
        return y1;
    }

    public float getX2() {
        return x2;
    }

    public float getY2() {
        return y2;
    }
}