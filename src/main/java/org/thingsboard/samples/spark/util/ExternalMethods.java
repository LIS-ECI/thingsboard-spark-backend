/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingsboard.samples.spark.util;
import com.baeldung.cassandra.java.client.CassandraConnector;
import com.baeldung.cassandra.java.client.repository.KeyspaceRepository;
import com.baeldung.cassandra.java.client.repository.LandlotRepository;
import com.datastax.driver.core.Session;
import com.mycompany.connection.MongoDBException;
import com.mycompany.connection.MongoDBSpatial;
import java.io.Serializable;
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
    private MqttAsyncClient client;
    
    private static final MongoDBSpatial mdbs = new MongoDBSpatial();
    
    public static MongoDBSpatial getMdbs() {
        return mdbs;
    }

    public static String getTokenSpark(String idLandlot, String Topic) {
        String token = null;
        try {
            token = mdbs.getTokenByIdLandlotTopic(idLandlot, Topic);
        } catch (MongoDBException ex) {
            Logger.getLogger(ReviewData.class.getName()).log(Level.SEVERE, null, ex);
        }
        return token;
    }

    public static String getLandlotNameCassandra(String idLandlot) {
        CassandraConnector connector = new CassandraConnector();
        connector.connect(CASSANDRA_IP, null);
        Session session = connector.getSession();
        KeyspaceRepository sr = new KeyspaceRepository(session);
        sr.useKeyspace("thingsboard");
        LandlotRepository pr = new LandlotRepository(session);
        Landlot p = pr.selectById(idLandlot);
        return p.getCrop().getName();
    }

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
   
    public void connectToThingsboard(String token) throws Exception {
        client = new MqttAsyncClient(THINGSBOARD_MQTT_ENDPOINT, MqttAsyncClient.generateClientId());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(token);
        try {
            client.connect(options, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken iMqttToken) {
                    System.out.println("Connected to Thingsboard!");
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
    
    public static void saveToRedis(String key, String idLandlot, String data) {
        Jedis jedis = JedisUtil.getPool().getResource();
        jedis.watch(key + idLandlot);
        Transaction t2 = jedis.multi();
        t2.set(key + idLandlot, data);
        t2.exec();
        jedis.close();
    }

  
}
