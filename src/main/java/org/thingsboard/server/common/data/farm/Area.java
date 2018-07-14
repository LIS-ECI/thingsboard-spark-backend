package org.thingsboard.server.common.data.farm;

public class Area {

    private float extension;
    private String symbol;

    public Area(float extension, String symbol) {
        this.extension = extension;
        this.symbol = symbol;
    }

    public Area() {}

    public float getExtension() {
        return extension;
    }

    public void setExtension(float extension) {
        this.extension = extension;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
