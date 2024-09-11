package com.wsh.homework;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.wsh.homework.dao.AppDatabase;
import com.wsh.homework.dao.Photo;

public class ResultActivity extends AppCompatActivity {

    private String result;
    private String photoPath;
    private AppDatabase db;

    private ImageView image;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);


        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class,"photo-database").build();
        result = getIntent().getStringExtra("result");
        photoPath = getIntent().getStringExtra("photoPath");

        TextView resultTextView = this.findViewById(R.id.resultTextView);
        image = this.findViewById(R.id.photo_image);
        Bitmap bitmap = BitmapFactory.decodeFile(photoPath);
        image.setImageBitmap(bitmap);
        resultTextView.setText(result);


        findViewById(R.id.approveButton).setOnClickListener(v -> saveResult(true));
        findViewById(R.id.rejectButton).setOnClickListener(v -> saveResult(false));

    }

    private void saveResult(boolean isApproved){
        new Thread(()->{
            Photo photo = new Photo(photoPath,result,isApproved);
            db.photoDao().insert(photo);
        }).start();
        finish();
    }

}