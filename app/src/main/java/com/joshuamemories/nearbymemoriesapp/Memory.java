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

    public String contactUri;   // optional
    public String photoUri;     // optional
    public String weatherJson;  // optional
    public long createdAt;
}
