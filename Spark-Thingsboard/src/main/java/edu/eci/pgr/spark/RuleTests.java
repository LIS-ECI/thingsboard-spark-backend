package edu.eci.pgr.spark;

import java.util.HashMap;

public class RuleTests extends  Rule {
    @Override
    public void execute(HashMap<String, String> data) {
        for (String name: data.keySet()){
            String key =name.toString();
            String value = data.get(name).toString();
            System.out.println(key + " " + value);
        }
    }

    public void run(){
        execute(this.getData());
    }
}
