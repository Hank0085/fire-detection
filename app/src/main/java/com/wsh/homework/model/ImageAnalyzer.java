package com.wsh.homework.model;

import static android.content.ContentValues.TAG;
import static androidx.camera.core.impl.utils.ContextUtil.getApplicationContext;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;


import com.wsh.homework.MainActivity;
import com.wsh.homework.R;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.Tensor;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.TensorProcessor;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ImageAnalyzer{
    private Interpreter interpreter;
    private MediaPlayer mediaPlayer;
    private Context context;
    private final String[] labels = {"fire", "smoke"};

    public ImageAnalyzer(Context context, String modelName) throws IOException {
        this.context = context;
        MappedByteBuffer tfliteModel = FileUtil.loadMappedFile(context,modelName);
        interpreter = new Interpreter(tfliteModel);
        Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        mediaPlayer = MediaPlayer.create(context, ringtoneUri);
    }

    public String analyzeFrame(Bitmap bitmap) {
        bitmap = padBitmap(bitmap, 640, 640);

        if (bitmap.getWidth() != 640 || bitmap.getHeight() != 640) {
            throw new IllegalArgumentException("Bitmap size must be 640x640");
        }
        if (bitmap.getConfig() != Bitmap.Config.ARGB_8888) {
            throw new IllegalArgumentException("Bitmap config must be ARGB_8888");
        }

        int[] inputShape = interpreter.getInputTensor(0).shape();
        int inputSize = inputShape[1];
        int numChannels = inputShape[3];


        ByteBuffer inputBuffer = ByteBuffer.allocateDirect(inputSize * inputSize * numChannels * 4); // 每个浮点数 4 字节
        inputBuffer.order(ByteOrder.nativeOrder());


        TensorImage inputImage = new TensorImage(DataType.FLOAT32);
        inputImage.load(bitmap);


        TensorProcessor processor = new TensorProcessor.Builder().add(new NormalizeOp(0, 255)).build();


        inputBuffer = processor.process(inputImage.getTensorBuffer()).getBuffer();


        int[] outputShape = {1, 25200, 7};
        TensorBuffer outputBuffer = TensorBuffer.createFixedSize(outputShape, DataType.FLOAT32);
        interpreter.run(inputBuffer, outputBuffer.getBuffer().rewind());


        List<RectF> boundingBoxes = new ArrayList<>();
        List<String> detectedLabels = new ArrayList<>();
        parseOutput(outputBuffer, boundingBoxes, detectedLabels);


        boolean fireDetected = detectedLabels.contains("fire");
        boolean smokeDetected = detectedLabels.contains("smoke");

        if (fireDetected || smokeDetected) {
            playDefaultRingtone(mediaPlayer);
        }


        drawBoundingBoxes(bitmap, boundingBoxes, detectedLabels);


        if (fireDetected) {
            return "fire";
        } else if (smokeDetected) {
            return "smoke";
        } else {
            return "safe";
        }
    }
    private void playDefaultRingtone(MediaPlayer mediaPlayer) {

        if(!mediaPlayer.isPlaying())
            mediaPlayer.start();
    }
    private void parseOutput(TensorBuffer outputBuffer, List<RectF> boundingBoxes, List<String> detectedLabels) {
        float[] output = outputBuffer.getFloatArray();
        int outputSize = output.length / 7;

        for (int i = 0; i < outputSize; i++) {
            float score = output[i * 7 + 4];
            if (score > 0.5) { // confidence threshold
                float x = output[i * 7];
                float y = output[i * 7 + 1];
                float w = output[i * 7 + 2];
                float h = output[i * 7 + 3];
                int classId = (int) output[i * 7 + 5];

                boundingBoxes.add(new RectF(x - w / 2, y - h / 2, x + w / 2, y + h / 2));
                detectedLabels.add(labels[classId]);
            }
        }
    }
    private void drawBoundingBoxes(Bitmap bitmap, List<RectF> boundingBoxes, List<String> detectedLabels) {
        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.RED);
        paint.setStrokeWidth(2.0f);

        for (int i = 0; i < boundingBoxes.size(); i++) {
            canvas.drawRect(boundingBoxes.get(i), paint);
            drawLabel(canvas, boundingBoxes.get(i), detectedLabels.get(i), paint);
        }
    }
    private void drawLabel(Canvas canvas, RectF rect, String label, Paint paint) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.YELLOW);
        paint.setTextSize(30.0f);
        canvas.drawText(label, rect.left, rect.top, paint);
    }
    private Bitmap padBitmap(Bitmap srcBitmap, int newWidth, int newHeight) {
        Bitmap outputBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(outputBitmap);
        canvas.drawColor(Color.BLACK);

        int offsetX = (newWidth - srcBitmap.getWidth()) / 2;
        int offsetY = (newHeight - srcBitmap.getHeight()) / 2;

        canvas.drawBitmap(srcBitmap, offsetX, offsetY, null);

        return outputBitmap;
    }

}
