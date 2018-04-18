/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.connection;

import com.mycompany.entities.SpatialDevice;
import com.mycompany.entities.SpatialFarm;
import com.mycompany.entities.SpatialParcel;

/**
 *
 * @author Sonia
 */
public interface SpatialIndexes {
    
    public SpatialFarm findFarmsByDeviceId(String device_id) throws MongoDBException;
    
    public String getTokenByIdParcelTopic(String idParcel,String token) throws MongoDBException;
    
    public SpatialParcel findParcelsByDeviceId(String device_id) throws MongoDBException;
    
    public SpatialDevice getCoordenatesByDeviceId(String device_id) throws MongoDBException;
}
