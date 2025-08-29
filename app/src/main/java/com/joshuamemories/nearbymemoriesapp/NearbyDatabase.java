package com.joshuamemories.nearbymemoriesapp;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Memory.class}, version = 1, exportSchema = false)
public abstract class NearbyDatabase extends RoomDatabase {
    public abstract MemoryDao memoryDao();

    private static volatile NearbyDatabase INSTANCE;

    public static NearbyDatabase get(Context ctx) {
        if (INSTANCE == null) {
            synchronized (NearbyDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            ctx.getApplicationContext(),
                            NearbyDatabase.class,
                            "nearby_memories.db"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}

