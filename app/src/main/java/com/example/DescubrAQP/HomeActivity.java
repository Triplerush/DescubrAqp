package com.example.DescubrAQP;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.DescubrAQP.fragments.EdificacionesFragment;
import com.example.DescubrAQP.fragments.HomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class HomeActivity extends AppCompatActivity {
    private FragmentManager fragmentManager = null;
    private HomeFragment homeFragment = null;
    private EdificacionesFragment edificacionesFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Configurar la Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Habilitar el botón de retroceso en la Toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back); // Icono personalizado
        }

        // Ajusta los insets de la ventana para que no haya espacio en la parte inferior
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0); // 0 en la parte inferior
            return WindowInsetsCompat.CONSUMED;
        });

        fragmentManager = getSupportFragmentManager();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.menu_home);

        // Configuración de los fragmentos al seleccionar una opción del menú de navegación
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.menu_home) {
                    if (homeFragment == null) {
                        homeFragment = HomeFragment.newInstance("", "");
                    }
                    loadFragment(homeFragment);
                    return true;
                } else if (item.getItemId() == R.id.menu_edificaciones) {
                    if (edificacionesFragment == null) {
                        edificacionesFragment = EdificacionesFragment.newInstance();
                    }
                    loadFragment(edificacionesFragment);
                    return true;
                } else {
                    return false;
                }
            }
        });

        // Cargar el fragmento inicial (Home)
        if (savedInstanceState == null) {
            homeFragment = HomeFragment.newInstance("", "");
            loadFragment(homeFragment);
        }
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainerView, fragment);
        fragmentTransaction.addToBackStack(null); // Añadir a la pila de retroceso
        fragmentTransaction.commit();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Manejar el botón de retroceso
            if (fragmentManager.getBackStackEntryCount() > 0) {
                fragmentManager.popBackStack(); // Regresar al fragmento anterior
            } else {
                finish(); // Salir de la actividad si no hay más fragmentos en la pila
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
