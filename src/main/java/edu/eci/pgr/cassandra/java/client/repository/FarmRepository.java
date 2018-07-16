/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.pgr.cassandra.java.client.repository;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import java.util.ArrayList;
import java.util.List;
import org.thingsboard.server.common.data.farm.Farm;
import org.thingsboard.server.common.data.id.FarmId;

/**
 *
 * @author cristian
 */
public class FarmRepository {
    
    private static final String TABLE_NAME = "farm";
    
    private Session session;

    public FarmRepository(Session session) {
        this.session = session;
    }
    
    public Farm selectById(String id) {
        FarmId farmId=  FarmId.fromString(id);
        System.out.println(farmId.toString());
        StringBuilder sb = new StringBuilder("SELECT * FROM ").append(TABLE_NAME).append(" WHERE id = ").append(farmId).append(";");
        final String query = sb.toString();
        System.out.println(query+" stringbouild");
        ResultSet rs = session.execute(query);
        System.out.println("result "+rs.toString());
        Farm farm=null;
        for (Row r : rs) {
            farm= new Farm(r.getString("name"),r.getString("type"),r.getString("dashboard_id"),r.getString("location_description"),r.getString("farm_details"),r.getString("home_details"),r.getString("farm_enviroment"),r.getString("total_area"),r.getString("irrigations_systems"));
        }
        System.out.println("farm:" +farm);
        return farm;
    }
    
}
