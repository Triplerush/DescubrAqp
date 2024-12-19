package com.example.DescubrAQP.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.DescubrAQP.dao.building.Building;
import com.example.DescubrAQP.BuildingRepository;
import com.example.DescubrAQP.R;
import com.example.DescubrAQP.fragments.HomeFragment;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import android.location.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.android.gms.maps.CameraUpdateFactory;

public class MarkerManager {

    private final Context context;
    private final BuildingRepository buildingRepository;
    private List<Marker> allMarkers = new ArrayList<>();
    private Map<Integer, Bitmap> markerWithLabelCache = new HashMap<>();
    private Map<Integer, Bitmap> markerWithoutLabelCache = new HashMap<>();

    public MarkerManager(Context context, BuildingRepository buildingRepository) {
        this.context = context;
        this.buildingRepository = buildingRepository;
    }

    public void addMarkersToMap(GoogleMap mMap) {
        List<Building> buildingList = buildingRepository.getBuildingList();
        for (int i = 0; i < buildingList.size(); i++) {
            Building building = buildingList.get(i);
            LatLng position = new LatLng(building.getLatitude(), building.getLongitude());
            Bitmap customMarker = createCustomMarkerWithoutLabel(i);

            Marker marker = mMap.addMarker(new com.google.android.gms.maps.model.MarkerOptions()
                    .position(position)
                    .icon(BitmapDescriptorFactory.fromBitmap(customMarker))
                    .snippet(String.valueOf(i)));
            if (marker != null) {
                marker.setTag(i);
                allMarkers.add(marker);
            }
        }
    }

    public void updateMarkerIcons(GoogleMap mMap, float currentZoom, float zoomThreshold) {
        List<Building> buildingList = buildingRepository.getBuildingList();
        for (Marker marker : allMarkers) {
            if (marker.getTag() instanceof Integer) {
                int buildingId = (Integer) marker.getTag();
                Building building = buildingList.get(buildingId);
                if (currentZoom >= zoomThreshold) {
                    marker.setIcon(BitmapDescriptorFactory.fromBitmap(createCustomMarkerWithLabel(buildingId, building.getTitle())));
                } else {
                    marker.setIcon(BitmapDescriptorFactory.fromBitmap(createCustomMarkerWithoutLabel(buildingId)));
                }
            }
        }
    }

    private Bitmap createCustomMarkerWithoutLabel(int buildingId) {
        if (markerWithoutLabelCache.containsKey(buildingId)) {
            return markerWithoutLabelCache.get(buildingId);
        }

        LayoutInflater inflater = LayoutInflater.from(context);
        View markerView = inflater.inflate(R.layout.custom_marker_without_label, null);

        markerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        markerView.layout(0, 0, markerView.getMeasuredWidth(), markerView.getMeasuredHeight());
        markerView.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(markerView.getMeasuredWidth(), markerView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        markerView.draw(canvas);

        markerWithoutLabelCache.put(buildingId, bitmap);
        return bitmap;
    }

    private Bitmap createCustomMarkerWithLabel(int buildingId, String title) {
        if (markerWithLabelCache.containsKey(buildingId)) {
            return markerWithLabelCache.get(buildingId);
        }

        LayoutInflater inflater = LayoutInflater.from(context);
        View markerView = inflater.inflate(R.layout.custom_marker_with_label, null);

        TextView titleTextView = markerView.findViewById(R.id.marker_title);
        titleTextView.setText(title);

        markerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        markerView.layout(0, 0, markerView.getMeasuredWidth(), markerView.getMeasuredHeight());
        markerView.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(markerView.getMeasuredWidth(), markerView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        markerView.draw(canvas);

        markerWithLabelCache.put(buildingId, bitmap);
        return bitmap;
    }

    public boolean onMarkerClick(Activity activity, GoogleMap mMap, Marker marker, HomeFragment homeFragment) {
        Object tag = marker.getTag();
        if (tag instanceof Integer) {
            int buildingId = (Integer) tag;
            Building building = buildingRepository.getBuildingList().get(buildingId);

            Projection projection = mMap.getProjection();
            Point screenPos = projection.toScreenLocation(marker.getPosition());
            showMarkerPopup(activity, homeFragment, building, screenPos, mMap);
            return true;
        }
        return false;
    }

    private void showMarkerPopup(Activity activity, HomeFragment homeFragment, Building building, Point screenPos, GoogleMap mMap) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View popupView = inflater.inflate(R.layout.popup_marker, null);

        final PopupWindow markerPopup = new PopupWindow(
                popupView,
                550,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        ImageView imageView = popupView.findViewById(R.id.popup_image);
        TextView titleTextView = popupView.findViewById(R.id.popup_title);
        Button irButton = popupView.findViewById(R.id.popup_btn_ir);
        Button detallesButton = popupView.findViewById(R.id.popup_btn_detalles);

        titleTextView.setText(building.getTitle());
        if (Integer.parseInt(building.getImageResId()) != 0) {
            imageView.setImageResource(Integer.parseInt(building.getImageResId()));
        } else {
            imageView.setImageResource(R.drawable.ic_building_placeholder);
        }

        irButton.setOnClickListener(v -> {
            homeFragment.setupRouteButton(building.getLatitude(), building.getLongitude());
            if (markerPopup.isShowing()) {
                markerPopup.dismiss();
            }
        });

        detallesButton.setOnClickListener(v -> {
            homeFragment.navigateToDetailFragment(buildingRepository.getBuildingList().indexOf(building));
            if (markerPopup.isShowing()) {
                markerPopup.dismiss();
            }
        });

        markerPopup.setOutsideTouchable(true);
        markerPopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        markerPopup.showAtLocation(activity.findViewById(android.R.id.content), Gravity.NO_GRAVITY, screenPos.x, screenPos.y - 150);
    }

    public LatLng getLatLngFromLocation(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    public void moveCameraToArequipa(GoogleMap mMap, float zoom) {
        LatLng arequipa = new LatLng(-16.409047, -71.537451);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(arequipa, zoom));
    }
}
