/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingsboard.server.common.data.farm;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.EqualsAndHashCode;

/**
 *
 * @author cristian
 */
public class Farm {

    private String name;
    private String type;
    private String dashboardId;
    private String locationDescription;
    private FarmDetails farmDetails;
    private HomeDetails homeDetails;
    private Enviroment enviroment;
    private Area totalArea;
    private List<IrrigationSystem> irrigationsSystems;

    public Farm() {

    }

    public Farm(String name, String type, String dashboardId, String locationDescription, String farmDetails, String homeDetails,String enviroment, String totalArea, String irrigationsSystems) {

        this.name = name;
        this.type = type;
        this.dashboardId = dashboardId;
        this.locationDescription = locationDescription;

        ObjectMapper mapper = new ObjectMapper();
        try {
            this.farmDetails = mapper.readValue(farmDetails, FarmDetails.class);
            this.homeDetails = mapper.readValue(homeDetails, HomeDetails.class);
            this.enviroment = mapper.readValue(enviroment, Enviroment.class);
            this.totalArea = mapper.readValue(totalArea, Area.class);
            this.irrigationsSystems = mapper.readValue(irrigationsSystems, mapper.getTypeFactory().constructParametricType(List.class, IrrigationSystem.class));

        } catch (IOException ex) {
            Logger.getLogger(Farm.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public Area getTotalArea() {
        return totalArea;
    }

    public void setTotalArea(Area totalArea) {
        this.totalArea = totalArea;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

  

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(", name=");
        builder.append(name);
        builder.append(", type=");
        builder.append(type);
        builder.append("]");
        return builder.toString();
    }

   
    public String getDashboardId() {
        return dashboardId;
    }

    public void setDashboardId(String dashboardId) {
        this.dashboardId = dashboardId;
    }

    public String getLocationDescription() {
        return locationDescription;
    }

    public void setLocationDescription(String locationDescription) {
        this.locationDescription = locationDescription;
    }

    public FarmDetails getFarmDetails() {
        return farmDetails;
    }

    public void setFarmDetails(FarmDetails farmDetails) {
        this.farmDetails = farmDetails;
    }


    public HomeDetails getHomeDetails() {
        return homeDetails;
    }

    public void setHomeDetails(HomeDetails homeDetails) {
        this.homeDetails = homeDetails;
    }


    public Enviroment getEnviroment() {
        return this.enviroment;
    }

    public void setEnviroment(Enviroment enviroment) {
        this.enviroment = enviroment;
    }

    public List<IrrigationSystem> getIrrigationsSystems() {
        return irrigationsSystems;
    }

    public void setIrrigationsSystems(List<IrrigationSystem> irrigationsSystems) {
        this.irrigationsSystems = irrigationsSystems;
    }
}
