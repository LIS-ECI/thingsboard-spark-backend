/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.pgr.spark;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
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
    
    private static JavaRDD<Rule> rules;


    public RulesEngine() {
 
    }
    

    public void execute( HashMap<String, String> data) {
        List<Rule> rulesList = new ArrayList<>();
        rulesList.add(new GotaRule());
        rulesList.add(new RuleTests());
        rulesList.add(new RuleTests2());
        rules=SparkKafkaStreamingTemperatureMain.sc.parallelize(rulesList);
        rules.foreach(r->r.execute(data));
                          
    }
    

}
