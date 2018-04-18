package org.thingsboard.server.common.data.farm;

import java.io.File;

public class Technology {

    private String name;
    private File picture;
    private String description;

    public Technology(String name, File picture, String description) {
        this.name = name;
        this.picture = picture;
        this.description = description;
    }

    public Technology(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public File getPicture() {
        return picture;
    }

    public void setPicture(File picture) {
        this.picture = picture;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
