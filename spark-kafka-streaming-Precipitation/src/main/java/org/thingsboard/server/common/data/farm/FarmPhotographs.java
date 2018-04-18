package org.thingsboard.server.common.data.farm;

import java.io.File;

public class FarmPhotographs {

    private File front;
    private File airPhoto;

    public FarmPhotographs(File front, File airPhoto) {
        this.front = front;
        this.airPhoto = airPhoto;
    }

    public FarmPhotographs(){}

    public File getFront() {
        return front;
    }

    public void setFront(File front) {
        this.front = front;
    }

    public File getAirPhoto() {
        return airPhoto;
    }

    public void setAirPhoto(File airPhoto) {
        this.airPhoto = airPhoto;
    }
}
