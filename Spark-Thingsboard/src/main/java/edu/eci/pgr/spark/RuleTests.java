package edu.eci.pgr.spark;

import java.io.Serializable;
import java.util.HashMap;

public class RuleTests extends  Rule implements Serializable{
    
    public RuleTests(){}
    
    @Override
    public void execute(HashMap<String, String> data) {
        for (String name: data.keySet()){
            String key =name.toString();
            String value = data.get(name).toString();
            System.out.println(key + " " + value+"hilo1");
        }
    }

    public void run(){
        execute(this.getData());
    }
}
