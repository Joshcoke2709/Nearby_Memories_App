package com.joshuamemories.nearbymemoriesapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.joshuamemories.nearbymemoriesapp.MainActivity;
import com.joshuamemories.nearbymemoriesapp.R;
import com.joshuamemories.nearbymemoriesapp.Memory;
import com.joshuamemories.nearbymemoriesapp.MemoryDao;
import com.joshuamemories.nearbymemoriesapp.NearbyDatabase;

import java.util.List;
import java.util.concurrent.Executors;

public class MemoryListFragment extends Fragment {

    private MemoryDao dao;
    private MemoryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_memory_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        dao = NearbyDatabase.get(requireContext()).memoryDao();

        RecyclerView rv = v.findViewById(R.id.rvMemories);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new MemoryAdapter(memory -> {
            // open detail
            ((MainActivity) requireActivity()).showFragment(new MemoryDetailFragment());
            // (Next step: pass memory.id to detail)
        });
        rv.setAdapter(adapter);

        // observe data
        LiveData<List<Memory>> live = dao.getAll();
        live.observe(getViewLifecycleOwner(), adapter::submitList);

        // TEMP: add dummy memory when + pressed (until we wire real Add flow)
        Button btnAdd = v.findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(view -> {
            Executors.newSingleThreadExecutor().execute(() -> {
                Memory m = new Memory();
                m.title = "Test memory";
                m.latitude = 18.0179;   // Kingston-ish dummy
                m.longitude = -76.8099;
                m.createdAt = System.currentTimeMillis();
                dao.insert(m);
            });
        });
    }
}
