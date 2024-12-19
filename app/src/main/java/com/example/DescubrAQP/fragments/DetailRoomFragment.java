package com.example.DescubrAQP.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.DescubrAQP.R;

public class DetailRoomFragment extends Fragment {

    private static final String ARG_TITLE = "title";
    private static final String ARG_IMAGE_RES = "image_res";
    private static final String ARG_DESCRIPTION = "description";

    public static DetailRoomFragment newInstance(String title, int imageRes, String description) {
        DetailRoomFragment fragment = new DetailRoomFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putInt(ARG_IMAGE_RES, imageRes);
        args.putString(ARG_DESCRIPTION, description);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail_room, container, false);

        TextView titleTextView = view.findViewById(R.id.title_text_view);
        ImageView imageView = view.findViewById(R.id.image_view);
        TextView descriptionTextView = view.findViewById(R.id.description_text_view);
        ImageButton closeButton = view.findViewById(R.id.close_button);

        if (getArguments() != null) {
            String title = getArguments().getString(ARG_TITLE);
            int imageRes = getArguments().getInt(ARG_IMAGE_RES);
            String description = getArguments().getString(ARG_DESCRIPTION);

            titleTextView.setText(title);
            imageView.setImageResource(imageRes);
            descriptionTextView.setText(description);
        }

        closeButton.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        return view;
    }
}
