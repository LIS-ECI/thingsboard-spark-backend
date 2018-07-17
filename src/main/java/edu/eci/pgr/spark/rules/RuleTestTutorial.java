/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.pgr.spark.rules;

import edu.eci.pgr.spark.actions.Action;
import edu.eci.pgr.spark.actions.ActionTutorial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.spark.mllib.tree.model.DecisionTreeModel;
import org.thingsboard.samples.spark.util.ExternalMethods;

/**
 *
 * @author cristian
 */
public class RuleTestTutorial extends Rule implements Serializable{
    
    List<String> types_Crops;
    List<Action> actions;

    public List<String> getTypes_Crops() {
        return types_Crops;
    }

    public void setTypes_Crops(List<String> types_Crops) {
        this.types_Crops = types_Crops;
    }
    
    public RuleTestTutorial(){
        types_Crops= new ArrayList<>();
        types_Crops.add("Potato");
        actions = new ArrayList<>();
        actions.add(new ActionTutorial());

    }
    
    @Override
    public void execute(HashMap<String,String> data,DecisionTreeModel model){
        System.out.println("RuleTestTutorial Is Working!!");
        String idLandlot = data.get("idLandlot");
        String lastHumidity = ExternalMethods.getValueOfRedis("humidity", idLandlot);
        System.out.println("This is the last humidity data received!: "+lastHumidity);
        String cropName = ExternalMethods.getCropNameCassandra(idLandlot);
        System.out.println("This is the crop name!: "+cropName);     
        actions.forEach((a) -> {
            a.setIdLandlot(idLandlot);
            a.execute();
        });
    } 
}

