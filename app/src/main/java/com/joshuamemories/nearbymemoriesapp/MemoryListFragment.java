package com.joshuamemories.nearbymemoriesapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.concurrent.Executors;

public class MemoryListFragment extends Fragment {

    private MemoryDao dao;
    private MemoryAdapter adapter;
    private TextView tvEmpty;

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
            // open details when a row is tapped
            MemoryDetailFragment f = MemoryDetailFragment.newInstance(memory.id);
            ((MainActivity) requireActivity()).showFragment(f);
        });
        rv.setAdapter(adapter);

        tvEmpty = v.findViewById(R.id.tvEmpty);
        Button btnAdd = v.findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(view -> showAddDialog());

        LiveData<List<Memory>> live = dao.getAll();
        live.observe(getViewLifecycleOwner(), list -> {
            adapter.submitList(list);
            tvEmpty.setVisibility((list == null || list.isEmpty()) ? View.VISIBLE : View.GONE);

            // Optional: auto-seed a few items the very first time if empty
            if (list == null || list.isEmpty()) seedSamplesOnce();
        });
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
        final String t = title;

        // For now, use safe non-zero coords. Map “Add Here” saves real coords anyway.
        final double lat = 18.1096;   // Jamaica-ish
        final double lon = -77.2975;

        Executors.newSingleThreadExecutor().execute(() -> {
            Memory m = new Memory();
            m.title = t;
            m.latitude = lat;
            m.longitude = lon;
            m.createdAt = System.currentTimeMillis();
            dao.insert(m);
        });
    }

    private void seedSamplesOnce() {
        // Drop a few recognizable pins so the list isn't empty
        Executors.newSingleThreadExecutor().execute(() -> {
            // avoid double seeding if user quickly navigates
            if (dao.getAll().getValue() != null && !dao.getAll().getValue().isEmpty()) return;

            Memory a = new Memory();
            a.title = "Downtown Kingston";
            a.latitude = 17.9970; a.longitude = -76.7936;
            a.createdAt = System.currentTimeMillis();

            Memory b = new Memory();
            b.title = "Montego Bay";
            b.latitude = 18.4762; b.longitude = -77.8939;
            b.createdAt = System.currentTimeMillis();

            Memory c = new Memory();
            c.title = "Ocho Rios";
            c.latitude = 18.4076; c.longitude = -77.1031;
            c.createdAt = System.currentTimeMillis();

            dao.insert(a);
            dao.insert(b);
            dao.insert(c);
        });
    }
}
