package com.wsh.homework.dao;

import android.app.Application;

import java.util.List;
import androidx.lifecycle.LiveData;
import androidx.room.Room;

public class photoRepository {
    private photoDao photoDao;
    private LiveData<List<Photo>> allPhotos;


    public photoRepository(Application application) {
        AppDatabase db = Room.databaseBuilder(application, AppDatabase.class, "photo-database").build();
        photoDao = db.photoDao();
        allPhotos = photoDao.getALlPhotos();
    }

    public LiveData<List<Photo>> getAllPhotos() {
        return allPhotos;
    }

    public void insert(Photo photo) {
        new Thread(() -> photoDao.insert(photo)).start();
    }

    public void update(Photo photo){new Thread(() -> photoDao.update(photo)).start();}


}
