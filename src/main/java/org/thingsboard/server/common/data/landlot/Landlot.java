/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingsboard.server.common.data.landlot;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

import org.thingsboard.server.common.data.crop.Crop;
import org.thingsboard.server.common.data.farm.Area;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author German Lopez
 */
public class Landlot {

    private static final long serialVersionUID = 2807343040519543363L;

    
    private String name;
    private String type;
    private String farmId;
    private Crop crop;
    private List<Crop> cropsHistory;
    private Area totalArea;
    private GroundFeatures groundFeatures;


    public Landlot() {
    }


    public Landlot(String name, String type, String farmId, String crop, String cropsHistory, String totalArea, String groundFeatures) {
        this.name = name;
        this.type = type;
        this.farmId = farmId;
        ObjectMapper mapper = new ObjectMapper();
        try {
            this.crop = mapper.readValue(crop, Crop.class);
            this.cropsHistory = mapper.readValue(cropsHistory, mapper.getTypeFactory().constructParametricType(List.class, Crop.class));
            this.totalArea = mapper.readValue(totalArea, Area.class);
            this.groundFeatures = mapper.readValue(groundFeatures, GroundFeatures.class);
        } catch (IOException ex) {
            Logger.getLogger(Landlot.class.getName()).log(Level.SEVERE, null, ex);
        }
        
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

    public String getSearchText() {
        return getName();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Landlot [tenantId=");
        builder.append(", customerId=");
        builder.append(", name=");
        builder.append(name);
        builder.append(", type=");
        builder.append(type);
        builder.append(", additionalInfo=");
        builder.append(", createdTime=");
        builder.append(", id=");
        builder.append(", farmId=");
        builder.append(farmId);
        builder.append("]");
        return builder.toString();
    }

    /**
     * @return the farmId
     */
    public String getFarmId() {
        return farmId;
    }

    /**
     * @param farmId the farmId to set
     */
    public void setFarmId(String farmId) {
        this.farmId = farmId;
    }


    public Crop getCrop() {
        return crop;
    }

    public void setCrop(Crop crop) {
        this.crop = crop;
    }

    public List<Crop> getCropsHistory() {
        return cropsHistory;
    }

    public void setCropsHistory(List<Crop> cropsHistory) {
        this.cropsHistory = cropsHistory;
    }

    public Area getTotalArea() {
        return totalArea;
    }

    public void setTotalArea(Area totalArea) {
        this.totalArea = totalArea;
    }

    public GroundFeatures getGroundFeatures() {
        return groundFeatures;
    }

    public void setGroundFeatures(GroundFeatures groundFeatures) {
        this.groundFeatures = groundFeatures;
    }
}
