/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.pgr.spark;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 *
 * @author cristian
 */
public abstract class Rule  implements Serializable{
    
    private List<String> types_Crops;

    public List<String> getTypes_Crops() {
        return types_Crops;
    }

    public void setTypes_Crops(List<String> types_Crops) {
        this.types_Crops = types_Crops;
    }

    private HashMap<String,String> data;
    
    public void execute(HashMap<String,String> data){};

    public HashMap<String, String> getData() {
        return data;
    }

    public void setData(HashMap<String, String> data) {
        this.data = data;
    }
}
