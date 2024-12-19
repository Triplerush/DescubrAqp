package com.example.DescubrAQP.data.dao.categoria;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Categorias")
public class Categoria {
    @PrimaryKey(autoGenerate = true)
    private int categoryId; // Cambiado a camelCase
    private String categoryName; // Cambiado a camelCase

    // Getters y Setters
    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
