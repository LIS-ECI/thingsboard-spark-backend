package edu.eci.pgr.spark;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RuleTests2  extends Rule implements Serializable{
    
    private List<String> types_Crops;

    
    public RuleTests2(){
        types_Crops= new ArrayList<>();
        types_Crops.add("Papa");
    }

    @Override
    public void execute(HashMap<String, String> data) {
        for (String name: data.keySet()){
            String key =name.toString();
            String value = data.get(name).toString();
            System.out.println(key + " " + value+ " "+"hilo2");
        }
    }

    public void run(){
        execute(this.getData());
    }
}
