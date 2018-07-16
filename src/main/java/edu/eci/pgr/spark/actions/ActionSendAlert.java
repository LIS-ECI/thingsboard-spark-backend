/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.pgr.spark.actions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mycompany.connection.MongoDBException;
import com.mycompany.connection.MongoDBSpatial;
import com.mycompany.entities.SpatialLandlot;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.thingsboard.samples.spark.util.ExternalMethods;


/**
 *
 * @author cristian
 */
@Slf4j
public class ActionSendAlert implements Action,Serializable {

    private static final String THINGSBOARD_MQTT_ENDPOINT = "tcp://10.8.0.19:1883";
    private MqttAsyncClient client;
    private String idLandlot;

    

  

    @Override
    public void execute() {
    
        String token = ExternalMethods.getTokenSpark(getIdLandlot(), "spark_detection");
        try {
            ExternalMethods.sendTelemetryDataToThingsboard(token, "pest_risk", 1);
        } 
        catch (Exception ex) {
            Logger.getLogger(ActionSendAlert.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        List<List<List<Double>>> coordinates = ExternalMethods.getLandlotCoordinates(idLandlot);
        System.out.println(String.format("|%20s|%20s|", "Longitude", "Latitude"));
        coordinates.get(0).forEach((containData) -> {
            double longitude = containData.get(0);
            double latitude = containData.get(1);
            System.out.println(String.format("|%20s|%20s|", Double.toString(longitude), Double.toString(latitude)));
        });

        System.out.println("Sending to neighbor crops ...");

        System.out.println("ALERT: RISK OF Phytophthora infestans in crop with Id: getIdLandlot()");
        

    }


    /**
     * @return the idLandlot
     */
    @Override
    public String getIdLandlot() {
        return idLandlot;
    }

    /**
     * @param idLandlot the idLandlot to set
     */
    @Override
    public void setIdLandlot(String idLandlot) {
        this.idLandlot = idLandlot;
    }

}
