package com.example.DescubrAQP.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.DescubrAQP.R;
import com.example.DescubrAQP.view_models.SharedViewModel;

public class Register1Fragment extends Fragment {

    private SharedViewModel sharedViewModel;
    private EditText firstNameEditText, lastNameEditText, dniEditText, phoneEditText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_register1, container, false);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        firstNameEditText = rootView.findViewById(R.id.firstNameEditText);
        lastNameEditText = rootView.findViewById(R.id.lastNameEditText);
        dniEditText = rootView.findViewById(R.id.dniEditText);
        phoneEditText = rootView.findViewById(R.id.phoneEditText);

        rootView.findViewById(R.id.btnNext).setOnClickListener(v -> {
            sharedViewModel.setFirstName(firstNameEditText.getText().toString());
            sharedViewModel.setLastName(lastNameEditText.getText().toString());
            sharedViewModel.setDni(dniEditText.getText().toString());
            sharedViewModel.setPhone(phoneEditText.getText().toString());

            goToRegister2();
        });

        rootView.findViewById(R.id.btnBack).setOnClickListener(v -> goBackToStart());

        return rootView;
    }

    private void goToRegister2() {
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerView, new Register2Fragment())
                .addToBackStack(null)
                .commit();
    }

    private void goBackToStart() {
        getActivity().getSupportFragmentManager().popBackStack();
    }
}

