package org.thingsboard.server.common.data.farm;

public class IrrigationSystem {

    private String name;
    private String description;

    public IrrigationSystem(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public IrrigationSystem(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
