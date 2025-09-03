package com.joshuamemories.nearbymemoriesapp;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "memories")
public class Memory {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String title;
    public double latitude;
    public double longitude;

    public String contactUri;
    public String photoUri;
    public String weatherJson;
    public long createdAt;

    public String placeName;
    public String imageUrl;    

}
