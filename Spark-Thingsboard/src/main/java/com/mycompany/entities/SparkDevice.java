/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.entities;

/**
 *
 * @author carlos
 */
public class SparkDevice {
    private String id, idLandlot,topic;

    public SparkDevice() {
    }

    public SparkDevice(String id, String idLandlot, String topic) {
        this.id = id;
        this.idLandlot = idLandlot;
        this.topic = topic;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdLandlot() {
        return idLandlot;
    }

    public void setIdLandlot(String idLandlot) {
        this.idLandlot = idLandlot;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
    
    
}
