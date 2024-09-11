package com.wsh.homework.dao;

import androidx.lifecycle.LiveData;
import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.PrimaryKey;
import androidx.room.Query;

import java.util.List;

@Entity(tableName = "photos")
public class Photo {
    @PrimaryKey(autoGenerate = true)
    public int id;
    @ColumnInfo(name = "path")
    public String path;
    @ColumnInfo(name = "result")
    public String result;
    @ColumnInfo(name = "isApproved")
    public boolean isApproved;


    public Photo(String path,String result,boolean isApproved){
        this.isApproved = isApproved;
        this.path = path;
        this.result = result;

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }
}
