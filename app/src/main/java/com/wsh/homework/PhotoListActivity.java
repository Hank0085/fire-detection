package com.wsh.homework;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.wsh.homework.Internet.internetConnection;
import com.wsh.homework.dao.AppDatabase;
import com.wsh.homework.dao.Photo;
import com.wsh.homework.dao.photoDao;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class PhotoListActivity extends AppCompatActivity {
    private PhotoViewModel photoViewModel;
    private PhotoAdapter adapter;

    private ExecutorService executorService = Executors.newFixedThreadPool(4);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_list);
        adapter = new PhotoAdapter(new PhotoAdapter.OnItemClickListener() {
            @Override
            public void onSendClick(Photo photo) {
                sendPhotoToServer(photo);
            }

            @Override
            public void onUpdateClick(Photo photo, String newResult) {
                photo.setResult(newResult);
                photo.setApproved(true);
                photoViewModel.update(photo);
            }
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); // 设置布局管理器
        recyclerView.setAdapter(adapter);

        photoViewModel = new ViewModelProvider(this).get(PhotoViewModel.class);
        photoViewModel.getAllPhotos().observe(this, photos -> {
            adapter.setPhotos(photos);
        });
    }
    private void sendPhotoToServer(Photo photo) {
        executorService.execute(() -> {
            try {
                String urlString = "http://172.26.112.236:8080/uploadPhoto";
                int responseCode = new internetConnection(urlString,photo,this).responseCode;

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    runOnUiThread(() -> Toast.makeText(PhotoListActivity.this, "Photo uploaded successfully", Toast.LENGTH_SHORT).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(PhotoListActivity.this, "Failed to upload photo", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(PhotoListActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }
}
