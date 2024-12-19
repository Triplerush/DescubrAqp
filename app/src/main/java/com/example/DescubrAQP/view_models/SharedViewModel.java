package com.example.DescubrAQP.view_models;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.DescubrAQP.dao.building.Building;

import java.util.List;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<String> firstName = new MutableLiveData<>();
    private final MutableLiveData<String> lastName = new MutableLiveData<>();
    private final MutableLiveData<String> dni = new MutableLiveData<>();
    private final MutableLiveData<String> phone = new MutableLiveData<>();
    private final MutableLiveData<String> email = new MutableLiveData<>();
    private final MutableLiveData<String> password = new MutableLiveData<>();

    // Lista de edificios
    private final MutableLiveData<List<Building>> buildingListLiveData = new MutableLiveData<>();

    public void setFirstName(String firstName) { this.firstName.setValue(firstName); }
    public void setLastName(String lastName) { this.lastName.setValue(lastName); }
    public void setDni(String dni) { this.dni.setValue(dni); }
    public void setPhone(String phone) { this.phone.setValue(phone); }
    public void setEmail(String email) { this.email.setValue(email); }
    public void setPassword(String password) { this.password.setValue(password); }

    public LiveData<String> getFirstName() { return firstName; }
    public LiveData<String> getLastName() { return lastName; }
    public LiveData<String> getDni() { return dni; }
    public LiveData<String> getPhone() { return phone; }
    public LiveData<String> getEmail() { return email; }
    public LiveData<String> getPassword() { return password; }

    // Métodos para la lista de edificios
    public void setBuildingList(List<Building> buildingList) { this.buildingListLiveData.setValue(buildingList); }
    public LiveData<List<Building>> getBuildingList() { return buildingListLiveData; }
}

