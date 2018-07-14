package org.thingsboard.server.common.data.farm;

import java.util.Date;

public class Person {

    private String name;
    private Date birthday;
    private String birthplace;
    private String maritalStatus;
    private String ethnicGroup;
    private String relation;

    public Person(String name, Date birthday, String birthplace, String maritalStatus, String ethnicGroup, String relation) {
        this.name = name;
        this.birthday = birthday;
        this.birthplace = birthplace;
        this.maritalStatus = maritalStatus;
        this.ethnicGroup = ethnicGroup;
        this.relation = relation;
    }

    public Person(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getBirthplace() {
        return birthplace;
    }

    public void setBirthplace(String birthplace) {
        this.birthplace = birthplace;
    }

    public String getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public String getEthnicGroup() {
        return ethnicGroup;
    }

    public void setEthnicGroup(String ethnicGroup) {
        this.ethnicGroup = ethnicGroup;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }
}
