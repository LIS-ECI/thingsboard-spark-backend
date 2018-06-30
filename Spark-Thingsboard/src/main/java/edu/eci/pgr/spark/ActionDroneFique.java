/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.pgr.spark;

import java.io.Serializable;
import org.springframework.stereotype.Service;

/**
 *
 * @author cristian
 */
public class ActionDroneFique implements Action,Serializable {
    
    public ActionDroneFique(){}
    private String idLandlot;


    @Override
    public void execute() {
        System.out.println("Ejecutando Acción Drone Fique");
    }

    /**
     * @return the idLandlot
     */
    @Override
    public String getIdLandlot() {
        return idLandlot;
    }

    /**
     * @param idLandlot the idLandlot to set
     */
    @Override
    public void setIdLandlot(String idLandlot) {
        this.idLandlot = idLandlot;
    }
    
}
