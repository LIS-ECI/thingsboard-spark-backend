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
import org.springframework.stereotype.Service;

/**
 *
 * @author cristian
 */
@Service
public class RulesEngine implements Serializable{
    
    private static List<Rule> rules;
    public static HashMap<String, String> data2;


    public RulesEngine() {
        rules = new ArrayList<>();
        rules.add(new GotaRule());
        rules.add(new RuleTests());
        rules.add(new RuleTests2());
 
    }

    public void execute( HashMap<String, String> data) {
        ExecutorService executorService = Executors.newFixedThreadPool(rules.size());
        for (Rule r: rules){
            executorService.submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    r.execute(data);
                    return null;
                }
            });
        }
    }

}
