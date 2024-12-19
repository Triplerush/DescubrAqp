package com.example.DescubrAQP.dao.categoria;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CategoriaDao {
    @Query("SELECT * FROM Categorias")
    List<Categoria> getAllCategorias();

    @Insert
    void insertCategoria(Categoria categoria);

    @Query("SELECT * FROM Categorias WHERE categoryId = :categoryId")
    Categoria getCategory(int categoryId);

    @Query("SELECT categoryId FROM Categorias WHERE categoryName = :categoryName")
    int getCategoryIdByName(String categoryName);
}
