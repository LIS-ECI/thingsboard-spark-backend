package org.thingsboard.server.common.data.crop;

import java.util.Date;
import java.util.List;

public class Crop {

    private String name;
    private String why;
    private String cause;
    private Date startCrop = new Date();
    private int weekens = 0;
    private String initialConditions;
    private List<Action> actions;
    private Boolean finish;
    private String state;
    private List<String> practices;

    public Crop(String name, String why, String cause, Date startCrop, String initialConditions, List<Action> actions,Boolean finish,String state,List<String> practices) {
        this.name = name;
        this.why = why;
        this.cause = cause;
        this.startCrop = startCrop;
        this.weekens = dateToWeek(startCrop);
        this.initialConditions = initialConditions;
        this.actions = actions;
        this.setFinish(finish);
        this.setState(state);
        this.practices=practices;
    }

    public Crop(){}

    private int dateToWeek(Date start){
        int weeks = 0;
        Date today = new Date();
        weeks = (int) ((today.getTime() - start.getTime()) / (1000 * 60 * 60 * 7));
        return weeks;
    }

    public List<String> getPractices() {
        return practices;
    }

    public void setPractices(List<String> practices) {
        this.practices = practices;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWhy() {
        return why;
    }

    public void setWhy(String why) {
        this.why = why;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public Date getStartCrop() {
        return startCrop;
    }

    public void setStartCrop(Date startCrop) {
        this.startCrop = startCrop;
    }

    public int getWeekens() {
        return weekens;
    }

    public void setWeekens(int weekens) {
        this.weekens = weekens;
    }

    public String getInitialConditions() {
        return initialConditions;
    }

    public void setInitialConditions(String initialConditions) {
        this.initialConditions = initialConditions;
    }

    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    public Boolean getFinish() {
        return finish;
    }

    public void setFinish(Boolean finish) {
        this.finish = finish;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
