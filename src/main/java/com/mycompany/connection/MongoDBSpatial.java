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
import com.mycompany.connection.dao.SpatialFarm.MongoDBSpatialLandlot;
import com.mycompany.entities.SpatialDevice;
import com.mycompany.entities.SpatialFarm;
import com.mycompany.entities.SpatialLandlot;
import java.io.Serializable;

/**
 *
 * @author Sonia
 */
public class MongoDBSpatial extends MongoConnection implements SpatialIndexes,Serializable {

    private final MongoDBSpatialLandlot mongodblandlot;
    private final MongoDBSpatialFarm mongodbFarm;
    private final MongoDBSpatialDevice mongodbDevice;
    private final MongoDBSpatialSpark mongodbspark;
    
    public MongoDBSpatial() {
        mongodblandlot = new MongoDBSpatialLandlot();
        mongodbFarm = new MongoDBSpatialFarm();
        mongodbDevice = new MongoDBSpatialDevice();
        mongodbspark = new MongoDBSpatialSpark();

    }

    public MongoDBSpatialLandlot getMongodblandlot() {
        return mongodblandlot;
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
            System.out.println("findFarmsByDeviceId device_id: "+device_id);
            SpatialDevice sdt = mongodbDevice.findById(device_id);
            System.out.println("segundo print");
            SpatialLandlot sct = mongodblandlot.findById(sdt.getDevice_Landlot_FK());
            System.out.println("tercer print");
            return mongodbFarm.findById(sct.getLandlot_Farm_FK());
        } catch (NullPointerException ex) {
            throw new MongoDBException("It wasn´t posible to load the farm associated with device!!");
        }
    }

    @Override
    public String getTokenByIdLandlotTopic(String idLandlot, String topic) throws MongoDBException {
        StringBuilder token = new StringBuilder();
        System.out.println("get token: idLandlot:"+idLandlot+" topic: "+topic);
        mongodbspark.getCollectionDependClass().find(and(eq("idLandlot",idLandlot),eq("topic",topic))).forEach((Block<SparkDevice>) sparkDevice -> {
            token.append(sparkDevice.getToken());
        });
        System.out.println("retorno token: "+token.toString());
        return token.toString();
    }

    @Override
    public SpatialLandlot findLandlotsByDeviceId(String device_id) throws MongoDBException {
        System.out.println("device_id: "+device_id);
        try {
            SpatialDevice sdt = mongodbDevice.findById(device_id);
            return mongodblandlot.findById(sdt.getDevice_Landlot_FK());
        } catch (NullPointerException ex) {
            throw new MongoDBException("It wasn´t posible to load the landlot associated with device!!");
        }
    }

    @Override
    public SpatialDevice getCoordenatesByDeviceId(String device_id) throws MongoDBException {
        return mongodbDevice.findById(device_id);
    }

}
