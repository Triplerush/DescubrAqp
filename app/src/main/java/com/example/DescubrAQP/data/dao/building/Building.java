package com.example.DescubrAQP.data.dao.building;


import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import com.example.DescubrAQP.data.dao.categoria.Categoria;

/**
 * Clase Building como entidad para Room.
 */
@Entity(
        tableName = "Buildings",
        foreignKeys = @ForeignKey(
                entity = Categoria.class,  // Relación con la tabla Categoria
                parentColumns = "categoryId",
                childColumns = "categoryId",
                onDelete = ForeignKey.SET_NULL,
                onUpdate = ForeignKey.CASCADE
        )
)
public class Building {
    @PrimaryKey(autoGenerate = true)
    private int buildingId;      // ID de la edificación (Primary Key)

    private Integer categoryId;  // ID de la categoría (Foreign Key, puede ser nulo)
    private String title;        // Título de la edificación
    private String description;  // Descripción
    private String imageResId;   // Nombre del recurso de imagen
    private double latitude;     // Latitud
    private double longitude;    // Longitud
    public Building(){

    }
    // Constructor con parámetros
    public Building(String title, Integer categoryId, String description, String imageResId, double latitude, double longitude) {
        this.title = title;
        this.categoryId = categoryId;
        this.description = description;
        this.imageResId = imageResId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters y Setters
    public int getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(int buildingId) {
        this.buildingId = buildingId;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageResId() {
        return imageResId;
    }

    public void setImageResId(String imageResId) {
        this.imageResId = imageResId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
