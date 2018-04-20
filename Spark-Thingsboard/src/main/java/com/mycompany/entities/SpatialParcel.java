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
public class SpatialParcel {

    private String id, parcel_Farm_FK;
    private Polygon polygons;

    public SpatialParcel() {
    }

    public SpatialParcel(String id, String parcel_Farm_FK, Polygon polygons) {
        this.id = id;
        this.parcel_Farm_FK = parcel_Farm_FK;
        this.polygons = polygons;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParcel_Farm_FK() {
        return parcel_Farm_FK;
    }

    public void setParcel_Farm_FK(String parcel_Farm_FK) {
        this.parcel_Farm_FK = parcel_Farm_FK;
    }

    public Polygon getPolygons() {
        return polygons;
    }

    public void setPolygons(Polygon polygons) {
        this.polygons = polygons;
    }
    
    @Override
    public String toString(){
        return ("ParcelId: "+id+" ,parcel_Farm_FK: "+parcel_Farm_FK+" ,polygon: "+polygons.getCoordinates());
    }

}
