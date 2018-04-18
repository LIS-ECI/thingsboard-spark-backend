/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.connection;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mycompany.mongodb.ServerProperties;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Sonia
 */
public abstract class MongoConnection {

    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;

    @Autowired
    private ServerProperties serverProperties;

    public MongoConnection() {
    }

    public ServerProperties getServerProperties() {
        return serverProperties;
    }

    public MongoClient getSession() {
        if (mongoClient == null) {
            //mongoClient = new MongoClient(new MongoClientURI(serverProperties.getMongoURI()));
            mongoClient = new MongoClient(new MongoClientURI("mongodb://10.8.0.23:27017"));
        }
        return mongoClient;
    }

    public MongoDatabase getMongoDatabase() {
        if (mongoDatabase == null) {
            CodecRegistry pojoCodecRegistry = fromRegistries(MongoClient.getDefaultCodecRegistry(),
                    fromProviders(PojoCodecProvider.builder().automatic(true).build()));
            //mongoDatabase = getSession().getDatabase(serverProperties.getMongoDB());
            mongoDatabase = getSession().getDatabase("prueba").withCodecRegistry(pojoCodecRegistry);
        }
        return mongoDatabase;
    }

    public MongoDatabase getMongoDatabaseByName(String databaseName) {
        if (mongoDatabase == null) {
            CodecRegistry pojoCodecRegistry = fromRegistries(MongoClient.getDefaultCodecRegistry(),
                    fromProviders(PojoCodecProvider.builder().automatic(true).build()));
            mongoDatabase = getSession().getDatabase(databaseName).withCodecRegistry(pojoCodecRegistry);
        }
        return mongoDatabase;
    }

    public List<String> getListCollectionsNames() {
        List<String> collectionsNames = new ArrayList<>();
        for (String name : mongoDatabase.listCollectionNames()) {
            collectionsNames.add(name);
        }
        return collectionsNames;
    }

    public void dropCollection(String nameCollection) throws MongoDBException {
        if (getListCollectionsNames().contains(nameCollection)) {
            MongoCollection<Document> collection = mongoDatabase.getCollection(nameCollection);
            collection.drop();
        } else {
            throw new MongoDBException("Collection not exist!!");
        }
    }

}
