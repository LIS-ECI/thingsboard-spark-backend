/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.baeldung.cassandra.java.client.repository;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import org.thingsboard.server.common.data.id.ParcelId;
import org.thingsboard.server.common.data.parcel.Parcel;

/**
 *
 * @author cristian
 */
public class ParcelRepository {
    private static final String TABLE_NAME = "parcel";
    
    private Session session;
    
    public ParcelRepository(Session session) {
        this.session = session;
    }
    public Parcel selectById(String id) {
        ParcelId farmId=  ParcelId.fromString(id);
        System.out.println(farmId.toString());
        StringBuilder sb = new StringBuilder("SELECT * FROM ").append(TABLE_NAME).append(" WHERE id = ").append(farmId).append(";");
        final String query = sb.toString();
        System.out.println(query+" stringbouild");


        ResultSet rs = session.execute(query);

        System.out.println("result "+rs.toString());
        Parcel parcel=null;

        for (Row r : rs) {
            parcel= new Parcel(r.getString("name"),r.getString("type"),r.getString("farm_id"),r.getString("crop"),r.getString("crops_history"),r.getString("total_area"),r.getString("ground_features"));
        }
        System.out.println("parcel:" +parcel);

        return parcel;
    }
}
