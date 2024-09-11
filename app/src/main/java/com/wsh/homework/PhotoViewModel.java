package com.wsh.homework;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.wsh.homework.dao.Photo;
import com.wsh.homework.dao.photoRepository;

import java.util.List;

public class PhotoViewModel extends AndroidViewModel {
    private photoRepository repository;
    private LiveData<List<Photo>> allPhotos;

    public PhotoViewModel(@NonNull Application application) {
        super(application);
        repository = new photoRepository(application);
        allPhotos = repository.getAllPhotos();
    }

    public LiveData<List<Photo>> getAllPhotos() {
        return allPhotos;
    }

    public void insert(Photo photo) {
        repository.insert(photo);
    }

    public void update(Photo photo) {
       repository.update(photo);
    }

}
