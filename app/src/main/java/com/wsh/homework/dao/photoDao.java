package com.wsh.homework.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface photoDao {
    @Insert
    void insert(Photo photo);

    @Query("SELECT * FROM photos")
    LiveData<List<Photo>> getALlPhotos();

    @Update
    void update(Photo photo);

}