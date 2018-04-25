/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.pgr.spark;

import java.util.HashMap;
import org.springframework.stereotype.Service;

/**
 *
 * @author cristian
 */
public interface Rule {
    
    public void execute(HashMap<String,String> data);
    
}
