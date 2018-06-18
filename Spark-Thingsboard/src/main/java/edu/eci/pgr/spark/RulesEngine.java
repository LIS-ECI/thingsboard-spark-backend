/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.pgr.spark;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.reflections.Reflections;
import org.springframework.stereotype.Service;

/**
 *
 * @author cristian
 */
@Service
public class RulesEngine {
    
    private Reflections reflections;
    private List<Rule> rules;

    public RulesEngine() {
        //reflections = new Reflections("edu.eci.pgr.spark");
        //Set<Class<? extends Rule>> ruleClasses = reflections.getSubTypesOf(Rule.class);
        System.out.println("Estas son las clases");
        //System.out.println(ruleClasses);
        rules = new ArrayList<>();
        rules.add(new GotaRule());
        rules.add(new RuleTests());
        rules.add(new RuleTests2());
        /*rules = new ArrayList<>();
        for (Class<? extends Rule> ruleClass : ruleClasses) {
            try {
                rules.add(ruleClass.newInstance());
            } catch (InstantiationException | IllegalAccessException ex) {
                Logger.getLogger(RulesEngine.class.getName()).log(Level.SEVERE, null, ex);
            } 
        }
        System.out.println("Este es el tamaño de la lista de objtos rule");
        System.out.println(rules.size());*/
    }


    public void execute(HashMap<String, String> data) {
        for (Rule r : rules) {
            r.setData(data);
            r.start();
        }
    }

}
