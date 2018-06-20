/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingsboard.samples.spark.precipitation;

import com.baeldung.cassandra.java.client.CassandraConnector;
import com.baeldung.cassandra.java.client.repository.KeyspaceRepository;
import com.baeldung.cassandra.java.client.repository.LandlotRepository;
import com.datastax.driver.core.Session;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mycompany.connection.MongoDBException;
import com.mycompany.connection.MongoDBSpatial;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.MutablePair;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;
import org.thingsboard.samples.spark.util.JedisUtil;
import org.thingsboard.server.common.data.landlot.Landlot;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

/**
 *
 * @author Cristian
 */
@Slf4j
public class MqttImplementation {
    
    private static final String THINGSBOARD_MQTT_ENDPOINT = "tcp://10.8.0.19:1883";
    private static final String TOPIC_TO_THINGSBOARD = "PrecipitationAvg";

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

        
        public void publishTelemetryToThingsboard(List<PrecipitationAndGeoZoneData> aggData,String Topic) throws Exception {
            if (!aggData.isEmpty()) {
                HashMap<String, List<MutablePair>> hmap = new HashMap<>();
                for (PrecipitationAndGeoZoneData agg: aggData){
                    String idLandlot=mdbs.findLandlotsByDeviceId(agg.getDeviceId()).getId();
                    if (hmap.containsKey(idLandlot)){
                         List<MutablePair> temp= hmap.get(idLandlot);
                         temp.add(new MutablePair(agg.getPrecipitation(),agg.getCount()) );
                         hmap.put(idLandlot, temp);
                    }
                    else{
                        List<MutablePair> temp= new ArrayList<>();
                        temp.add(new MutablePair(agg.getPrecipitation(),agg.getCount()));
                        hmap.put(idLandlot, temp);
                    }
                }
                //Sacar promedio respecto al cultivo
                HashMap<String, Double> hmap_promedios = new HashMap<>();
                hmap.keySet().forEach((idLandlot) -> {
                    List<MutablePair> list= hmap.get(idLandlot);
                   
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

                    hmap_promedios.put(idLandlot, avg);
                });
                //Enviar datos al device de spark 
                hmap_promedios.keySet().forEach((idLandlot) -> {
                    try {
                        //Sacar token del device de Spark del cultivo
                        String token=getTokenSpark(idLandlot,Topic);
                        toDataJson(token,hmap_promedios.get(idLandlot),idLandlot,Topic);
                    } catch (MqttException ex) {
                        Logger.getLogger(MqttImplementation.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(MqttImplementation.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
                
            }
        }
        
        private String getTokenSpark(String idLandlot,String Topic){
        String token=null;
        try {
            token= mdbs.getTokenByIdLandlotTopic(idLandlot, Topic);
        } catch (MongoDBException ex) {
            Logger.getLogger(MqttImplementation.class.getName()).log(Level.SEVERE, null, ex);
        }
        return token;   
        }
        
        
        private void review_data(String idLandlot,String json){
            boolean funciono = true;
            boolean func2=true;
            Double temperatureData;
            Double precipitationData;
            Double humidityData=0.0;
            while (funciono){
                    Jedis jedis = JedisUtil.getPool().getResource();
                    jedis.watch ("humidity"+idLandlot);
                    Transaction t = jedis.multi();
                    Response<String> valor=t.get("humidity"+idLandlot);
                    List<Object> res = t.exec();
                    if (res.size()>0){
                        funciono=false;
                        String content=valor.get();
                        JSONObject humidity = new JSONObject(content);
                        humidityData= Double.parseDouble(humidity.get("HumidityAvg").toString());
                        jedis.close();
                    }
            }
            
            System.out.println("humidityData: "+humidityData);
            while (func2){
                    Jedis jedis = JedisUtil.getPool().getResource();
                    jedis.watch ("temperature"+idLandlot);
                    Transaction t = jedis.multi();
                    Response<String> valor=t.get("temperature"+idLandlot);

                    List<Object> res = t.exec();
                    if (res.size()>0){
                        func2=false;
                        String content=valor.get();

                        JSONObject temperature = new JSONObject(content);
                        JSONObject precipitation = new JSONObject(json);
                        temperatureData= Double.parseDouble(temperature.get("TemperatureAvg").toString());
                        precipitationData= Double.parseDouble(precipitation.get("PrecipitationAvg").toString());
                        //Si se cumple la condición se envía a otro spark
                         //Uso De Cassandra Para saber el nombre del cultivo asociado al landlot
                        CassandraConnector connector = new CassandraConnector();
                        connector.connect("localhost", null);
                        Session session = connector.getSession();
                        KeyspaceRepository sr = new KeyspaceRepository(session);
                        sr.useKeyspace("thingsboard");
                        LandlotRepository pr= new LandlotRepository(session);
                        Landlot p= pr.selectById(idLandlot);
                        String landlot_name=p.getCrop().getName();
                        System.out.println("Nombre Cultivo: "+p.getCrop().getName());
                        if (landlot_name.equals("Papa") && humidityData>70 && temperatureData>80 && precipitationData>3 ){
                            String token=getTokenSpark(idLandlot,"spark_detection");
                            try {
                                connectToThingsboard(token);
                                ObjectMapper mapper = new ObjectMapper();
                                ObjectNode dataUrg = mapper.createObjectNode();
                                ObjectNode values = dataUrg.put("pest_risk", 1);
                                MqttMessage dataMsg2 = new MqttMessage(mapper.writeValueAsString(dataUrg).getBytes(StandardCharsets.UTF_8));
                                client.publish("v1/devices/me/telemetry", dataMsg2, null, getCallback());
                                client.disconnect();
                            } catch (Exception ex) {
                                Logger.getLogger(MqttImplementation.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            
                        }
                        jedis.close();
                    }
                }       
        }
        
        
        private void publicTo(String jsonB,String token, String idLandlot,String topic) throws MqttException, IOException, Exception{
            connectToThingsboard(token);
            MqttMessage dataMsg = new MqttMessage(jsonB.getBytes(StandardCharsets.UTF_8));
            client.publish("v1/devices/me/telemetry", dataMsg, null, getCallback());                 
            client.disconnect();
            review_data(idLandlot,jsonB);
            
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

       

    
    private void toDataJson(String token, Double temperature, String idLandlot, String topic) throws JsonProcessingException, MqttException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonB = mapper.createObjectNode();
        ObjectNode values = jsonB.put(TOPIC_TO_THINGSBOARD,temperature );
        if (jsonB.size()>0){
            try {
                publicTo(mapper.writeValueAsString(jsonB),token,idLandlot,topic);
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
