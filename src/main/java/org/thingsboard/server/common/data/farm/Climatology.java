package org.thingsboard.server.common.data.farm;

public class Climatology {

    private String temperature;
    private String humidity;
    private String rainFall;
    private String solarIrradiance;

    public Climatology(String temperature, String humidity, String rainFall, String solarIrradiance) {
        this.temperature = temperature;
        this.humidity = humidity;
        this.rainFall = rainFall;
        this.solarIrradiance = solarIrradiance;
    }

    public Climatology(){}

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getRainFall() {
        return rainFall;
    }

    public void setRainFall(String rainFall) {
        this.rainFall = rainFall;
    }

    public String getSolarIrradiance() {
        return solarIrradiance;
    }

    public void setSolarIrradiance(String solarIrradiance) {
        this.solarIrradiance = solarIrradiance;
    }
}
