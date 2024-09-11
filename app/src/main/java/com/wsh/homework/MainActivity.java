package com.wsh.homework;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageCapture;
import androidx.camera.view.PreviewView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.cameraButton).setOnClickListener(v -> startActivity(new Intent(this, CameraActivity.class)));
        findViewById(R.id.viewPhotosButton).setOnClickListener(v -> startActivity(new Intent(this, PhotoListActivity.class)));
    }
}