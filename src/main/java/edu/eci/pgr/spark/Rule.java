/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.pgr.spark;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import org.apache.spark.mllib.tree.model.DecisionTreeModel;

/**
 *
 * @author cristian
 */
public abstract class Rule  implements Serializable{
    
    private List<String> types_Crops;
    private HashMap<String,String> data;
    
    public void execute(HashMap<String,String> data, DecisionTreeModel model){};


    public List<String> getTypes_Crops() {
        return types_Crops;
    }

    public void setTypes_Crops(List<String> types_Crops) {
        this.types_Crops = types_Crops;
    }

}
