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
        Landlot f= fr.selectById("321a7e50-74e5-11e8-9cab-55a210069b83");   
        System.out.println("farm: "+f);
        connector.close();
    }
}