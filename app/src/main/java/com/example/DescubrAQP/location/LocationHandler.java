package com.example.DescubrAQP.location;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import com.example.DescubrAQP.dao.building.Building;
import com.example.DescubrAQP.BuildingRepository;
import com.example.DescubrAQP.HomeActivity;
import com.example.DescubrAQP.R;
import com.example.DescubrAQP.fragments.HomeFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import android.content.Intent;
import android.app.PendingIntent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LocationHandler {

    private static final String TAG = "LocationHandler";
    private static final String CHANNEL_ID = "proximity_channel";
    private static final String CHANNEL_NAME = "Notificaciones de Proximidad";
    private static final String CHANNEL_DESC = "Recibe notificaciones cuando te acerques a una edificación específica.";
    private static final int NOTIFICATION_ID_BASE = 1000;

    private final Context context;
    private final Activity activity;
    private final FusedLocationProviderClient fusedLocationClient;
    private final BuildingRepository buildingRepository;
    private final HomeFragment homeFragment;
    private final float proximityThreshold;

    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Set<Integer> notifiedBuildings = new HashSet<>();

    public LocationHandler(Context context, Activity activity, FusedLocationProviderClient fusedLocationClient,
                           BuildingRepository buildingRepository, HomeFragment homeFragment, float proximityThreshold) {
        this.context = context;
        this.activity = activity;
        this.fusedLocationClient = fusedLocationClient;
        this.buildingRepository = buildingRepository;
        this.homeFragment = homeFragment;
        this.proximityThreshold = proximityThreshold;

        initLocationRequest();
        initLocationCallback();
    }

    private void initLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void initLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    handleNewLocation(location);
                }
            }
        };
    }

    @SuppressLint("MissingPermission")
    public void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    public void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void handleNewLocation(Location userLocation) {
        List<Building> buildingList = buildingRepository.getBuildingList();
        for (int i = 0; i < buildingList.size(); i++) {
            Building building = buildingList.get(i);
            float[] results = new float[1];
            Location.distanceBetween(
                    userLocation.getLatitude(),
                    userLocation.getLongitude(),
                    building.getLatitude(),
                    building.getLongitude(),
                    results
            );
            float distanceInMeters = results[0];

            if (distanceInMeters <= proximityThreshold && !notifiedBuildings.contains(i)) {
                String logMessage = "El usuario se ha acercado a la edificación: " + building.getTitle() + " (" + distanceInMeters + " metros)";
                Log.d(TAG, logMessage);
                notifiedBuildings.add(i);

                sendProximityNotification(i, building, distanceInMeters);
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void sendProximityNotification(int buildingId, Building building, float distance) {
        Intent intent = new Intent(context, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("buildingId", buildingId);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, buildingId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Te has acercado a una edificación")
                .setContentText(building.getTitle() + " está a " + String.format("%.2f", distance) + " metros de ti.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        int notificationId = NOTIFICATION_ID_BASE + buildingId;
        notificationManager.notify(notificationId, builder.build());
        Log.d(TAG, "Notificación enviada para la edificación: " + building.getTitle());
    }

    public void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESC);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                Log.d(TAG, "Canal de notificación creado");
            } else {
                Log.e(TAG, "No se pudo obtener NotificationManager");
            }
        }
    }

    public Set<Integer> getNotifiedBuildings() {
        return notifiedBuildings;
    }

    public void setNotifiedBuildings(ArrayList<Integer> notified) {
        notifiedBuildings.clear();
        notifiedBuildings.addAll(notified);
    }

}
