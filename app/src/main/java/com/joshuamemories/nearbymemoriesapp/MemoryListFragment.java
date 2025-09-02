package com.joshuamemories.nearbymemoriesapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

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
            // later: pass memory.id to detail
            ((MainActivity) requireActivity()).showFragment(new MemoryDetailFragment());
        });
        rv.setAdapter(adapter);

        LiveData<List<Memory>> live = dao.getAll();
        live.observe(getViewLifecycleOwner(), adapter::submitList);

        Button btnAdd = v.findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(view -> showAddDialog());
    }

    private void showAddDialog() {
        final EditText input = new EditText(requireContext());
        input.setHint("Title");
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        new AlertDialog.Builder(requireContext())
                .setTitle("New Memory")
                .setMessage("Enter a title")
                .setView(input)
                .setPositiveButton("Save", (d, which) -> insertMemory(input.getText().toString().trim()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void insertMemory(String title) {
        if (title.isEmpty()) title = "Untitled";
        // For now, placeholder coords — we’ll fill real location in the Add flow
        final String t = title;
        Executors.newSingleThreadExecutor().execute(() -> {
            Memory m = new Memory();
            m.title = t;
            m.latitude = 0.0;     // TODO: replace with real location soon
            m.longitude = 0.0;
            m.createdAt = System.currentTimeMillis();
            dao.insert(m);
        });
    }
}
