/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.connection.dao.SpatialFarm;

import com.mongodb.Block;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import static com.mongodb.client.model.Filters.eq;
import com.mongodb.client.result.DeleteResult;
import com.mycompany.connection.MongoConnectionPOJO;
import com.mycompany.connection.dao.Dao;
import com.mycompany.entities.SpatialDevice;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author Sonia
 */
public class MongoDBSpatialDevice extends MongoConnectionPOJO<SpatialDevice> implements Dao<SpatialDevice> {

    @Override
    public MongoCollection<SpatialDevice> getCollectionDependClass() {
        return this.getMongoDatabase().getCollection("Devices", SpatialDevice.class);
    }

    @Override
    public List<SpatialDevice> find() {
        MongoCollection<SpatialDevice> deviceCollection = getCollectionDependClass();
        List<SpatialDevice> resultSet = new CopyOnWriteArrayList<>();
        deviceCollection.find().forEach((Block<SpatialDevice>) device -> {
            resultSet.add(device);
        });
        return resultSet;
    }

    @Override
    public SpatialDevice findById(String id) {
        return getCollectionDependClass().find(eq("_id", id)).first();
    }

    @Override
    public SpatialDevice save(SpatialDevice t) {
        try {
            getCollectionDependClass().insertOne(t);
            return t;
        } catch (MongoWriteException ex) {
            System.out.println("No fue posible agregar el device");
        }
        return null;
    }

    @Override
    public boolean removeById(String id) {
        DeleteResult deleteResult = getCollectionDependClass().deleteMany(eq("_id", id));
        return deleteResult.getDeletedCount() >= 1;
    }

}
