package com.joshuamemories.nearbymemoriesapp;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MemoryDao {
    @Query("SELECT * FROM memories ORDER BY createdAt DESC")
    LiveData<List<Memory>> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Memory memory);

    @Query("DELETE FROM memories")
    void clearAll();
}


