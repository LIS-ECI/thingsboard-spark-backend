/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.pgr.spark;

import java.util.HashMap;
import java.util.List;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author cristian
 */
@Service
public class RulesEngine {
        
    public RulesEngine(){}

    Reflections reflections = new Reflections("com.mycompany.rules");
    
    private List<Rule> rules;
    
    public void execute(HashMap<String,String> data){
        for(Rule r: rules){
            r.setData(data);
            r.start();
        }
    }
    
}
