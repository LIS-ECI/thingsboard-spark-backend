/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.connection;

import com.mycompany.connection.dao.SpatialFarm.MongoDBSpatialSpark;
import com.mycompany.entities.SparkDevice;
import com.mongodb.Block;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import com.mycompany.connection.dao.SpatialFarm.MongoDBSpatialDevice;
import com.mycompany.connection.dao.SpatialFarm.MongoDBSpatialFarm;
import com.mycompany.connection.dao.SpatialFarm.MongoDBSpatialParcel;
import com.mycompany.entities.SpatialDevice;
import com.mycompany.entities.SpatialFarm;
import com.mycompany.entities.SpatialParcel;

/**
 *
 * @author Sonia
 */
public class MongoDBSpatial extends MongoConnection implements SpatialIndexes {

    private final MongoDBSpatialParcel mongodbparcel;
    private final MongoDBSpatialFarm mongodbFarm;
    private final MongoDBSpatialDevice mongodbDevice;
    private final MongoDBSpatialSpark mongodbspark;
    
    public MongoDBSpatial() {
        mongodbparcel = new MongoDBSpatialParcel();
        mongodbFarm = new MongoDBSpatialFarm();
        mongodbDevice = new MongoDBSpatialDevice();
        mongodbspark = new MongoDBSpatialSpark();

    }

    public MongoDBSpatialParcel getMongodbparcel() {
        return mongodbparcel;
    }
    
    public MongoDBSpatialSpark getMongodbspark() {
        return mongodbspark;
    }

    public MongoDBSpatialFarm getMongodbFarm() {
        return mongodbFarm;
    }

    public MongoDBSpatialDevice getMongodbDevice() {
        return mongodbDevice;
    }

    @Override
    public SpatialFarm findFarmsByDeviceId(String device_id) throws MongoDBException {
        try {
            SpatialDevice sdt = mongodbDevice.findById(device_id);
            SpatialParcel sct = mongodbparcel.findById(sdt.getDevice_Parcel_FK());
            return mongodbFarm.findById(sct.getParcel_Farm_FK());
        } catch (NullPointerException ex) {
            throw new MongoDBException("It wasn´t posible to load the farm associated with device!!");
        }
    }

    @Override
    public String getTokenByIdParcelTopic(String idParcel, String topic) throws MongoDBException {
        StringBuilder token = new StringBuilder();
        System.out.println("get token: idParcel:"+idParcel+" topic: "+topic);
        mongodbspark.getCollectionDependClass().find(and(eq("idParcel",idParcel),eq("topic",topic))).forEach((Block<SparkDevice>) sparkDevice -> {
            token.append(sparkDevice.getId());
        });
        return token.toString();
    }

    @Override
    public SpatialParcel findParcelsByDeviceId(String device_id) throws MongoDBException {
        System.out.println("device_id: "+device_id);
        try {
            SpatialDevice sdt = mongodbDevice.findById(device_id);
            return mongodbparcel.findById(sdt.getDevice_Parcel_FK());
        } catch (NullPointerException ex) {
            throw new MongoDBException("It wasn´t posible to load the parcel associated with device!!");
        }
    }

    @Override
    public SpatialDevice getCoordenatesByDeviceId(String device_id) throws MongoDBException {
        return mongodbDevice.findById(device_id);
    }

}
