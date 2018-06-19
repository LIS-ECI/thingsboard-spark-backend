/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.connection;

import com.mycompany.entities.SpatialDevice;
import com.mycompany.entities.SpatialFarm;
import com.mycompany.entities.SpatialLandlot;

/**
 *
 * @author Sonia
 */
public interface SpatialIndexes {
    
    public SpatialFarm findFarmsByDeviceId(String device_id) throws MongoDBException;
    
    public String getTokenByIdLandlotTopic(String idLandlot,String token) throws MongoDBException;
    
    public SpatialLandlot findLandlotsByDeviceId(String device_id) throws MongoDBException;
    
    public SpatialDevice getCoordenatesByDeviceId(String device_id) throws MongoDBException;
}
