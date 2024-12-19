package com.example.DescubrAQP.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Geometry {
    @SerializedName("coordinates")
    private List<List<Double>> coordinates;

    public List<List<Double>> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<List<Double>> coordinates) {
        this.coordinates = coordinates;
    }
}
