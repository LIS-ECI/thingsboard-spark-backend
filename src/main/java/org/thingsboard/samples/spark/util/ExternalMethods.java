/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingsboard.samples.spark.util;
import edu.eci.pgr.cassandra.java.client.connector.CassandraConnector;
import edu.eci.pgr.cassandra.java.client.repository.KeyspaceRepository;
import edu.eci.pgr.cassandra.java.client.repository.LandlotRepository;
import com.datastax.driver.core.Session;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.thingsboard.samples.spark.temperature.ReviewData;
import org.thingsboard.server.common.data.landlot.Landlot;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.extern.slf4j.Slf4j;
/**
 *
 * @author cristian
 */
public class ExternalMethods implements Serializable {
    
    private static final String THINGSBOARD_MQTT_ENDPOINT = "tcp://10.8.0.19:1883";
    private static final String CASSANDRA_IP = "10.8.0.19";
    private static final String CASSANDRA_KEYSPACE = "thingsboard";
    private static CassandraConnector connector = new CassandraConnector();
    private static final MongoDBSpatial mdbs = new MongoDBSpatial();
    
    public static MongoDBSpatial getMdbs() {
        return mdbs;
    }

    //Cassandra Methods:
    
     public static String getCropNameCassandra(String idLandlot) {
        connector.connect(CASSANDRA_IP, null);
        Session session = connector.getSession();
        KeyspaceRepository sr = new KeyspaceRepository(session);
        sr.useKeyspace(CASSANDRA_KEYSPACE);
        LandlotRepository pr = new LandlotRepository(session);
        Landlot p = pr.selectById(idLandlot);
        connector.close();
        return p.getCrop().getName();
    }
     
    //Redis Methods
    
    public static String getValueOfRedis(String key, String idLandlot) {
        boolean funciono = true;
        String content = "";
        while (funciono) {
            Jedis jedis = JedisUtil.getPool().getResource();
            jedis.watch(key + idLandlot);
            Transaction t = jedis.multi();
            Response<String> valor = t.get(key + idLandlot);

            List<Object> res = t.exec();
            if (res.size() > 0) {
                funciono = false;
                content = valor.get();
                jedis.close();
            }
        }
        return content;
    }
   
    public static void saveToRedis(String key, String idLandlot, String data) {
        Jedis jedis = JedisUtil.getPool().getResource();
        jedis.watch(key + idLandlot);
        Transaction t2 = jedis.multi();
        t2.set(key + idLandlot, data);
        t2.exec();
        jedis.close();
    }
    
    
    
    // Mongo Methods
    
    
    
    public static String getTokenSpark(String idLandlot, String Topic) {
        String token = null;
        try {
            token = mdbs.getTokenByIdLandlotTopic(idLandlot, Topic);
        } catch (MongoDBException ex) {
            Logger.getLogger(ReviewData.class.getName()).log(Level.SEVERE, null, ex);
        }
        return token;
    }

    public static List<List<List<Double>>> getLandlotCoordinates(String idLandlot){
        SpatialLandlot landlot = mdbs.getMongodblandlot().findById(idLandlot);
        List<List<List<Double>>> coordinates = landlot.getPolygons().getCoordinates();
        return coordinates;
    
    }
   
    
    
    public static void sendTelemetryDataToThingsboard(String token, String key, double value) throws Exception {
        MqttAsyncClient client = new MqttAsyncClient(THINGSBOARD_MQTT_ENDPOINT, MqttAsyncClient.generateClientId());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(token);
        try {
            client.connect(options, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken iMqttToken) {
                    try {
                        System.out.println("Connected to Thingsboard!");
                        ObjectMapper mapper = new ObjectMapper();
                        ObjectNode dataUrg = mapper.createObjectNode();
                        ObjectNode values = dataUrg.put(key, value);
                        MqttMessage dataMsg2 = new MqttMessage(mapper.writeValueAsString(dataUrg).getBytes(StandardCharsets.UTF_8));
                        client.publish("v1/devices/me/telemetry", dataMsg2, null, getCallback());
                    } catch (MqttException ex) {
                        Logger.getLogger(ExternalMethods.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (JsonProcessingException ex) {
                        Logger.getLogger(ExternalMethods.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                @Override
                public void onFailure(IMqttToken iMqttToken, Throwable e) {
                    System.out.println("Failed to connect to Thingsboard!");
                }
            }).waitForCompletion();
        } catch (MqttException e) {
            System.out.println("Failed to connect to the server");
        }

    }
    
     private static IMqttActionListener getCallback() {
        return new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                System.out.println("Telemetry data updated!");
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                System.out.println("Telemetry data update failed!");
            }
        };
    }
    
    

  
}
