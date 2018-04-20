/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.entities;

import com.mycompany.entities.model.Polygon;

/**
 *
 * @author Sonia
 */
public class SpatialFarm {

    private String id, farm_Name;
    private Polygon polygons;

    public SpatialFarm() {
    }

    public SpatialFarm(String id, String farm_Name, Polygon polygons) {
        this.id = id;
        this.farm_Name = farm_Name;
        this.polygons = polygons;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFarm_Name() {
        return farm_Name;
    }

    public void setFarm_Name(String farm_Name) {
        this.farm_Name = farm_Name;
    }

    public Polygon getPolygons() {
        return polygons;
    }

    public void setPolygons(Polygon polygons) {
        this.polygons = polygons;
    }

    @Override
    public String toString() {
        return ("FarmId: " + id + " ,Farm_Name: " + farm_Name + " ,Polygon: " + polygons.getCoordinates());
    }

}
