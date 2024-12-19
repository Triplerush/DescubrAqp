package com.example.DescubrAQP.network;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.DescubrAQP.fragments.HomeFragment;
import com.example.DescubrAQP.models.RouteResponse;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RouteManager {
    private static final String ROUTE_API_KEY = "5b3ce3597851110001cf62485dfe553cb7f64ce7947641a29d521509";
    private static final String TAG = "RouteManager";

    private Retrofit retrofit;
    private ApiService apiService;
    private Context context;
    private FusedLocationProviderClient fusedLocationClient;
    private HomeFragment homeFragment;
    private com.google.android.gms.maps.model.Polyline currentRoutePolyline = null;

    private String routeStart = "";
    private String routeEnd = "";

    public RouteManager(Context context, FusedLocationProviderClient fusedLocationClient, HomeFragment homeFragment) {
        this.context = context;
        this.fusedLocationClient = fusedLocationClient;
        this.homeFragment = homeFragment;

        retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openrouteservice.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);
    }

    public void setupRouteButton(GoogleMap mMap, Double latitudeEnd, Double longitudeEnd) {
        routeStart = "";
        routeEnd = longitudeEnd + "," + latitudeEnd;
        if (currentRoutePolyline != null) {
            currentRoutePolyline.remove();
            currentRoutePolyline = null;
        }

        getCurrentCoordinates((latitude, longitude) -> {
            routeStart = longitude + "," + latitude;
            LatLng startLatLng = new LatLng(latitude, longitude);
            mMap.addMarker(new MarkerOptions().position(startLatLng).title("Origen"));
            createRoute(mMap);
        });
    }

    private void createRoute(GoogleMap mMap) {
        if (routeStart.isEmpty() || routeEnd.isEmpty()) {
            Toast.makeText(context, "Selecciona ambos puntos: origen y destino.", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(context, "Calculando ruta...", Toast.LENGTH_SHORT).show();
        Call<RouteResponse> call = apiService.getRoute(ROUTE_API_KEY, routeStart, routeEnd);
        call.enqueue(new Callback<RouteResponse>() {
            @Override
            public void onResponse(Call<RouteResponse> call, Response<RouteResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    drawRoute(mMap, response.body());
                } else {
                    Log.e(TAG, "Error en la respuesta de la API: " + response.code());
                    Toast.makeText(context, "Error al obtener la ruta.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RouteResponse> call, Throwable t) {
                Log.e(TAG, "Fallo en la llamada a la API: " + t.getMessage());
                Toast.makeText(context, "Fallo al conectar con el servicio de rutas.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void drawRoute(GoogleMap mMap, RouteResponse routeResponse) {
        if (routeResponse.getFeatures().isEmpty()) {
            Toast.makeText(context, "No se encontró una ruta válida.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<List<Double>> coordinates = routeResponse.getFeatures().get(0).getGeometry().getCoordinates();
        PolylineOptions polylineOptions = new PolylineOptions();

        for (List<Double> coordinate : coordinates) {
            if (coordinate.size() >= 2) {
                double longitude = coordinate.get(0);
                double latitude = coordinate.get(1);
                polylineOptions.add(new LatLng(latitude, longitude));
            }
        }

        polylineOptions.color(Color.parseColor("#D32F2F"));
        polylineOptions.width(10);

        if (((FragmentActivity)context) != null) {
            ((FragmentActivity)context).runOnUiThread(() -> {
                currentRoutePolyline = mMap.addPolyline(polylineOptions);
                adjustCameraView(mMap, polylineOptions);
            });
        }
    }

    private void adjustCameraView(GoogleMap mMap, PolylineOptions polylineOptions) {
        List<LatLng> points = polylineOptions.getPoints();
        if (points.isEmpty()) return;

        double minLat = points.get(0).latitude;
        double maxLat = points.get(0).latitude;
        double minLng = points.get(0).longitude;
        double maxLng = points.get(0).longitude;

        for (LatLng point : points) {
            if (point.latitude < minLat) minLat = point.latitude;
            if (point.latitude > maxLat) maxLat = point.latitude;
            if (point.longitude < minLng) minLng = point.longitude;
            if (point.longitude > maxLng) maxLng = point.longitude;
        }

        LatLng southwest = new LatLng(minLat, minLng);
        LatLng northeast = new LatLng(maxLat, maxLng);

        LatLngBounds bounds = new LatLngBounds(southwest, northeast);
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
    }

    private void getCurrentCoordinates(CurrentCoordinatesCallback callback) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener((FragmentActivity)context, (OnSuccessListener<Location>) location -> {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            callback.onCoordinatesObtained(latitude, longitude);
                        } else {
                            Toast.makeText(context, "No se pudo obtener la ubicación actual.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(context, "Permiso de ubicación denegado.", Toast.LENGTH_SHORT).show();
        }
    }

    public interface CurrentCoordinatesCallback {
        void onCoordinatesObtained(double latitude, double longitude);
    }
}
