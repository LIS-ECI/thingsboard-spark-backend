package org.thingsboard.server.common.data.parcel;

public class GroundFeatures {

    private String density;
    private String compaction;
    private String inclination;
    private String higrologicData;

    public GroundFeatures(String density, String compaction, String inclination, String higrologicData) {
        this.density = density;
        this.compaction = compaction;
        this.inclination = inclination;
        this.higrologicData = higrologicData;
    }

    public GroundFeatures(){}

    public String getDensity() {
        return density;
    }

    public void setDensity(String density) {
        this.density = density;
    }

    public String getCompaction() {
        return compaction;
    }

    public void setCompaction(String compaction) {
        this.compaction = compaction;
    }

    public String getInclination() {
        return inclination;
    }

    public void setInclination(String inclination) {
        this.inclination = inclination;
    }

    public String getHigrologicData() {
        return higrologicData;
    }

    public void setHigrologicData(String higrologicData) {
        this.higrologicData = higrologicData;
    }
}
