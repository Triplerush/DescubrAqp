package com.example.DescubrAQP;

import org.junit.Test;

import static org.junit.Assert.*;

import com.example.DescubrAQP.dao.building.Building;

public class BuildingTest {

    @Test
    public void testBuildingWithValidValues() {
    /*    Building building = new Building(
                "Catedral",
                "2",
                "Santuario principal de la ciudad",
                123,
                -16.3981,
                -71.5364
        );

        // Verificar todos los atributos
        assertEquals("Catedral", building.getTitle());
      //  assertEquals("Iglesia", building.getCategory());
        assertEquals("Santuario principal de la ciudad", building.getDescription());
        assertEquals(123, building.getImageResId());
        assertEquals(-16.3981, building.getLatitude(), 0.0001);
        assertEquals(-71.5364, building.getLongitude(), 0.0001);
    }

    @Test
    public void testBuildingWithEdgeValues() {
        Building building = new Building(
                "",                // Título vacío
                1,                // Categoría vacía
                null,              // Descripción nula
                "2147483647", // Valor máximo para el ID de imagen
                Double.MAX_VALUE,  // Valor máximo para la latitud
                Double.MIN_VALUE   // Valor mínimo para la longitud
        );

        assertEquals("", building.getTitle());
        assertEquals("", building.getCategoryId());
        assertNull(building.getDescription());
        assertEquals(Integer.MAX_VALUE, building.getImageResId());
        assertEquals(Double.MAX_VALUE, building.getLatitude(), 0.0001);
        assertEquals(Double.MIN_VALUE, building.getLongitude(), 0.0001);
    }

    @Test
    public void testBuildingWithInvalidValues() {
        Building building = new Building(
                null,  // Título nulo
                null,  // Categoría nula
                null,  // Descripción nula
                "-1",    // ID de imagen negativo (valor inválido)
                -91.0, // Latitud fuera del rango permitido
                181.0  // Longitud fuera del rango permitido
        );

        assertNull(building.getTitle());
        assertNull(building.getCategoryId());
        assertNull(building.getDescription());
     //   assertTrue("Image ID should not be negative", building.getImageResId() < 0);
        assertTrue("Latitude is out of range", building.getLatitude() < -90 || building.getLatitude() > 90);
        assertTrue("Longitude is out of range", building.getLongitude() < -180 || building.getLongitude() > 180);
    }
}
*/
    }}