/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.pgr.spark.actions;

import java.io.Serializable;
import java.util.List;
import org.thingsboard.samples.spark.util.ExternalMethods;

/**
 *
 * @author cristian
 */
public class ActionTutorial implements Action,Serializable{

    private String idLandlot;
    
    @Override
    public void execute() {
        System.out.println("Executing ActionTutorial! ");
        List<List<List<Double>>> coordinates = ExternalMethods.getLandlotCoordinates(idLandlot);
        System.out.println(String.format("|%20s|%20s|", "Longitude", "Latitude"));
        coordinates.get(0).forEach((containData) -> {
            double longitude = containData.get(0);
            double latitude = containData.get(1);
            System.out.println(String.format("|%20s|%20s|", Double.toString(longitude), Double.toString(latitude)));
        });
    }

    @Override
    public String getIdLandlot() {
        return idLandlot;
    }

    @Override
    public void setIdLandlot(String idLandlot) {
        this.idLandlot=idLandlot;
    }
    
}
