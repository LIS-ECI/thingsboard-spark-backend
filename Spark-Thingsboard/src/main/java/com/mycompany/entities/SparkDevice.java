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
    private String id, idParcel,topic;

    public SparkDevice() {
    }

    public SparkDevice(String id, String idParcel, String topic) {
        this.id = id;
        this.idParcel = idParcel;
        this.topic = topic;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdParcel() {
        return idParcel;
    }

    public void setIdParcel(String idParcel) {
        this.idParcel = idParcel;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
    
    
}
