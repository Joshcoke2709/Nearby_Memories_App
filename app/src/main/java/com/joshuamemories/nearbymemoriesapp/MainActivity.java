package com.joshuamemories.nearbymemoriesapp;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.joshuamemories.nearbymemoriesapp.MapFragment;
import com.joshuamemories.nearbymemoriesapp.MemoryDetailFragment;
import com.joshuamemories.nearbymemoriesapp.MemoryListFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            showFragment(new MemoryListFragment()); // default screen
        }

        Button btnMap = findViewById(R.id.btnMap);
        Button btnList = findViewById(R.id.btnList);
        Button btnDetail = findViewById(R.id.btnDetail);

        btnMap.setOnClickListener(v -> showFragment(new MapFragment()));
        btnList.setOnClickListener(v -> showFragment(new MemoryListFragment()));
        btnDetail.setOnClickListener(v -> showFragment(new MemoryDetailFragment()));
    }

    public void showFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
    }
}
