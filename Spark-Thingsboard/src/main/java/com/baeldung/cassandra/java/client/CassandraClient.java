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
import com.datastax.driver.core.Session;
import org.thingsboard.server.common.data.farm.Farm;

public class CassandraClient {
    private static final Logger LOG = LoggerFactory.getLogger(CassandraClient.class);

    //Example
    public static void main(String args[]) {
        CassandraConnector connector = new CassandraConnector();
        connector.connect("127.0.0.1", null);
        Session session = connector.getSession();

        KeyspaceRepository sr = new KeyspaceRepository(session);
        sr.useKeyspace("thingsboard");
        FarmRepository fr= new FarmRepository(session);
        Farm f= fr.selectById("fa5eddb0-3d96-11e8-afb4-8d11adb9c84e");   

        connector.close();
    }
}