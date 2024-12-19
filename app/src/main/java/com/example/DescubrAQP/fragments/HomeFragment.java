package com.example.DescubrAQP.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.DescubrAQP.BuildingRepository;
import com.example.DescubrAQP.LoginActivity;
import com.example.DescubrAQP.R;
import com.example.DescubrAQP.location.LocationHandler;
import com.example.DescubrAQP.network.RouteManager;
import com.example.DescubrAQP.ui.MarkerManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import java.util.ArrayList;
import java.util.Set;

public class HomeFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private static final String TAG = "HomeFragment";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String KEY_ZOOM_LEVEL = "zoom_level";

    private GoogleMap mMap;
    private Button viewUbiButton;
    private Button logoutButton;
    private BuildingRepository buildingRepository;
    private FusedLocationProviderClient fusedLocationClient;

    private ActivityResultLauncher<String> requestPermissionLauncher;
    private LocationHandler locationHandler;
    private MarkerManager markerManager;
    private RouteManager routeManager;

    private float lastZoom = 12.0f;
    private Set<Integer> notifiedBuildings;
    private static final float ZOOM_THRESHOLD = 17.0f;
    private static final float PROXIMITY_THRESHOLD_METERS = 50.0f;

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewUbiButton = view.findViewById(R.id.viewUbi);
        logoutButton = view.findViewById(R.id.logoutButton);

        buildingRepository = new BuildingRepository(getContext());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        locationHandler = new LocationHandler(requireContext(), requireActivity(), fusedLocationClient, buildingRepository, this, PROXIMITY_THRESHOLD_METERS);
        routeManager = new RouteManager(requireContext(), fusedLocationClient, this);
        markerManager = new MarkerManager(getContext(), buildingRepository);

        viewUbiButton.setOnClickListener(v -> {
            if (mMap != null) {
                centerMapOnCurrentLocation();
            }
        });

        logoutButton.setOnClickListener(v -> logout());

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
            }
        }

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Log.d(TAG, "Permiso de notificación concedido");
                    } else {
                        Log.e(TAG, "Permiso de notificación denegado");
                    }
                }
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        if (savedInstanceState != null) {
            lastZoom = savedInstanceState.getFloat(KEY_ZOOM_LEVEL, 12.0f);
            ArrayList<Integer> notified = savedInstanceState.getIntegerArrayList("notifiedBuildings");
            if (notified != null) {
                locationHandler.setNotifiedBuildings(notified);
            }
        }

        locationHandler.startLocationUpdates();
        locationHandler.createNotificationChannel();
    }

    @Override
    public void onResume() {
        super.onResume();
        locationHandler.startLocationUpdates();
    }

    @Override
    public void onPause() {
        super.onPause();
        locationHandler.stopLocationUpdates();
    }

    private void centerMapOnCurrentLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(requireActivity(), location -> {
                        if (location != null) {
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markerManager.getLatLngFromLocation(location), 15));
                        } else {
                            Toast.makeText(getContext(), "No se pudo obtener la ubicación actual.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(getContext(), "Permiso de ubicación denegado.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);

        markerManager.addMarkersToMap(mMap);

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        // Al cambiar el zoom, actualizar íconos
        mMap.setOnCameraIdleListener(() -> {
            float currentZoom = mMap.getCameraPosition().zoom;
            markerManager.updateMarkerIcons(mMap, currentZoom, ZOOM_THRESHOLD);
            lastZoom = currentZoom;
        });

        // Mover la cámara a Arequipa inicialmente
        markerManager.moveCameraToArequipa(mMap, lastZoom);
    }

    @Override
    public boolean onMarkerClick(@NonNull com.google.android.gms.maps.model.Marker marker) {
        return markerManager.onMarkerClick(requireActivity(), mMap, marker, this);
    }

    public void navigateToDetailFragment(int buildingId) {
        DetailFragment detailFragment = DetailFragment.newInstance(buildingId);
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerView, detailFragment)
                .addToBackStack(null)
                .commit();
    }

    private void logout() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPrefs", getActivity().MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("loggedInUser");
        editor.apply();

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    public void setupRouteButton(Double latitudeEnd, Double longitudeEnd) {
        routeManager.setupRouteButton(mMap, latitudeEnd, longitudeEnd);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    if (mMap != null) {
                        mMap.setMyLocationEnabled(true);
                    }
                }
            } else {
                Toast.makeText(getContext(), "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putIntegerArrayList("notifiedBuildings", new ArrayList<>(locationHandler.getNotifiedBuildings()));
        if (mMap != null) {
            outState.putFloat(KEY_ZOOM_LEVEL, mMap.getCameraPosition().zoom);
        }
    }

}
