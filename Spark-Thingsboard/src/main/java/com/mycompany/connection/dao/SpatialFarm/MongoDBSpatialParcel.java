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
import com.mycompany.entities.SpatialParcel;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author Sonia
 */
public class MongoDBSpatialParcel extends MongoConnectionPOJO<SpatialParcel> implements Dao<SpatialParcel> {

    @Override
    public MongoCollection<SpatialParcel> getCollectionDependClass() {
        return this.getMongoDatabase().getCollection("Parcels", SpatialParcel.class);
    }

    @Override
    public List<SpatialParcel> find() {
        MongoCollection<SpatialParcel> farmCollection = getCollectionDependClass();
        List<SpatialParcel> resultSet = new CopyOnWriteArrayList<>();
        farmCollection.find().forEach((Block<SpatialParcel>) parcel -> {
            resultSet.add(parcel);
        });
        return resultSet;
    }

    @Override
    public SpatialParcel findById(String id) {
        return getCollectionDependClass().find(eq("_id", id)).first();
    }

    @Override
    public SpatialParcel save(SpatialParcel t){
        try{
            getCollectionDependClass().insertOne(t);
            return t;
        }catch(MongoWriteException ex){
            System.out.println("No fue posible agregar el parcel");
            
        }
        return null;
    }

    @Override
    public boolean removeById(String id) {
        DeleteResult deleteResult = getCollectionDependClass().deleteMany(eq("_id", id));
        return deleteResult.wasAcknowledged();
    }

}
