package com.example.lab4_fragments.dao.mansion;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface MansionDao {

    @Insert
    void insertRoom(RoomEntity room);

    @Insert
    void insertDoor(Door door);

    @Insert
    void insertRoomInfo(RoomInfo roomInfo);

    @Query("SELECT * FROM rooms")
    List<RoomEntity> getAllRooms();

    @Query("SELECT * FROM doors")
    List<Door> getAllDoors();

    @Query("SELECT * FROM room_info WHERE id = :roomId")
    List<RoomInfo> getAllRoomInfo(int roomId);

    @Query("SELECT * FROM room_info")
    List<RoomInfo> getAllRoomInfo();}
