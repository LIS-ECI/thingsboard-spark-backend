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
import com.mycompany.entities.SparkDevice;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author carlos
 */
public class MongoDBSpatialSpark extends MongoConnectionPOJO<SparkDevice> implements Dao<SparkDevice> {

    @Override
    public MongoCollection<SparkDevice> getCollectionDependClass() {
        return this.getMongoDatabase().getCollection("SparkDevice", SparkDevice.class);
    }

    @Override
    public List<SparkDevice> find() {
        MongoCollection<SparkDevice> sparkCollection = getCollectionDependClass();
        List<SparkDevice> resultSet = new CopyOnWriteArrayList<>();
        sparkCollection.find().forEach((Block<SparkDevice>) sparkDevice -> {
            resultSet.add(sparkDevice);
        });
        return resultSet;
    }

    @Override
    public SparkDevice findById(String id) {
        return getCollectionDependClass().find(eq("_id", id)).first();
    }

    @Override
    public SparkDevice save(SparkDevice t) {
        try {
            getCollectionDependClass().insertOne(t);
            return t;
        } catch (MongoWriteException ex) {
            System.out.println("No fue posible agregar el spark device");
        }
        return null;
    }

    @Override
    public boolean removeById(String id) {
        DeleteResult deleteResult = getCollectionDependClass().deleteMany(eq("_id", id));
        return deleteResult.getDeletedCount() >= 1;
    }

}
