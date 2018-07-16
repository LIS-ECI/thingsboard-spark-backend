/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.pgr.cassandra.java.client.repository;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import org.thingsboard.server.common.data.id.LandlotId;
import org.thingsboard.server.common.data.landlot.Landlot;

/**
 *
 * @author cristian
 */
public class LandlotRepository {
    private static final String TABLE_NAME = "landlot";
    
    private Session session;
    
    public LandlotRepository(Session session) {
        this.session = session;
    }
    public Landlot selectById(String id) {
        LandlotId farmId=  LandlotId.fromString(id);
        System.out.println(farmId.toString());
        StringBuilder sb = new StringBuilder("SELECT * FROM ").append(TABLE_NAME).append(" WHERE id = ").append(farmId).append(";");
        final String query = sb.toString();
        System.out.println(query+" stringbouild");


        ResultSet rs = session.execute(query);

        System.out.println("result "+rs.toString());
        Landlot landlot=null;

        for (Row r : rs) {
            landlot= new Landlot(r.getString("name"),r.getString("type"),r.getString("farm_id"),r.getString("crop"),r.getString("crops_history"),r.getString("total_area"),r.getString("ground_features"));
        }
        System.out.println("landlot:" +landlot);

        return landlot;
    }
}
