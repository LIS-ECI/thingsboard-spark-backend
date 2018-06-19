/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.entities;

import com.mycompany.entities.model.Point;

/**
 *
 * @author Sonia
 */
public class SpatialDevice {

    private String id, device_Landlot_FK;
    private Point point;

    public SpatialDevice() {
    }

    public SpatialDevice(String id, String device_Landlot_FK, Point point) {
        this.id = id;
        this.device_Landlot_FK = device_Landlot_FK;
        this.point = point;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDevice_Landlot_FK() {
        return device_Landlot_FK;
    }

    public void setDevice_Landlot_FK(String device_Landlot_FK) {
        this.device_Landlot_FK = device_Landlot_FK;
    }

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    @Override
    public String toString(){
        return ("DeviceId: "+id+" ,device_Landlot_FK: "+device_Landlot_FK+" ,Point: "+point.getCoordinates());
    }

}
