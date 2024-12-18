package com.example.lab4_fragments.dao.building;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface BuildingDao {
    @Insert
    void insertBuilding(Building edificacion);

    @Query("SELECT COUNT(*) FROM Buildings")
    int getBuildingsCount();

    @Query("SELECT * FROM Buildings")
    List<Building> getAllBuildings();
}
