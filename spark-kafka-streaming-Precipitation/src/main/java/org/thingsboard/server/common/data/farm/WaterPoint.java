package org.thingsboard.server.common.data.farm;

import java.util.Date;

public class WaterPoint {

    private int numberPoint;
    private String resolution;
    private Date validity;

    public WaterPoint(int number, String resolution, Date validity) {
        this.numberPoint = number;
        this.resolution = resolution;
        this.validity = validity;
    }

    public WaterPoint(){}

    public int getNumberPoint() {
        return numberPoint;
    }

    public void setNumberPoint(int numberPoint) {
        this.numberPoint = numberPoint;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public Date getValidity() {
        return validity;
    }

    public void setValidity(Date validity) {
        this.validity = validity;
    }

    @Override
    public String toString(){
        return "[Waterpoint -> number: "+Integer.toString(numberPoint)+", resolution: "+resolution+", validity: "+validity.toString()+"]";
    }
}
