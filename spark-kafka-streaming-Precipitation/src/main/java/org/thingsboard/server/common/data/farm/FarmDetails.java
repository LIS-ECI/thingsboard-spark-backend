package org.thingsboard.server.common.data.farm;


import java.util.List;

public class FarmDetails {

    private String destination;
    private String useDetails;
    private PublicServices publicServices;
    private String productionTransport;
    private List<WaterPoint> waterPoints;

    public FarmDetails(String destination, String useDetails, PublicServices publicServices, String productionTransport, List<WaterPoint> waterPoints) {
        this.destination = destination;
        this.useDetails = useDetails;
        this.publicServices = publicServices;
        this.productionTransport = productionTransport;
        this.waterPoints = waterPoints;
    }

    public FarmDetails(){}

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getUseDetails() {
        return useDetails;
    }

    public void setUseDetails(String useDetails) {
        this.useDetails = useDetails;
    }

    public PublicServices getPublicServices() {
        return publicServices;
    }

    public void setPublicServices(PublicServices publicServices) {
        this.publicServices = publicServices;
    }

    public String getProductionTransport() {
        return productionTransport;
    }

    public void setProductionTransport(String productionTransport) {
        this.productionTransport = productionTransport;
    }

    public List<WaterPoint> getWaterPoints() {
        return waterPoints;
    }

    public void setWaterPoints(List<WaterPoint> waterPoints) {
        this.waterPoints = waterPoints;
    }
}
