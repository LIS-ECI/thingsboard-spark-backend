/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingsboard.samples.spark;

import com.baeldung.cassandra.java.client.CassandraConnector;
import com.baeldung.cassandra.java.client.repository.FarmRepository;
import com.baeldung.cassandra.java.client.repository.KeyspaceRepository;
import com.baeldung.cassandra.java.client.repository.ParcelRepository;
import com.datastax.driver.core.Session;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mycompany.connection.MongoDBException;
import com.mycompany.connection.MongoDBSpatial;
import com.mycompany.entities.SparkDevice;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.spark.api.java.function.Function;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.thingsboard.samples.spark.util.JedisUtil;
import org.thingsboard.server.common.data.parcel.Parcel;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

/**
 *
 * @author Cristian
 */
@Slf4j
public class MqttImplementation {
    
    private static final String THINGSBOARD_MQTT_ENDPOINT = "tcp://10.8.0.18:1883";
    private static final String TOPIC_TO_THINGSBOARD = "TemperatureAvg";

    private  MqttAsyncClient client;
    private  MongoDBSpatial mdbs;



    MqttImplementation() throws MqttException {
        mdbs = new MongoDBSpatial();
        }
    
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

        
        public void publishTelemetryToThingsboard(List<TemperatureAndGeoZoneData> aggData,String Topic) throws Exception {
            if (!aggData.isEmpty()) {
                HashMap<String, List<MutablePair>> hmap = new HashMap<>();
                for (TemperatureAndGeoZoneData agg: aggData){
                    String idParcel=mdbs.findParcelsByDeviceId(agg.getDeviceId()).getId();
                    
                    //Uso De Cassandra Para saber el nombre del cultivo asociado al parcel
                    //CassandraConnector connector = new CassandraConnector();
                    //connector.connect("10.8.0.18", null);
                    //Session session = connector.getSession();
                    //KeyspaceRepository sr = new KeyspaceRepository(session);
                    //sr.useKeyspace("thingsboard");
                    //ParcelRepository pr= new ParcelRepository(session);
                    //Parcel p= pr.selectById(idParcel);  
                    //System.out.println("Nombre Cultivo: "+p.getCrop().getName());
                    
                    if (hmap.containsKey(idParcel)){
                         List<MutablePair> temp= hmap.get(idParcel);
                         temp.add(new MutablePair(agg.getTemperature(),agg.getCount()) );
                         hmap.put(idParcel, temp);
                    }
                    else{
                        List<MutablePair> temp= new ArrayList<>();
                        temp.add(new MutablePair(agg.getTemperature(),agg.getCount()));
                        hmap.put(idParcel, temp);
                    }
                }
                //Sacar promedio respecto al cultivo
                HashMap<String, Double> hmap_promedios = new HashMap<>();
                hmap.keySet().forEach((idParcel) -> {
                    List<MutablePair> list= hmap.get(idParcel);
                   
                    Double total=0.0;
                    for (int i=0;i<list.size();i++){
                        Double cont=Double.parseDouble(list.get(i).right.toString());
                        total+=cont;
                    }

                    Double avg=0.0;
                    for (int i=0;i<list.size();i++){
                        Double cont=Double.parseDouble(list.get(i).right.toString());
                        Double value=Double.parseDouble(list.get(i).left.toString());
                        avg+=(cont/total)*value;
                    }

                    hmap_promedios.put(idParcel, avg);
                });
                //Enviar datos al device de spark 
                hmap_promedios.keySet().forEach((idParcel) -> {
                    try {
                        //Sacar token del device de Spark del cultivo
                        String token=getTokenSpark(idParcel,Topic);
                        toDataJson(token,hmap_promedios.get(idParcel),idParcel,Topic);
                    } catch (MqttException ex) {
                        Logger.getLogger(MqttImplementation.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(MqttImplementation.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
                
            }
        }
        
        private String getTokenSpark(String idParcel,String Topic){
        String token=null;
        try {
            token= mdbs.getTokenByIdParcelTopic(idParcel, Topic);
        } catch (MongoDBException ex) {
            Logger.getLogger(MqttImplementation.class.getName()).log(Level.SEVERE, null, ex);
        }
        return token;   
        }
        
        private void publicTo(String jsonB,String token, String idParcel,String topic) throws MqttException, IOException, Exception{
            connectToThingsboard(token);
            MqttMessage dataMsg = new MqttMessage(jsonB.getBytes(StandardCharsets.UTF_8));
            client.publish("v1/devices/me/telemetry", dataMsg, null, getCallback());                 
            client.disconnect();
            Jedis jedis = JedisUtil.getPool().getResource();   
            jedis.watch (topic+idParcel);
            Transaction t = jedis.multi();
            t.set(topic+idParcel, jsonB);
            t.exec();
            jedis.close();        
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

       

    
    private void toDataJson(String token, Double temperature, String idParcel,String topic) throws JsonProcessingException, MqttException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonB = mapper.createObjectNode();
        ObjectNode values = jsonB.put(TOPIC_TO_THINGSBOARD,temperature );
        if (jsonB.size()>0){
            try {
                publicTo(mapper.writeValueAsString(jsonB),token,idParcel,topic);
            } catch (Exception ex) {
                Logger.getLogger(MqttImplementation.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
            
    }
   
    
    private static String toConnectJson(String geoZone) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode json = mapper.createObjectNode();
        json.put("device", geoZone);
        return mapper.writeValueAsString(json);
    }
}
