package org.thingsboard.server.common.data.farm;

public class PublicServices {
    private Boolean electricity;
    private Boolean water;
    private Boolean sewerage;
    private Boolean gas;
    private Boolean garbage_collection;
    private Boolean collection;
    private Boolean internet;
    private Boolean telephony;
    private Boolean television;

    public PublicServices(Boolean electricity, Boolean water, Boolean sewerage, Boolean gas, Boolean garbage_collection, Boolean collection, Boolean internet, Boolean telephony, Boolean television) {
        this.electricity = electricity;
        this.water = water;
        this.sewerage = sewerage;
        this.gas = gas;
        this.garbage_collection = garbage_collection;
        this.collection = collection;
        this.internet = internet;
        this.telephony = telephony;
        this.television = television;
    }

    public PublicServices(){}

    public Boolean getElectricity() {
        return electricity;
    }

    public void setElectricity(Boolean electricity) {
        this.electricity = electricity;
    }

    public Boolean getGarbage_collection() {
        return garbage_collection;
    }

    public void setGarbage_collection(Boolean garbage_collection) {
        this.garbage_collection = garbage_collection;
    }

    public Boolean getWater() {
        return water;
    }

    public void setWater(Boolean water) {
        this.water = water;
    }

    public Boolean getSewerage() {
        return sewerage;
    }

    public void setSewerage(Boolean sewerage) {
        this.sewerage = sewerage;
    }

    public Boolean getGas() {
        return gas;
    }

    public void setGas(Boolean gas) {
        this.gas = gas;
    }

    public Boolean getCollection() {
        return collection;
    }

    public void setCollection(Boolean collection) {
        this.collection = collection;
    }

    public Boolean getInternet() {
        return internet;
    }

    public void setInternet(Boolean internet) {
        this.internet = internet;
    }

    public Boolean getTelephony() {
        return telephony;
    }

    public void setTelephony(Boolean telephony) {
        this.telephony = telephony;
    }

    public Boolean getTelevision() {
        return television;
    }

    public void setTelevision(Boolean television) {
        this.television = television;
    }
}
