/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.pgr.spark;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.springframework.stereotype.Service;
import org.thingsboard.samples.spark.temperature.SparkKafkaStreamingTemperatureMain;

/**
 *
 * @author cristian
 */
@Service
public class RulesEngine implements Serializable{
    
    private List<Rule> rules;
    private ExecutorService executorService;



    public RulesEngine() {

        rules = new ArrayList<>();
        rules.add(new GotaRule());
        rules.add(new RuleTests());
        rules.add(new RuleTests2());
    }
    

    public void execute( HashMap<String, String> data) {
        executorService = Executors.newFixedThreadPool(rules.size());
        Future<Long> future1;
        
        for (Rule r: rules){
            
            if (r.getTypes_Crops().contains(data.get("landlot_name"))){
                future1 = executorService.submit(new Callable<Long>() {
                @Override
                public Long call() throws Exception {
                    r.execute(data);
                    return 0l;
                }
            });
            }
            
        }
            
            
       

    }

  
  
    

}
