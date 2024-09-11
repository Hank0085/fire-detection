package com.wsh.homework;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wsh.homework.dao.Photo;

import java.util.ArrayList;
import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>{

    private List<Photo> photoList;
    private OnItemClickListener listener;


    public interface OnItemClickListener{
        void onSendClick(Photo photo);
        void onUpdateClick(Photo photo, String newResult);

    }
    public PhotoAdapter(OnItemClickListener listener) {
        this.listener = listener;
        this.photoList = new ArrayList<>();
    }
    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 加载 item_photo 布局并创建 PhotoViewHolder
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo, parent, false);
        return new PhotoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        // 获取当前位置的照片并绑定数据
        Photo photo = photoList.get(position);
        holder.bind(photo, listener);
    }


    // 返回照片列表的大小
    @Override
    public int getItemCount() {
        return photoList.size();
    }

    public void setPhotos(List<Photo> photos) {
        this.photoList = photos;
        notifyDataSetChanged();
    }
    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView photoImage;
        EditText photoResultEdit;
        Button sendButton;
        Button editButton;
        Button updateButton;

        TextView resulTextView;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            photoImage = itemView.findViewById(R.id.photo_image);
            photoResultEdit = itemView.findViewById(R.id.photo_result_edit);
            sendButton = itemView.findViewById(R.id.send_button);
            editButton = itemView.findViewById(R.id.edit_button);
            updateButton = itemView.findViewById(R.id.update_button);
            resulTextView = itemView.findViewById(R.id.resultText);
        }

        public void bind(Photo photo, OnItemClickListener listener) {
            // 载入图片
            Bitmap bitmap = BitmapFactory.decodeFile(photo.getPath());
            photoImage.setImageBitmap(bitmap);
            resulTextView.setText("Result="+photo.getResult()+"        isApprove="+photo.isApproved());

            photoResultEdit.setText(photo.getResult());
            photoResultEdit.setVisibility(View.GONE);
            updateButton.setVisibility(View.GONE);

            editButton.setOnClickListener(v -> {
                photoResultEdit.setVisibility(View.VISIBLE);
                updateButton.setVisibility(View.VISIBLE);
            });

            updateButton.setOnClickListener(v -> {
                String newResult = photoResultEdit.getText().toString();
                listener.onUpdateClick(photo, newResult);
                photoResultEdit.setVisibility(View.GONE);
                updateButton.setVisibility(View.GONE);
            });

            sendButton.setOnClickListener(v -> listener.onSendClick(photo));
        }
    }


}
