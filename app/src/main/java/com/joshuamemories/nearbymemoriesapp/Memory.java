package com.joshuamemories.nearbymemoriesapp;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "memories")
public class Memory {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String title;          // required
    public double latitude;       // required
    public double longitude;      // required

    public String contactUri;     // optional
    public String photoUri;       // optional
    public String weatherJson;    // optional
    public long createdAt;        // epoch millis
}
