package com.joshuamemories.nearbymemoriesapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.DateFormat;
import java.util.Calendar;

public class MemoryDetailFragment extends Fragment {

    private static final String ARG_ID = "memory_id";
    public static MemoryDetailFragment newInstance(long id) {
        MemoryDetailFragment f = new MemoryDetailFragment();
        Bundle b = new Bundle();
        b.putLong(ARG_ID, id);
        f.setArguments(b);
        return f;
    }

    private MemoryDao dao;
    private Memory current; // currently loaded memory

    // Pick image from gallery (no permission required via SAF)
    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    // Show it
                    ImageView img = requireView().findViewById(R.id.imgPhoto);
                    img.setImageURI(uri);

                    // Persist URI in model
                    if (current != null) {
                        current.imageUri = uri.toString();
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_memory_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        long id = getArguments() != null ? getArguments().getLong(ARG_ID, -1L) : -1L;
        if (id <= 0) return;

        dao = NearbyDatabase.get(requireContext()).memoryDao();

        final ImageView imgPhoto = v.findViewById(R.id.imgPhoto);
        final TextView tvTitle   = v.findViewById(R.id.tvTitle);
        final TextView tvPlace   = v.findViewById(R.id.tvPlace);
        final TextView tvCoords  = v.findViewById(R.id.tvCoords);
        final TextView tvCreated = v.findViewById(R.id.tvCreated);
        final TextView tvEventDate = v.findViewById(R.id.tvEventDate);
        final EditText etDescription = v.findViewById(R.id.etDescription);
        final Button btnPickPhoto = v.findViewById(R.id.btnPickPhoto);
        final Button btnPickDate  = v.findViewById(R.id.btnPickDate);
        final Button btnSave      = v.findViewById(R.id.btnSave);

        // Load + observe this memory
        dao.observeById(id).observe(getViewLifecycleOwner(), m -> {
            current = m;
            if (m == null) return;

            tvTitle.setText(m.title != null ? m.title : "Untitled");
            tvPlace.setText(m.placeName != null ? m.placeName : "(No place name)");
            tvCoords.setText(String.format("Lat: %.5f, Lon: %.5f", m.latitude, m.longitude));
            tvCreated.setText("Saved: " + DateFormat.getDateTimeInstance().format(m.createdAt));

            if (m.eventDateMs != null) {
                tvEventDate.setText("Event: " + DateFormat.getDateInstance().format(m.eventDateMs));
            } else {
                tvEventDate.setText("(No event date)");
            }

            etDescription.setText(m.description != null ? m.description : "");

            if (m.imageUri != null && !m.imageUri.isEmpty()) {
                imgPhoto.setImageURI(Uri.parse(m.imageUri));
            } else {
                imgPhoto.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        });

        btnPickPhoto.setOnClickListener(v1 -> {
            // Pick only images
            pickImage.launch("image/*");
        });

        btnPickDate.setOnClickListener(v12 -> {
            final Calendar cal = Calendar.getInstance();
            if (current != null && current.eventDateMs != null) {
                cal.setTimeInMillis(current.eventDateMs);
            }
            DatePickerDialog dp = new DatePickerDialog(
                    requireContext(),
                    (DatePicker view1, int year, int month, int day) -> {
                        Calendar picked = Calendar.getInstance();
                        picked.set(Calendar.YEAR, year);
                        picked.set(Calendar.MONTH, month);
                        picked.set(Calendar.DAY_OF_MONTH, day);
                        picked.set(Calendar.HOUR_OF_DAY, 0);
                        picked.set(Calendar.MINUTE, 0);
                        picked.set(Calendar.SECOND, 0);
                        long when = picked.getTimeInMillis();

                        if (current != null) {
                            current.eventDateMs = when;
                            tvEventDate.setText("Event: " + DateFormat.getDateInstance().format(when));
                        }
                    },
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
            );
            dp.show();
        });

        btnSave.setOnClickListener(v13 -> {
            if (current == null) return;

            // Pull latest description from UI
            current.description = etDescription.getText() != null
                    ? etDescription.getText().toString().trim() : null;

            // Persist to DB on background thread
            java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
                dao.update(current);
            });

            // Optionally pop back to previous screen:
            // requireActivity().getSupportFragmentManager().popBackStack();
        });
    }
}
