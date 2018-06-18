package edu.eci.pgr.spark;

import java.util.HashMap;

public class RuleTests2  extends Rule{
    
    public RuleTests2(){}

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
