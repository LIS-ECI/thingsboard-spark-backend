/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingsboard.samples.spark.temperature;

import com.baeldung.cassandra.java.client.CassandraConnector;
import com.baeldung.cassandra.java.client.repository.KeyspaceRepository;
import com.baeldung.cassandra.java.client.repository.LandlotRepository;
import com.datastax.driver.core.Session;
import com.mycompany.connection.MongoDBException;
import com.mycompany.connection.MongoDBSpatial;
import edu.eci.pgr.spark.RulesEngine;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.thingsboard.samples.spark.util.JedisUtil;
import org.thingsboard.server.common.data.landlot.Landlot;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;
import scala.Tuple2;

/**
 *
 * @author Cristian
 */
@Slf4j
public class ReviewData implements Serializable{

    //private static final String THINGSBOARD_MQTT_ENDPOINT = "tcp://10.8.0.19:1883";
    //private MqttAsyncClient client;
    private MongoDBSpatial mdbs;
    private RulesEngine rulesEngine;

    ReviewData() {
        mdbs = new MongoDBSpatial();
        ApplicationContext ac = new ClassPathXmlApplicationContext("applicationContext.xml");
        rulesEngine = ac.getBean(RulesEngine.class);
    }

    public void analizeTelemetry(List<TemperatureAndGeoZoneData> aggData, String Topic, JavaStreamingContext sc) {
        if (!aggData.isEmpty()) {
            JavaRDD<TemperatureAndGeoZoneData> telemetryData = sc.sparkContext().parallelize(aggData);

            //Convertir a un map(idlandlot, list<Integer>)
            JavaPairRDD<String, Double> hmap;
            hmap = telemetryData.mapToPair((TemperatureAndGeoZoneData telemetryData1) -> {
                String idLandlot = mdbs.findLandlotsByDeviceId(telemetryData1.getDeviceId()).getId();
                return new Tuple2(idLandlot, telemetryData1.getTemperature());
            });

            List<Tuple2<String, Double>> hmap2 = hmap.collect();
            
            //count each values per key
            JavaPairRDD<String, Tuple2<Double, Double>> valueCount = hmap.mapValues(value -> new Tuple2<Double, Double>(value, 1.0));
            //add values by reduceByKey
            JavaPairRDD<String, Tuple2<Double, Double>> reducedCount = valueCount.reduceByKey((tuple1, tuple2) -> new Tuple2<Double, Double>(tuple1._1 + tuple2._1, tuple1._2 + tuple2._2));
            //calculate average
            JavaPairRDD<String, Double> averagePair = reducedCount.mapToPair(getAverageByKey);
            //for each landlot review sick probability
            averagePair.foreach(data -> {
                //data.1: idlandlot
                //data.2: telemetry data
                System.out.println("Key=" + data._1() + " Average=" + data._2());
                String idLandlot = data._1();
                double temperatureData = data._2();
                String token = getTokenSpark(idLandlot, Topic);

                //review enfermedades
                String temphumi = getValueOfRedis("humidity", idLandlot);
                Double humidityData = 0.0;
                if (temphumi != null) {
                    humidityData = Double.parseDouble(temphumi);
                }

                HashMap<String, String> data2 = new HashMap<>();
                data2.put("humidityData", String.valueOf(humidityData));
                data2.put("temperatureData", String.valueOf(temperatureData));
                data2.put("idLandlot", idLandlot);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSS");
                Date now = new Date();
                data2.put("first_time", sdf.format(now));

                String landlot_name = getLandlotNameCassandra(idLandlot);
                System.out.println("landlot_name: " + landlot_name);
                if (landlot_name.equals("Papa")) {
                    rulesEngine.execute(data2);
                }

            });

        }
    }

    private String getTokenSpark(String idLandlot, String Topic) {
        String token = null;
        try {
            token = mdbs.getTokenByIdLandlotTopic(idLandlot, Topic);
        } catch (MongoDBException ex) {
            Logger.getLogger(ReviewData.class.getName()).log(Level.SEVERE, null, ex);
        }
        return token;
    }

    private String getLandlotNameCassandra(String idLandlot) {
        CassandraConnector connector = new CassandraConnector();
        connector.connect("localhost", null);
        Session session = connector.getSession();
        KeyspaceRepository sr = new KeyspaceRepository(session);
        sr.useKeyspace("thingsboard");
        LandlotRepository pr = new LandlotRepository(session);
        System.out.println("LANDLOT " + idLandlot);
        Landlot p = pr.selectById(idLandlot);
        return p.getCrop().getName();
    }

    private String getValueOfRedis(String key, String idLandlot) {
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
    private static PairFunction<Tuple2<String, Tuple2<Double, Double>>, String, Double> getAverageByKey = (tuple) -> {
        Tuple2<Double, Double> val = tuple._2;
        double total = val._1;
        double count = val._2;
        Tuple2<String, Double> averagePair = new Tuple2<String, Double>(tuple._1, total / count);
        return averagePair;
    };
/*
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

    }*/
}
