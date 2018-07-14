package org.thingsboard.server.common.data.crop;

import java.util.Date;

public class Action {

    private String action;
    private Date actionDate;

    public Action(String action, Date actionDate) {
        this.action = action;
        this.actionDate = actionDate;
    }

    public Action(){}

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Date getActionDate() {
        return actionDate;
    }

    public void setActionDate(Date actionDate) {
        this.actionDate = actionDate;
    }
}
