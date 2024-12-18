package com.example.lab4_fragments.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.lab4_fragments.R;
import com.example.lab4_fragments.HomeActivity;
import com.example.lab4_fragments.dao.user.User;
import com.example.lab4_fragments.database.AppDatabase;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class LoginFragment extends Fragment {

    public LoginFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);
        checkIfLoggedIn();
        rootView.findViewById(R.id.btnLogin).setOnClickListener(v -> attemptLogin(rootView));
        rootView.findViewById(R.id.btnBack).setOnClickListener(v -> goBackToStart());
        return rootView;
    }

    private void checkIfLoggedIn() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPrefs", getActivity().MODE_PRIVATE);
        String loggedInUser = sharedPreferences.getString("loggedInUser", null);

        if (loggedInUser != null) {
            Intent intent = new Intent(getActivity(), HomeActivity.class);
            startActivity(intent);
            getActivity().finish();
        }
    }

    private void attemptLogin(View rootView) {
        EditText emailEditText = rootView.findViewById(R.id.emailEditText);
        EditText passwordEditText = rootView.findViewById(R.id.passwordEditText);

        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        new Thread(() -> {
            if (validateCredentials(email, password)) {
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPrefs", getActivity().MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("loggedInUser", email);
                editor.apply();

                Intent intent = new Intent(getActivity(), HomeActivity.class);
                startActivity(intent);
                requireActivity().finish();
            }
        }).start();
    }

    private boolean validateCredentials(String email, String password) {
        AppDatabase appDatabase = AppDatabase.getInstance(requireContext());
        User user = appDatabase.userDao().getUserByEmail(email);

        if (user != null && user.getPassword().equals(password)) {
            requireActivity().runOnUiThread(() ->
                    Toast.makeText(getActivity(), "Bienvenido, " + user.getFirstName(), Toast.LENGTH_SHORT).show()
            );
            return true;
        } else {
            requireActivity().runOnUiThread(() ->
                    Toast.makeText(getActivity(), "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
            );
            return false;
        }
    }

    private void goBackToStart() {
        getActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }
}