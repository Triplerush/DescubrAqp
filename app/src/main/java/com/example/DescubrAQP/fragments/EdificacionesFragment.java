package com.example.DescubrAQP.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.DescubrAQP.BuildingAdapter;
import com.example.DescubrAQP.data.dao.building.Building;
import com.example.DescubrAQP.data.repositories.BuildingRepository;
import com.example.DescubrAQP.R;
import com.example.DescubrAQP.data.dao.categoria.Categoria;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Fragmento que muestra la lista de edificaciones.
 */
public class EdificacionesFragment extends Fragment {
    private RecyclerView recyclerView;
    private BuildingAdapter buildingAdapter;
    private List<Building> buildingList;           // Lista original
    private List<Building> filteredBuildingList;   // Lista filtrada

    private EditText searchBar;
    private RadioGroup filterOptions;
    private TextView emptyView;
    private BuildingRepository buildingRepository;

    public static EdificacionesFragment newInstance() {
        return new EdificacionesFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edificaciones, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        searchBar = view.findViewById(R.id.search_bar);
        filterOptions = view.findViewById(R.id.filter_options);
        emptyView = view.findViewById(R.id.empty_view);

        buildingList = new ArrayList<>();
        filteredBuildingList = new ArrayList<>();

        // Inicializar BuildingRepository y cargar la lista de edificaciones
        buildingRepository = new BuildingRepository(getContext());
        buildingList = buildingRepository.getBuildingList();
        filteredBuildingList.addAll(buildingList);

        new Thread(() -> {
            try {
                Future<List<Categoria>> futureCategorias = buildingRepository.getAllCategorias();
                List<Categoria> categorias = futureCategorias.get();

                synchronized (cachedCategorias) {
                    cachedCategorias.clear();
                    cachedCategorias.addAll(categorias);
                }

                Log.d("Categoria", "Categorías precargadas: " + cachedCategorias.size());
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        // Inicializar el adaptador con la lista filtrada
        buildingAdapter = new BuildingAdapter(getContext(),filteredBuildingList, position -> {
            // Manejar el clic en el elemento
            Building selectedBuilding = filteredBuildingList.get(position);
            DetailFragment detailFragment = DetailFragment.newInstance(selectedBuilding.getBuildingId());
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragmentContainerView, detailFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });
        recyclerView.setAdapter(buildingAdapter);

        // Configurar el listener para el cambio de texto en la barra de búsqueda
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No se necesita implementar
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No se necesita implementar
            }
        });

        // Configurar el listener para el cambio de opción en el RadioGroup
        filterOptions.setOnCheckedChangeListener((group, checkedId) -> {
            // Reiniciar el filtrado cada vez que se cambia la opción
            filter(searchBar.getText().toString());
        });

        // Verificar si la lista está vacía al inicio
        updateEmptyView();

        return view;
    }

    private void filter(String text) {
        String lowerText = text.toLowerCase();
        filteredBuildingList.clear();

        int checkedId = filterOptions.getCheckedRadioButtonId();

        for (Building building : buildingList) {
            boolean matches = false;

            if (checkedId == R.id.rb_title) {
                matches = building.getTitle().toLowerCase().contains(lowerText);
            } else if (checkedId == R.id.rb_category) {
                Integer categoryId=building.getCategoryId();
                String categoria = getCategoryNameById(categoryId);
                Log.d("EDIFICACIONESFRAGMENT",categoria);
                matches = categoria.toLowerCase().contains(lowerText);
            } else if (checkedId == R.id.rb_description) {
                matches = building.getDescription().toLowerCase().contains(lowerText);
            }

            if (matches) {
                filteredBuildingList.add(building);
            }
        }

        buildingAdapter.notifyDataSetChanged();

        // Actualizar la vista vacía
        updateEmptyView();
    }
    private List<Categoria> cachedCategorias = new ArrayList<>();

    public String getCategoryNameById(int categoryId) {
        synchronized (cachedCategorias) {
            for (Categoria categoria : cachedCategorias) {
                if (categoria.getCategoryId() == categoryId) {
                    return categoria.getCategoryName();
                }
            }
        }
        return ""; // Retorna cadena vacía si no encuentra coincidencias
    }

    private void updateEmptyView() {
        if (filteredBuildingList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }
}
