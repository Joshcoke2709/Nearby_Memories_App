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
    public String imageUri; // persistable content:// URI for a photo
    public String description;   // free text
    public Long   eventDateMs;   // optional custom date (millis since epoch)


}
