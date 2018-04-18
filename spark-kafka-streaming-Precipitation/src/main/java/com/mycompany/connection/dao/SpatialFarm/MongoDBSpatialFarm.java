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
import com.mycompany.entities.SpatialFarm;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author Sonia
 */
public class MongoDBSpatialFarm extends MongoConnectionPOJO<SpatialFarm> implements Dao<SpatialFarm> {

    @Override
    public MongoCollection<SpatialFarm> getCollectionDependClass() {
        return this.getMongoDatabase().getCollection("Farms", SpatialFarm.class);
    }

    @Override
    public List<SpatialFarm> find() {
        MongoCollection<SpatialFarm> deviceCollection = getCollectionDependClass();
        List<SpatialFarm> resultSet = new CopyOnWriteArrayList<>();
        deviceCollection.find().forEach((Block<SpatialFarm>) farm -> {
            resultSet.add(farm);
        });
        return resultSet;
    }

    @Override
    public SpatialFarm findById(String id) {
        return getCollectionDependClass().find(eq("_id", id)).first();
    }

    @Override
    public SpatialFarm save(SpatialFarm t) {
        try {
            getCollectionDependClass().insertOne(t);
            return t;
        } catch (MongoWriteException ex) {
            System.out.println("No fue posible agregar la farm");
        }
        return null;
    }

    @Override
    public boolean removeById(String id) {
        DeleteResult deleteResult = getCollectionDependClass().deleteMany(eq("_id", id));
        return deleteResult.getDeletedCount() >= 1;
    }

}
