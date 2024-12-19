package com.example.DescubrAQP;

import java.util.List;

import static org.junit.Assert.assertEquals;

import com.example.DescubrAQP.dao.building.Building;

public class BuildingAdapterTest {

    private BuildingAdapter adapter;
    private List<Building> buildings;

/*    @Before
    public void setUp() {
        // Crear lista de edificios
        buildings = Arrays.asList(
                new Building("Catedral", "Iglesia", "Descripción Catedral", 123, -16.3981, -71.5364),
                new Building("Monasterio", "Monasterio", "Descripción Monasterio", 124, -16.3955, -71.5369)
        );

        // Inicializar el adaptador sin necesidad de contexto
        adapter = new BuildingAdapter(buildings, position -> {});
    }

    @Test
    public void testAdapterItemCount() {
        List<Building> buildings = Arrays.asList(
                new Building("Catedral", "Iglesia", "Descripción Catedral", 123, -16.3981, -71.5364),
                new Building("Monasterio", "Monasterio", "Descripción Monasterio", 124, -16.3955, -71.5369)
        );

        BuildingAdapter adapter = new BuildingAdapter(buildings, position -> {});
        assertEquals(2, adapter.getItemCount());
    }

    @Test
    public void testBuildingData() {
        // Verificar que los datos en la lista son correctos
        Building building = buildings.get(0);

        assertEquals("Catedral", building.getTitle());
        assertEquals("Iglesia", building.getCategory());
        assertEquals("Descripción Catedral", building.getDescription());
        assertEquals(123, building.getImageResId());
        assertEquals(-16.3981, building.getLatitude(), 0.0001);
        assertEquals(-71.5364, building.getLongitude(), 0.0001);
    }
}

 */
}