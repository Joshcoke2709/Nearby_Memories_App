package com.joshuamemories.nearbymemoriesapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.Task;
import com.joshuamemories.nearbymemoriesapp.R;

import android.widget.Button;
import android.widget.TextView;

public class MapFragment extends Fragment {

    private FusedLocationProviderClient fusedClient;
    private TextView tvStatus, tvCoords;
    private Button btnLocate;

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean fine = Boolean.TRUE.equals(result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false));
                boolean coarse = Boolean.TRUE.equals(result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false));
                if (fine || coarse) {
                    getLocation();
                } else {
                    tvStatus.setText("Location permission denied.");
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        tvStatus = view.findViewById(R.id.tvStatus);
        tvCoords = view.findViewById(R.id.tvCoords);
        btnLocate = view.findViewById(R.id.btnLocate);

        fusedClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        btnLocate.setOnClickListener(v -> ensurePermissionThenLocate());
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvStatus = view.findViewById(R.id.tvStatus);
        tvCoords = view.findViewById(R.id.tvCoords);
        Button btnLocate = view.findViewById(R.id.btnLocate);

        fusedClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        btnLocate.setOnClickListener(v -> ensurePermissionThenLocate());
    }
    private void ensurePermissionThenLocate() {
        boolean fineGranted = ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean coarseGranted = ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (fineGranted || coarseGranted) {
            getLocation();
        } else {
            permissionLauncher.launch(new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    @SuppressLint("MissingPermission")
    private void getLocation() {
        tvStatus.setText("Getting location…");

        // Try last known location first (fast)
        Task<Location> lastTask = fusedClient.getLastLocation();
        lastTask.addOnSuccessListener(location -> {
            if (location != null) {
                showLocation(location);
            } else {
                // Request a fresh update if last known is null
                requestSingleUpdate();
            }
        }).addOnFailureListener(e -> tvStatus.setText("Failed: " + e.getMessage()));
    }

    @SuppressLint("MissingPermission")
    private void requestSingleUpdate() {
        LocationRequest req = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, /*intervalMillis*/ 0
        )
                .setMaxUpdates(1)
                .build();

        fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) showLocation(location);
                    else tvStatus.setText("Could not get a current fix.");
                })
                .addOnFailureListener(e -> tvStatus.setText("Failed: " + e.getMessage()));
    }

    private void showLocation(@NonNull Location loc) {
        String text = String.format("Latitude: %.6f  Longitude: %.6f", loc.getLatitude(), loc.getLongitude());
        tvCoords.setText(text);
        tvStatus.setText("Location acquired");
    }
}
