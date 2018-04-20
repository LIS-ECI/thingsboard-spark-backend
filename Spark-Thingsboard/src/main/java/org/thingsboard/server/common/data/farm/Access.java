package org.thingsboard.server.common.data.farm;

public class Access {

    private Boolean air;
    private Boolean land;
    private Boolean fluvial;


    public Access(Boolean air, Boolean land, Boolean fluvial) {
        this.air = air;
        this.land = land;
        this.fluvial = fluvial;
    }

    public Access(){}

    public Boolean isAir() {
        return this.air;
    }

    public void setAir(Boolean air) {
        this.air = air;
    }

    public Boolean isLand() {
        return this.land;
    }

    public void setLand(Boolean land) {
        this.land = land;
    }

    public Boolean isFluvial() {
        return this.fluvial;
    }

    public void setFluvial(Boolean fluvial) {
        this.fluvial = fluvial;
    }
}
