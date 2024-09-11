package com.wsh.homework.dao;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Photo.class},version=1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract photoDao photoDao();
}