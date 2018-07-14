/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.entities;

import com.mycompany.entities.model.Polygon;
import java.io.Serializable;

/**
 *
 * @author Sonia
 */
public class SpatialLandlot implements Serializable{

    private String id, landlot_Farm_FK;
    private Polygon polygons;

    public SpatialLandlot() {
    }

    public SpatialLandlot(String id, String landlot_Farm_FK, Polygon polygons) {
        this.id = id;
        this.landlot_Farm_FK = landlot_Farm_FK;
        this.polygons = polygons;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLandlot_Farm_FK() {
        return landlot_Farm_FK;
    }

    public void setLandlot_Farm_FK(String landlot_Farm_FK) {
        this.landlot_Farm_FK = landlot_Farm_FK;
    }

    public Polygon getPolygons() {
        return polygons;
    }

    public void setPolygons(Polygon polygons) {
        this.polygons = polygons;
    }

    @Override
    public String toString(){
        return ("LandlotId: "+id+" ,landlot_Farm_FK: "+landlot_Farm_FK+" ,polygon: "+polygons.getCoordinates());
    }

}

