package edu.eci.pgr.spark;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.spark.mllib.linalg.SparseVector;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.tree.model.DecisionTreeModel;
import org.thingsboard.samples.spark.temperature.SparkKafkaStreamingTemperatureMain;

public class RuleTests extends  Rule implements Serializable{
    
    private List<String> types_Crops;

    
    public RuleTests(){
        types_Crops= new ArrayList<>();
        types_Crops.add("Papa");
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
    public void execute(HashMap<String, String> data) {
        DecisionTreeModel model= SparkKafkaStreamingTemperatureMain.model;
        double[] vector = {15.0,27.0,25.0,3.7,2.0};
        int[] index = {0,1,2,3,4};
        Vector v = new SparseVector(5,index,vector);
        System.out.println("El modelo predijo que: "+  model.predict(v));
    }


}
