package edu.eci.pgr.spark.rules;

import edu.eci.pgr.spark.actions.ActionModelSendAlert;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.spark.mllib.linalg.SparseVector;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.tree.model.DecisionTreeModel;
import org.thingsboard.samples.spark.temperature.SparkKafkaStreamingTemperatureMain;
import edu.eci.pgr.spark.actions.Action;

public class RuleDecisionTree extends  Rule implements Serializable{
    
    private List<String> types_Crops;
    private List<Action> actions;

    
    public RuleDecisionTree(){
        types_Crops= new ArrayList<>();
        types_Crops.add("Papa");
        actions= new ArrayList<>();
        actions.add(new ActionModelSendAlert());
        
    }
    
    @Override
    public List<String> getTypes_Crops() {
        return types_Crops;
    }

    @Override
    public void setTypes_Crops(List<String> types_Crops) {
        this.types_Crops = types_Crops;
    }
    
    @Override
    public void execute(HashMap<String, String> data,DecisionTreeModel model) {
        System.out.println("Executing Decision Tree ML");
        double temperature= Double.parseDouble(data.get("temperatureData"));
        double humidity = Double.parseDouble(data.get("humidityData")); 
        double light = Double.parseDouble(data.get("lightData"));
        double[] vector = {temperature,humidity,light};
        int[] index = {0,1,2};
        Vector v = new SparseVector(3,index,vector);
        double answer= model.predict(v);
        
        if (answer==1.0){
            System.out.println("El modelo predijo que hay riesgo de la enfermadad X");
            String idLandlot= data.get("idLandlot");
            actions.forEach((a) -> {
                a.setIdLandlot(idLandlot);
                a.execute();
            });
        }
        
        
    }


}
