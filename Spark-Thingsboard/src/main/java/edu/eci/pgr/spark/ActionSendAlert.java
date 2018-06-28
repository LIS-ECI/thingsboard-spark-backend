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


/**
 *
 * @author cristian
 */
@Slf4j
public class ActionSendAlert implements Action,Serializable {

    private static final String THINGSBOARD_MQTT_ENDPOINT = "tcp://10.8.0.19:1883";
    private MqttAsyncClient client;
    private MongoDBSpatial mdbs = new MongoDBSpatial();
    private String idLandlot;

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

    private String getTokenSpark(String idLandlot, String Topic) {
        String token = null;
        try {
            token = mdbs.getTokenByIdLandlotTopic(idLandlot, Topic);
        } catch (MongoDBException ex) {
            Logger.getLogger(ActionSendAlert.class.getName()).log(Level.SEVERE, null, ex);
        }
        return token;
    }

    @Override
    public void execute() {
        MongoDBSpatial mdbs = new MongoDBSpatial();

        SpatialLandlot landlot = mdbs.getMongodblandlot().findById(getIdLandlot());

        List<List<List<Double>>> datos = landlot.getPolygons().getCoordinates();
        System.out.println(String.format("|%20s|%20s|", "Longitude", "Latitude"));
        for (List<Double> containData : datos.get(0)) {
            double longitude = containData.get(0);
            double latitude = containData.get(1);
            System.out.println(String.format("|%20s|%20s|", Double.toString(longitude), Double.toString(latitude)));
        }

        System.out.println("Sending to neighbor crops ...");

        System.out.println("ALERT: RISK OF Phytophthora infestans in crop with Id: getIdLandlot()");
        //String token = getTokenSpark(getIdLandlot(), "spark_detection");

        //try {
            //connectToThingsboard(token);
            //ObjectMapper mapper = new ObjectMapper();
            //ObjectNode dataUrg = mapper.createObjectNode();
            //ObjectNode values = dataUrg.put("pest_risk", 1);
            //MqttMessage dataMsg2 = new MqttMessage(mapper.writeValueAsString(dataUrg).getBytes(StandardCharsets.UTF_8));
            //client.publish("v1/devices/me/telemetry", dataMsg2, null, getCallback());
            //client.disconnect();
        //} catch (Exception ex) {
        //    Logger.getLogger(org.thingsboard.samples.spark.precipitation.MqttImplementation.class.getName()).log(Level.SEVERE, null, ex);
        //}

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
