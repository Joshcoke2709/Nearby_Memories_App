package com.joshuamemories.nearbymemoriesapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.concurrent.Executors;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap gmap;
    private TextView tvStatus, tvCoords;
    private Button btnLocate, btnAddHere;

    private FusedLocationProviderClient fusedClient;
    private MemoryDao dao;

    private LatLng lastFix; // remember last known location for "Add Here"

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
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        tvStatus = v.findViewById(R.id.tvStatus);
        tvCoords = v.findViewById(R.id.tvCoords);
        btnLocate = v.findViewById(R.id.btnLocate);
        btnAddHere = v.findViewById(R.id.btnAddHere);

        fusedClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        dao = NearbyDatabase.get(requireContext()).memoryDao();

        // Attach SupportMapFragment into our map_container
        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map_container);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.map_container, mapFragment)
                    .commitNow();
        }
        mapFragment.getMapAsync(this);

        btnLocate.setOnClickListener(v1 -> ensurePermissionThenLocate());
        btnAddHere.setOnClickListener(v12 -> promptAndSaveAtLastFix());
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

        fusedClient.getLastLocation()
                .addOnSuccessListener(loc -> {
                    if (loc != null) {
                        onNewFix(loc);
                    } else {
                        fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                                .addOnSuccessListener(this::onNewFix)
                                .addOnFailureListener(e -> tvStatus.setText("Failed: " + e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> tvStatus.setText("Failed: " + e.getMessage()));
    }

    private void onNewFix(@Nullable Location loc) {
        if (loc == null) {
            tvStatus.setText("Could not get a current fix.");
            return;
        }
        lastFix = new LatLng(loc.getLatitude(), loc.getLongitude());
        tvCoords.setText(String.format("Latitude: %.6f   Longitude: %.6f",
                lastFix.latitude, lastFix.longitude));
        tvStatus.setText("Location acquired ✅");

        if (gmap != null) {
            gmap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastFix, 16f));
        }
    }

    private void promptAndSaveAtLastFix() {
        if (lastFix == null) {
            tvStatus.setText("Get your location first, then add.");
            return;
        }
        final EditText input = new EditText(requireContext());
        input.setHint("Memory title");
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("New Memory")
                .setMessage("Enter a title for this location")
                .setView(input)
                .setPositiveButton("Save", (d, w) -> saveMemory(input.getText().toString().trim()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveMemory(String title) {
        if (title.isEmpty()) title = "Untitled";
        final String t = title;
        final double lat = lastFix.latitude;
        final double lon = lastFix.longitude;

        Executors.newSingleThreadExecutor().execute(() -> {
            Memory m = new Memory();
            m.title = t;
            m.latitude = lat;
            m.longitude = lon;
            m.createdAt = System.currentTimeMillis();
            dao.insert(m);
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gmap = googleMap;

        // Interactive controls
        gmap.getUiSettings().setZoomControlsEnabled(true);
        gmap.getUiSettings().setZoomGesturesEnabled(true);
        gmap.getUiSettings().setScrollGesturesEnabled(true);
        gmap.getUiSettings().setRotateGesturesEnabled(true);
        gmap.getUiSettings().setTiltGesturesEnabled(true);

        // My Location (blue dot) if permission granted
        boolean fine = ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean coarse = ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (fine || coarse) gmap.setMyLocationEnabled(true);

        // Show existing memories as red pins
        dao.getAll().observe(getViewLifecycleOwner(), this::renderMarkers);
    }

    private void renderMarkers(@Nullable List<Memory> memories) {
        if (gmap == null) return;

        gmap.clear();

        if (memories == null || memories.isEmpty()) {
            // Default: Jamaica view
            LatLng jamaica = new LatLng(18.1096, -77.2975);
            gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(jamaica, 7f));
            return;
        }

        LatLngBounds.Builder bounds = LatLngBounds.builder();
        for (Memory m : memories) {
            LatLng pos = new LatLng(m.latitude, m.longitude);
            gmap.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(m.title)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            bounds.include(pos);
        }
        gmap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 80));
    }
}
