/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.baeldung.cassandra.java.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baeldung.cassandra.java.client.repository.FarmRepository;
import com.baeldung.cassandra.java.client.repository.KeyspaceRepository;
import com.baeldung.cassandra.java.client.repository.LandlotRepository;
import com.datastax.driver.core.Session;
import com.mycompany.connection.MongoDBException;
import com.mycompany.connection.MongoDBSpatial;
import java.util.logging.Level;
import org.thingsboard.server.common.data.farm.Farm;
import org.thingsboard.server.common.data.landlot.Landlot;

public class CassandraClient {
    private static final Logger LOG = LoggerFactory.getLogger(CassandraClient.class);

    //Example
    public static void main(String args[]) {
        CassandraConnector connector = new CassandraConnector();
        connector.connect("10.8.0.19", null);
        Session session = connector.getSession();

        KeyspaceRepository sr = new KeyspaceRepository(session);
        sr.useKeyspace("thingsboard");
        LandlotRepository fr= new LandlotRepository(session);
        
        //Landlot f= fr.selectById("1ffce660-80af-11e8-9659-3fd35edd3cfd");   
        //System.out.println("farm: "+f);
        connector.close();
        
        MongoDBSpatial mdbs = new MongoDBSpatial();
        String idLandlot;
        try {
            idLandlot = mdbs.findLandlotsByDeviceId("1ffce660-80af-11e8-9659-3fd35edd3cfd").getId();
            System.out.println("idLand:" +idLandlot);

        } catch (MongoDBException ex) {
            java.util.logging.Logger.getLogger(CassandraClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}