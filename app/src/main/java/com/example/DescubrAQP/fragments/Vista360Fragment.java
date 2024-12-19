package com.example.DescubrAQP.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.DescubrAQP.R;
import com.panoramagl.PLImage;
import com.panoramagl.PLManager;
import com.panoramagl.PLSphericalPanorama;
import com.panoramagl.utils.PLUtils;

public class Vista360Fragment extends Fragment {

    private FrameLayout flView;
    private PLManager plManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vista360, container, false);
        flView = view.findViewById(R.id.fl_view);
        initialisePlManager();

        PLSphericalPanorama panorama = new PLSphericalPanorama();
        panorama.getCamera().lookAt(30.0f, 90.0f);
        panorama.getCamera().setRotationSensitivity(14.0f);
        panorama.setImage(new PLImage(PLUtils.getBitmap(getContext(), R.raw.catedral)));

        plManager.setPanorama(panorama);

        // Agregar el OnTouchListener al FrameLayout
        flView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return plManager.onTouchEvent(event);
            }
        });

        return view;
    }

    private void initialisePlManager() {
        plManager = new PLManager(getContext());
        plManager.setContentView(flView);
        plManager.onCreate();

        plManager.setAccelerometerInterval(1);
        plManager.setInertiaInterval(1);
        plManager.setScrollingEnabled(true);
        plManager.setAccelerometerEnabled(false);
        plManager.setZoomEnabled(true);
        plManager.setInertiaEnabled(true);
        plManager.setAcceleratedTouchScrollingEnabled(true);
        plManager.setMinDistanceToEnableScrolling(1);
        plManager.setMinDistanceToEnableDrawing(1);
    }

    @Override
    public void onResume() {
        super.onResume();
        plManager.onResume();
    }

    @Override
    public void onPause() {
        plManager.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        plManager.onDestroy();
        super.onDestroy();
    }
}