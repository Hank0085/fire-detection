package com.wsh.homework.Internet;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.widget.Toast;

import com.wsh.homework.PhotoListActivity;
import com.wsh.homework.dao.Photo;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class internetConnection {
    public int responseCode;
    public internetConnection(String url, Photo photo, Context context){
        HttpURLConnection httpConn = null;
        String charset = "UTF-8";


        try{
            Bitmap bitmap = BitmapFactory.decodeFile(photo.getPath());
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            String imageBase64 = Base64.encodeToString(imageBytes, Base64.NO_WRAP);


            JSONObject jsonParam = new JSONObject();;
            jsonParam.put("result", photo.getResult());
            jsonParam.put("isapprove", photo.isApproved);
            jsonParam.put("image", imageBase64);

            URL conntection = new URL(url);

            httpConn = (HttpURLConnection) conntection.openConnection();
            httpConn.setRequestMethod("POST");

            httpConn.setRequestProperty("Content-Type", "application/json; charset=" + charset);
            httpConn.setRequestProperty("Accept", "application/json");
            httpConn.setDoOutput(true);

            // 写入JSON数据到输出流
            OutputStream os = httpConn.getOutputStream();
            os.write(jsonParam.toString().getBytes(charset));
            os.flush();
            os.close();

            responseCode = httpConn.getResponseCode();
        }catch (Exception e){
            e.printStackTrace();
        }finally{
            if (httpConn != null) {
                httpConn.disconnect();
            }
        }
    }
}
