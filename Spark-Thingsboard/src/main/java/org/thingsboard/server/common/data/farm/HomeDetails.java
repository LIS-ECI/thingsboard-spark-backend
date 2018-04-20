package org.thingsboard.server.common.data.farm;

import java.util.List;

public class HomeDetails {
    private String homeMaterial;
    private String floorMaterial;
    private int rooms;
    private String bathroom;
    private String kitchen;
    private int dependingPeople;
    private int workers;
    private List<Person> people;

    public HomeDetails(String homeMaterial, String floorMaterial, int rooms, String bathroom, String kitchen, int dependingPeople, int workers, List<Person> people) {
        this.homeMaterial = homeMaterial;
        this.floorMaterial = floorMaterial;
        this.rooms = rooms;
        this.bathroom = bathroom;
        this.kitchen = kitchen;
        this.dependingPeople = dependingPeople;
        this.workers = workers;
        this.people = people;
    }

    public HomeDetails(){}

    public String getHomeMaterial() {
        return homeMaterial;
    }

    public void setHomeMaterial(String homeMaterial) {
        this.homeMaterial = homeMaterial;
    }

    public String getFloorMaterial() {
        return floorMaterial;
    }

    public void setFloorMaterial(String floorMaterial) {
        this.floorMaterial = floorMaterial;
    }

    public int getRooms() {
        return rooms;
    }

    public void setRooms(int rooms) {
        this.rooms = rooms;
    }

    public String getBathroom() {
        return bathroom;
    }

    public void setBathroom(String bathroom) {
        this.bathroom = bathroom;
    }

    public String getKitchen() {
        return kitchen;
    }

    public void setKitchen(String kitchen) {
        this.kitchen = kitchen;
    }

    public int getDependingPeople() {
        return dependingPeople;
    }

    public void setDependingPeople(int dependingPeople) {
        this.dependingPeople = dependingPeople;
    }

    public int getWorkers() {
        return workers;
    }

    public void setWorkers(int workers) {
        this.workers = workers;
    }

    public List<Person> getPeople() {
        return people;
    }

    public void setPeople(List<Person> people) {
        this.people = people;
    }
}
