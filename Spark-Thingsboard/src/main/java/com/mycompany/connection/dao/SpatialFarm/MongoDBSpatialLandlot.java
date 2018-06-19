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
import com.mycompany.entities.SpatialLandlot;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author Sonia
 */
public class MongoDBSpatialLandlot extends MongoConnectionPOJO<SpatialLandlot> implements Dao<SpatialLandlot> {

    @Override
    public MongoCollection<SpatialLandlot> getCollectionDependClass() {
        return this.getMongoDatabase().getCollection("Landlots", SpatialLandlot.class);
    }

    @Override
    public List<SpatialLandlot> find() {
        MongoCollection<SpatialLandlot> farmCollection = getCollectionDependClass();
        List<SpatialLandlot> resultSet = new CopyOnWriteArrayList<>();
        farmCollection.find().forEach((Block<SpatialLandlot>) landlot -> {
            resultSet.add(landlot);
        });
        return resultSet;
    }

    @Override
    public SpatialLandlot findById(String id) {
        return getCollectionDependClass().find(eq("_id", id)).first();
    }

    @Override
    public SpatialLandlot save(SpatialLandlot t){
        try{
            getCollectionDependClass().insertOne(t);
            return t;
        }catch(MongoWriteException ex){
            System.out.println("No fue posible agregar el landlot");
            
        }
        return null;
    }

    @Override
    public boolean removeById(String id) {
        DeleteResult deleteResult = getCollectionDependClass().deleteMany(eq("_id", id));
        return deleteResult.wasAcknowledged();
    }

}
