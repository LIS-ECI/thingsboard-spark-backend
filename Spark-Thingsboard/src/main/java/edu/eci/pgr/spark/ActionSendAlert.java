/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.pgr.spark;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mycompany.connection.MongoDBException;
import com.mycompany.connection.MongoDBSpatial;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.thingsboard.samples.spark.temperature.MqttImplementation;

/**
 *
 * @author cristian
 */
@Slf4j
public class ActionSendAlert implements Action{

    private static final String THINGSBOARD_MQTT_ENDPOINT = "tcp://10.8.0.18:1883";
    private MqttAsyncClient client;
    private MongoDBSpatial mdbs = new MongoDBSpatial();
    private String idParcel;
    
    public void connectToThingsboard(String token) throws Exception {
        client = new MqttAsyncClient(THINGSBOARD_MQTT_ENDPOINT, MqttAsyncClient.generateClientId());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(token);
        try {
            client.connect(options, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken iMqttToken) {
                    log.info("Connected to Thingsboard!");
                }

                @Override
                public void onFailure(IMqttToken iMqttToken, Throwable e) {
                    log.error("Failed to connect to Thingsboard!", e);
                }
            }).waitForCompletion();
        } catch (MqttException e) {
            log.error("Failed to connect to the server", e);
        }

    }
    
    private String getTokenSpark(String idParcel, String Topic) {
        String token = null;
        try {
            token = mdbs.getTokenByIdParcelTopic(idParcel, Topic);
        } catch (MongoDBException ex) {
            Logger.getLogger(MqttImplementation.class.getName()).log(Level.SEVERE, null, ex);
        }
        return token;
    }
    
    @Override
    public void execute() {
        System.out.println("ENVIOOO ALERTA");
        String token = getTokenSpark(getIdParcel(), "spark_detection");
        System.out.println("token: "+token);
        try {
            connectToThingsboard(token);
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode dataUrg = mapper.createObjectNode();
            ObjectNode values = dataUrg.put("pest_risk", 1);
            MqttMessage dataMsg2 = new MqttMessage(mapper.writeValueAsString(dataUrg).getBytes(StandardCharsets.UTF_8));
            client.publish("v1/devices/me/telemetry", dataMsg2, null, getCallback());
            client.disconnect();
        } catch (Exception ex) {
            Logger.getLogger(org.thingsboard.samples.spark.precipitation.MqttImplementation.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

  private IMqttActionListener getCallback() {
        return new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                log.info("Telemetry data updated!");
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                log.error("Telemetry data update failed!", exception);
            }
        };
    }
    
    
    /**
     * @return the idParcel
     */
    @Override
    public String getIdParcel() {
        return idParcel;
    }

    /**
     * @param idParcel the idParcel to set
     */
    @Override
    public void setIdParcel(String idParcel) {
        this.idParcel = idParcel;
    }
    
    
}
