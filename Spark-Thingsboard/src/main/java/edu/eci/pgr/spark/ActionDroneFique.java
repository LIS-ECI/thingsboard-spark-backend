/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.pgr.spark;

import org.springframework.stereotype.Service;

/**
 *
 * @author cristian
 */
public class ActionDroneFique implements Action {
    
    public ActionDroneFique(){}
    private String idParcel;


    @Override
    public void execute() {
        System.out.println("Ejecutando Acción Drone Fique");
    }

    /**
     * @return the idParcel
     */
    @Override
    public String getIdParcel() {
        return idParcel;
    }

    /**
     * @param idParcel the idParcel to set
     */
    @Override
    public void setIdParcel(String idParcel) {
        this.idParcel = idParcel;
    }
    
}
