package com.wsh.homework;

import static androidx.activity.result.ActivityResultLauncherKt.launch;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.wsh.homework.model.Camera2Manager;
import com.wsh.homework.model.ImageAnalyzer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import android.Manifest;

public class CameraActivity extends AppCompatActivity {
    private static final int REQUEST_CAMERA_PERMISSION = 200;

    private static final long FRAME_PROCESS_INTERVAL_MS = 1000;
    private long lastFrameProcessedTime = 0;
    private TextureView textureView;
    private ImageAnalyzer imageAnalyzer;
    private Camera2Manager camera2Manager;
    private Bitmap latestAnalyzedBitmap;

    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);


        textureView = findViewById(R.id.texture_view);
        Button captureButton = findViewById(R.id.capture_button);

        // 初始化 ImageAnalyzer
        try {
            imageAnalyzer = new ImageAnalyzer(this, "fire.tflite");
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera2Manager = new Camera2Manager(this, textureView, new Size(640, 640), new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                long currentTime = SystemClock.elapsedRealtime();
                if (currentTime - lastFrameProcessedTime >= FRAME_PROCESS_INTERVAL_MS) {
                    lastFrameProcessedTime = currentTime;
                    Image image = reader.acquireNextImage();
                    if (image != null) {
                        Bitmap bitmap = imageToBitmap(image);
                        image.close();
                        String result = imageAnalyzer.analyzeFrame(bitmap);
                        latestAnalyzedBitmap = bitmap;
                    }

                }else{
                    Image image = reader.acquireNextImage();
                    if (image != null) {
                        image.close();
                    }
                }

            }
        });
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });

        requestCameraPermission();

    }
    @Override
    protected void onResume() {
        super.onResume();
        camera2Manager.startCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        camera2Manager.stopCamera();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRingtone();
    }
    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            camera2Manager.startCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                camera2Manager.startCamera();
            } else {
                finish();
            }
        }
    }
    private void takePhoto() {
        if (latestAnalyzedBitmap != null) {
            String result = imageAnalyzer.analyzeFrame(latestAnalyzedBitmap);
            saveImageAndResult(latestAnalyzedBitmap, result);
        }
    }

    private void saveImageAndResult(Bitmap analyzedBitmap, String result) {
        // Display the result in the UI

        // 保存分析后的图片和结果
        String fileName = "IMG_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".jpg";
        File file = new File(getExternalFilesDir(null), fileName);

        try {
            FileOutputStream fos = new FileOutputStream(file);
            analyzedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 保存路径和结果到数据库
        saveToDatabase(file.getAbsolutePath(), result);

    }
    private void saveToDatabase(String photoPath, String result) {
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra("result", result);
        intent.putExtra("photoPath", photoPath);
        startActivity(intent);
    }
    private String assetFilePath(Context context,String assetName) throws IOException {
            // 创建一个File对象，表示存储在应用内部存储中的目标文件
            File file = new File(context.getFilesDir(), assetName);

            // 如果文件已存在且大小大于0，则表示文件已经被复制到应用的内部存储中，直接返回该文件的绝对路径
            if (file.exists() && file.length() > 0) {
                return file.getAbsolutePath();
            }

            // 如果文件不存在或大小为0，则从Assets中读取文件，并将其复制到应用的内部存储中
            try (InputStream is = context.getAssets().open(assetName);
                 FileOutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                // 循环读取输入流中的数据，并写入输出流，直到读取完整个文件
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
                // 返回复制后的文件的绝对路径
                return file.getAbsolutePath();
            }
        }
    private Bitmap imageToBitmap(Image image) {
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer yBuffer = planes[0].getBuffer();
        ByteBuffer uBuffer = planes[1].getBuffer();
        ByteBuffer vBuffer = planes[2].getBuffer();
        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();
        byte[] nv21 = new byte[ySize + uSize + vSize];
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);
        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 75, out);
        byte[] imageBytes = out.toByteArray();
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }
    private Bitmap rotateBitmap(Bitmap bitmap, int rotationDegrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(rotationDegrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private void stopRingtone() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}