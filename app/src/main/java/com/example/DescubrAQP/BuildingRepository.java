package com.example.DescubrAQP;

import android.content.Context;
import android.util.Log;

import com.example.DescubrAQP.dao.building.Building;
import com.example.DescubrAQP.dao.categoria.Categoria;
import com.example.DescubrAQP.database.AppDatabase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Clase para cargar y manejar la lista de edificaciones.
 */
public class BuildingRepository {
    private AppDatabase appDatabase;
    private ExecutorService executorService;
    private List<Building> buildingList = new ArrayList<>();

    public BuildingRepository(Context context) {
        this.appDatabase= AppDatabase.getInstance(context);
        executorService = Executors.newSingleThreadExecutor();
    }
    public void initializeBuildings(Context context) {

        executorService.execute(() -> {
            try {
                // Verificar si la base de datos ya tiene registros
                int count = appDatabase.buildingDao().getBuildingsCount();
                Log.d("BuildingRepository", "Número de edificios en la base de datos: " + count);

                if (count == 0) {
                    Log.d("BuildingRepository", "La base de datos está vacía. Cargando edificios desde el archivo...");
                    loadBuildings(context);
                    Log.d("BuildingRepository", "Datos cargados correctamente desde el archivo edificaciones.txt.");
                } else {
                    Log.d("BuildingRepository", "La base de datos ya contiene datos. No se cargará el archivo.");
                }
            } catch (Exception e) {
                Log.e("BuildingRepository", "Error al inicializar los edificios: " + e.getMessage(), e);
            }
        });
    }
    /**
     * Inserta una edificación en la base de datos.
     */
    public void insertBuilding(final Building edificacion) {
        Log.d("BuildingRepository", "Iniciando la inserción de la edificación: " + edificacion.getTitle());

        executorService.execute(() -> {
            try {
                appDatabase.buildingDao().insertBuilding(edificacion);
                Log.d("BuildingRepository", "Edificación insertada exitosamente en la base de datos: " + edificacion.getTitle());
            } catch (Exception e) {
                Log.e("BuildingRepository", "Error al insertar la edificación: " + edificacion.getTitle() + ". " + e.getMessage(), e);
            }
        });
    }
    private int getCategoryIdByName(String categoryName) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Integer> future = executorService.submit(() -> appDatabase.categoriaDao().getCategoryIdByName(categoryName));

        try {
            return future.get(); // Espera el resultado de la tarea y lo devuelve
        } catch (Exception e) {
            Log.e("AppDatabase", "Error al obtener el ID de la categoría: " + e.getMessage(), e);
            return -1; // Retorna -1 en caso de error
        } finally {
            executorService.shutdown(); // Asegura que el ExecutorService se cierre
        }
    }
    //Cargar datos desde el archivo 'edificaciones.txt' y  agregar las edificaciones a la base de datos.
    private void loadBuildings(Context context) {
        // Cargar categorías antes de las edificaciones
        loadCategories(context);
        try {
            Log.d("BuildingRepository", "Iniciando la carga de edificios desde el archivo edificaciones.txt...");
            InputStream is = context.getAssets().open("edificaciones.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                Log.d("BuildingRepository1",parts[0]);
                if (parts.length == 6) {

                    Log.d("BuildingRepository1","hola");
                    String title = parts[0];
                    String category = parts[1];
                    String description = parts[2];
                    String imageName = parts[3];
                    double latitude = Double.parseDouble(parts[4]);
                    double longitude = Double.parseDouble(parts[5]);
                    // Obtener el ID de la categoría (puedes agregar lógica para manejar categorías)
                    Log.d("BuildingRepository1","hola2");
                    int categoryId = getCategoryIdByName(category);
                    Log.d("BuildingRepository1","EL ID DE MI CATEGORIA ES" + categoryId);
                    int imageResId = context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
                    Log.d("BuildingRepository1","hola4");
                    // Crear objeto Edificacion
                    Building edificacion = new Building();
                    edificacion.setTitle(title);
                    edificacion.setCategoryId(categoryId);
                    edificacion.setDescription(description);
                    edificacion.setImageResId(
                            String.valueOf(context.getResources().getIdentifier(imageName, "drawable", context.getPackageName()))
                    );
                    edificacion.setLatitude(latitude);
                    edificacion.setLongitude(longitude);

                    Log.d("BuildingRepository","Se creo edificacion");
                    // Insertar edificación en la base de datos
                    insertBuilding(edificacion);
                    Log.d("BuildingRepository","Edificación insertada en la base de datos: " + title);
                }

            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Agregar método para cargar categorías desde el archivo
    private void loadCategories(Context context) {
        try {
            Log.d("BuildingRepository123", "Iniciando la carga de categorías desde el archivo edificaciones.txt...");
            InputStream is = context.getAssets().open("edificaciones.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            Set<String> uniqueCategories = new HashSet<>(); // Usamos un Set para evitar duplicados

            // Leer líneas y extraer las categorías
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 2) { // Verificar que la línea tenga al menos dos partes
                    String category = parts[1];
                    uniqueCategories.add(category); // Agregar la categoría al Set
                }
            }
            reader.close();

            // Insertar las categorías en la base de datos
            for (String category : uniqueCategories) {
                Categoria categoria = new Categoria();
                categoria.setCategoryName(category);
                insertCategoria(categoria);
                Log.d("BuildingRepository123", "Categoría insertada: " + category);
            }

            Log.d("BuildingRepository", "Carga de categorías completada.");
        } catch (IOException e) {
            Log.e("BuildingRepository", "Error al cargar categorías desde el archivo: " + e.getMessage(), e);
        }
    }

    // Método para insertar una categoría en la base de datos
    private void insertCategoria(Categoria categoria) {
        executorService.execute(() -> {
            try {
                appDatabase.categoriaDao().insertCategoria(categoria);
                Log.d("BuildingRepository", "Categoría insertada exitosamente en la base de datos: " + categoria.getCategoryName());
            } catch (Exception e) {
                Log.e("BuildingRepository", "Error al insertar la categoría: " + categoria.getCategoryName() + ". " + e.getMessage(), e);
            }
        });
    }
    public List<Building> getBuildingList() {
        try {
            Future<List<Building>> future = executorService.submit(() -> {
                Log.d("BuildingRepository", "Consultando edificios en un hilo en segundo plano...");
                Log.d("BuildingRepository", String.valueOf(appDatabase.buildingDao().getAllBuildings().size()));
                return appDatabase.buildingDao().getAllBuildings();
            });
            return future.get(); // Espera a que se complete la operación y devuelve el resultado
        } catch (ExecutionException | InterruptedException e) {
            Log.e("BuildingRepository", "Error al consultar edificios desde la base de datos: " + e.getMessage(), e);
            return new ArrayList<>(); // Retorna una lista vacía en caso de error
        }
    }
    public Future<List<Categoria>> getAllCategorias() {
        return executorService.submit(() -> {
            Log.d("BuildingRepository", "Consultando categorías en un hilo en segundo plano...");
            return appDatabase.categoriaDao().getAllCategorias();
        });
    }

}
