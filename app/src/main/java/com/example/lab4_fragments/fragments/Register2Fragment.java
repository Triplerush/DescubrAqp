package com.example.lab4_fragments.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import com.example.lab4_fragments.R;
import com.example.lab4_fragments.dao.user.User;
import com.example.lab4_fragments.database.AppDatabase;
import com.example.lab4_fragments.view_models.SharedViewModel;

public class Register2Fragment extends Fragment {

    private SharedViewModel sharedViewModel;
    private EditText emailEditText, passwordEditText, confirmPasswordEditText;
    private AppDatabase appDatabase;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_register2, container, false);

        appDatabase = AppDatabase.getInstance(requireContext());
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        emailEditText = rootView.findViewById(R.id.emailEditText);
        passwordEditText = rootView.findViewById(R.id.passwordEditText);
        confirmPasswordEditText = rootView.findViewById(R.id.confirmPasswordEditText);

        rootView.findViewById(R.id.btnBack).setOnClickListener(v -> goBackToRegister1());

        rootView.findViewById(R.id.btnFinish).setOnClickListener(v -> {
            if (validatePasswords()) {
                saveUserToDatabase(() -> {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getActivity(), "Registro exitoso", Toast.LENGTH_SHORT).show();
                        goBackToStart();
                    });
                });
            }
        });

        return rootView;
    }

    private void goBackToRegister1() {
        requireActivity().getSupportFragmentManager().popBackStack();
    }

    private void goBackToStart() {
        requireActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    private boolean validatePasswords() {
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        if (password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(getActivity(), "Las contraseñas no pueden estar vacías", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(getActivity(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void saveUserToDatabase(Runnable onComplete) {
        new Thread(() -> {
            String firstName = sharedViewModel.getFirstName().getValue();
            String lastName = sharedViewModel.getLastName().getValue();
            String dni = sharedViewModel.getDni().getValue();
            String phone = sharedViewModel.getPhone().getValue();
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            User user = new User(firstName, lastName, dni, phone, email, password);
            appDatabase.userDao().insertUser(user);

            Log.d("Register", "Usuario guardado en la base de datos");

            // Ejecutar el callback al finalizar el hilo
            if (onComplete != null) {
                onComplete.run();
            }
        }).start();
    }
}
