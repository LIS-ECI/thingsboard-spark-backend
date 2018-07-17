/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.pgr.spark.actions;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.thingsboard.samples.spark.util.ExternalMethods;

/**
 *
 * @author cristian
 */
public class ActionModelSendAlert implements Action,Serializable{

    private String idLandlot;

    
    @Override
    public void execute() {
        
        String token = ExternalMethods.getTokenSpark(getIdLandlot(), "spark_detection");
        try {
            ExternalMethods.sendTelemetryDataToThingsboard(token, "risk_sickness_X", 1);
        } 
        catch (Exception ex) {
            Logger.getLogger(ActionSendAlert.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        System.out.println("ALERT: RISK Of Sickness X in crop with Id: "+ getIdLandlot());
        
    }

    @Override
    public String getIdLandlot() {
        return idLandlot;
    }

    @Override
    public void setIdLandlot(String idLandlot) {
        this.idLandlot = idLandlot;
    }
    
}
