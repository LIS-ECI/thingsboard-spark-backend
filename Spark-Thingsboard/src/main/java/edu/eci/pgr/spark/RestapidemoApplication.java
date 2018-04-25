/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.pgr.spark;

import java.util.HashMap;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 * @author cristian
 */
public class RestapidemoApplication {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
      ApplicationContext ac = new ClassPathXmlApplicationContext("applicationContext.xml");

            // TODO code application logic here
        RulesEngine rulesEngine = ac.getBean(RulesEngine.class);
      HashMap<String,String> data= new HashMap();
      data.put("humidity", "76.0");
      data.put("temperature", "11.0");
      rulesEngine.execute(data);
    }
    
}
